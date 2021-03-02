package cn.bgotech.wormhole.olap.mddm.data;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.MultiDimensionalDomainModel;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface BasicData extends MultiDimensionalDomainModel {

    /**
     * @param v1
     * @param op ">" ">=" "<" "<=" "=" "<>"
     * @param v2
     * @return
     */
    static boolean evaluateBoolean(BasicData v1, String op, BasicData v2) {
        /*
		 * 只实现等于(v1 = v2)和小于判断(v1 < v2)
		 * <> 相当于: !(v1 = v2)
		 * <= 相当于: (v1 = v2) || (v1 < v2)
		 * >  相当于: (v2 < v1)
		 * >= 相当于: (v1 = v2) || (v1 > v2)
		 */
        if ("=".equals(op)) {
            return v1.equals(v2);
        } else if ("<".equals(op)) {
            if (v1 instanceof BasicBoolean || v1 instanceof BasicNull
                    || v2 instanceof BasicBoolean || v2 instanceof BasicNull) {
                throw new RuntimeException("不允许对空值或布尔值使用小于运算符: " + v1.image() + " < " + v2.image());
            } else if (v1 instanceof BasicNumeric && v2 instanceof BasicNumeric) {
                // 如果v1,v2全部为数值型,按数值大小计算
                return Double.compare(((BasicNumeric) v1).doubleValue(), ((BasicNumeric) v2).doubleValue()) < 0;
            } else { // 如果v1,v2不全为数值型,按数据字面值计算
                return v1.image().compareTo(v2.image()) < 0;
            }
        } else if ("<>".equals(op)) {
            return !evaluateBoolean(v1, "=", v2);
        } else if ("<=".equals(op)) {
            return evaluateBoolean(v1, "<", v2) || evaluateBoolean(v1, "=", v2);
        } else if (">".equals(op)) {
            return evaluateBoolean(v2, "<", v1);
        } else if (">=".equals(op)) {
            return evaluateBoolean(v1, ">", v2) || evaluateBoolean(v1, "=", v2);
        } else {
            throw new RuntimeException("非法的逻辑运算符( " + op + " )");
        }
    }

    /**
     *
     * @param v1
     * @param operator must be in scope ( '+' | '-' | '*' | '/' | '%' )
     * @param v2
     * @return
     */
    static BasicData calculate(BasicData v1, String operator, BasicData v2) {
        switch (operator) {
            case "+":
                return math_PLUS(v1, v2);
            case "-":
                return math_MINUS(v1, v2);
            case "*":
                return math_STAR(v1, v2);
            case "/":
                return math_SLASH(v1, v2);
            case "%":
                return math_MOD(v1, v2);
            default:
                throw new RuntimeException("not support operator '" + operator + "'");
        }
    }

    /**
     * 运算: v1 * v2
     *
     * @param v1
     * @param v2
     * @return
     */
    static BasicData math_STAR(BasicData v1, BasicData v2) {
        if (v1 instanceof BasicNumeric && v2 instanceof BasicNumeric) {
            return new BasicNumeric(((BasicNumeric) v1).doubleValue() * ((BasicNumeric) v2).doubleValue());
        } else {
            throw new OlapRuntimeException
                    ("not support " + v1.getClass().getSimpleName() + " * " + v2.getClass().getSimpleName());
        }
    }

    /**
     * 运算: v1 / v2
     *
     * @param v1
     * @param v2
     * @return
     */
    static BasicData math_SLASH(BasicData v1, BasicData v2) {
        if (v1 instanceof BasicNumeric && v2 instanceof BasicNumeric) {
            return new BasicNumeric(((BasicNumeric) v1).doubleValue() / ((BasicNumeric) v2).doubleValue());
        } else {
//            throw new OlapRuntimeException
//                    ("not support " + v1.getClass().getSimpleName() + " / " + v2.getClass().getSimpleName());
            return BasicNull.INSTANCE;
        }
    }

    /**
     * 运算: v1 + v2
     *
     * @param v1
     * @param v2
     * @return
     */
    static BasicData math_PLUS(BasicData v1, BasicData v2) {
        if (v1 instanceof BasicNumeric && v2 instanceof BasicNumeric) {
            return new BasicNumeric(((BasicNumeric) v1).doubleValue() + ((BasicNumeric) v2).doubleValue());
        } else {
            return new BasicString(v1.image() + v2.image());
        }
    }

    /**
     * 运算: v1 - v2
     *
     * @param v1
     * @param v2
     * @return
     */
    static BasicData math_MINUS(BasicData v1, BasicData v2) {
        if (v1 instanceof BasicNull || v2 instanceof BasicNull)
            return BasicNull.INSTANCE;

        if (v1 instanceof BasicNumeric && v2 instanceof BasicNumeric) {
            return new BasicNumeric(((BasicNumeric) v1).doubleValue() - ((BasicNumeric) v2).doubleValue());
        } else {
            throw new OlapRuntimeException
                    ("not support " + v1.getClass().getSimpleName() + " - " + v2.getClass().getSimpleName());
        }
    }

    /**
     * v1 % v2
     *
     * @param v1
     * @param v2
     * @return
     */
    static BasicData math_MOD(BasicData v1, BasicData v2) {
        throw new OlapRuntimeException("// TODO:"); // TODO: complete this logic at later
    }

    /**
     *
     * @return
     */
    String image();

    Object value();
}
