// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.mddm;

import cn.bgotech.analytics.bi.bean.mddm.MddmAttribute;
import org.apache.ibatis.annotations.Param;

/**
 * Created by ChenZhiGang on 2018/2/19.
 */
public interface AttributesDAO {

    MddmAttribute load(@Param("mgId") long mgId, @Param("attributeKey") String attributeKey);

    int save(MddmAttribute mddmAttribute);

}
