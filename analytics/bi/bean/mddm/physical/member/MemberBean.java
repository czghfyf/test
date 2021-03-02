// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.member;

import cn.bgotech.analytics.bi.bean.mddm.MddmBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.LevelBean;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.component.MddmStorageService;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.*;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public class MemberBean extends MddmBean implements Member {

    private Long parentMemberId = -1L;
    private Long dimensionId;
    private Long hierarchyId = -1L;
    private Integer memberLevel = 0;
    private Long spaceId;

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    // (000:SUM|001:MAX|010:MIN|011:AVG|100:COUNT|<Others are illegal>) (1:measure|0:) (1:leaf|0:)
    private Integer binaryControlFlag = 1;

    private Member _parent;

    @Deprecated
    private String measureColumn;

    /**
     * 比较两个Member的Level层次
     * SuperRoot成员的Level为0
     *
     * @param m1
     * @param m2
     * @return
     */
    public static int compareLevel(Member m1, Member m2) {
        if ((!m1.isRoot()) && (!m2.isRoot())) {
            return m1.getLevel().getLevelValue() - m2.getLevel().getLevelValue();
        }
        if (m1.isRoot() && (!m2.isRoot())) {
            return -1;
        }
        if ((!m1.isRoot()) && m2.isRoot()) {
            return 1;
        }
        return 0;
    }

    public Long getParentMemberId() {
        return parentMemberId;
    }

    public void setParentMemberId(Long parentMemberId) {
        this.parentMemberId = parentMemberId;
    }

    public Long getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(Long dimensionId) {
        this.dimensionId = dimensionId;
    }

    public Long getHierarchyId() {
        return hierarchyId;
    }

    public void setHierarchyId(Long hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public Integer getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(Integer memberLevel) {
        this.memberLevel = memberLevel;
    }

    public Integer getBinaryControlFlag() {
        return binaryControlFlag;
    }

    public void setBinaryControlFlag(Integer binaryControlFlag) {
        this.binaryControlFlag = binaryControlFlag;
    }

    @Deprecated
    public String getMeasureColumn() {
        return measureColumn;
    }

    @Deprecated
    public void setMeasureColumn(String measureColumn) {
        this.measureColumn = measureColumn;
    }

    @Override
    public boolean isWithinRange(Space space) {
        MddmStorageService mss = OlapEngine.hold().getMddmStorageService();
        return mss.findDimension(dimensionId).isWithinRange(space);
    }

    @Override
    public Member getParent() {
        return memberLevel > 0 ? getParent_() : Member.NONEXISTENT_MEMBER;
    }

    public Member getParent_() {
        return _parent != null ? _parent : (_parent = OlapEngine.hold().findMemberByMgId(parentMemberId));
    }

    @Override
    public boolean associatedWith(Cube cube) {
        return cube.isReferenceDim(this.getDimension());
    }

    @Override
    public Member selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        Member subMember = OlapEngine.hold().selectSingleEntity(this, segmentedSelector.getPart(0));
        return segmentedSelector.length() > 1 ?
                subMember.selectSingleEntity(segmentedSelector.subSelector(1)) : subMember;
    }


    @Override
    public Dimension getDimension() {
        // return OlapEngine.getSingleInstance().find(Dimension.class, this.dimensionId);
        return OlapEngine.hold().findDimensionById(dimensionId);
    }


    @Override
    public int compareTo(Member m) {
        return (int) (getMgId() - m.getMgId());
    }

    @Override
    public boolean isRoot() {
        return memberLevel.equals(0);
    }

    @Override
    public Hierarchy getHierarchy() {
        return OlapEngine.hold().find(Hierarchy.class, hierarchyId);
    }

    @Override
    public Member findNearestDescendantMember(String descendantName) {
//        List<Member> descendants = DefaultModelServiceImpl.getInstance().findMemberDescendants(this);
        List<Member> descendants = OlapEngine.hold().findMemberDescendants(this);
//        int highestLevel = Integer.MAX_VALUE;
        int highestLevel = Level.HIGHEST_LEVEL;
//        for (int i = 0; i < descendants.size(); i++) {
//            if (! descendants.get(i).getName().equals(desdntMbrName)) {
//                descendants.remove(i--);
//            } else {
//                int diMbrLevel = descendants.get(i).getLevel().getMemberLevel();
//                if (diMbrLevel < highestLevel) {
//                    highestLevel = diMbrLevel;
//                }
//            }
//        }
        for (int i = 0; i < descendants.size(); i++) {
            if (!descendants.get(i).getName().equals(descendantName)) {
                descendants.remove(i--);
            } else {
                int currentMemberLevel = ((LevelBean) descendants.get(i).getLevel()).getMemberLevel();
                highestLevel = currentMemberLevel < highestLevel ? currentMemberLevel : highestLevel;
            }
        }

        for (int i = 0; i < descendants.size(); i++) {
            if (((LevelBean) descendants.get(i).getLevel()).getMemberLevel() > highestLevel) {
                descendants.remove(i--);
            }
        }

        if (descendants.size() > 1) {
            throw new OlapRuntimeException("当前维度成员具有多个距离最近且名为desdntMbrName的后代成员，无法确定选择哪个");
        }
        return descendants.isEmpty() ? null : descendants.get(0);

    }

    @Override
    public boolean hasAncestor(Member m) {
        MemberBean o = (MemberBean) m;
        return o.isRoot() ? m.getDimension().equals(getDimension()) && !isRoot()
                : isRoot() ? false : findAncestors().indexOf(m) >= 0;
    }

    @Override
    public boolean isMeasure() {
        return (binaryControlFlag & 2) == 2;
    }

    @Override
    public boolean isLeaf() {
        return (binaryControlFlag & 1) > 0;
    }

    @Override
    public Member getBrotherMember(int offset) {
        if (isRoot()) {
            return Member.NONEXISTENT_MEMBER;
        }
        List<Member> brothers = OlapEngine.hold().getMddmStorageService().findChildren(getParent());
        Collections.sort(brothers);
        int theIndex = brothers.indexOf(this);
        int brotherIndex = theIndex - offset;
        return brotherIndex < 0 || brotherIndex >= brothers.size()
                ? Member.NONEXISTENT_MEMBER : brothers.get(brotherIndex);
    }

    @Override
    public Member findDescendantMember(long descendantId) {
        Member member = OlapEngine.hold().find(Member.class, descendantId);
        if (member == null || member.isRoot()) {
            return null;
        }
        Member parent = member.getParent();
        while (true) {
            if (parent.equals(this)) {
                return member;
            }
            if (!parent.isRoot()) {
                parent = parent.getParent();
            } else {
                return null;
            }
        }
    }

    @Override
    public Level getLevel() {
//        ModelService modelService = wApp.getModelService();
//        Level level = modelService.getLevel(this.hierarchyID, this.memberLevel);
//        return level;
        return OlapEngine.hold().getLevel(hierarchyId, memberLevel);
    }

    @Override
    public List<Integer> relativePosition(Member ancestorMember) {

        List<Integer> position = new ArrayList();
        if (this.getLevel().getLevelValue() <= ancestorMember.getLevel().getLevelValue()) {
            return position;
        }

        Member currentMember = this;
        Member parent;
        do {
            parent = currentMember.getParent();
            List<Member> children = OlapEngine.hold().getMddmStorageService().findChildren(parent);
            Collections.sort(children);
            position.add(0, children.indexOf(currentMember));
            currentMember = parent;
            parent = parent.getParent();

        } while (compareLevel(parent, ancestorMember) >= 0);

        return position;

    }

    @Override
    public Member moveRelativePosition(List<Integer> relativePosition) {

        List<Member> children;
        Member currentMember = this;

        for (Integer index : relativePosition) {
            children = OlapEngine.hold().getMddmStorageService().findChildren(currentMember);
            Collections.sort(children);
            currentMember = children.get(index);
        }

        return currentMember;
    }

    @Override
    public List<String> getFullNamesPath() {
        if (isRoot()) {
            return null; // TODO:
        }
        List<String> result = memberLevel == 1 ? new LinkedList() : getParent().getFullNamesPath();
        result.add(getName());
        return result;
    }

    public void setAllAttributesNull() {
        parentMemberId = null;
        dimensionId = null;
        hierarchyId = null;
        memberLevel = null;
        binaryControlFlag = null;
        super.setAllAttributesNull();
    }
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    package com.dao.wormhole.FS.modelServices.model.entity;
    //
    //            import com.dao.wormhole.FS.modelServices.DefaultModelServiceImpl;
    //            import com.dao.wormhole.core.app.WormholeApplication;
    //            import com.dao.wormhole.core.mdx.IR.EntityPathBlock;
    //            import com.dao.wormhole.core.model.entity.Cube;
    //            import com.dao.wormhole.core.model.entity.Dimension;
    //            import com.dao.wormhole.core.model.entity.Hierarchy;
    //            import com.dao.wormhole.core.model.entity.Level;
    //            import com.dao.wormhole.core.model.entity.Member;
    //            import com.dao.wormhole.core.model.entity.property.Property;
    //            import com.dao.wormhole.core.services.ModelService;
    //            import com.dao.wormhole.log.WormholeLog;
    //
    //    public abstract class OlapMember implements Member {
    //
    //        public WormholeApplication getwApp() {
    //            return wApp;
    //        }
    //
    //        public void setwApp(WormholeApplication wApp) {
    //            this.wApp = wApp;
    //        }
    //
    //        public int getHierarchyID() {
    //            return hierarchyID;
    //        }
    //
    //        public void setHierarchyID(int hierarchyID) {
    //            this.hierarchyID = hierarchyID;
    //        }
    //
    //        public int getMemberLevel() {
    //            return memberLevel;
    //        }
    //
    //        public void setMemberLevel(int memberLevel) {
    //            this.memberLevel = memberLevel;
    //        }
    //
    //        public int getParentID() {
    //            return parentID;
    //        }
    //
    //        public void setParentID(int parentID) {
    //            this.parentID = parentID;
    //        }
    //
    //        public int getDimensionID() {
    //            return dimensionID;
    //        }
    //
    //        public void setDimensionID(int dimensionID) {
    //            this.dimensionID = dimensionID;
    //        }
    //
    //        public void setWormholeID(int wormholeID) {
    //            this.wormholeID = wormholeID;
    //        }
    //
    //        public void setName(String name) {
    //            this.name = name;
    //        }
    //
    //        public void setLeaf(boolean leaf) {
    //            this.leaf = leaf;
    //        }
    //        private WormholeApplication wApp = null;
    //
    //        //	private String measureColumn = null;
    //        private int wormholeID = -1;
    //    //	private boolean measureMember = false;
    //
    //        private int hierarchyID = -1;
    //        private int memberLevel = -1;
    //        private int parentID = -1;
    //
    //        private int dimensionID = -1;
    //
    //        private String name = null;
    //
    //        private boolean leaf = false;
    //
    //
    //
    //        public OlapMember(int wormholeID, String name, int parentID, int memberLevel,
    //                          int hierarchyID, int dimensionID, int leaf/*, String measureColumn*/) {
    //            this();
    //    //		this.measureColumn = measureColumn;
    //            this.wormholeID = wormholeID;
    //            this.hierarchyID = hierarchyID;
    //            this.memberLevel = memberLevel;
    //            this.parentID = parentID;
    //            this.dimensionID = dimensionID;
    //            this.name = name;
    //            this.leaf = leaf == 1 ? true : false;
    //        }
    //
    //        public OlapMember() {
    //            wApp = WormholeApplication.getWormholeAppInstance();
    //        }
                                                                                                                //
                                                                                                                //        @Override
                                                                                                                //        public int compareTo(Member m) {
                                                                                                                //            if (this.wormholeID < m.getWormholeID()) {
                                                                                                                //                return -1;
                                                                                                                //            } else if (this.wormholeID > m.getWormholeID()) {
                                                                                                                //                return 1;
                                                                                                                //            } else {
                                                                                                                //                return 0;
                                                                                                                //            }
                                                                                                                //        }
                                                                                                                //
    //        @Override
    //        public String fullPathName() {
    //            if (this.isSuperRootMember()) {
    //                return this.getDimension().fullPathName() + ".&" + this.getWormholeID() + "[" + this.getName() + "]";
    //            } else if (this.memberLevel == 1) {
    //                return this.getHierarchy().fullPathName() + ".&" + this.getWormholeID() + "[" + this.getName() + "]";
    //            } else {
    //                return this.getParentMember().fullPathName() + ".&" + this.getWormholeID() + "[" + this.getName() + "]";
    //            }
    //        }
    //
    //        @Override
    //        public int getWormholeID() {
    //            return wormholeID;
    //        }
    //
    //        @Override
    //        public String getName() {
    //            return name;
    //        }
                                                                                        //
                                                                                        //        @Override
                                                                                        //        public boolean byCubeReference(Cube cube) {
                                                                                        //            return cube.isReferenceDim(this.getDimension());
                                                                                        //        }
                                                                                        //
                                                                                        //        /**
                                                                                        //         * @Deprecated 废弃日期：2015/10/27
                                                                                        //         * @see #selectSingleEntity(List)
                                                                                        //         */
                                                                                        //        @Override
                                                                                        //        public /*WormholeBaseEntity*/Member selectWormholeEntityByPath(List<String> path) {
                                                                                        //            // 返回结果类型为Member
                                                                                        //            Member member =
                                                                                        //                    wApp.getModelService().getChildMember(this, path.get(0));
                                                                                        //
                                                                                        //            if (member == null) {
                                                                                        //    //					throw new WromholeRuntimeException("[ID=" + this.wormholeID + ", name=" + this.memberName + "]不存在直接的子级成员[" + path.get(0) + "]");
                                                                                        //                WormholeLog.WARN("[ID=" + this.wormholeID + ", name=" + this.name + "]不存在直接的子级成员[" + path.get(0) + "]");
                                                                                        //                return null;
                                                                                        //            }
                                                                                        //
                                                                                        //            if (path.size() > 1) {
                                                                                        //                List<String> newPath = new ArrayList<String>(path);
                                                                                        //                newPath.remove(0);
                                                                                        //                member = (Member) member.selectWormholeEntityByPath(newPath);
                                                                                        //    //					member = member.selectHyperspaceModelObjectByPath(newPath);
                                                                                        //            }
                                                                                        //            return member;
                                                                                        //        }
                                                                                        //
                                                                                        //        @Override
                                                                                        //        public /*WormholeBaseEntity*/Member selectSingleEntity(List<EntityPathBlock> path) {
                                                                                        //            ModelService modelService = WormholeApplication.getWormholeAppInstance().getModelService();
                                                                                        //            Member returnThisOne = modelService.selectSingleEntity(this, path.get(0));
                                                                                        //            if (path.size() > 1) {
                                                                                        //                List<EntityPathBlock> newList = new ArrayList<EntityPathBlock>(path);
                                                                                        //                newList.remove(0);
                                                                                        //                return (Member) returnThisOne.selectSingleEntity(newList);
                                                                                        //            } else {
                                                                                        //                return returnThisOne;
                                                                                        //            }
                                                                                        //        }
                                                                                        //

    //        @Override
    //        public List<Property> getProperties() {
    //            return this.wApp.getModelService().getProperties(this);
    //        }
    //
    //        @Override
    //        public Property getProperty(String key) {
    //            return this.wApp.getModelService().getProperty(this, key);
    //        }
    //
    //    //	@Override
    //    //	public boolean isMeasureMember() {
    //    //		return this instanceof MeasuresMember;
    //    //	}
    //
    //        @Override
    //        public boolean isSuperRootMember() {
    //            return this.memberLevel > 0 ? false : true;
    //        }
    //
    //        @Override
    //        public boolean isCustomFormulaMember() {
    //            return false;
    //        }
                                                                                                    //
                                                                                                    //        @Override
                                                                                                    //        public Hierarchy getHierarchy() {
                                                                                                    //            return wApp.getModelService().findHierarchyByID(this.hierarchyID);
                                                                                                    //        }
                                                                                                    //
                                                                                                    //        @Override
                                                                                                    //        public Level getLevel() {
                                                                                                    //            ModelService modelService = wApp.getModelService();
                                                                                                    //            Level level = modelService.getLevel(this.hierarchyID, this.memberLevel);
                                                                                                    //            return level;
                                                                                                    //        }
                                                                                                    //
    //        @Override
    //        public boolean isLeaf() {
    //            return leaf;
    //        }
                                                                                                //
                                                                                                //        @Override
                                                                                                //        public Dimension getDimension() {
                                                                                                //
                                                                                                //            return wApp.getModelService().getDimensionByID(this.dimensionID);
                                                                                                //        }
                                                                                                //
    //        @Override
    //        public Member getParentMember() {
    //            if (memberLevel > 0) {
    //                return wApp.getModelService().findMemberByID(parentID);
    //            }
    //            return null;
    //        }
    //
    //        @Override
    //        public List<Member> getChildren() {
    //            return this.wApp.getModelService().findMemberChildren(this);
    //        }
    //
                                                                                                                    //        @Override
                                                                                                                    //        public Member findDescendantMember(int desdntMbrId) {
                                                                                                                    //            Member m = DefaultModelServiceImpl.getInstance().findMemberByID(desdntMbrId);
                                                                                                                    //            if (m == null || m.isSuperRootMember()) { return null; }
                                                                                                                    //            for (Member pm = m.getParentMember(); ; ) {
                                                                                                                    //                if (pm.equals(this)) {
                                                                                                                    //                    return m;
                                                                                                                    //                }
                                                                                                                    //                if (!pm.isSuperRootMember()) {
                                                                                                                    //                    pm = pm.getParentMember();
                                                                                                                    //                } else {
                                                                                                                    //                    return null;
                                                                                                                    //                }
                                                                                                                    //            }
                                                                                                                    //        }
                                                                                                                    //        @Override
                                                                                                                    //        public Member findNearestDescendantMember(String desdntMbrName) {
                                                                                                                    //            List<Member> descendants = DefaultModelServiceImpl.getInstance().findMemberDescendants(this);
                                                                                                                    //            int highestLevel = Integer.MAX_VALUE;
                                                                                                                    //            for (int i = 0; i < descendants.size(); i++) {
                                                                                                                    //                if (! descendants.get(i).getName().equals(desdntMbrName)) {
                                                                                                                    //                    descendants.remove(i--);
                                                                                                                    //                } else {
                                                                                                                    //                    int diMbrLevel = descendants.get(i).getLevel().getMemberLevel();
                                                                                                                    //                    if (diMbrLevel < highestLevel) {
                                                                                                                    //                        highestLevel = diMbrLevel;
                                                                                                                    //                    }
                                                                                                                    //                }
                                                                                                                    //            }
                                                                                                                    //
                                                                                                                    //            for (int i = descendants.size() - 1; i >= 0; i--) {
                                                                                                                    //                if (descendants.get(i).getLevel().getMemberLevel() > highestLevel) {
                                                                                                                    //                    descendants.remove(i);
                                                                                                                    //                }
                                                                                                                    //            }
                                                                                                                    //
                                                                                                                    //            if (descendants.size() == 1) {
                                                                                                                    //                return descendants.get(0);
                                                                                                                    //            } else if (descendants.isEmpty()) {
                                                                                                                    //                return null;
                                                                                                                    //            } else {
                                                                                                                    //                throw new RuntimeException("当前维度成员具有多个距离最近且名为desdntMbrName的后代成员，无法确定选择哪个");
                                                                                                                    //            }
                                                                                                                    //        }
    //
    //        @Override
    //        public int hashCode() {
    //            return this.wormholeID * 101001;
    //        }
    //
    //        @Override
    //        public boolean equals(Object obj) {
    //            if (obj == null || !(obj instanceof OlapMember)) {
    //                return false;
    //            }
    //            return this.wormholeID == ((OlapMember)obj).wormholeID;
    //        }
    //
    //
    //
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
