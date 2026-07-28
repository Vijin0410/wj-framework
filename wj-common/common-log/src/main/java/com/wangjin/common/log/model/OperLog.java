package com.wangjin.common.log.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志。
 */
@Data
public class OperLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 操作标题 */
    private String title;
    /** 业务操作类型 */
    private String businessType;
    /** 请求方法全名 */
    private String method;
    /** HTTP 方法 */
    private String requestMethod;
    /** 操作人类别 */
    private String operatorType;
    /** 操作人名称 */
    private String operatorName;
    /** 部门 */
    private String deptName;
    /** 请求 URL */
    private String operateUrl;
    /** 操作人 IP */
    private String operateIp;
    /** 请求参数 */
    private String requestParam;
    /** 响应结果 */
    private String jsonResult;
    /** 状态：0 成功 / 1 失败 */
    private Integer status;
    /** 异常信息 */
    private String errorMsg;
    /** 操作时间 */
    private LocalDateTime operateTime;
    /** 耗时（毫秒） */
    private Long costTime;
    /** 操作人用户 ID */
    private Long createBy;
    /** 业务主键 */
    private String bizNo;
}
