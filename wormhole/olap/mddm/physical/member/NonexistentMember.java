package cn.bgotech.wormhole.olap.mddm.physical.member;

import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.List;

/**
 * Created by czg on 2019/5/30.
 */
public final class NonexistentMember implements Member {

    NonexistentMember() {

    }

    @Override
    public Member getParent() {
        return null;
    }

    @Override
    public Dimension getDimension() {
        return null;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public Hierarchy getHierarchy() {
        return null;
    }

    @Override
    public Level getLevel() {
        return null;
    }

    @Override
    public Member findDescendantMember(long descendantId) {
        return null;
    }

    @Override
    public Member findNearestDescendantMember(String descendantName) {
        return null;
    }

    @Override
    public boolean hasAncestor(Member m) {
        return false;
    }

    @Override
    public boolean isMeasure() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Member getBrotherMember(int offset) {
        return null;
    }

    @Override
    public Member selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        return null;
    }

    @Override
    public Long getMgId() {
        return 0L;
    }

    @Override
    public String getName() {
        return "?";
    }

    @Override
    public boolean isWithinRange(Space space) {
        return true;
    }

    @Override
    public boolean associatedWith(Cube cube) {
        return true;
    }

    @Override
    public int compareTo(Member o) {
        return 0;
    }

    @Override
    public boolean isExistentMember() {
        return false;
    }

    @Override
    public List<Integer> relativePosition(Member ancestorMember) {
        return null; // TODO: May cause: NullPointerException
    }

    @Override
    public Member moveRelativePosition(List<Integer> relativePosition) {
        return null; // TODO: May cause: NullPointerException
    }

}
