package cn.bgotech.analytics.bi.controller.response;

public class ResponseData {

    public static final ResponseData SUCCESS_INSTANCE = new ResponseData();

    public enum Status {
        SUCCESS,
        FAILURE,
        SESSION_TIMEOUT
    }

    private Object data;

    private Status status = Status.SUCCESS;

//    public ResponseData(Object data) {
//        this.data = data;
//    }

//    public ResponseData(Status status) {
//        this.status = status;
//    }

    public Object getData() {
        return data;
    }

    public ResponseData setData(Object data) {
        this.data = data;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public ResponseData setStatus(Status status) {
        this.status = status;
        return this;
    }
}
