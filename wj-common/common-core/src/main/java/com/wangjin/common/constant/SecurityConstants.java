package com.wangjin.common.constant;

/**
 * 安全相关常量。
 */
public interface SecurityConstants {

    String DETAILS_USER_ID = "userId";
    String DETAILS_USERNAME = "username";
    String DETAILS_NICKNAME = "nickname";
    String DETAILS_TENANT_ID = "tenantId";
    String DETAILS_DEPT_ID = "deptId";
    String AUTHORITIES_CLAIM_NAME = "authorities";

    /** Token 黑名单缓存前缀 */
    String BLACKLIST_TOKEN_PREFIX = "AUTH:BLACKLIST_TOKEN:";
    /** 验证码缓存前缀 */
    String VERIFY_CODE_CACHE_KEY_PREFIX = "AUTH:VERIFY_CODE:";
    /** 短信验证码缓存前缀 */
    String SMS_CODE_PREFIX = "AUTH:SMS_CODE:";
    /** 用户权限缓存前缀 */
    String USER_PERMS_CACHE_KEY_PREFIX = "AUTH:USER_PERMS:";

    String HEADER_AUTHORIZATION = "Authorization";
    String TOKEN_PREFIX = "Bearer ";
}
