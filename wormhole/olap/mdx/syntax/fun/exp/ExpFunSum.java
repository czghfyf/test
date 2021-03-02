//core_source_code!_yxIwIywIyzIx_1llll1

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.exp;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.List;

public class
/*^!*/ExpFunSum/*?$*//*_yxIwIywIyzIx_1llll1*/ implements ExpressionFunction {

    private ContextAtExecutingMDX ctx;
    private SetPE setPe;
    private Expression exp;

    public ExpFunSum(ContextAtExecutingMDX ctx, SetPE setPe, Expression exp) {
        this.ctx = ctx;
        this.setPe = setPe;
        this.exp = exp;
    }

    @Override
    public BasicData evaluate(MultiDimensionalVector v) {
        BasicData value = new BasicNumeric(null, 0.0D);
        List<Tuple> ts = setPe.evolving(v).getTupleList();
        MultiDimensionalVector tv;
        for (Tuple t : ts) {
            tv = new MultiDimensionalVector(v, t, null);
            if (exp != null) {
                value = BasicData.calculate(value, "+", exp.evaluate(tv));
            } else {
                value = BasicData.calculate(value, "+", OlapEngine.hold().getVectorComputingEngine().vectorValue(ctx.getCube(), tv));
            }
        }
        return value;
    }

}
