// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.mddm;

import cn.bgotech.analytics.bi.bean.mddm.physical.LevelBean;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/17.
 */
public interface LevelDAO {

    int save(LevelBean level);

    LevelBean loadById(long id);

    LevelBean load(@Param("hierarchyId") long hierarchyId, @Param("memberLevel") int memberLevel);

    List<LevelBean> loadByName(String name);

    List<Level> selectByHierarchyId(Long hierarchyId);

}
