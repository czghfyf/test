//core_source_code!_yxIwIywIyzIx_1ll1ll

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.exp;

import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import static cn.bgotech.wormhole.olap.mdx.syntax.fun.exp.ExpFunCount.Option.EXCLUDE_EMPTY;

//import static cn.bgotech.wormhole.olap.mdx.syntax.fun.exp.ExpFunCount.Option.EXCLUDE_EMPTY;

public class
/*^!*/ExpFunCount/*?$*//*_yxIwIywIyzIx_1ll1ll*/ implements ExpressionFunction {

    private Option option = EXCLUDE_EMPTY;
    private SetPE setPe;

    @Override
    public BasicNumeric evaluate(MultiDimensionalVector v) {
        return new BasicNumeric(null, setPe.evolving(v).getTupleList().size());
    }

    public enum Option {
        EXCLUDE_EMPTY,
        INCLUDE_EMPTY
    }

    public ExpFunCount(SetPE setPe, Option option) {
        this.setPe = setPe;
        if (option != null) {
            this.option = option;
        }
    }
}


// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151022
    //    package com.dao.wormhole.core.mdx.production.fun;
    //
    //            import com.dao.wormhole.core.basic.datatype.WormholeBasicData;
    //            import com.dao.wormhole.core.basic.datatype.WormholeNumeric;
    //            import com.dao.wormhole.core.mdx.production.NumericFunction;
    //    //import com.dao.wormhole.core.mdx.production.fun.NumFun_Count.Option;
    //            import com.dao.wormhole.core.mdx.production.shell.SetShell;
    //            import com.dao.wormhole.core.model.entity.compositeObjects.Set;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-8 下午2:44:43
    //     *
    //     */
    //    public class NumFun_Count implements NumericFunction {
    //
    //        private SetShell setShell = null;
    //        private Option countOpt = null; // TODO 未起作用 ？？？！！！
    //
    //        /**
    //         * numeric_functions ::=
    //         *     <COUNT> "(" set ( "," <EXCLUDE_EMPTY> | <INCLUDE_EMPTY> )? ")"
    //         *
    //         * @param setShell
    //         * @param countOpt
    //         */
    //        public NumFun_Count(SetShell setShell, Option countOpt) {
    //            this.setShell = setShell;
    //            this.countOpt = countOpt;
    //        }
    //
    //        public static enum Option {
    //            EXCLUDE_EMPTY,
    //            INCLUDE_EMPTY;
    //        }
                                                                                                                //
                                                                                                                //    //	@Override
                                                                                                                //    //	public WormholeBasicData doubleValue(VectorCoordinateFragment vcf) {
                                                                                                                //    //		return calculateValue(vcf);
                                                                                                                //    //	}
                                                                                                                //
                                                                                                                //    //	@Override
                                                                                                                //    //	public WormholeBasicData calculateValue(VectorCoordinateFragment vcf) {
                                                                                                                //    //		Set set = (Set) setShell.hatch(vcf);
                                                                                                                //    //		return new WormholeNumeric((double)set.getTupleList().size());
                                                                                                                //    //	}
                                                                                                                //
                                                                                                                //        @Override
                                                                                                                //        public WormholeBasicData evaluate(MultidimensionalVector vector) {
                                                                                                                //            Set set = (Set) setShell.hatch(vector);
                                                                                                                //            return new WormholeNumeric((double)set.getTupleList().size());
                                                                                                                //        }
                                                                                                                //
                                                                                                                //    //	@Override
                                                                                                                //    //	public double doubleValue(VectorCoordinateFragment vcf) {
                                                                                                                //    //		Set set = (Set) setShell.hatch(vcf);
                                                                                                                //    //		return set.getTupleList().size();
                                                                                                                //    //	}
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
