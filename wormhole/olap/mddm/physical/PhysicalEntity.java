package cn.bgotech.wormhole.olap.mddm.physical;

import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface PhysicalEntity extends BasicEntityModel {

    Long getMgId();

    String getName();

    /**
     * Whether the 'PhysicalEntity'(this object) is within this 'space'(parameter: space) ?
     * @param space
     * @return
     */
    boolean isWithinRange(Space space);
}
