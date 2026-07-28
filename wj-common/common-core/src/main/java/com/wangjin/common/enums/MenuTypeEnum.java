package com.wangjin.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.wangjin.common.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型。
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum implements IBaseEnum<Integer> {

    MENU(1, "菜单"),
    CATALOG(2, "目录"),
    EXTLINK(3, "外链"),
    BUTTON(4, "按钮");

    @EnumValue
    private final Integer value;
    private final String label;
}
