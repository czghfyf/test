package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mdx.profile.DimensionPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.ArrayList;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class SetFunMembers implements SetFunction {

    private DimensionPE dimPE;

    public SetFunMembers(DimensionPE dimPE) {
        this.dimPE = dimPE;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {
        return new Set(new ArrayList<>(dimPE.evolving(v).findMemberRoles()));
    }

}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151023
    //    package com.dao.wormhole.core.mdx.production.fun;
    //
    //            import java.util.ArrayList;
    //    //import java.util.List;
    //
    //
    //
    //
    //            import java.util.List;
    //
    //
    //    //import com.dao.wormhole.core.mdx.IR.CoordinateReferenceDatum;
    //            import com.dao.wormhole.core.mdx.parser.context.AnalyticalContext;
    //            import com.dao.wormhole.core.mdx.production.SetFunction;
    //            import com.dao.wormhole.core.mdx.production.shell.DimensionShell;
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //    //import com.dao.wormhole.core.model.entity.Dimension;
    //    //import com.dao.wormhole.core.model.entity.IDimensionRole;
    //    //import com.dao.wormhole.core.model.entity.Member;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.entity.compositeObjects.Set;
    //            import com.dao.wormhole.core.model.entity.role.IDimensionRole;
    //            import com.dao.wormhole.core.model.entity.role.IMemberRole;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-8 上午10:53:24
    //     *
    //     */
    //    public class SetFun_Members implements SetFunction {
    //
    //        private DimensionShell dimShell = null;
    //
    //        /**
    //         * set_functions ::= <MEMBERS> "(" dimension ")"
    //         *
    //         * @param dimShell
    //         */
    //        public SetFun_Members(DimensionShell dimShell) {
    //            this.dimShell = dimShell;
    //        }
    //
    //    //	@Override
    //    //	public VectorCoordinateFragment getHeadVectorCoordinateFragment() {
    //    ////		Dimension dimension = (Dimension) dimShell.hatch(null);
    //    ////		List<Member> dimMembers = dimension.getAllMembers();
    //    ////		return new VectorCoordinateFragment(dimMembers.get(0));
    //    //
    //    ////		Set s = (Set) this.hatch(null);
    //    ////		if (s != null) {
    //    ////			return new VectorCoordinateFragment(s.getTuple(0));
    //    ////		}
    //    ////		return null;
    //    //
    //    //		return CoordinateReferenceDatum.Util.getHeadVectorCoordinateFragment(this);
    //    //	}
    //
    //        @Override
    //        public /*WormholeBaseEntity*/WormholeBaseObject hatch(MultidimensionalVector vector
    //                /*VectorCoordinateFragment vcf*/) {
    //    //		Dimension dimension = (Dimension) dimShell.hatch(vcf);
    //    //		return new Set(dimension.getAllMembers(), Member.class);
    //
    //            IDimensionRole dr = (IDimensionRole) dimShell.hatch(vector);
    //            List<IMemberRole> allmrs = dr.getAllMemberRoles();
    //            return new Set(new ArrayList<WormholeBaseObject>(/*dr.getDimension().getAllMembers()*/ allmrs));
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
