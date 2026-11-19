package com.wangjin.common.security.context;

import java.util.Objects;

/**
 * 当前线程的租户操作范围，与登录身份分离。
 * 调用方负责授权及租户有效性校验；作用域不可自动传播到异步线程。
 */
public final class TenantContext {
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Long getTenantId() {
        return HOLDER.get();
    }

    /**
     * 建立临时租户作用域，关闭时恢复外层作用域；应使用 try-with-resources。
     */
    public static Scope use(Long tenantId) {
        Objects.requireNonNull(tenantId, "tenantId");
        Long previous = HOLDER.get();
        HOLDER.set(tenantId);
        return new Scope(previous);
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static final class Scope implements AutoCloseable {
        private final Long previous;
        private boolean closed;

        private Scope(Long previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (previous == null) {
                HOLDER.remove();
            } else {
                HOLDER.set(previous);
            }
        }
    }
}
