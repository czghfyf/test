package cn.bgotech.wormhole.olap.mddm.physical.role;

import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.PhysicalEntity;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface DimensionRole extends EntityRole, PhysicalEntity, Comparable<DimensionRole> {

    /**
     * find and return all member role objects
     * @return
     */
    List<MemberRole> findMemberRoles();

    Dimension getDimension();

    Cube getCube();
}
