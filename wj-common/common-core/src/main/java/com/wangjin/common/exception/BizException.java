package com.wangjin.common.exception;

import com.wangjin.common.result.IResultCode;
import com.wangjin.common.result.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常。由 common-web 全局异常处理器捕获并转为 {@link com.wangjin.common.result.Result}。
 */
@Getter
public class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final IResultCode resultCode;

    public BizException(IResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }

    public BizException(IResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public BizException(String message) {
        super(message);
        this.resultCode = ResultCode.SYSTEM_EXECUTION_ERROR;
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.resultCode = ResultCode.SYSTEM_EXECUTION_ERROR;
    }
}
