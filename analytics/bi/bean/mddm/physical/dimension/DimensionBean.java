// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.dimension;

import cn.bgotech.analytics.bi.bean.mddm.MddmBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.HierarchyBean;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public class DimensionBean extends MddmBean implements Dimension {

    private Long defaultHierarchyId;
    private Integer binaryControlFlag = 0; // (1:measure dim|0:)
    private Long spaceId;
    private Integer maxMemberLevel;

    public Long getDefaultHierarchyId() {
        return defaultHierarchyId;
    }

    public void setDefaultHierarchyId(Long defaultHierarchyId) {
        this.defaultHierarchyId = defaultHierarchyId;
    }

    public Integer getBinaryControlFlag() {
        return binaryControlFlag;
    }

    public void setBinaryControlFlag(Integer binaryControlFlag) {
        this.binaryControlFlag = binaryControlFlag;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    @Override
    public boolean isWithinRange(Space space) {
//        return getSpaceId().equals(space.getMgId());
        return space.getMgId().equals(getSpaceId());
    }


    @Override
    public List<? extends Member> getAllMembers() {
        return OlapEngine.hold().findDimensionMembers(this);
    }

    @Override
    public List<Hierarchy> getHierarchies(String name) {
        List<Hierarchy> hierarchies = new ArrayList<>();
        List<Hierarchy> allHierarchies = OlapEngine.hold().find(Hierarchy.class, name);
        for (Hierarchy h : allHierarchies) {
            if (((HierarchyBean) h).getDimensionId().equals(getMgId())) {
                hierarchies.add(h);
            }
        }
        return hierarchies;
    }


    @Override
    public Member getDefaultMember() {
        return OlapEngine.hold().getMddmStorageService().getDefaultMember(this);
    }


    @Override
    public boolean associatedWith(Cube cube) {
        return cube.isReferenceDim(this);
    }

    @Override
    public Hierarchy getDefaultHierarchy() {
        return OlapEngine.hold().find(Hierarchy.class, defaultHierarchyId);
    }

    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        /*
         * BasicEntityModel entity = null;
         * if (当前维度对象.segmentedSelector[0] is superRootMember) {
         *     entity = superRootMember
         * } else if (当前维度对象.segmentedSelector[0] is Hierarchy) {
         *     entity = Hierarchy
         * } else if (当前维度对象.segmentedSelector[0] is Level) {
         *     entity = Level
         * } else if (当前维度对象.segmentedSelector[0] is not superRootMember) {
         *     entity = not superRootMember
         * }
         * if (segmentedSelector length == 1) {
         *     return entity
         * } else {
         *     return entity.selectSingleEntity(segmentedSelector[1... end])
         * }
         */
        BasicEntityModel entity = OlapEngine.hold().selectSingleEntity(this, segmentedSelector.getPart(0));
        return entity == null ? null : segmentedSelector.length() > 1 ? entity.selectSingleEntity(segmentedSelector.subSelector(1)) : entity;
    }

    @Override
    public Member getSuperRootMember() {
        for (Member m : getAllMembers()) {
            if (m.isRoot()) {
                return m;
            }
        }
        throw new OlapRuntimeException(getName() + " dimension have no super root member");
    }

    @Override
    public boolean isMeasureDimension() {
        return (binaryControlFlag & 1) > 0;
    }

    @Override
    public List<? extends Member> getMembers(String name) {
        List<? extends Member> members = getAllMembers();
        if (name != null) {
            for (int i = 0; i < members.size(); i++) {
                if (!members.get(i).getName().equals(name)) {
                    members.remove(i--);
                }
            }
        }
        return members;
    }

    @Override
    public boolean isPresetDimension() {
        return isSystemPreset();
    }

    @Override
    public Integer getMaxMemberLevel() {
        return maxMemberLevel;
    }

    public void setMaxMemberLevel(Integer maxLv) {
        maxMemberLevel = maxLv;
    }
}

