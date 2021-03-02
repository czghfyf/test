package cn.bgotech.analytics.bi.component.system;

import cn.bgotech.analytics.bi.bean.system.SystemStatusBean;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.system.SystemStatusDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by ChenZhiGang on 2017/6/15.
 */
@Component
public class SystemStatus {

//    @Resource
//    private SystemStatusDAO systemStatusDAO;

    @Resource
    private SystemStatusDAO sysStatDAO;

    @Autowired
    private BeanFactory beanFactory;

    public enum Type {

        // valid status [ WAIT_FOR_INIT | INIT_COMPLETE ]
        BI_SYSTEM_INIT_STATUS,

        // valid status [ WAIT_FOR_INIT | INIT_COMPLETE ]
        OLAP_ENGINE_INIT_STATUS

    }

    public enum Value {
        WAIT_FOR_INIT,
        INIT_COMPLETE,
        UNKNOWN
    }

    public SystemStatusBean get(Type statusType) {
        return sysStatDAO.load(statusType.name());
    }

    public boolean match(Type statusType, Value statusValue) {
        return statusValue.name().equals(sysStatDAO.load(statusType.name()).getStatusValue());
    }

    public void alter(Type statusType, Value statusValue) {

        String action = "update";
        SystemStatusBean statusBean = get(statusType);
        if (statusBean == null) {
            statusBean = (SystemStatusBean) beanFactory.create(SystemStatusBean.class);
            statusBean.setName(statusType.name());
            action = "create";
        }
        statusBean.setStatusValue(statusValue.name());
        switch (action) {
            case "update":
                sysStatDAO.update(statusBean);
                return;
            case "create":
                sysStatDAO.save(statusBean);
        }
    }



}
