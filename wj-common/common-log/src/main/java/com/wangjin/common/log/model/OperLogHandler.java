package com.wangjin.common.log.model;

/**
 * 操作日志处理器扩展点。
 * 业务侧可实现此接口将日志入库 / 发 MQ。
 */
@FunctionalInterface
public interface OperLogHandler {

    void handle(OperLog operLog);
}
