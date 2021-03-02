package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.component.VectorComputingEngine;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.syntax.fun.exp.ExpFunSum;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.LinkedList;
import java.util.List;

public class SetFnBottomOrTopPercent implements SetFunction {

    private VectorComputingEngine vce;

    private ContextAtExecutingMDX context;

    private String functionName;

    private SetPE setPE;

    private Expression percentageExp;

    private Expression expression;

    public SetFnBottomOrTopPercent(ContextAtExecutingMDX ctx, String fnName, SetPE setPE, Expression percentage, Expression expression) {
        context = ctx;
        functionName = "TopPercent".equalsIgnoreCase(fnName) ? "TopPercent" : "BottomPercent";
        this.setPE = setPE;
        percentageExp = percentage;
        this.expression = expression;

        vce = context.getOLAPEngine().getVectorComputingEngine();
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {

        double totalValue = ((BasicNumeric) new ExpFunSum(context, setPE, expression).evaluate(v)).doubleValue();

        Set set = new SetFnOrder(context, setPE, expression, "DESC").evolving(v);

        List<Tuple> tupleList = set.getTupleList();

        double numerator = 0;

        double percentage = ((BasicNumeric) percentageExp.evaluate(v)).doubleValue();

        List<Tuple> newSetTupleList = new LinkedList<>();

        if ("TopPercent".equals(functionName)) {
            for (Tuple t : tupleList) {
                numerator += calculateTupleValue(v, t);
                newSetTupleList.add(t);
                if (numerator / totalValue * 100 >= percentage) {
                    break;
                }
            }
        } else { // "BottomPercent".equals(functionName)
            for (int i = tupleList.size() - 1; i >= 0; i--) {
                numerator += calculateTupleValue(v, tupleList.get(i));
                newSetTupleList.add(0, tupleList.get(i));
                if (numerator / totalValue * 100 >= percentage) {
                    break;
                }
            }
        }

        return new Set(newSetTupleList);

    }

    private double calculateTupleValue(MultiDimensionalVector v, Tuple t) {
        BasicData value = vce.vectorValue(context.getCube(), new MultiDimensionalVector(v, t, null));
        return value instanceof BasicNumeric ? ((BasicNumeric) value).doubleValue() : 0;
    }

}
