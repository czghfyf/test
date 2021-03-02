package cn.bgotech.analytics.bi.component.common;

import cn.bgotech.analytics.bi.bean.seq.SequenceBean;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.common.SequenceDAO;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.analytics.bi.system.config.SystemConfiguration;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.physical.member.DateMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
@Component
public class SequenceGenerator {

    private static final int SEQUENCE_VALUE_INCREMENT = 50;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SystemConfiguration sysConfig;

    @Autowired
    private BeanFactory beanFactory;

    @Resource
    private SequenceDAO sequenceDAO;

    private final Map<Class, SequenceUnit> seqUnitPool = new HashMap<>();

    // @Transactional // not require transactional support
    public Long nextValue(Class clazz) {
        if (!seqUnitPool.containsKey(clazz)) {
            activateSequence(clazz);
        }
        SequenceUnit seqMng = seqUnitPool.get(clazz);
        return seqMng.nextValue(sysConfig);
    }

    /**
     * create or load SequenceBean
     * @param clazz
     */
    private synchronized void activateSequence(Class clazz) {

        if (seqUnitPool.containsKey(clazz)) {
            return;
        }

        SequenceUnit seqUnit;
        SequenceBean sequenceBean = sequenceDAO.loadByName(clazz.getName());

        if (sequenceBean == null) {

            sequenceBean = (SequenceBean) beanFactory.create(SequenceBean.class);

            sequenceBean.setName(clazz.getName());
            sequenceBean.setCurrentValue(1L + SEQUENCE_VALUE_INCREMENT);

//            Account currentAccount = ThreadLocalTool.getCurrentAccount();
//            if (currentAccount == null) {
//                if (getSpringAppContext().getBean(ProphetAnalyticsApplication.class)
//                        .isSystemInitComplete()) {
//                    throw new BIRuntimeException(
//                            "system initialization has been completed, the current account can not be empty");
//                }
//            } else {
//                sequenceBean.setCreatorAccountId(currentAccount.getId());
//                sequenceBean.setLastModifiedAccountId(currentAccount.getId());
//            }

            sequenceDAO.save(sequenceBean);

            seqUnit = new SequenceUnit(1L, sequenceBean, this);
        } else {

            long currentValue = sequenceBean.getCurrentValue();
            sequenceBean.setCurrentValue(currentValue + SEQUENCE_VALUE_INCREMENT);

            sequenceBean.setLastModifiedTime(new Date(System.currentTimeMillis()));
            sequenceBean.setLastModifierId(ThreadLocalTool.getCurrentUser() == null ? -1 : ThreadLocalTool.getCurrentUser().getId());
logger.info("update sequence: " + sequenceBean.toString());
            sequenceDAO.update(sequenceBean);

            seqUnit = new SequenceUnit(currentValue, sequenceBean, this);
        }

//        SeqManager seqMng = new SeqManager(sequenceBean, this);
        seqUnitPool.put(clazz, seqUnit);
    }

    /**
     * @return next MDDM Global ID
     */
    public Long nextMDDMGlobalId() {
        return nextValue(OlapEngine.class);
    }

    private static class SequenceUnit {

        private Long currentValue;

        private SequenceBean sequenceBean;

        private SequenceGenerator sequenceGenerator;

        SequenceUnit(long currentValue, SequenceBean sequenceBean, SequenceGenerator sequenceGenerator) {
            this.sequenceBean = sequenceBean;
            this.currentValue = currentValue;
            this.sequenceGenerator = sequenceGenerator;
        }

        synchronized Long nextValue(SystemConfiguration sysCfg) {

            long presetDateMemberGlobalIdStart = DateMember.MD_GLOBAL_ID_START;
            long presetDateMemberGlobalIdEnd = DateMember.MD_GLOBAL_ID_END;

            // if current sequence mean the MDDMGlobalSequence
            if (OlapEngine.class.getName().equals(sequenceBean.getName())) {
                // skip [15000000, 22001402], because the scope be used at DateMemberBean already
                if (currentValue >= presetDateMemberGlobalIdStart
                        && currentValue <= presetDateMemberGlobalIdEnd) {
                    currentValue = presetDateMemberGlobalIdEnd + 1;
                    sequenceBean.setCurrentValue(presetDateMemberGlobalIdEnd + SEQUENCE_VALUE_INCREMENT);
                    sequenceGenerator.sequenceDAO.update(sequenceBean);
                }
            }

            if (currentValue + 1 > sequenceBean.getCurrentValue()) {
                throw new BIRuntimeException(
                        "The current value of the sequence[" + sequenceBean.getName() + "] is abnormal");
            }

            if (currentValue + 1 == sequenceBean.getCurrentValue()) {
                sequenceBean.setCurrentValue(sequenceBean.getCurrentValue() + SEQUENCE_VALUE_INCREMENT);
                sequenceGenerator.sequenceDAO.update(sequenceBean);
            }
            return currentValue++;
        }

    }

}
