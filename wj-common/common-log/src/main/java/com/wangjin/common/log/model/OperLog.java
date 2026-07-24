package com.wangjin.common.log.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志模型（业务侧可持久化或发送 MQ）。
 */
@Data
public class OperLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String title;
    private String businessType;
    private String operatorType;
    private String method;
    private String requestMethod;
    private String operatorName;
    private String operateUrl;
    private String operateIp;
    private String requestParam;
    private String jsonResult;
    private Integer status;
    private String errorMsg;
    private Long costTime;
    private LocalDateTime operateTime;
}
