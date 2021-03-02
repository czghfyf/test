//core_source_code!_yxIwIywIyzIx_1lll11

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.exp;

import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mdx.syntax.b00lean.exp.BooleanExpression;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

public class
/*^!*/ExpFnIIF/*?$*//*_yxIwIywIyzIx_1lll11*/ implements ExpressionFunction {

    private BooleanExpression boolExp;
    private Expression trueExp;
    private Expression falseExp;

    /**
     * @param boolExp
     * @param trueExp
     * @param falseExp
     */
    public ExpFnIIF(BooleanExpression boolExp, Expression trueExp, Expression falseExp) {
        this.boolExp = boolExp;
        this.trueExp = trueExp;
        this.falseExp = falseExp;
    }

    @Override
    public BasicData evaluate(MultiDimensionalVector v) {

        if (boolExp.determine(v).isTrue()) {
            return trueExp.evaluate(v);
        } else {
            return falseExp.evaluate(v);
        }

    }
}
