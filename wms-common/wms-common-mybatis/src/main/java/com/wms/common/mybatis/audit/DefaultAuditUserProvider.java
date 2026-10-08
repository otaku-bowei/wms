package com.wms.common.mybatis.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 默认审计用户提供者
 *
 * @author WMS
 */
@Component
@ConditionalOnMissingBean(AuditUserProvider.class)
public class DefaultAuditUserProvider implements AuditUserProvider {

    public static final String SYSTEM = "system";

    @Override
    public String currentUsername() {
        return SYSTEM;
    }
}
