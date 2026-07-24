package com.wangjin.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.wangjin.common.constant.GlobalConstants;
import com.wangjin.common.security.context.UserContext;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 自动填充 create/update 字段。
 * createBy/updateBy 优先取登录用户，无登录时填 0。
 */
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = safeUserId();

        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", Long.class, userId);
        strictInsertFill(metaObject, "updateBy", Long.class, userId);
        strictInsertFill(metaObject, "deleted", Integer.class, GlobalConstants.DELETED_NO);

        if (metaObject.hasGetter("tenantId") && getFieldValByName("tenantId", metaObject) == null) {
            strictInsertFill(metaObject, "tenantId", Long.class, UserContext.getTenantId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时强制覆盖 updateTime / updateBy
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        setFieldValByName("updateBy", safeUserId(), metaObject);
    }

    private Long safeUserId() {
        try {
            Long id = UserContext.getUserId();
            return id == null ? 0L : id;
        } catch (Throwable ex) {
            return 0L;
        }
    }
}
