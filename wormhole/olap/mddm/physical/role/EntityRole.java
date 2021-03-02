package cn.bgotech.wormhole.olap.mddm.physical.role;

import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.ClassicMDDM;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface EntityRole extends ClassicMDDM/*BasicEntityModel*/ {

    /**
     *
     * @param entity
     * @param dimensionRole
     * @return
     */
    static EntityRole createOne(ClassicMDDM entity, DimensionRole dimensionRole) throws OlapException {
        if (entity instanceof Dimension) {
            if (dimensionRole.getDimension().equals(entity)) {
                return dimensionRole; // return (EntityRole) dimensionRole.clone();
            }
            throw new OlapException("Parameters dimension information does not match");
        } else if (entity instanceof Hierarchy) {
            return new HierarchyRole(dimensionRole, (Hierarchy) entity);
        } else if (entity instanceof Level) {
            return new LevelRole(dimensionRole, (Level) entity);
        } else if (entity instanceof Member) {
            return new MemberRole(dimensionRole, (Member) entity);
        }
        return null;
    }


}
