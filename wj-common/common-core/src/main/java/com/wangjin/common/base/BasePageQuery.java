package com.wangjin.common.base;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询基类。业务 model 层 query 包下的查询对象继承此类。
 *
 * <pre>
 * public class UserQuery extends BaseQuery {
 *     private String username;
 * }
 * </pre>
 */
@Data
public class BasePageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前页，从 1 开始 */
    @Min(value = 1, message = "页码最小为 1")
    private long pageNum = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = Integer.MAX_VALUE, message = "最大为Integer.MAX_VALUE")
    private long pageSize = 10;

    /** SQL OFFSET = (pageNum - 1) * pageSize */
    public long getOffset() {
        long num = pageNum < 1 ? 1 : pageNum;
        long size = pageSize < 1 ? 10 : pageSize;
        return (num - 1) * size;
    }
}
