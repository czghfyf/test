package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.ArrayList;
import java.util.List;

public class SetFnUnion implements SetFunction {

    private ContextAtExecutingMDX context;

    private List<SetPE> setPEList;

    private boolean containAll;

    public SetFnUnion(ContextAtExecutingMDX context, List<SetPE> setPEList, boolean containAll) {
        this.context = context;
        this.setPEList = setPEList;
        this.containAll = containAll;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {
        List<Tuple> tupleList = new ArrayList<>();
        for (SetPE setPE : setPEList) {
            tupleList.addAll(setPE.evolving(v).getTupleList());
        }
        return new Set(containAll ? tupleList : BGTechUtil.removeDuplicates(tupleList));
    }
}
