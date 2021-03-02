// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical;

import cn.bgotech.analytics.bi.bean.mddm.MddmBean;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class LevelBean extends MddmBean implements Level {

    private Long hierarchyId;
    private Integer memberLevel;

    public Long getHierarchyId() {
        return hierarchyId;
    }

    public void setHierarchyId(Long hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public Integer getMemberLevel() {
        return memberLevel;
    }

    @Override
    public int getLevelValue() {
        return memberLevel;
    }

    public void setMemberLevel(Integer memberLevel) {
        this.memberLevel = memberLevel;
    }

    @Override
    public boolean isWithinRange(Space space) {
        return false; // TODO: do it later. 'return getHierarchy().isWithinRange(space)'
    }

    @Override
    public Hierarchy getHierarchy() {
        return OlapEngine.hold().find(Hierarchy.class, hierarchyId);
    }

    @Override
    public Member findUniqueNamedMember(String memberName) {

        List<Member> members = getMembers();
        for (int i = 0; i < members.size(); i++) {
            if (!members.get(i).getName().equals(memberName)) {
                members.remove(i--);
            }
        }
//
//        List<Member> list = new LinkedList<Member>();
//        DefaultModelServiceImpl dms = DefaultModelServiceImpl.getInstance();
//        List<Member> mbrs = dms.getAllMembers(this.getHierarchy());
//        for (Member m : mbrs) {
//            if (m.getName().equals(memberName)) {
//                list.add(m);
//            }
//        }
        if (members.size() > 1) {
            throw new OlapRuntimeException("there are multiple members of the same name");
        }
        return members.isEmpty() ? null : members.get(0);
//        if (list.size() > 1) {
//            throw new RuntimeException("level下具有多个名为memberName的维度成员，无法判定应该返回哪个");
//        } else if (!list.isEmpty()) {
//            return list.get(0);
//        } else {
//            return null;
//        }
    }

    @Override
    public List<Member> getMembers() {
        return OlapEngine.hold().findMembers(this);
    }

    /**
     * TODO: see TODO 201706111534
     *
     * @param segmentedSelector Multidimensional selector fragment
     * @return
     */
    @Override
    public Member selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {

        Member member = OlapEngine.hold().selectSingleEntity(this, segmentedSelector.getPart(0));
        return segmentedSelector.length() > 1 ?
                member.selectSingleEntity(segmentedSelector.subSelector(1)) : member;

    }

    @Override
    public Level getAboveLevel() {
        if (this.memberLevel == 1) {
            return null;
        }
        return OlapEngine.hold().getMddmStorageService().getLevel(hierarchyId, memberLevel - 1);
    }

}

