package com.wangjin.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应结构体（与 {@link Result} 同级的外层包装）。
 *
 * <pre>
 * {
 *   "code": "00000",
 *   "msg": "成功",
 *   "data": { "list": [...], "total": 100 }
 * }
 * </pre>
 *
 * @param <T> 行数据类型，通常为 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;
    private String msg;
    private PageData<T> data;

    public static <T> PageResult<T> success(List<T> list, long total) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(new PageData<>(
                list == null ? Collections.emptyList() : list,
                total
        ));
        return result;
    }

    public static <T> PageResult<T> success(PageData<T> pageData) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(pageData);
        return result;
    }

    public static <T> PageResult<T> empty() {
        return success(Collections.emptyList(), 0);
    }

    public static boolean isSuccess(PageResult<?> result) {
        return result != null && ResultCode.SUCCESS.getCode().equals(result.getCode());
    }

    /**
     * 分页 data 节点。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageData<T> implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 当前页数据 */
        private List<T> list;
        /** 总记录数 */
        private long total;
    }
}
