package cn.bgotech.analytics.bi.service;

import cn.bgotech.analytics.bi.bean.mddm.physical.schema.SpaceBean;
import cn.bgotech.analytics.bi.bean.security.Role;
import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.mddm.SchemaDAO;
import cn.bgotech.analytics.bi.dao.security.SecurityDao;
import cn.bgotech.analytics.bi.service.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created by czg on 2019/5/30.
 */
@Service
public class SuperAdminService {

    @Autowired
    private SecurityService securityService;

    @Resource
    private SchemaDAO schemaDAO;

    @Resource
    private SecurityDao securityDao;

    @Autowired
    private BeanFactory beanFactory;

    @Transactional
    public void createSpaceMasterUser(String username, String password) {

        SpaceBean space = schemaDAO.findByName(username);
        if (space != null)
            throw new RuntimeException("space [" + username + "] already exist.");

        securityService.createUser(username, password, Role.Type.SPACE_USER.name());

        space = (SpaceBean) beanFactory.create(SpaceBean.class);
        space.setName(username);
        schemaDAO.save(space);

        User u = securityDao.loadUserByName(username);
        u.setSpaceName(username);
        securityDao._updateUserById(u);
    }

}
