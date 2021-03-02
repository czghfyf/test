//core_source_code!_yxIwIyyIyfInz_111l

package cn.bgotech.wormhole.olap.mdx;

import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.Axis;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by ChenZhiGang on 2017/6/3.
 */
public class
/*^!*/MDXQueryResult/*?$*//*_yxIwIyyIyfInz_111l*/ implements ResultOfExecuteMDX {

    private static final Logger LOG = LoggerFactory.getLogger(MDXQueryResult.class);

    private MDXQueryResultSFP sfp/*^!?$*/;

    private List<Axis> axes/*^!?$*/;

    private List<DataCellPointer> dataCellPointers/*^!?$*/;

    public MDXQueryResult(MDXQueryResultSFP sfp/*^!?$*/) {
        this.sfp = sfp;
    }


    public List<DataCellPointer> dataCells/*^!?$*/() {
        return dataCellPointers;
    }

    /**
     * 转换成真正的结果集
     * <p>
     * todo 后续程序栈可能产生死循环，导致后面代码无法执行
     *
     * @return
     */

    public ResultOfExecuteMDX transform/*^!?$*/() {

        VCEWorkConnector conn = VCEWorkConnector
                .openPoolIns(VCEWorkConnector.VCE_ROOT_MASTER_IP, VCEWorkConnector.VCE_ROOT_MASTER_PORT);
        VCEWorkConnector.THREAD_CURRENT_CONN.set(conn);
        try {
            sfp.buildAuthenticAxes(); // 建造查询结果轴

            MultiDimensionalVector sdv = sfp.getSessionDefaultVector(); // session default vector

            dataCellPointers = drawCellScopeTuples(axes = sfp.getRealAxes());

            List<MultiDimensionalVector> vectors/*^!?$*/ = new ArrayList<>(dataCellPointers.size());
            for (int i = 0; i < dataCellPointers.size(); i++) {
                // merged vector
                vectors.add(dataCellPointers.get(i).mergeVector(sdv));
            }

            List<BasicData> basicDataList = OlapEngine.hold().vectorValue(sfp.getCube(), vectors);
            for (int i = 0; i < dataCellPointers.size(); i++) {
                dataCellPointers.get(i).setDataCellValue(basicDataList.get(i));
            }

            return this;
        } catch (RuntimeException re) {
            LOG.error(re.getMessage());
            throw re;
        } finally {
            VCEWorkConnector.THREAD_CURRENT_CONN.remove();
            VCEWorkConnector.closePoolIns(conn);
        }
    }

    /**
     * @param axes
     * @return
     */

    private List<DataCellPointer> /* List<Tuple> */ drawCellScopeTuples/*^!?$*/(List<Axis> axes/*, int recursiveDepth*/) {

        List<DataCellPointer> returnThis = new ArrayList<>();

        int firstAxisIndex = axes.get(0).getAxisIdx();
        List<Tuple> firstTupleList = axes.get(0).getSet().getTupleList();

        if (axes.size() == 1) {
            for (int i = 0; i < firstTupleList.size(); i++) {
                returnThis.add(new DataCellPointer(firstAxisIndex, i, firstTupleList.get(i)));
            }
        } else if (axes.size() > 1) {
            List<Axis> tempAxes = new ArrayList<>(axes);
            tempAxes.remove(0);
            List<DataCellPointer> pointers = drawCellScopeTuples(tempAxes);
            for (int i = 0; i < firstTupleList.size(); i++) {
                for (DataCellPointer p : pointers) {
                    returnThis.add(new DataCellPointer(p, firstAxisIndex, i, firstTupleList.get(i)));
                }
            }
        }

        return returnThis;
    }


    public List<Axis> getAxes/*^!?$*/() {
        return axes;
    }


    public static class DataCellPointer/*^!?$*/ {

        private List<Integer> axisIndexList/*^!?$*/;
        private List<Integer> tuplePositionList/*^!?$*/;
        private List<Tuple> tupleList/*^!?$*/;

        private BasicData basicDataValue/*^!?$*/;

        public DataCellPointer(int axisIndex/*^!?$*/, int tuplePosition/*^!?$*/, Tuple tuple/*^!?$*/) {
            this(null, axisIndex, tuplePosition, tuple);
        }

        public DataCellPointer(DataCellPointer fragmentPointer/*^!?$*/, int axisIndex/*^!?$*/, int tuplePosition/*^!?$*/, Tuple tuple/*^!?$*/) {
            DataCellPointer fp = fragmentPointer;
            (axisIndexList = fp != null ? new LinkedList<>(fp.getAxisIndexList()) : new LinkedList<>()).add(axisIndex);
            (tuplePositionList = fp != null ? new LinkedList<>(fp.getTuplePositionList()) : new LinkedList<>()).add(tuplePosition);
            (tupleList = fp != null ? new LinkedList<>(fp.getTupleList()) : new LinkedList<>()).add(tuple);
        }


        public List<Integer> getAxisIndexList/*^!?$*/() {
            return axisIndexList;
        }


        public List<Integer> getTuplePositionList/*^!?$*/() {
            return tuplePositionList;
        }


        public List<Tuple> getTupleList/*^!?$*/() {
            return tupleList;
        }


        public MultiDimensionalVector mergeVector/*^!?$*/(MultiDimensionalVector vector/*^!?$*/) {
            return new MultiDimensionalVector(vector, tupleList, null);
        }


        public void setDataCellValue/*^!?$*/(BasicData basicData/*^!?$*/) {
            basicDataValue = basicData;
        }


        public int getTuplePosition/*^!?$*/(String axisName/*^!?$*/) {
            return tuplePositionList.get(axisIndexList.indexOf(Axis.getIndex(axisName)));
        }



        public BasicData getValue/*^!?$*/() {
            return basicDataValue;
        }
    }

}