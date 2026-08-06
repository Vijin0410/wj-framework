package com.wangjin.common.web.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.wangjin.common.constant.CacheConstants;
import com.wangjin.common.redis.service.RedisService;
import com.wangjin.common.web.model.Option;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 从 Redis 读取字典 / 部门缓存并翻译。
 */
@Component
public class DictUtils {

    @Resource
    private RedisService redisService;

    /**
     * 字典 value → label（多值逗号分隔）。
     */
    @SuppressWarnings("rawtypes")
    public String translateDictValue(String dictType, String keys) {
        if (StrUtil.isBlank(keys)) {
            return "";
        }
        List<?> dictList = redisService.getCacheList(CacheConstants.SYS_DICT_KEY + dictType);
        if (dictList == null || dictList.isEmpty()) {
            return keys;
        }
        StringBuilder sb = new StringBuilder();
        for (String key : keys.split(",")) {
            String label = dictList.stream()
                    .filter(dict -> Objects.equals(getDictItemValue(dict), key))
                    .findFirst()
                    .map(this::getDictItemLabel)
                    .orElse("-");
            sb.append(label).append(",");
        }
        if (!sb.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 字典 label → value（导入场景）。
     */
    @SuppressWarnings("rawtypes")
    public String translateDictName(String dictType, String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        name = name.trim().replace("、", ",").replace("，", ",");
        List<?> dictList = redisService.getCacheList(CacheConstants.SYS_DICT_KEY + dictType);
        if (dictList == null || dictList.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String k : name.split(",")) {
            String value = dictList.stream()
                    .filter(dict -> Objects.equals(getDictItemLabel(dict), k))
                    .findFirst()
                    .map(this::getDictItemValue)
                    .orElse("");
            sb.append(value).append(",");
        }
        String result = sb.toString().endsWith(",") ? sb.substring(0, sb.length() - 1) : sb.toString();
        return StrUtil.isBlank(result) ? null : result;
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

    public String getDeptNameById(Object deptId) {
        if (deptId == null) {
            return "-";
        }
        Object obj = redisService.getCacheMap(CacheConstants.SYS_DEPT_KEY + deptId);
        obj = Optional.ofNullable(obj).orElseGet(HashMap::new);
        Map<?, ?> map = BeanUtil.copyProperties(obj, HashMap.class);
        return MapUtil.get(map, "name", String.class, "-");
    }

    public String getUserNameById(Object userId) {
        if (userId == null) {
            return "-";
        }
        Object obj = redisService.getCacheMap(CacheConstants.SYS_USER_KEY + userId);
        obj = Optional.ofNullable(obj).orElseGet(HashMap::new);
        Map<?, ?> map = BeanUtil.copyProperties(obj, HashMap.class);
        String nickname = MapUtil.get(map, "nickname", String.class, null);
        if (StrUtil.isNotBlank(nickname)) {
            return nickname;
        }
        return MapUtil.get(map, "username", String.class, "-");
    }
}
