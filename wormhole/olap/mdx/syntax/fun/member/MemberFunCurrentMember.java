//core_source_code!_yxIwIywIyzIx_111l1

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.member;

import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.profile.DimensionPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class
/*^!*/MemberFunCurrentMember/*?$*//*_yxIwIywIyzIx_111l1*/ implements MemberFunction {

    private DimensionPE dimPe;

    public MemberFunCurrentMember(DimensionPE dimPe) {
        this.dimPe = dimPe;
    }

    @Override
    public MemberRole evolving(MultiDimensionalVector v) {
        return v.findMemberRole(dimPe.evolving(v));
    }
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151024
    //    package com.dao.wormhole.core.mdx.production.fun;
    //
    //            import com.dao.wormhole.core.mdx.parser.context.AnalyticalContext;
    //            import com.dao.wormhole.core.mdx.production.MemberFunction;
    //            import com.dao.wormhole.core.mdx.production.shell.DimensionShell;
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //            import com.dao.wormhole.core.model.entity.role.IDimensionRole;
    //    //import com.dao.wormhole.core.model.entity.Dimension;
    //    //import com.dao.wormhole.core.model.entity.IDimensionRole;
    //    //import com.dao.wormhole.core.model.entity.IMemberRole;
    //    //import com.dao.wormhole.core.model.entity.Member;
    //    //import com.dao.wormhole.core.model.entity.MemberRole;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-7 下午4:55:20
    //     *
    //     */
    //    public class MemberFun_CurrentMember implements MemberFunction {
    //
    //        private DimensionShell dimShell = null;
    //
    //        /**
    //         * member_functions ::= "CurrentMember" "(" dimension ")"
    //         *
    //         * @param dimShell
    //         */
    //        public MemberFun_CurrentMember(DimensionShell dimShell) {
    //            this.dimShell = dimShell;
    //        }
    //
    //    //	@Override
    //    //	public VectorCoordinateFragment getHeadVectorCoordinateFragment() {
    //    ////		Dimension d = (Dimension) dimShell.hatch(null);
    //    ////		if (d == null) {
    //    ////			return null;
    //    ////		} else {
    //    ////			return new VectorCoordinateFragment(d.getDefaultMember());
    //    ////		}
    //    //		IDimensionRole dr = (IDimensionRole) dimShell.hatch(null);
    //    //		if (dr == null) { return null; }
    //    //		return new VectorCoordinateFragment(new MemberRole(dr, dr.getDimension().getDefaultMember()));
    //    //	}
    //
    //    //	@Override
    //    //	public WormholeBaseEntity hatch(VectorCoordinateFragment vcf) {
    //    ////		Dimension d = (Dimension) dimShell.hatch(vcf);
    //    ////		Member m = vcf.getDimensionCurrentMember(d);
    //    ////		return m;
    //    //		return vcf.getCurrentMemberRole((IDimensionRole) dimShell.hatch(vcf));
    //    //	}
    //
    //        @Override
    //        public WormholeBaseObject hatch(MultidimensionalVector vector) {
    //            return vector.getCurrentMemberRole((IDimensionRole) dimShell.hatch(vector));
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
