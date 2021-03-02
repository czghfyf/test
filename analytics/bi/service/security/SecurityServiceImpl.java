package cn.bgotech.analytics.bi.service.security;

import cn.bgotech.analytics.bi.bean.security.Role;
import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.bean.security.UserRoleMapper;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.security.SecurityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
@Service
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private SecurityDao securityDao;

    @Autowired
    private BeanFactory beanFactory;

    @Transactional
    @Override
    public void createUser(String userName, String password, String roleName) {

        if (securityDao.loadUserByName(userName) != null) {
            throw new RuntimeException("username '" + userName + "' already exist");
        }

        // create user
        User user = (User) beanFactory.create(User.class);
        user.setName(userName);
        user.setPassword(SecurityService.encryptPassword(password));
        user.setExpireTime(new Date(System.currentTimeMillis() + 3600000 * 24 * 365 * 10));
        user.setStatus(1);
        securityDao.saveUser(user);

        // select or create role
        Role role = securityDao.loadRoleByName(roleName);
        if (role == null) {
            role = (Role) beanFactory.create(Role.class);
            role.setName(roleName);
            securityDao.saveRole(role);
        }

        // create user role mapping
        UserRoleMapper urm = (UserRoleMapper) beanFactory.create(UserRoleMapper.class);
        urm.setUserId(user.getId());
        urm.setRoleId(role.getId());
        securityDao.saveUserRoleMapper(urm);

    }

    @Transactional
    @Override
    public void createAffiliatedUser(String affiliatedUsername, String affiliatedPassword, String spaceName, String roleName) {
        createUser(affiliatedUsername, affiliatedPassword, roleName);
        User u = securityDao.loadUserByName(affiliatedUsername);
        u.setSpaceName(spaceName);
        securityDao._updateUserById(u);
    }

    @Override
    public void resetPassword(String username, String newPassword) {

        User user = securityDao.loadUserByName(username);

        if (user == null) {
            throw new RuntimeException("username [" + username + "] isn't exist");
        }

        user.setPassword(SecurityService.encryptPassword(newPassword));

        user.setLastModifiedTime(new Date(System.currentTimeMillis()));

        securityDao.saveUser(user);

    }

    @Override
    public List<User> loadAllUsers() {
        return securityDao.loadAllUsers();
    }

    @Override
    public boolean isAdminUser(User user) {
        List<User> adminUsers = securityDao.loadAllAdminUsers();
        for (User u : adminUsers) {
            if (u.getName().equals(user.getName())) {
                return true;
            }
        }
        return false;
    }
}
