// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.dimension;

import cn.bgotech.wormhole.olap.mddm.physical.dimension.MeasureDimension;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public class MeasureDimensionBean extends DimensionBean implements MeasureDimension {

    public MeasureDimensionBean() {
        setBinaryControlFlag(1);
    }

    public MeasureDimensionBean(DimensionBean dimBean) {
        this();
        try {
            BeanUtils.copyProperties(this, dimBean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
