package com.wangjin.common.security.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * 当前登录用户快照。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickname;
    private Long tenantId;
    private Long deptId;
    private String tokenId;

    /**
     * 数据权限范围，见 {@link com.wangjin.common.enums.DataScopeEnum}。
     * ROOT / ALL 时数据权限拦截器不加额外条件。
     */
    private Integer dataScope;

    /** 数据权限可见部门（本部门 / 本部门及子 / 自定义） */
    @Builder.Default
    private Set<Long> dataScopeDeptIds = Collections.emptySet();

    @Builder.Default
    private Set<String> roles = Collections.emptySet();
    @Builder.Default
    private Set<String> permissions = Collections.emptySet();
}
