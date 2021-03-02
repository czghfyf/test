package cn.bgotech.wormhole.olap.exception;

/**
 * Created by ChenZhiGang on 2017/5/22.
 */
public class OlapException extends Exception {
    public OlapException(Throwable cause) {
        super(cause);
    }

    public OlapException(String message) {
        super(message);
    }
}
