// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.spa;

import cn.bgotech.analytics.bi.bean.spa.MDQuerySchema;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;

import java.util.List;

/**
 * Created by ChenZhigang on 2017/8/11.
 */
public interface SinglePageAppDAO {

    /**
     * @param qs
     * @return - number of inserts
     */
    int saveMDQuerySchema(MDQuerySchema qs);

    /**
     *
     * @param qs
     * @return - number of updates
     */
    int updateMDQuerySchema(MDQuerySchema qs);

    List<MDQuerySchema> findMDQuerySchemas(Space space);

}
