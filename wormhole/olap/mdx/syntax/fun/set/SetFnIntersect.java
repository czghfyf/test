package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import cn.bgotech.wormhole.olap.util.BGTechUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SetFnIntersect implements SetFunction {

    private ContextAtExecutingMDX context;

    private List<SetPE> setPEList;

    private boolean containAll;

    public SetFnIntersect(ContextAtExecutingMDX context, List<SetPE> setPEList, boolean containAll) {
        this.context = context;
        this.setPEList = setPEList;
        this.containAll = containAll;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {
        Collection<Tuple> tupleCollection = setPEList.get(0).evolving(v).getTupleList();
        for (int i = 1; i < setPEList.size(); i++) {
            tupleCollection = CollectionUtils.intersection(tupleCollection, setPEList.get(i).evolving(v).getTupleList());
        }
        List<Tuple> tupleList = new ArrayList<>(tupleCollection);
        return new Set(containAll ? tupleList : BGTechUtil.removeDuplicates(tupleList));
    }
}
