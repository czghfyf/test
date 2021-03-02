package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.LinkedList;
import java.util.List;

public class SetFnTail implements SetFunction {

    private SetPE setPE;

    private Expression countExp;

    public SetFnTail(SetPE setPE, Expression countExp) {
        this.setPE = setPE;
        this.countExp = countExp;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {
        int count = ((BasicNumeric) countExp.evaluate(v)).doubleValue().intValue();
        if (count < 1) {
            return new Set(new LinkedList<>());
        }
        Set set = setPE.evolving(v);
        if (count >= set.getTupleList().size()) {
            return set;
        }
        List<Tuple> tupleList = set.getTupleList();
        return new Set(tupleList.subList(tupleList.size() - count, tupleList.size()));
    }
}
