package cn.bgotech.wormhole.olap.mdx.syntax.exp;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.LinkedList;
import java.util.List;

public class CommonExpression implements Expression {

    private List<String> operators = new LinkedList<>();
    private List<Term> terms = new LinkedList<>();

    /**
     * @param operator "+" or "-"
     * @param term
     */
    public void addTerm(String operator, Term term) {
        if (!("+".equals(operator) || "-".equals(operator) || operator == null)) {
            throw new OlapRuntimeException("incorrect operator is '" + operator + "'");
        }
        operators.add(operator);
        terms.add(term);
    }

    @Override
    public BasicData evaluate(MultiDimensionalVector vector) {
        BasicData value = terms.get(0).evaluate(vector);
        for (int i = 1; i < terms.size(); i++) {
            switch (operators.get(i)) {
                case "+":
//                    value = WormholeBasicDataUtil.evaluate(value, '+', signedNumericTermList.get(i).getNumericTerm().evaluate(vector));
//                    break;
                case "-":
//                    value = WormholeBasicDataUtil.evaluate(value, '-', signedNumericTermList.get(i).getNumericTerm().evaluate(vector));
                    value = BasicData.calculate(value, operators.get(i), terms.get(i).evaluate(vector));
                    break;
                default:
//                    throw new /*Wromhole*/RuntimeException("错误的枚举值:" + signedNumericTermList.get(i).getSymbol().toString());
                    throw new OlapRuntimeException("illegal operator [" + operators.get(i) + "], need '+' or '-' here");
            }
        }
        return value;
    }
}
