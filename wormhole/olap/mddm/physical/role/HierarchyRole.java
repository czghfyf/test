package cn.bgotech.wormhole.olap.mddm.physical.role;

import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public class HierarchyRole implements EntityRole {

    private DimensionRole dimensionRole;
    private Hierarchy hierarchy;

    public HierarchyRole(DimensionRole dimensionRole, Hierarchy hierarchy) {
        this.dimensionRole = dimensionRole;
        this.hierarchy = hierarchy;
    }

    @Override
    public Long getMgId() {
        return null; // TODO: do it later.
    }

    @Override
    public String getName() {
        return null; // TODO: do it later.
    }

    public DimensionRole getDimensionRole() {
        return dimensionRole;
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    @Override
    public boolean isWithinRange(Space space) {
        return false; // TODO: do it later.
    }

    @Override
    public boolean associatedWith(Cube cube) {
        return false; // TODO: do it later.
    }

    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        return new HierarchyRole(getDimensionRole(), getHierarchy());
    }
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    package com.dao.wormhole.core.model.entity.role;
    //
    //            import java.util.List;
    //
    //            import com.dao.wormhole.core.mdx.IR.EntityPathBlock;
    //            import com.dao.wormhole.core.model.entity.Cube;
    //    //import com.dao.wormhole.core.model.entity.Dimension;
    //            import com.dao.wormhole.core.model.entity.Hierarchy;
    //            import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.entity.property.Property;
    //
    //    public class HierarchyRole implements IHierarchyRole {
    //
    //        private IDimensionRole dimensionRole;
    //        private Hierarchy hierarchy;
    //
    //        public HierarchyRole(IDimensionRole dr, Hierarchy h) {
    //            hierarchy = h;
    //            dimensionRole = dr;
    //        }
    //
    //    //	@Override
    //    //	public Cube getCube() {
    //    //		// TODO Auto-generated method stub
    //    //		return null;
    //    //	}
    //
    //    //	@Override
    //    //	public Dimension getDimension() {
    //    //		// TODO Auto-generated method stub
    //    //		return null;
    //    //	}
    //
    //        @Override
    //        public int getWormholeID() {
    //            // TODO Auto-generated method stub
    //            return 0;
    //        }
    //
    //        @Override
    //        public String getName() {
    //            // TODO Auto-generated method stub
    //            return null;
    //        }
    //
    //        @Override
    //        public boolean byCubeReference(Cube cube) {
    //            // TODO Auto-generated method stub
    //            return false;
    //        }
                                                                                                //
                                                                                                //        @Override
                                                                                                //        public WormholeBaseEntity selectWormholeEntityByPath(List<String> path) {
                                                                                                //            // TODO Auto-generated method stub
                                                                                                //            return null;
                                                                                                //        }
                                                                                                //
                                                                                                //        @Override
                                                                                                //        public WormholeBaseEntity selectSingleEntity(List<EntityPathBlock> path) {
                                                                                                //            // TODO Auto-generated method stub
                                                                                                //            return null;
                                                                                                //        }
                                                                                                //
    //        @Override
    //        public String fullPathName() {
    //            // TODO Auto-generated method stub
    //            return null;
    //        }
    //
    //        @Override
    //        public List<Property> getProperties() {
    //            // TODO Auto-generated method stub
    //            return null;
    //        }
    //
    //        @Override
    //        public Property getProperty(String key) {
    //            // TODO Auto-generated method stub
    //            return null;
    //        }
    //
    //    //	@Override
    //    //	public int compareTo(IDimensionRole o) {
    //    //		// TODO Auto-generated method stub
    //    //		return 0;
    //    //	}
    //
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
