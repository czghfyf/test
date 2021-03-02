// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean;

import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;

import java.util.Date;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public abstract class Bean {

    private Date createdTime;
    private Long creatorId;
    private Date lastModifiedTime;
    private Long lastModifierId;
    private Integer commonBinaryFlag = 0; // (1:preset | 0:) (1:deleted | 0:)

    public Bean() {
        lastModifiedTime = createdTime = new Date(System.currentTimeMillis());
//logger.debug("---------------->>>>>>>>>>>>>>>>>>>> createdTime = " + createdTime);
        User currentUser = ThreadLocalTool.getCurrentUser();
        lastModifierId = creatorId = currentUser != null ? currentUser.getId() : -1;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getLastModifierId() {
        return lastModifierId;
    }

    public void setLastModifierId(Long lastModifierId) {
        this.lastModifierId = lastModifierId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Integer getCommonBinaryFlag() {
        return commonBinaryFlag;
    }

    public void setCommonBinaryFlag(Integer commonBinaryFlag) {
        this.commonBinaryFlag = commonBinaryFlag;
    }

    public boolean isSystemPreset() {
        return (commonBinaryFlag & 2) == 2;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "createdTime=" + createdTime +
                ", creatorId=" + creatorId +
                ", lastModifiedTime=" + lastModifiedTime +
                ", lastModifierId=" + lastModifierId +
                ", commonBinaryFlag=" + commonBinaryFlag +
                '}';
    }

    public void setAllAttributesNull() {
        createdTime = null;
        creatorId = null;
        lastModifiedTime = null;
        lastModifierId = null;
        commonBinaryFlag = null;
    }
}
