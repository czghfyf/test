//core_source_code!_yxIwIywIyzIx_1lll1l

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.exp;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.MemberPE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

public class
/*^!*/ExpFnProperties/*?$*//*_yxIwIywIyzIx_1lll1l*/ implements ExpressionFunction {

    private ContextAtExecutingMDX context;

    private MemberPE memberPE;

    private Expression expression;

    public ExpFnProperties(ContextAtExecutingMDX context, MemberPE memberPE, Expression expression) {
        this.context = context;
        this.memberPE = memberPE;
        this.expression = expression;
    }

    @Override
    public BasicData evaluate(MultiDimensionalVector v) {
        return OlapEngine.hold().findProperty(memberPE.evolving(v).getMember(), expression.evaluate(v).image());
    }

}
