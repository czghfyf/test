//core_source_code!_yxIwIywIyzIx_111ll

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.member;

import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.profile.MemberPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class
/*^!*/MemberFunParent/*?$*//*_yxIwIywIyzIx_111ll*/ implements MemberFunction {

    private MemberPE mbrPe;

    public MemberFunParent(MemberPE mbrPe) {
        this.mbrPe = mbrPe;
    }

    @Override
    public MemberRole evolving(MultiDimensionalVector v) {
        return mbrPe.evolving(v).getParent();
    }
}


// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151024
    //    package com.dao.wormhole.core.mdx.production.fun;
    //
    //            import com.dao.wormhole.core.mdx.parser.context.AnalyticalContext;
    //            import com.dao.wormhole.core.mdx.production.MemberFunction;
    //            import com.dao.wormhole.core.mdx.production.shell.MemberShell;
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //            import com.dao.wormhole.core.model.entity.role.IMemberRole;
    //            import com.dao.wormhole.core.model.entity.role.MemberRole;
    //    //import com.dao.wormhole.core.model.entity.IMemberRole;
    //    //import com.dao.wormhole.core.model.entity.Member;
    //    //import com.dao.wormhole.core.model.entity.MemberRole;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-7 下午5:01:26
    //     *
    //     */
    //    public class MemberFun_Parent implements MemberFunction {
    //
    //        private MemberShell mbrShell = null;
    //
    //        /**
    //         * member_functions ::= "parent" "(" member ")"
    //         *
    //         * @param mbrShell
    //         */
    //        public MemberFun_Parent(MemberShell mbrShell) {
    //            this.mbrShell = mbrShell;
    //        }
    //
    //    //	@Override
    //    //	public VectorCoordinateFragment getHeadVectorCoordinateFragment() {
    //    ////		Member m = (Member) mbrShell.hatch(null);
    //    ////		if (m == null) {
    //    ////			return null;
    //    ////		} else {
    //    ////			return new VectorCoordinateFragment(m.getParentMember());
    //    ////		}
    //    //
    //    ////		if (mr != null) {
    //    //////			Member m = mr.getMember();
    //    //////			Member parent = m.getParentMember();
    //    //////			if (parent != null) {
    //    //////			}
    //    ////			return new VectorCoordinateFragment(mr);
    //    ////		}
    //    ////		return null;
    //    //
    //    //		IMemberRole mr = (IMemberRole) mbrShell.hatch(null);
    //    //		return mr == null ? null : new VectorCoordinateFragment(mr);
    //    //	}
    //
    //    //	@Override
    //    //	public WormholeBaseEntity hatch(VectorCoordinateFragment vcf) {
    //    ////		Member m = (Member) mbrShell.hatch(vcf);
    //    ////		return m.getParentMember();
    //    //
    //    //		IMemberRole mr = (IMemberRole) mbrShell.hatch(vcf);
    //    //		if (mr != null) {
    //    //			Member parent = mr.getMember().getParentMember();
    //    //			if (parent != null) {
    //    //				return new MemberRole(mr.getDimensionRole(), parent);
    //    //			}
    //    //		}
    //    //		return null;
    //    //	}
    //
    //        @Override
    //        public WormholeBaseObject hatch(MultidimensionalVector vector) {
    //            IMemberRole mr = (IMemberRole) mbrShell.hatch(vector);
    //            return new MemberRole(mr.getDimensionRole(), mr.getMember().getParentMember());
    //        }
    //
    //        @Override
    //        public void setContext(AnalyticalContext context) {
    //            // TODO Auto-generated method stub
    //
    //        }
    //
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
