package com.wangjin.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 多租户实体基类。
 * <p>
 * 在 {@link BaseEntity} 上增加 tenantId，需要租户隔离的表继承此类。
 *
 * @param <T> 主键类型
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseTenantEntity<T> extends BaseEntity<T> {

    /** 租户 ID */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
