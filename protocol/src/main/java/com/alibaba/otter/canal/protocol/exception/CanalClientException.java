package com.alibaba.otter.canal.protocol.exception;

import org.apache.commons.lang3.exception.ContextedRuntimeException;

/**
 * @author zebin.xuzb @ 2012-6-20
 * @version 1.0.0
 */
public class CanalClientException extends ContextedRuntimeException {

    private static final long serialVersionUID = -7545341502620139031L;

    public CanalClientException(String errorCode){
        super(errorCode);
    }

    public CanalClientException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public CanalClientException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public CanalClientException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public CanalClientException(Throwable cause){
        super(cause);
    }
}
