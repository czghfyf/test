package cn.bgotech.wormhole.olap.mdx.syntax.fun.set;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.data.BasicString;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetFnOrder implements SetFunction {

    private ContextAtExecutingMDX ctx;

    private SetPE setPE;

    private Expression exp;

    private String strategy;

    public SetFnOrder(ContextAtExecutingMDX ctx, SetPE setPE, Expression exp, String strategy) {
        this.ctx = ctx;
        this.setPE = setPE;
        this.exp = exp;
        this.strategy = strategy;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {

        Set set = setPE.evolving(v);

        List<OrderTool> tools = new ArrayList<>();
        List<Tuple> tupleList = set.getTupleList();

        for (Tuple t : tupleList) {

            MultiDimensionalVector newV = new MultiDimensionalVector(v, t, null);

            BasicData basicData = exp.evaluate(newV);

            tools.add(new OrderTool(t, basicData));
        }

        tupleList = executeOrder(tools, strategy);

        return new Set(tupleList);

    }

    private List<Tuple> executeOrder(List<OrderTool> tools, String strategy) {
        Collections.sort(tools); // TODO: 改用工具类实现
        List<Tuple> tupleList = new ArrayList<>();
        switch (strategy) {
            case "ASC":
                for (int i = 0; i < tools.size(); i++) {
                    tupleList.add(tools.get(i).getTuple());
                }
                break;
            case "DESC":
                for (int i = tools.size() - 1; i >=0; i--) {
                    tupleList.add(tools.get(i).getTuple());
                }
                break;
            case "BASC":
            case "BDESC":
            default:
                throw new OlapRuntimeException(String.format("'%s' order strategy is not supported", strategy));
        }
        return tupleList;
    }

    private static class OrderTool implements Comparable<OrderTool> {

        private Tuple tuple;
        private BasicData basicData;

        public OrderTool(Tuple tuple, BasicData basicData) {
            this.tuple = tuple;
            this.basicData = basicData;
        }

        public Tuple getTuple() {
            return tuple;
        }

        @Override
        public int compareTo(OrderTool ot) {

            if (basicData instanceof BasicNumeric && ot.basicData instanceof BasicNumeric) {
                return Double.compare(((BasicNumeric) basicData).doubleValue(), ((BasicNumeric) ot.basicData).doubleValue());
            }
            if (basicData instanceof BasicString && ot.basicData instanceof BasicString) {
                return basicData.image().compareTo(ot.basicData.image());
            }
            if (basicData instanceof BasicNumeric && ot.basicData instanceof BasicString) {
                return -1;
            }
            if (basicData instanceof BasicString && ot.basicData instanceof BasicNumeric) {
                return 1;
            }

            return 0;
        }
    }

}
