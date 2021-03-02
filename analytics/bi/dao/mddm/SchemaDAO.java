// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.mddm;

import cn.bgotech.analytics.bi.bean.mddm.physical.schema.SpaceBean;

import java.util.List;
import java.util.Map;

/**
 * Created by ChenZhiGang on 2017/5/19.
 */
public interface SchemaDAO {

    SpaceBean findByName(String name);

    int save(SpaceBean space);

    List<Map> cubeLeafMembersMap();
}

