package cn.bgotech.wormhole.olap.mdx.syntax.b00lean.exp;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.data.BasicBoolean;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mdx.syntax.b00lean.functions.BooleanFunction;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

/**
 * Created by ChenZhiGang on 2018/2/22.
 */
public class BooleanFactory implements BooleanJudge {

    private enum TypeConstructorEnum {
        EXP_OPT_EXP,
        BOOL_EXP,
        NOT_BOOLEAN_FACTORY,
        BOOLEAN_FUNCTION
    }

    private TypeConstructorEnum typeConstructor;

    private boolean conversely;
    private BooleanFactory boolFactory;

    private String op; // ( ">" | ">=" | "<" | "<=" | "=" | "<>" )

    private Expression exp1;
    private Expression exp2;

    private BooleanExpression boolExp;

    private BooleanFunction booleanFunction;

    public BooleanFactory(BooleanFunction booleanFunction) {
        typeConstructor = TypeConstructorEnum.BOOLEAN_FUNCTION;
        this.booleanFunction = booleanFunction;
    }

    /**
     * boolean_factory ::= <NOT> boolean_factory
     *
     * @param conversely true-conversely 取原值；false-conversely 取反
     * @param boolFactory
     */
    public BooleanFactory(boolean conversely, BooleanFactory boolFactory) {
        this.typeConstructor = TypeConstructorEnum.NOT_BOOLEAN_FACTORY;
        this.conversely = conversely;
        this.boolFactory = boolFactory;
    }

    /**
     * boolean_factory ::= expression boolean_operators expression
     *
     * @param exp1
     * @param op - ( ">" | ">=" | "<" | "<=" | "=" | "<>" )
     * @param exp2
     */
    public BooleanFactory(Expression exp1, String op, Expression exp2) {
        this.typeConstructor = TypeConstructorEnum.EXP_OPT_EXP;
        this.exp1 = exp1;
        this.op = op;
        this.exp2 = exp2;
    }

    /**
     * boolean_factory ::=	"(" boolean_expression ")"
     *
     * @param boolExp
     */
    public BooleanFactory(BooleanExpression boolExp) {
        this.typeConstructor = TypeConstructorEnum.BOOL_EXP;
        this.boolExp = boolExp;
    }

    @Override
    public BasicBoolean determine(MultiDimensionalVector vector) {
        switch (typeConstructor) {
            case EXP_OPT_EXP:
                return execute(exp1, op, exp2, vector);
            case BOOL_EXP:
                return boolExp.determine(vector);
            case NOT_BOOLEAN_FACTORY:
                BasicBoolean anotherValue = boolFactory.determine(vector);
                return conversely ? anotherValue : anotherValue.inversely();
            case BOOLEAN_FUNCTION:
                return booleanFunction.determine(vector);
            default:
                throw new OlapRuntimeException(String.format("error in %s.%s", getClass().getName(), "getBooleanValue()"));
        }
    }

    /**
     * 返回两个基础数据类型的逻辑运算结果
     * return leftExp operator rightExp
     *
     * @param leftExp
     * @param operator - ( ">" | ">=" | "<" | "<=" | "=" | "<>" )
     * @param rightExp
     * @param vector 多维向量上下文
     * @return
     */
    private BasicBoolean execute(Expression leftExp, String operator,
                            Expression rightExp, MultiDimensionalVector vector) {

        BasicData leftValue = leftExp.evaluate(vector);
        BasicData rightValue = rightExp.evaluate(vector);

        boolean aBoolean = BasicData.evaluateBoolean(leftValue, operator, rightValue);
        return aBoolean ? BasicBoolean.TRUE : BasicBoolean.FALSE;

    }

}
