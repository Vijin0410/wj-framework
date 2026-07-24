package com.wangjin.common.result;

/**
 * 状态码契约。业务模块可自行实现扩展枚举。
 */
public interface IResultCode {

    /** 状态码 */
    String getCode();

    /** 提示信息 */
    String getMsg();
}
