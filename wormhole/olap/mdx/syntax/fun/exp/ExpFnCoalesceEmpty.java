//core_source_code!_yxIwIywIyzIx_1ll11l

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.exp;

import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNull;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.List;

public class
/*^!*/ExpFnCoalesceEmpty/*?$*//*_yxIwIywIyzIx_1ll11l*/ implements ExpressionFunction {

    private List<Expression> expressions;

    public ExpFnCoalesceEmpty(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public BasicData evaluate(MultiDimensionalVector v) {

        BasicData basicData;
        for (Expression exp : expressions) {
            basicData = exp.evaluate(v);
            if (basicData != null && !(basicData instanceof BasicNull)) {
                return basicData;
            }
        }
        return BasicNull.INSTANCE;

    }
}
