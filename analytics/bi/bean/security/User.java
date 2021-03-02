// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.security;

//import cn.bgotech.analytics.bi.bean.Bean;
import cn.bgotech.analytics.bi.bean.CommonBean;

import java.util.Date;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class User extends CommonBean {

//    private Long id;
//    private String username;
    private String password;
    private Integer status = 0; // (0:created|1:active)
    private Date expireTime = new Date(System.currentTimeMillis() + 3600000 * 24 * 60); // 100 days later expired
    private String spaceName;

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return "User { id = " + getId() + ", name = " + getName() + " }";
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }
}
