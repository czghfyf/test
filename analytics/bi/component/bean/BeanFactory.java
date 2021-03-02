package cn.bgotech.analytics.bi.component.bean;

import cn.bgotech.analytics.bi.bean.Bean;
import cn.bgotech.analytics.bi.bean.CommonBean;
import cn.bgotech.analytics.bi.bean.mddm.MddmBean;
import cn.bgotech.analytics.bi.component.common.SequenceGenerator;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
@Component
public class BeanFactory {

    @Autowired
    private SequenceGenerator sequenceGenerator;

    public Bean create(Class<? extends Bean> clazz) {
        if (clazz == null || !Bean.class.isAssignableFrom(clazz)) {
            throw new BIRuntimeException("not support class type: " + clazz);
        }
        Bean bean;
        try {
            bean = clazz.newInstance();
            if (bean instanceof MddmBean) {
                ((MddmBean) bean).setMgId(sequenceGenerator.nextMDDMGlobalId());
            }
            if (bean instanceof CommonBean) {
                ((CommonBean) bean).setId(sequenceGenerator.nextValue(bean.getClass()));
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BIRuntimeException(e);
        }
        return bean;
    }

}
