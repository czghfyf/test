package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.profile.MemberPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetFunLateralMembers implements SetFunction {

    private MemberPE memberPE;

    public SetFunLateralMembers(MemberPE memberPE) {
        this.memberPE = memberPE;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {
        MemberRole memberRole = memberPE.evolving(v);
        if (memberRole.getMember().isRoot()) {
            return new Set(Arrays.asList(memberRole));
        }
        List<MemberRole> lateralMemberRoles = memberRole.getDimensionRole().findMemberRoles().stream()
                .filter(mr ->
                        (!mr.getMember().isRoot()) && mr.getMember().getLevel().equals(memberRole.getMember().getLevel()))
                .collect(Collectors.toList());
        return new Set(lateralMemberRoles);
    }

}
