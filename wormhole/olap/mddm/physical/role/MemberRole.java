package cn.bgotech.wormhole.olap.mddm.physical.role;

import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public class MemberRole implements EntityRole { // TODO: HierarchyRole, LevelRole, MemberRole should be LogicEntity !!!!!!

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DimensionRole dimensionRole;
    private Member member;

    public static List<DimensionRole> extract(List<MemberRole> memberRoles) {
//        return (List<DimensionRole>) ((SetConverter<DimensionRole, MemberRole>) o -> null).convert(memberRoles);
        List<DimensionRole> dimensionRoles = new ArrayList<>(memberRoles.size());
        for (int i = 0; i < memberRoles.size(); i++) {
            dimensionRoles.add(memberRoles.get(i).getDimensionRole());
        }
        return dimensionRoles;
    }

    public MemberRole(DimensionRole dimensionRole, Member member) {
        this.dimensionRole = dimensionRole;
        this.member = member;
    }

    public DimensionRole getDimensionRole() {
        return dimensionRole;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public Long getMgId() {
        logger.warn("Member role object do not have an [mgId] attribute !!!");
        return null /*member.getMgId()*/;
    }

    @Override
    public String getName() {
        return member.getName();
    }

    @Override
    public boolean isWithinRange(Space space) {
        // TODO: at once. return this.dimensionRole.dimension.isWithinRange(space)
        throw new RuntimeException("TODO: at once. return this.dimensionRole.dimension.isWithinRange(space)");

    }

    @Override
    public boolean associatedWith(Cube cube) {
        return member.associatedWith(cube);
    }

    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        return new MemberRole(getDimensionRole(), getMember().selectSingleEntity(segmentedSelector));
    }

    public MemberRole getParent() {
        return new MemberRole(dimensionRole, member.getParent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemberRole that = (MemberRole) o;
        if (dimensionRole != null ? !dimensionRole.equals(that.dimensionRole) : that.dimensionRole != null) {
            return false;
        }
        return member != null ? member.equals(that.member) : that.member == null;
    }

    @Override
    public int hashCode() {
        int result = dimensionRole != null ? dimensionRole.hashCode() : 0;
        result = 31 * result + (member != null ? member.hashCode() : 0);
        return result;
    }

}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    package com.dao.wormhole.core.model.entity.role;
    //
    //            import com.dao.wormhole.core.mdx.IR.EntityPathBlock;
    //            import com.dao.wormhole.core.model.entity.Cube;
    //            import com.dao.wormhole.core.model.entity.Dimension;
    //            import com.dao.wormhole.core.model.entity.Member;
    //            import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.entity.property.Property;
    //
    //    public class MemberRole implements IMemberRole {
    //
    //        private IDimensionRole dr;
    //        private Member m;
    //
    //        public MemberRole(IDimensionRole dr, Member m) {
    //            this.dr = dr;
    //            this.m = m;
    //        }
    //
    //        @Override
    //        public String getName() {
    //            return m.getName();
    //        }
    //
    //        @Override
    //        public boolean byCubeReference(Cube cube) {
    //            return m.byCubeReference(cube);
    //        }
    //
    //        @Override
    //        public WormholeBaseEntity selectWormholeEntityByPath(List<String> path) {
    //            return m.selectWormholeEntityByPath(path);
    //        }
    //        @Override
    //        public String fullPathName() {
    //            return m.fullPathName();
    //        }
    //
    //        @Override
    //        public List<Property> getProperties() {
    //            return m.getProperties();
    //        }
    //
    //        @Override
    //        public Property getProperty(String key) {
    //            return m.getProperty(key);
    //        }
    //
    //        @Override
    //        public Dimension getDimension() {
    //            return m.getDimension();
    //        }
    //
    //        @Override
    //        public IDimensionRole getDimensionRole() {
    //            return dr;
    //        }
    //
    //        @Override
    //        public Member getMember() {
    //            return m;
    //        }
    //
    //        @Override
    //        public String getDisplay() {
    //            return new StringBuilder("&")
    //                    .append(dr.getWormholeID())
    //                    .append("[")
    //                    .append(dr.getName())
    //                    .append("].&")
    //                    .append(m.getWormholeID())
    //                    .append("[")
    //                    .append(m.getName())
    //                    .append("]")
    //                    .toString();
    //        }
    //
    //    //	@Override
    //    //	public ILevelRole getLevelRole() {
    //    //		return m.getLevel() == null ? null : new LevelRole(dr, m.getLevel());
    //    //	}
    //
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????