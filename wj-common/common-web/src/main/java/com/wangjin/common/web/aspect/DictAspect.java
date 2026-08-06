package com.wangjin.common.web.aspect;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wangjin.common.constant.CacheConstants;
import com.wangjin.common.redis.service.RedisService;
import com.wangjin.common.result.PageResult;
import com.wangjin.common.result.Result;
import com.wangjin.common.web.annotation.Dict;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private final RedisService redisService;
    private final DictUtils dictUtils;
    private final ObjectMapper objectMapper;

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
            ObjectNode node = objectMapper.valueToTree(data);
            Map<String, Object> item = objectMapper.convertValue(node, Map.class);
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
        Object fieldValue = item.get(field.getName());
        if (fieldValue == null) {
            Dict dict = field.getAnnotation(Dict.class);
            if (dict != null) {
                translateDict(item, field);
            }
            return;
        }

        if (BeanUtil.isBean(field.getType())
                && !Number.class.isAssignableFrom(field.getType())
                && !(fieldValue instanceof Map)
                && !(fieldValue instanceof List)) {
            try {
                Map<String, Object> child = translateBean(ReflectUtil.getFieldValue(source, field));
                item.put(field.getName(), child);
            } catch (Exception ignored) {
                // keep original
            }
            return;
        }

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

        if (field.getAnnotation(Dict.class) != null) {
            translateDict(item, field);
        }
    }

    private void translateDict(Map<String, Object> item, Field field) {
        Dict dict = field.getAnnotation(Dict.class);
        if (dict == null) {
            return;
        }
        Object raw = item.get(field.getName());
        String key = raw == null ? null : String.valueOf(raw);

        if (StrUtil.isNotBlank(dict.dictCode())) {
            String text = translateDictValue(dict.dictCode(), key, dict.valueType());
            item.put(field.getName() + CacheConstants.DICT_KEY_SUFFIX, text);
        }
        if (dict.queryUserName()) {
            item.put(field.getName() + CacheConstants.USER_KEY_SUFFIX, translateUserNames(key));
        }
        if (dict.queryDeptName()) {
            item.put(field.getName() + CacheConstants.USER_KEY_SUFFIX, translateDeptNames(key));
        }
    }

    @SuppressWarnings("rawtypes")
    private String translateDictValue(String dictType, String key, String valueType) {
        if (key == null || "null".equals(key)) {
            return "-";
        }
        List<Map> dictList = redisService.getCacheList(CacheConstants.SYS_DICT_KEY + dictType);
        if (dictList == null || dictList.isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        if ("long".equals(valueType)) {
            Arrays.stream(key.split(SPLIT)).map(String::trim).filter(StrUtil::isNotBlank).mapToLong(Long::parseLong)
                    .forEach(k -> sb.append(dictList.stream()
                                    .filter(d -> {
                                        try {
                                            return Long.parseLong(String.valueOf(d.get("value"))) == k;
                                        } catch (Exception e) {
                                            return false;
                                        }
                                    })
                                    .findFirst()
                                    .map(d -> String.valueOf(d.get("label")))
                                    .orElse("-"))
                            .append(SPLIT));
        } else {
            Arrays.stream(key.split(SPLIT)).map(String::trim).filter(StrUtil::isNotBlank)
                    .forEach(k -> sb.append(dictList.stream()
                                    .filter(d -> Objects.equals(String.valueOf(d.get("value")), k))
                                    .findFirst()
                                    .map(d -> String.valueOf(d.get("label")))
                                    .orElse("-"))
                            .append(SPLIT));
        }
        return sb.toString().endsWith(SPLIT) ? sb.substring(0, sb.length() - 1) : sb.toString();
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
