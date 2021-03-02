//core_source_code!_yxIwIywIyzIx_11l11

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.member;

import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.profile.MemberPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

/**
 * Created by czg on 2018/2/4.
 */
public class
/*^!*/MemberFunPrevMember/*?$*//*_yxIwIywIyzIx_11l11*/ implements MemberFunction {

    private MemberPE mbrPe;

    public MemberFunPrevMember(MemberPE mbrPe) {
        this.mbrPe = mbrPe;
    }

    @Override
    public MemberRole evolving(MultiDimensionalVector v) {
        MemberRole mr = mbrPe.evolving(v);
        Member brotherMember = mr.getMember().getBrotherMember(1);
        return new MemberRole(mr.getDimensionRole(), brotherMember);
    }

}
