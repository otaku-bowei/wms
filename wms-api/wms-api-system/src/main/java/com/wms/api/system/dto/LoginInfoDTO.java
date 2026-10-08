package com.wms.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录信息更新参数（跨服务传输）
 *
 * @author WMS
 */
@Data
public class LoginInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 登录 IP */
    private String ipAddress;
}
