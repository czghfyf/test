// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical;

import cn.bgotech.analytics.bi.bean.mddm.MddmBean;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class HierarchyBean extends MddmBean implements Hierarchy {

    private Long dimensionId;
    private Long defaultMemberId;

    public Long getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(Long dimensionId) {
        this.dimensionId = dimensionId;
    }

    public Long getDefaultMemberId() {
        return defaultMemberId;
    }

    public void setDefaultMemberId(Long defaultMemberId) {
        this.defaultMemberId = defaultMemberId;
    }

    @Override
    public boolean isWithinRange(Space space) {
        return false; // TODO: do it later. 'return getDimension().isWithinRange(space)'
    }

    @Override
    public Dimension getDimension() {
        return OlapEngine.hold().find(Dimension.class, dimensionId);
    }

    @Override
    public Member getDefaultMember() {
        return OlapEngine.hold().find(Member.class, defaultMemberId);
    }

    @Override
    public List<Level> getLevels() {
        Set<Level> levels = new HashSet<>();
        for (Level l : OlapEngine.hold().findAll(Level.class)) {

            if (((LevelBean) l).getHierarchyId().equals(getMgId())) {
                levels.add(l);
            }

//            if (l.getHierarchy().equals(this)) {
//                levels.add(l);
//            }
        }
        return new LinkedList<>(levels);
    }

    @Override
    public boolean associatedWith(Cube cube) {
        return getDimension().associatedWith(cube);
    }

    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {

        BasicEntityModel returnThisOne
                = OlapEngine.hold().selectSingleEntity(this, segmentedSelector.getPart(0));

        return segmentedSelector.length() > 1 ?
                returnThisOne.selectSingleEntity(segmentedSelector.subSelector(1)) : returnThisOne;

    }
}

