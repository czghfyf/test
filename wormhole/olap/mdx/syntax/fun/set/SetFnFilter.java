package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.syntax.b00lean.exp.BooleanExpression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.List;

/**
 * Created by ChenZhiGang on 2018/2/22.
 */
public class SetFnFilter implements SetFunction {

    private SetPE setPE;

    private BooleanExpression booleanExp;

    public SetFnFilter(SetPE setPE, BooleanExpression booleanExp) {
        this.setPE = setPE;
        this.booleanExp = booleanExp;
    }

    @Override
    public Set evolving(MultiDimensionalVector vector) {

        Set set = setPE.evolving(vector);
        List<Tuple> tupleList = set.getTupleList();

        // 注释: tupleList中的Tuple对象要和vector结合以形成新的多维向量！！！
        for (int i = tupleList.size() - 1; i >= 0; i--) {

            MultiDimensionalVector newV = new MultiDimensionalVector(vector, tupleList.get(i), null);

            if (!booleanExp.determine(newV).isTrue()) {
                tupleList.remove(i);
            }
        }
        return new Set(tupleList);
    }
}
