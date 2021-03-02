// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.security;

import cn.bgotech.analytics.bi.bean.security.Role;
import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.bean.security.UserRoleMapper;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public interface SecurityDao {

    default int saveUser(User user) {
        User u = loadUserByName(user.getName());
        if (u == null) {
            return _insertIntoUser(user);
        } else {
            return _updateUserById(user);
        }
    }

    int _insertIntoUser(User user);

    int _updateUserById(User user);

    int saveRole(Role role);

    User loadUserByName(String username);

    int saveUserRoleMapper(UserRoleMapper mapper);

    Role loadRoleByName(String roleName);

    List<User> loadAllUsers();

    List<User> loadAllAdminUsers();
}
