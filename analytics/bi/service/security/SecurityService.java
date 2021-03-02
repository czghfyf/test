package cn.bgotech.analytics.bi.service.security;

import cn.bgotech.analytics.bi.bean.security.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public interface SecurityService {

    static String encryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    void createUser(String userName, String password, String roleName);

    void resetPassword(String username, String newPassword);

    List<User> loadAllUsers();

    boolean isAdminUser(User user);

    void createAffiliatedUser(String affiliatedUsername, String affiliatedPassword, String spaceName, String roleName);
}
