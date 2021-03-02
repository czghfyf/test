package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.MemberPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2018/2/23.
 */
public class SetFnChildren implements SetFunction {

    private ContextAtExecutingMDX context;

    private MemberPE memberPE;

    public SetFnChildren(ContextAtExecutingMDX context, MemberPE memberPE) {
        this.context = context;
        this.memberPE = memberPE;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {

        MemberRole mr = memberPE.evolving(v);
        if (mr == null) {
            return null; // TODO: It may cause a program error
        }
        List<Member> children = context.getOLAPEngine().getMddmStorageService().findChildren(mr.getMember());
        if (children.isEmpty()) {
            return null; // TODO: It may cause a program error
        }
        Collections.sort(children); // TODO: 使用工具类排序

        List<BasicEntityModel> mrs = new LinkedList();
        for (Member m : children) {
            mrs.add(new MemberRole(mr.getDimensionRole(), m));
        }

        return new Set(mrs);
    }

}
