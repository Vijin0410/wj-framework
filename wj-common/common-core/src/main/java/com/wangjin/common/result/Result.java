package com.wangjin.common.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一 API 响应体。
 *
 * <pre>
 * {
 *   "code": "00000",
 *   "msg": "成功",
 *   "data": { ... }
 * }
 * </pre>
 *
 * @param <T> 业务数据
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> failed() {
        return failed(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    public static <T> Result<T> failed(String msg) {
        return result(ResultCode.SYSTEM_EXECUTION_ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> failed(IResultCode resultCode) {
        return result(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> Result<T> failed(IResultCode resultCode, String msg) {
        return result(resultCode.getCode(), msg, null);
    }

    public static <T> Result<T> judge(boolean status) {
        return status ? success() : failed();
    }

    public static <T> Result<T> result(String code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static boolean isSuccess(Result<?> result) {
        return result != null && ResultCode.SUCCESS.getCode().equals(result.getCode());
    }

    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
