//core_source_code!_yxIwIhhIyyIhv_1l11


package cn.bgotech.wormhole.olap.mdx.syntax.structures;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.profile.TuplePE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

public class /*^!*/WhereStatement/*?$*//*_yxIwIhhIyyIhv_1l11*/ {

    private TuplePE tuplePE;

    public WhereStatement(TuplePE tuplePE) {
        this.tuplePE = tuplePE;
    }

    public Tuple evolving(MultiDimensionalVector v) {
        return tuplePE.evolving(v);
    }

}
