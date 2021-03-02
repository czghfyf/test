package cn.bgotech.wormhole.olap.mdx.syntax.b00lean.exp;

import cn.bgotech.wormhole.olap.mddm.data.BasicBoolean;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.List;

/**
 * Created by ChenZhiGang on 2018/2/22.
 */
public class BooleanTerm implements BooleanJudge {

    private List<BooleanFactory> boolFactoryList;

    /**
     * boolean_term ::= boolean_factory ( <AND> boolean_factory )*
     *
     * @param boolFactoryList
     */
    public BooleanTerm(List<BooleanFactory> boolFactoryList) {
        this.boolFactoryList = boolFactoryList;
    }

    /**
     * 通过多维向量计算出布尔表达式的真实值
     *
     * @param vector
     * @return
     */
    @Override
    public BasicBoolean determine(MultiDimensionalVector vector) {
        for (BooleanFactory bf : boolFactoryList) {
            if (!bf.determine(vector).isTrue()) {
                return BasicBoolean.FALSE;
            }
        }
        return BasicBoolean.TRUE;
    }

}
