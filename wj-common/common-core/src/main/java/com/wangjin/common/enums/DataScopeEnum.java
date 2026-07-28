package com.wangjin.common.enums;

import com.wangjin.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色数据权限范围（数值越小权限越大）。
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum implements IBaseEnum<Integer> {

    ALL(1, "全部数据"),
    DEPT_AND_SUB(2, "部门及子部门"),
    DEPT(3, "本部门"),
    SELF(4, "仅本人"),
    CUSTOM(5, "自定义部门");

    private final Integer value;
    private final String label;
}
