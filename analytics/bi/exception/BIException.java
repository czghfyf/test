package cn.bgotech.analytics.bi.exception;

public class BIException extends Exception {

    public BIException(Throwable cause) {
        super(cause);
    }

    public BIException(String message) {
        super(message);
    }
}
