package com.wms.common.mybatis.audit;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import jakarta.annotation.Resource;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计字段自动填充
 *
 * <p>自动填充 createBy / createTime / updateBy / updateTime，
 * 实体需存在对应字段才会填充。
 *
 * @author WMS
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATE_BY = "createBy";
    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_BY = "updateBy";
    private static final String UPDATE_TIME = "updateTime";

    @Resource
    private AuditUserProvider auditUserProvider;

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String username = resolveUsername();
        if (metaObject.hasSetter(CREATE_TIME)) {
            this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        }
        if (metaObject.hasSetter(CREATE_BY)) {
            this.strictInsertFill(metaObject, CREATE_BY, String.class, username);
        }
        if (metaObject.hasSetter(UPDATE_TIME)) {
            this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);
        }
        if (metaObject.hasSetter(UPDATE_BY)) {
            this.strictInsertFill(metaObject, UPDATE_BY, String.class, username);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter(UPDATE_TIME)) {
            this.strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter(UPDATE_BY)) {
            this.strictUpdateFill(metaObject, UPDATE_BY, String.class, resolveUsername());
        }
    }

    private String resolveUsername() {
        try {
            String username = auditUserProvider.currentUsername();
            return (username == null || username.isBlank()) ? DefaultAuditUserProvider.SYSTEM : username;
        } catch (Exception e) {
            return DefaultAuditUserProvider.SYSTEM;
        }
    }
}
