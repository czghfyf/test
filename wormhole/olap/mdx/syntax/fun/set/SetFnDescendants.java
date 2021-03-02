package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.LevelRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.LevelPE;
import cn.bgotech.wormhole.olap.mdx.profile.MemberPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetFnDescendants implements SetFunction {

    public enum Option {
        SELF,
        AFTER,
        BEFORE,
        BEFORE_AND_AFTER,
        SELF_AND_AFTER,
        SELF_AND_BEFORE,
        SELF_BEFORE_AFTER,
        LEAVES
    }

    private ContextAtExecutingMDX context;

    private MemberPE memberPE;

    private LevelPE levelPE;

    private Option option;

    public SetFnDescendants(ContextAtExecutingMDX context, MemberPE memberPE, LevelPE levelPE, Option _option) {
        this.context = context;
        this.memberPE = memberPE;
        this.levelPE = levelPE;
        this.option = _option;
        option = option != null ? option : (levelPE == null ? Option.SELF_BEFORE_AFTER : Option.SELF);
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {

        MemberRole mr = memberPE.evolving(v);

        // TODO: if ( mr 属于度量维成员 ) { 抛出异常 }; // TODO: why？？？
//		if (/*member instanceof MeasuresMember*/ member.isMeasureMember()) {
//			throw new /*Wromhole*/RuntimeException("函数Descendants不能作用于度量维度成员");
//		}

        LevelRole lr;
        if (levelPE != null) {
            lr = levelPE.evolving(v);
        } else {
            lr = new LevelRole(mr.getDimensionRole(), mr.getMember().getLevel());
        }

        List<Member> memberDescendants = context.getOLAPEngine().getMddmStorageService().findMemberDescendants(mr.getMember()); // TODO: findMemberDescendants方法做啥的？
        memberDescendants = executeDescendantsFunction(mr.getMember(), lr.getLevel(), memberDescendants);
        return new Set(memberDescendants, mr.getDimensionRole());
    }

    private List<Member> executeDescendantsFunction(Member member, Level level, List<Member> memberDescendants) {

        List<Member> members = new ArrayList<>();
        List<Member> selfMembers = new ArrayList<>();
        List<Member> beforeMembers = new ArrayList<>();
        beforeMembers.add(member);
        List<Member> afterMembers = new ArrayList<>();

        for (Member mbr : memberDescendants) {
            if (mbr.getLevel().getLevelValue() > level.getLevelValue()) {
                afterMembers.add(mbr);
            } else if (mbr.getLevel().getLevelValue() < level.getLevelValue()) {
                beforeMembers.add(mbr);
            } else {
                selfMembers.add(mbr);
            }
        }

        switch (this.option) {
            case SELF_BEFORE_AFTER:
                members.addAll(selfMembers);
                members.addAll(beforeMembers);
                members.addAll(afterMembers);
                break;
            case BEFORE_AND_AFTER:
                members.addAll(beforeMembers);
                members.addAll(afterMembers);
                break;
            case SELF_AND_AFTER:
                members.addAll(selfMembers);
                members.addAll(afterMembers);
                break;
            case SELF_AND_BEFORE:
                members.addAll(selfMembers);
                members.addAll(beforeMembers);
                break;
            case AFTER:
                members.addAll(afterMembers);
                break;
            case BEFORE:
                members.addAll(beforeMembers);
                if (member.getLevel() != null && member.getLevel().getMgId() == level.getMgId()) {
                    members.add(member);
                }
                break;
            case SELF:
                members.addAll(selfMembers);
                if (member.getLevel() != null && member.getLevel().getMgId() == level.getMgId()) {
                    members.add(member);
                }
                break;
            case LEAVES:
                members.addAll(selfMembers);
                members.addAll(beforeMembers);
                for (int i = members.size() - 1; i >= 0; i--) {
                    if (members.get(i).isLeaf()) {
                        members.remove(i);
                    }
                }
                break;
            default:
                throw new RuntimeException("未知:" + option.toString());
        }

        Collections.sort(members); // TODO: 以后改用工具类实现

        return members;
    }
}
