package cn.bgotech.wormhole.olap.mddm.physical;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/6/8.
 */
public interface ClassicMDDM extends PhysicalEntity {

    default List<String> getFullNamesPath() {
        return null;
    }

}
