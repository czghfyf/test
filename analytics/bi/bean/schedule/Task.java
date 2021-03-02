// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.schedule;

import cn.bgotech.analytics.bi.bean.CommonBean;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;

import java.util.Date;

public class Task extends CommonBean {

    public enum Status {
        INITIAL,    // binaryControlFlag: 00
        EXECUTING,  // binaryControlFlag: 01
        SUCCESS,    // binaryControlFlag: 10
        FAILED      // binaryControlFlag: 11
    }

    private String description;

    private Date startTime;

    private Date endTime;

    // (00:not executed yet|01:executing|10:success|11:failed)
    private Integer binaryControlFlag = 0;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getBinaryControlFlag() {
        return binaryControlFlag;
    }

    public void setBinaryControlFlag(Integer binaryControlFlag) {
        this.binaryControlFlag = binaryControlFlag;
    }

    public void changeState(Status status) {
        binaryControlFlag = binaryControlFlag == null ? 0 : binaryControlFlag;
        switch (status) {
            case INITIAL:
                binaryControlFlag = binaryControlFlag - binaryControlFlag % 4;
                break;
            case EXECUTING:
                binaryControlFlag = binaryControlFlag - binaryControlFlag % 4 + 1;
                break;
            case SUCCESS:
                binaryControlFlag = binaryControlFlag - binaryControlFlag % 4 + 2;
                break;
            case FAILED:
                binaryControlFlag = binaryControlFlag - binaryControlFlag % 4 + 3;
                break;
            default:
                throw new BIRuntimeException("status is " + status);
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "description='" + description + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", binaryControlFlag=" + binaryControlFlag +
                '}';
    }
}
