package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SetFunCrossJoin implements SetFunction {

    private List<SetPE> setPEList;

    public SetFunCrossJoin(ContextAtExecutingMDX context, List<SetPE> setPEList) {
        this.setPEList = setPEList;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {
        return crossJoin(v, setPEList);
    }

    private Set crossJoin(MultiDimensionalVector v, List<SetPE> _setPEList) {

        Set result;

        Set joinLeftSet = _setPEList.get(0).evolving(v);
        Set joinRightSet;

        List<Tuple> tupleList = new ArrayList<>();
        for (int i = 0; i < joinLeftSet.getTupleList().size(); i++) {
            joinRightSet = _setPEList.get(1).evolving(new MultiDimensionalVector(v, joinLeftSet.getTuple(i), null));
            for (int j = 0; j < joinRightSet.getTupleList().size(); j++) {
                tupleList.add(new Tuple(joinLeftSet.getTuple(i), joinRightSet.getTuple(j)));
            }
        }

        if (_setPEList.size() == 2) {
            result = new Set(tupleList);
        } else {
            List<SetPE> tempSetPEs = new LinkedList<>(_setPEList.subList(1, _setPEList.size()));
            tempSetPEs.set(0, new SetPE(null, v1 -> new Set(tupleList)));
            result = crossJoin(v, tempSetPEs);
        }

        return result;

    }
}
