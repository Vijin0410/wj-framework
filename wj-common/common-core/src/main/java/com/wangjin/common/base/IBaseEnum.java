package com.wangjin.common.base;

import cn.hutool.core.util.ObjectUtil;

import java.util.EnumSet;

/**
 * 枚举通用接口。
 *
 * @param <T> 枚举值类型
 */
public interface IBaseEnum<T> {

    T getValue();

    String getLabel();

    static <E extends Enum<E> & IBaseEnum<?>> E getEnumByValue(Object value, Class<E> clazz) {
        if (value == null) {
            return null;
        }
        return EnumSet.allOf(clazz).stream()
                .filter(e -> ObjectUtil.equal(e.getValue(), value))
                .findFirst()
                .orElse(null);
    }

    static <E extends Enum<E> & IBaseEnum<?>> String getLabelByValue(Object value, Class<E> clazz) {
        E match = getEnumByValue(value, clazz);
        return match == null ? null : match.getLabel();
    }

    static <E extends Enum<E> & IBaseEnum<?>> Object getValueByLabel(String label, Class<E> clazz) {
        if (label == null) {
            return null;
        }
        return EnumSet.allOf(clazz).stream()
                .filter(e -> ObjectUtil.equal(e.getLabel(), label))
                .findFirst()
                .map(IBaseEnum::getValue)
                .orElse(null);
    }
}
