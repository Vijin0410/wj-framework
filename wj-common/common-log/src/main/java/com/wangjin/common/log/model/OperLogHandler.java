package com.wangjin.common.log.model;

/**
 * 操作日志处理扩展点（入库 / 发 MQ 等由业务实现）。
 */
@FunctionalInterface
public interface OperLogHandler {

    void handle(OperLog operLog);
}
