package cn.bgotech.wormhole.olap.mdx.syntax.b00lean.functions;

import cn.bgotech.wormhole.olap.mddm.data.BasicBoolean;
import cn.bgotech.wormhole.olap.mddm.data.BasicNull;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

/**
 * Created by chenzhigang on 2018/2/28.
 */
public class BooleanFnIsEmpty implements BooleanFunction {

    private Expression expression;

    public BooleanFnIsEmpty(Expression expression) {
        this.expression = expression;
    }

    @Override
    public BasicBoolean determine(MultiDimensionalVector vector) {
        return expression.evaluate(vector) == BasicNull.INSTANCE ? BasicBoolean.TRUE : BasicBoolean.FALSE;
    }
}
