// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.exception;

public class BIRuntimeException extends RuntimeException {
    public BIRuntimeException(Throwable cause) {
        super(cause);
    }

    public BIRuntimeException(String message) {
        super(message);
    }
}
