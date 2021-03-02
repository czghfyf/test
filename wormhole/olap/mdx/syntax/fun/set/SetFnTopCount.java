package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.List;

public class SetFnTopCount implements SetFunction {

    private ContextAtExecutingMDX context;

    private SetPE setPE;

    private Expression countExp;

    private Expression orderExp;

    public SetFnTopCount(ContextAtExecutingMDX context, SetPE setPE, Expression countExp, Expression orderExp) {
        this.context = context;
        this.setPE = setPE;
        this.countExp = countExp;
        this.orderExp = orderExp;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {

        int topCount = (int) ((BasicNumeric) countExp.evaluate(v)).doubleValue().doubleValue();

        Set set;

        if (orderExp == null) {
            set = setPE.evolving(v);
        } else {
            set = new SetFnOrder(context, setPE, orderExp, "ASC").evolving(v);
        }

        if (topCount < set.getTupleList().size()) {
            List<Tuple> tupleList = set.getTupleList();
            for (int i = tupleList.size() - 1; i >= topCount; i--) {
                tupleList.remove(i);
            }
            set = new Set(tupleList);
        }

        return set;
    }
}
