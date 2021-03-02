package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhigang on 2018/2/25.
 */
public class SetFnExcept implements SetFunction {

    private SetPE setPE_A;

    private SetPE setPE_B;

    private boolean keepDuplicates;

    public SetFnExcept(SetPE setPE_A, SetPE setPE_B, String all) {
        this.setPE_A = setPE_A;
        this.setPE_B = setPE_B;
        keepDuplicates = "all".equalsIgnoreCase(all);
    }

    /**
     * 第一步: 遍历set_1中的tuple, 如果在set_1中的某个tuple也存在于set_2中则将其从set_1中移除。
     * 第二步: 在第一步完成后, 如果set_1中存在重复的tuple, 则根据keepDuplicates决定其取舍
     * if (keepDuplicates) { 重复tuple全部保留 } else { 重复tuple只保留一个 }
     */
    @Override
    public Set evolving(MultiDimensionalVector v) {

        Set set_1 = setPE_A.evolving(v);
        Set set_2 = setPE_B.evolving(v);

        List<Tuple> tupleList1 = set_1.getTupleList();
        List<Tuple> tupleList2 = set_2.getTupleList();

        tupleList1.removeAll(tupleList2);

        if (!keepDuplicates) {
            List<Tuple> tempList = new ArrayList();
            for (Tuple t : tupleList1) {
                if (!tempList.contains(t)) {
                    tempList.add(t);
                }
            }
            tupleList1 = tempList;
        }

        return new Set(tupleList1);

    }

}
