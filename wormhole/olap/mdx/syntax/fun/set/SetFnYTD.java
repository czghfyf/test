package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.physical.member.DateMember;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.MemberPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SetFnYTD implements SetFunction {

    private ContextAtExecutingMDX context;

    private MemberPE memberPE;

    public SetFnYTD(ContextAtExecutingMDX context, MemberPE memberPE) {
        this.context = context;
        this.memberPE = memberPE;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {

        MemberRole dateMemberRole;
        if (memberPE == null) {
            List<MemberRole> dateMrs = v.getDateMemberRoles();
            if (dateMrs.size() != 1) {
                throw new RuntimeException("MDX function: YTD(),当前上下文日期成员角色数量为" + dateMrs.size() + ",正确值应该为1");
            }
            dateMemberRole = dateMrs.get(0);
        } else {
            dateMemberRole = memberPE.evolving(v);
        }

        DateMember year = (DateMember) dateMemberRole.getMember();
        while (year.getLevel().getLevelValue() > 1) { // TODO: 是否属于年级别应由系统预置对象属性决定
            year = (DateMember) year.getParent();
        }
        List<Member> members = context.getOLAPEngine().getMddmStorageService().findMemberDescendants(year);
        for (int i = members.size() - 1; i >= 0; i--) {
            if (!dateMemberRole.getMember().getLevel().equals(members.get(i).getLevel())) {
                members.remove(i);
            }
        }
        Collections.sort(members); // TODO: 应由系统工具类进行排序

        List<MemberRole> dateMemberRolesList = new LinkedList<>();

        int dateMemberIndex = members.indexOf(dateMemberRole.getMember());

        for (int i = 0; i <= dateMemberIndex; i++) {
            dateMemberRolesList.add(new MemberRole(dateMemberRole.getDimensionRole(), members.get(i)));
        }

        return new Set(dateMemberRolesList);

    }
}
