package cn.bgotech.analytics.bi.service;

import cn.bgotech.analytics.bi.bean.mddm.physical.schema.SpaceBean;
import cn.bgotech.analytics.bi.bean.security.Role;
import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.bean.security.UserRoleMapper;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.component.system.SystemStatus;
import cn.bgotech.analytics.bi.dao.mddm.SchemaDAO;
import cn.bgotech.analytics.bi.dao.security.SecurityDao;
import cn.bgotech.analytics.bi.service.security.SecurityService;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.analytics.bi.system.config.SystemConfiguration;
import cn.bgotech.wormhole.olap.OlapEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
@Service
public class InitializationActionService {

    @Autowired
    private SystemConfiguration sysCfg;

    @Autowired
    private SystemStatus sysStat;

    @Autowired
    private BeanFactory beanFactory;

    @Resource
    private SecurityDao securityDao;

    @Resource
    private SchemaDAO schemaDAO;

    @Autowired
    @Qualifier("olap_engine_instance")
    private OlapEngine olapEngine;

    @Transactional("transactionManager")
    public void init() {

        User admin = new User();
        admin.setId(0L);
        admin.setName(sysCfg.getBsia());
        admin.setSpaceName(sysCfg.getBsia());
        admin.setPassword(SecurityService.encryptPassword(sysCfg.getBsip()));
        admin.setStatus(1);
        admin.setExpireTime(new Date(System.currentTimeMillis() + 3600000 * 24 * 365 * 10));
        admin.setCommonBinaryFlag(2);

        ThreadLocalTool.setCurrentUser(admin);

        Role superRole = (Role) beanFactory.create(Role.class);
        superRole.setName(Role.Type.CHEP_MAIN_BRAIN.name());
        superRole.setCommonBinaryFlag(2);

        securityDao.saveUser(admin);
        securityDao.saveRole(superRole);

        UserRoleMapper urm = (UserRoleMapper) beanFactory.create(UserRoleMapper.class);
        urm.setUserId(admin.getId());
        urm.setRoleId(superRole.getId());
        securityDao.saveUserRoleMapper(urm);

        SpaceBean space = (SpaceBean) beanFactory.create(SpaceBean.class);
        space.setName(sysCfg.getBsia());
        schemaDAO.save(space);

        if (sysStat.get(SystemStatus.Type.OLAP_ENGINE_INIT_STATUS) == null
                || sysStat.match(SystemStatus.Type.OLAP_ENGINE_INIT_STATUS, SystemStatus.Value.WAIT_FOR_INIT)) {
            olapEngine.init();
            sysStat.alter(SystemStatus.Type.OLAP_ENGINE_INIT_STATUS, SystemStatus.Value.INIT_COMPLETE);
        }

        sysStat.alter(SystemStatus.Type.BI_SYSTEM_INIT_STATUS, SystemStatus.Value.INIT_COMPLETE);

    }
}
