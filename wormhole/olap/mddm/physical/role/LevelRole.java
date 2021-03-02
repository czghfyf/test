package cn.bgotech.wormhole.olap.mddm.physical.role;

import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public class LevelRole implements EntityRole {

    private DimensionRole dimensionRole;
    private Level level;

    public LevelRole(DimensionRole dimensionRole, Level level) {
        this.dimensionRole = dimensionRole;
        this.level = level;
    }

    @Override
    public Long getMgId() {
        return -1L;
    }

    @Override
    public String getName() {
        return level.getName();
    }

    public DimensionRole getDimensionRole() {
        return dimensionRole;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public boolean isWithinRange(Space space) {
        return dimensionRole.isWithinRange(space);
    }

    @Override
    public boolean associatedWith(Cube cube) {
        return dimensionRole.associatedWith(cube);
    }

    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        return new LevelRole(getDimensionRole(), getLevel());
    }
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151022
    //    package com.dao.wormhole.core.model.entity.role;
    //
    //            import java.util.List;
    //
    //            import com.dao.wormhole.core.mdx.IR.EntityPathBlock;
    //            import com.dao.wormhole.core.model.entity.Cube;
    //            import com.dao.wormhole.core.model.entity.Level;
    //            import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.entity.property.Property;
    //
    //    public class LevelRole implements ILevelRole {
    //
    //        private IDimensionRole dr;
    //
    //        private Level l;
    //
    //        public LevelRole(IDimensionRole dr, Level l) {
    //    //		super();
    //            this.dr = dr;
    //            this.l = l;
    //        }
    //
    //        @Override
    //        public int getWormholeID() {
    //    //		return l.getWormholeID();
    //            return -1;
    //        }
    //
    //        @Override
    //        public String getName() {
    //            return l.getName();
    //        }
    //
    //        @Override
    //        public boolean byCubeReference(Cube cube) {
    //    //		return l.byCubeReference(cube);
    //            return this.dr.getDimension().byCubeReference(cube);
    //        }
    //
    //        @Override
    //        public WormholeBaseEntity selectWormholeEntityByPath(List<String> path) {
    //            return l.selectWormholeEntityByPath(path);
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
    //            return l.fullPathName();
    //        }
    //
    //        @Override
    //        public List<Property> getProperties() {
    //            return l.getProperties();
    //        }
    //
    //        @Override
    //        public Property getProperty(String key) {
    //            return l.getProperty(key);
    //        }
                                                                                                //
                                                                                                //        @Override
                                                                                                //        public IDimensionRole getDimensionRole() {
                                                                                                //            return dr;
                                                                                                //        }
                                                                                                //
                                                                                                //        @Override
                                                                                                //        public Level getLevel() {
                                                                                                //            return l;
                                                                                                //        }
                                                                                                //
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
