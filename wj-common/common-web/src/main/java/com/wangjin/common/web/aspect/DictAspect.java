package com.wangjin.common.web.aspect;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wangjin.common.constant.CacheConstants;
import com.wangjin.common.redis.service.RedisService;
import com.wangjin.common.result.PageResult;
import com.wangjin.common.result.Result;
import com.wangjin.common.security.util.SecurityUtils;
import com.wangjin.common.web.annotation.Dict;
import com.wangjin.common.web.model.Option;
import com.wangjin.common.web.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link com.wangjin.common.web.annotation.QueryDict} 字典翻译切面。
 * <p>
 * 将返回体中标注 {@link Dict} 的字段翻译为 {@code xxx_text} / {@code xxx_name}。
 * 返回结构仍包在 Result / PageResult 中，list 元素转为 Map 形态以承载扩展字段。
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class DictAspect {

    private static final String SPLIT = ",";

    // ponytail: 独立 ObjectMapper 隔离容器 defaultTyping 污染（RedisConfig:29 的 activateDefaultTyping
    //   会让 convertValue(Map) 把时间数组首元素当多态 typeId）。ceiling: 时间格式复制自 JacksonConfig，
    //   改格式需同步；升级路径：抽公共 DateTimeFormatter 常量，或修复注入源后复用容器 ObjectMapper。
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new SimpleModule()
                    .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME))
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME)))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private final RedisService redisService;
    private final DictUtils dictUtils;

    @Around("@annotation(com.wangjin.common.web.annotation.QueryDict)")
    public Object translation(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        try {
            if (result instanceof Result<?> r) {
                Object data = r.getData();
                if (data instanceof List<?> list) {
                    return Result.success(translateList(list));
                }
                if (data != null && BeanUtil.isBean(data.getClass())) {
                    return Result.success(translateBean(data));
                }
                return result;
            }
            if (result instanceof PageResult<?> page) {
                if (page.getData() == null || page.getData().getList() == null) {
                    return page;
                }
                List<?> list = page.getData().getList();
                List<Map<String, Object>> translated = translateList(list);
                return PageResult.success(translated, page.getData().getTotal());
            }
            if (result instanceof List<?> list) {
                return translateList(list);
            }
            if (result != null && BeanUtil.isBean(result.getClass())) {
                return translateBean(result);
            }
        } catch (Exception e) {
            log.error("QueryDict 字典翻译异常", e);
        }
        return result;
    }

    private List<Map<String, Object>> translateList(List<?> list) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return items;
        }
        for (Object record : list) {
            if (record == null) {
                continue;
            }
            if (record instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cast = (Map<String, Object>) map;
                items.add(cast);
                continue;
            }
            items.add(translateBean(record));
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> translateBean(Object data) {
        if (data == null) {
            return Map.of();
        }
        try {
            ObjectNode node = MAPPER.valueToTree(data);
            Map<String, Object> item = MAPPER.convertValue(node, Map.class);
            for (Field field : ReflectUtil.getFields(data.getClass())) {
                recursion(item, field, data);
            }
            return item;
        } catch (Exception e) {
            log.warn("字典翻译序列化失败: {}", e.getMessage());
            return BeanUtil.beanToMap(data);
        }
    }

    @SuppressWarnings("unchecked")
    private void recursion(Map<String, Object> item, Field field, Object source) {
        // @Dict 字段优先翻译：标量值（Integer/Long/String 等）直接翻译，避免被嵌套 Bean/List 分支拦截
        if (field.getAnnotation(Dict.class) != null) {
            translateDict(item, field);
            return;
        }
        Object fieldValue = item.get(field.getName());
        if (fieldValue == null) {
            return;
        }
        // 嵌套 Bean：递归翻译其字段
        if (isNestedBeanField(field.getType(), fieldValue)) {
            try {
                Map<String, Object> child = translateBean(ReflectUtil.getFieldValue(source, field));
                item.put(field.getName(), child);
            } catch (Exception ignored) {
                // keep original
            }
            return;
        }
        // List<Bean>：递归翻译每个元素
        if (field.getType() == List.class && fieldValue instanceof List<?> childList) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType pt) {
                Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof Class<?> clazz && BeanUtil.isBean(clazz)) {
                    item.put(field.getName(), translateList(childList));
                }
            }
            return;
        }
    }

    private boolean isNestedBeanField(Class<?> type, Object fieldValue) {
        return BeanUtil.isBean(type)
                && !isSimpleValueType(type)
                && !(fieldValue instanceof Map)
                && !(fieldValue instanceof List);
    }

    private boolean isSimpleValueType(Class<?> type) {
        return type.isPrimitive()
                || CharSequence.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class == type
                || Character.class == type
                || java.util.Date.class.isAssignableFrom(type)
                || java.time.temporal.Temporal.class.isAssignableFrom(type)
                || type.isEnum();
    }

    private void translateDict(Map<String, Object> item, Field field) {
        Dict dict = field.getAnnotation(Dict.class);
        if (dict == null) {
            return;
        }
        Object raw = item.get(field.getName());
        String key = raw == null ? null : String.valueOf(raw);

        if (StrUtil.isNotBlank(dict.dictCode())) {
            String text = translateDictValue(dict.dictCode(), key);
            item.put(field.getName() + CacheConstants.DICT_KEY_SUFFIX, text);
        }
        if (dict.queryUserName()) {
            item.put(field.getName() + CacheConstants.USER_KEY_SUFFIX, translateUserNames(key));
        }
        if (dict.queryDeptName()) {
            item.put(field.getName() + CacheConstants.USER_KEY_SUFFIX, translateDeptNames(key));
        }
    }

    /**
     * 加载字典 Map：通用字典 + 当前租户自定义字典，同 value 租户覆盖通用。
     * <p>
     * key 约定见 {@link CacheConstants#SYS_DICT_KEY}；ROOT 只看通用（跨租户管理视角）。
     */
    private Map<String, String> loadDictMap(String dictType) {
        // ponytail: 每次调用查 2 次 Redis（通用 + 租户），同请求多字段同 dictCode 未去重；
        //   ceiling: 大列表高并发下 Redis 调用翻倍；升级路径：请求级 ThreadLocal 缓存 dictCode -> map。
        Map<String, String> map = new LinkedHashMap<>();
        putDictItems(map, redisService.getCacheList(CacheConstants.SYS_DICT_KEY + dictType));
        String suffix = tenantSuffix();
        if (suffix != null) {
            putDictItems(map, redisService.getCacheList(CacheConstants.SYS_DICT_KEY + dictType + suffix));
        }
        return map;
    }

    private void putDictItems(Map<String, String> map, List<?> dictList) {
        if (dictList == null || dictList.isEmpty()) {
            return;
        }
        for (Object dict : dictList) {
            String value = getDictItemValue(dict);
            if (value != null) {
                map.put(value, getDictItemLabel(dict));
            }
        }
    }

    /** 租户 key 后缀；ROOT 或无租户返回 null（只查通用字典）。 */
    private String tenantSuffix() {
        if (SecurityUtils.isRoot()) {
            return null;
        }
        Long tenantId = SecurityUtils.getTenantId();
        return tenantId == null ? null : ":" + tenantId;
    }

    private String translateDictValue(String dictType, String key) {
        // ponytail: 统一 String 匹配。ceiling: 放弃原 long 分支的前导零兼容（"01" 匹配 "1"），
        //   字典 value 应为规范数字串；若出现前导零，再加 long 回退遍历。
        if (key == null || "null".equals(key)) {
            return "-";
        }
        Map<String, String> dictMap = loadDictMap(dictType);
        if (dictMap.isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (String k : key.split(SPLIT)) {
            String trimmed = k.trim();
            if (StrUtil.isBlank(trimmed)) {
                continue;
            }
            sb.append(dictMap.getOrDefault(trimmed, "-")).append(SPLIT);
        }
        return sb.toString().endsWith(SPLIT) ? sb.substring(0, sb.length() - 1) : sb.toString();
    }

    private String getDictItemValue(Object dict) {
        Object value = getDictItemProperty(dict, "value");
        return value == null ? null : String.valueOf(value);
    }

    private String getDictItemLabel(Object dict) {
        Object label = getDictItemProperty(dict, "label");
        return label == null ? "-" : String.valueOf(label);
    }

    private Object getDictItemProperty(Object dict, String propertyName) {
        if (dict == null) {
            return null;
        }
        if (dict instanceof Option<?> option) {
            return "value".equals(propertyName) ? option.getValue() : option.getLabel();
        }
        if (dict instanceof Map<?, ?> map) {
            return map.get(propertyName);
        }
        return BeanUtil.beanToMap(dict).get(propertyName);
    }

    private String translateUserNames(String ids) {
        if (StrUtil.isBlank(ids) || "null".equals(ids)) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (String id : ids.split(SPLIT)) {
            if (StrUtil.isBlank(id)) {
                continue;
            }
            sb.append(dictUtils.getUserNameById(id.trim())).append(SPLIT);
        }
        return sb.toString().endsWith(SPLIT) ? sb.substring(0, sb.length() - 1) : sb.toString();
    }

    private String translateDeptNames(String ids) {
        if (StrUtil.isBlank(ids) || "null".equals(ids)) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (String id : ids.split(SPLIT)) {
            if (StrUtil.isBlank(id)) {
                continue;
            }
            sb.append(dictUtils.getDeptNameById(id.trim())).append(SPLIT);
        }
        return sb.toString().endsWith(SPLIT) ? sb.substring(0, sb.length() - 1) : sb.toString();
    }
}
