package cn.bgotech.analytics.bi.dto.security;

import cn.bgotech.analytics.bi.bean.security.User;

/**
 * Created by ChenZhiGang on 2017/6/5.
 */
public class UserDTO {

    private User user;

    public UserDTO(User user) {
        this.user = user;
    }

    public String getName() {
        return user.getName();
    }

    public String getSpaceName() {
        return user.getSpaceName();
    }
}
