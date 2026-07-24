package com.wangjin.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一业务状态码（参考阿里错误码规约简化版）。
 * <ul>
 *   <li>00000 — 成功</li>
 *   <li>Axxxx — 用户端错误（参数、登录、权限）</li>
 *   <li>Bxxxx — 系统执行错误</li>
 *   <li>Cxxxx — 第三方/中间件错误</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ResultCode implements IResultCode, Serializable {

    SUCCESS("00000", "成功"),

    // ========== A 用户端 ==========
    /** 用户端通用错误 */
    USER_ERROR("A0001", "用户端错误"),
    /** 登录异常 */
    USER_LOGIN_ERROR("A0200", "用户登录异常"),
    USER_NOT_EXIST("A0201", "用户不存在"),
    USER_ACCOUNT_LOCKED("A0202", "用户账户被冻结"),
    USER_ACCOUNT_INVALID("A0203", "用户账户已作废"),
    USERNAME_OR_PASSWORD_ERROR("A0210", "用户名或密码错误"),
    INVALID_TOKEN("A0230", "token无效或已过期"),
    TOKEN_ACCESS_FORBIDDEN("A0231", "token已被禁止访问"),

    AUTHORIZED_ERROR("A0300", "访问权限异常"),
    ACCESS_UNAUTHORIZED("A0301", "访问未授权"),
    REPEAT_SUBMIT_ERROR("A0303", "请求重复提交"),

    PARAM_ERROR("A0400", "请求参数错误"),
    RESOURCE_NOT_FOUND("A0401", "请求资源不存在"),
    PARAM_IS_NULL("A0410", "请求必填参数为空"),

    // ========== B 系统 ==========
    SYSTEM_EXECUTION_ERROR("B0001", "系统执行出错"),
    SYSTEM_EXECUTION_TIMEOUT("B0100", "系统执行超时"),
    FLOW_LIMITING("B0210", "系统限流"),
    DEGRADATION("B0220", "系统功能降级"),

    // ========== C 第三方 / 中间件 ==========
    CALL_THIRD_PARTY_SERVICE_ERROR("C0001", "调用第三方服务出错"),
    MIDDLEWARE_SERVICE_ERROR("C0100", "中间件服务出错"),
    MESSAGE_SERVICE_ERROR("C0120", "消息服务出错"),
    DATABASE_ERROR("C0300", "数据库服务出错"),
    DATABASE_PRIMARY_KEY_CONFLICT("C0341", "主键冲突");

    private String code;
    private String msg;

    public static ResultCode getValue(String code) {
        for (ResultCode value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return SYSTEM_EXECUTION_ERROR;
    }
}
