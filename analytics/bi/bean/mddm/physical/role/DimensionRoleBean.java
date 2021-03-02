// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.role;

import cn.bgotech.analytics.bi.bean.mddm.MddmBean;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.HierarchyRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.LevelRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public class DimensionRoleBean extends MddmBean implements DimensionRole {

    private Long cubeId;
    private Long dimensionId;

    private Cube cube;
    private Dimension dimension;

    @Deprecated
    private String mappingColumn;

    public DimensionRoleBean() {

    }

    public DimensionRoleBean(Cube cube, Dimension dimension) {
        this.cube = cube;
        this.dimension = dimension;
        cubeId = cube.getMgId();
        dimensionId = dimension.getMgId();
    }

    public Long getCubeId() {
        return cubeId;
    }

    public void setCubeId(Long cubeId) {
        this.cubeId = cubeId;
    }

    public Long getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(Long dimensionId) {
        this.dimensionId = dimensionId;
    }


    @Deprecated
    public String getMappingColumn() {
        return mappingColumn;
    }

    @Deprecated
    public void setMappingColumn(String mappingColumn) {
        this.mappingColumn = mappingColumn;
    }

    @Override
    public boolean isWithinRange(Space space) {
        OlapEngine oe = OlapEngine.hold();
        Dimension dimension = oe.getMddmStorageService().findDimension(dimensionId);
        Cube cube = oe.getMddmStorageService().findCube(cubeId);
        return cube.isWithinRange(space) || dimension.isWithinRange(space);
    }

    @Override
    public List<MemberRole> findMemberRoles() {
        List<MemberRole> mrs = new ArrayList<>();
        for (Member m : getDimension().getAllMembers()) {
            mrs.add(new MemberRole(this, m));
        }
        return mrs;
    }

    @Override
    public Dimension getDimension() {
        return OlapEngine.hold().findDimensionById(dimensionId);
    }

    @Override
    public Cube getCube() {
        return OlapEngine.hold().getMddmStorageService().find(Cube.class, getCubeId());
    }

    @Override
    public int compareTo(DimensionRole o) {
        return (int) (getMgId() - o.getMgId());
    }

    @Override
    public boolean associatedWith(Cube cube) {
        return cube.getMgId().equals(cubeId);
    }

    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {

        BasicEntityModel e = this.getDimension().selectSingleEntity(segmentedSelector);

        if (e instanceof Hierarchy) {
            return new HierarchyRole(this, ((Hierarchy) e));
        } else if (e instanceof Level) {
            return new LevelRole(this, ((Level) e));
        } else if (e instanceof Member) {
            return new MemberRole(this, ((Member) e));
        }/* else {
            throw new RuntimeException("Illegal class [" + e.getClass().getName() + "]");
        }*/
        return null;

    }
}

