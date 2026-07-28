package com.wangjin.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注字段需要字典 / 用户 / 部门名称翻译。
 * <p>
 * 配合 {@link QueryDict} 使用，翻译结果写入同名字段后缀：
 * <ul>
 *   <li>字典：{@code field + "_text"}</li>
 *   <li>用户/部门：{@code field + "_name"}</li>
 * </ul>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {

    /** 字典类型编码，对应 sys_dict_type.code / sys_dict.type_code */
    String dictCode() default "";

    /**
     * 字典值类型：string / long（影响匹配时的类型比较）。
     */
    String valueType() default "string";

    /** 是否按用户 ID 翻译昵称（支持逗号分隔多 ID） */
    boolean queryUserName() default false;

    /** 是否按部门 ID 翻译部门名称（支持逗号分隔多 ID） */
    boolean queryDeptName() default false;
}
