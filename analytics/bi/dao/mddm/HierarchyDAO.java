// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.mddm;

import cn.bgotech.analytics.bi.bean.mddm.physical.HierarchyBean;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import org.apache.ibatis.annotations.Param;
//import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/17.
 */
public interface HierarchyDAO {

    int save(HierarchyBean hierarchy);

    HierarchyBean load(long id);

    List<HierarchyBean> loadByName(@Param("name") String name);

    List<Hierarchy> selectByDimensionId(Long dimensionId);
}
