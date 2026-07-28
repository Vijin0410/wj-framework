package com.wangjin.common.constant;

/**
 * 缓存 Key 常量（字典翻译 / 用户 / 部门）。
 */
public final class CacheConstants {

    private CacheConstants() {
    }

    /** 字典项缓存前缀：system:core:dict:{typeCode} → List&lt;Option&gt; */
    public static final String SYS_DICT_KEY = "system:core:dict:";

    /** 字典类型缓存前缀 */
    public static final String SYS_DICT_TYPE_KEY = "system:core:dictType:";

    /** 用户缓存前缀：system:core:user:{userId} → Map */
    public static final String SYS_USER_KEY = "system:core:user:";

    /** 部门缓存前缀：system:core:dept:{deptId} → Map */
    public static final String SYS_DEPT_KEY = "system:core:dept:";

    /** 字典翻译字段后缀：gender → gender_text */
    public static final String DICT_KEY_SUFFIX = "_text";

    /** 用户/部门名称翻译后缀：createBy → createBy_name */
    public static final String USER_KEY_SUFFIX = "_name";

    public static final String DEPT = "dept";
}
