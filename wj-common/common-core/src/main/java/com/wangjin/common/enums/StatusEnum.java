package com.wangjin.common.enums;

import com.wangjin.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用启用/禁用状态。
 */
@Getter
@AllArgsConstructor
public enum StatusEnum implements IBaseEnum<Integer> {

    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private final Integer value;
    private final String label;
}
