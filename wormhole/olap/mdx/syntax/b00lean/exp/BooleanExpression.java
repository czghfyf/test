package cn.bgotech.wormhole.olap.mdx.syntax.b00lean.exp;

import cn.bgotech.wormhole.olap.mddm.data.BasicBoolean;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.List;

/**
 * Created by ChenZhiGang on 2018/2/22.
 */
public class BooleanExpression implements BooleanJudge {

    private List<BooleanTerm> boolTermList;

    /**
     * boolean_expression ::= boolean_term ( <OR> boolean_term )*
     *
     * @param boolTermList
     */
    public BooleanExpression(List<BooleanTerm> boolTermList) {
        this.boolTermList = boolTermList;
    }

    @Override
    public BasicBoolean determine(MultiDimensionalVector vector) {
        for (BooleanTerm bt : boolTermList) {
            if (bt.determine(vector).isTrue()) {
                return BasicBoolean.TRUE;
            }
        }
        return BasicBoolean.FALSE;
    }
}
