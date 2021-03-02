package cn.bgotech.wormhole.olap.exception;

/**
 * Created by ChenZhiGang on 2017/5/23.
 */
public class OlapRuntimeException extends RuntimeException {
    public OlapRuntimeException(Throwable cause) {
        super(cause);
    }

    public OlapRuntimeException(String message) {
        super(message);
    }

    public OlapRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
