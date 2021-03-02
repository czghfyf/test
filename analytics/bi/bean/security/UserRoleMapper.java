// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.security;

import cn.bgotech.analytics.bi.bean.Bean;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class UserRoleMapper extends Bean {

    private Long userId;
    private Long roleId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
