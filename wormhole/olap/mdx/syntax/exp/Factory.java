package cn.bgotech.wormhole.olap.mdx.syntax.exp;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.data.BasicString;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.TuplePE;
import cn.bgotech.wormhole.olap.mdx.syntax.fun.exp.ExpressionFunction;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.Arrays;

public class Factory implements EvaluateAble {

//    private ContextAtExecutingMDX ctx;
//    private Double numeric;
//    private String string;
//    private TuplePE tuplePe;
//    private Expression exp;
//    private ExpressionFunction fun;

    private EvaluateAble evaluateDevice;

    /**
     * @param context
     * @param sign [ "+" | "-" ]
     * @param numStr
     */
    public Factory(ContextAtExecutingMDX context, String sign, String numStr) {
//        this.ctx = context;
//        double d = Double.parseDouble(numStr);
//        if (sign == null || "+".equals(sign)) {
//            numeric = d;
//        } else if ("-".equals(sign)) {
//            numeric = 0 - d;
//        } else {
//            throw new OlapRuntimeException("sign is " + sign + ". right sign is in (+, -)");
//        }
//
//        new WormholeNumeric(Double.parseDouble(numStr));

        evaluateDevice = v -> new BasicNumeric(sign, Double.parseDouble(numStr));

    }

    public Factory(ContextAtExecutingMDX context, String strValue) {
//        this.ctx = context;
//        string = strValue;
//
//        new WormholeString(this.strValue);

        evaluateDevice = v -> new BasicString(strValue);
    }

    public Factory(ContextAtExecutingMDX context, TuplePE tuplePe) {

//        this.ctx = context;
//        this.tuplePe = tuplePe;


//        // 要根据Tuple求值啦！
//        Tuple tuple = (Tuple) this.tupleShell.hatch(vector);
//        WormholeDataBridge dataBridge = new WormholeDataBridge(this.context.getWormholeApp());
//            /*double*/WormholeBasicData value = //dataBridge.measureValue(this.context.getExecuteCube(), /*tuple*/new Tuple(new VectorCoordinateFragment(vcf, tuple).getMembers()));
//                // dataBridge.measureValue(this.context.getExecuteCube(), /*tuple*/new Tuple(new VectorCoordinateFragment(vcf, tuple).getMemberRoles()));
//                dataBridge.vectorValue(this.context.getExecuteCube(), new MultidimensionalVector(vector, tuple, null)); // TODO 需要另一种构造方法
//        return value;


        evaluateDevice = v -> OlapEngine.hold().vectorValue(context.getCube(),
                Arrays.asList(new MultiDimensionalVector(v, tuplePe.evolving(v), null))).get(0);

    }

    public Factory(ContextAtExecutingMDX context, Expression exp) {
//        this.ctx = context;
//        this.exp = exp;
//
//        /*WormholeValue*/WormholeBasicData wv = this.numExp.evaluate(vector);

        evaluateDevice = v -> exp.evaluate(v);
    }

    public Factory(ContextAtExecutingMDX context, ExpressionFunction fun) {
//        this.ctx = context;
//        this.fun = fun;
//
//        numFun.evaluate(vector);

        evaluateDevice = v -> fun.evaluate(v);
    }

    @Override
    public BasicData evaluate(MultiDimensionalVector vector) {
        return evaluateDevice.evaluate(vector);
    }
}
