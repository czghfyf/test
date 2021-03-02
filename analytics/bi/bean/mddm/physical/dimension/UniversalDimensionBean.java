// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.dimension;

import cn.bgotech.wormhole.olap.mddm.physical.dimension.UniversalDimension;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public class UniversalDimensionBean extends DimensionBean implements UniversalDimension {

    public UniversalDimensionBean() {

    }

    public UniversalDimensionBean(DimensionBean dimBean) {
        try {
            BeanUtils.copyProperties(this, dimBean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}