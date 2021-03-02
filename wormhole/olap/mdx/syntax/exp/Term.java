package cn.bgotech.wormhole.olap.mdx.syntax.exp;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.LinkedList;
import java.util.List;

public class Term implements EvaluateAble {

    private List<String> operators = new LinkedList<>();
    private List<Factory> factories = new LinkedList<>();

    /**
     * @param operator [ "*" | "/" | "%" ]
     * @param factory
     */
    public void addFactory(String operator, Factory factory) {
        if (!("*".equals(operator) || "/".equals(operator) || "%".equals(operator) || operator == null)) {
            throw new OlapRuntimeException("incorrect operator is '" + operator + "'");
        }
        operators.add(operator);
        factories.add(factory);
    }

    @Override
    public BasicData evaluate(MultiDimensionalVector vector) {
        BasicData value = factories.get(0).evaluate(vector);
        for (int i = 1; i < factories.size(); i++) {
            switch (operators.get(i)) {
                case "*":
                case "/":
                case "%":
                    value = BasicData.calculate(value, operators.get(i), factories.get(i).evaluate(vector));
                    break;
                default:
                    throw new OlapRuntimeException("illegal operator [" + operators.get(i) + "], need '*' or '/' or '%' here");
            }
        }
        return value;
    }
}
