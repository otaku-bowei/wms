package com.wms.common.core.exception;

import com.wms.common.core.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * <p>用于承载可预期的业务错误，由全局异常处理器转换为统一响应体。
 *
 * @author WMS
 */
@Getter
public class BizException extends RuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }
}
