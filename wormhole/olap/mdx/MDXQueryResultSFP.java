//core_source_code!_yxIwIyyIyfInz_1lll1

package cn.bgotech.wormhole.olap.mdx;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.logical.combination.*;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.member.CalculatedMember;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.profile.CubePE;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.*;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import cn.bgotech.wormhole.olap.util.collection.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Set;

/**
 * Created by ChenZhiGang on 2017/5/15.
 * MDX Query Result Semi-Finished-Product
 */
public class
/*^!*/MDXQueryResultSFP/*?$*//*_yxIwIyyIyfInz_1lll1*/ implements SFPOfExecuteMDX {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ContextAtExecutingMDX ctx/*^!?$*/;

    private boolean completed/*^!?$*/;

    // the container of named sets
    private Map<MultiDimensionalDomainSelector, SetPE> namedSetMap/*^!?$*/ = new HashMap<>();
    // The container of calculated members
    private Map<MultiDimensionalDomainSelector, Expression> calculatedMemberMap/*^!?$*/ = new HashMap<>();

    private List<Axis> axes/*^!?$*/;
    private Cube cube/*^!?$*/;
    private WhereStatement whereStat/*^!?$*/;

    private MultiDimensionalVector sessionDefaultVector/*^!?$*/;


    public MDXQueryResultSFP(ContextAtExecutingMDX ctx/*^!?$*/, List<WithFormula> withFormulaList/*^!?$*/,
                             List<Axis> axisList/*^!?$*/, CubePE cubePe/*^!?$*/, WhereStatement whereStat/*^!?$*/) {

        this.ctx = ctx;

        // Collections.sort(axisList, (a1, a2) -> a1.getAxisIdx() - a2.getAxisIdx());
        Collections.sort(axisList, Comparator.comparingInt(Axis::getAxisIdx));
        this.axes = axisList;

        this.cube = cubePe.evolving(null);
        this.whereStat = whereStat;

        for (WithFormula f : withFormulaList) {
            if (f instanceof SetFormula) {
                SetFormula sf = (SetFormula) f;
                namedSetMap.put(sf.getMDDSelector(), sf.getSetPe());
            } else if (f instanceof MemberFormula) {
                MemberFormula mf = (MemberFormula) f;
                calculatedMemberMap.put(mf.getMDDSelector(), mf.getExp());
            } else {
                throw new OlapRuntimeException(f.getClass().getName() + " is incorrect class");
            }
        }
    }


    public OlapEngine getOLAPEngine/*^!?$*/() {
        return ctx.getOLAPEngine();
    }

    /**
     * When the MDX has just been parsed, all axes represent only its prototype,
     * need to call this method to evolve all axes into real form,
     * and construct the session default multidimensional vector.
     */

    public void buildAuthenticAxes/*^!?$*/() {

        Set<DimensionRole> checkRepeatDimensionRoleSet/*^!?$*/ = new HashSet<>();

        // Gets the cube's default multidimensional vector,
        // and acts as the default multidimensional vector for the current session
        sessionDefaultVector = this.cube.getDefaultVector();

        if (whereStat != null) {

            // get 'head tuple reference' of whereStat
            // whereTuple can only be affected by the sessionDefaultVector, and can not be affected by the other axes
            Tuple whereTuple = whereStat.evolving(sessionDefaultVector);

            if (whereTuple == null) {
                throw new OlapRuntimeException("Can not generate whereTuple");
            }

            try {
                CollectionUtil.mergeCollection(checkRepeatDimensionRoleSet, MemberRole.extract(whereTuple.getMemberRoles()));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new OlapRuntimeException("possible reason: dimension role definition is repeated", e);
            }

            // whereTuple impact sessionDefaultVector
            sessionDefaultVector = new MultiDimensionalVector(sessionDefaultVector, whereTuple, null);
        }

        Map<Integer, Tuple> axisHeadTupleMap/*^!?$*/ = new HashMap<>();

        /**
         * 例:
         *
         * 查询结果集为5个轴,分别为:A/B/C/D/E
         * A轴的Set对象需要{@sessionDefaultVector}与whereTuple作为上下文,B轴的Set对象需要A轴头部tuple参考作为上下文,
         * C/D/E规则同上,轴依赖关系如下(左依赖右):
         *     A -> {@sessionDefaultVector} + whereTuple, B -> A, C -> B, D -> C, E -> D
         * 程序块需要执行5次，正好是this.axes的长度。
         *
         * 2018/1/31补充:
         *     假设查询结果集有N个轴，在 buildSessionDefaultVector 循环第一次执行前，
         *     cube默认多维向量与whereTuple(可能为空)已经确定了sessionDefaultVector(简称SDV)。
         *
         *     buildSessionDefaultVector 循环第 1 次执行
         *         axisTupleHeader 循环全部执行后
         *             SDV影响了Axis(1)，Axis(1)首次给出了正确的头部tuple参考
         *             SDV影响了Axis(2)至Axis(N)，但Axis(2)至Axis(N)并未给出正确的头部tuple参考
         *             注意！！！Axis后的序号不代表轴在查询结果集中的位置，而表示依次给出正确头部tuple参考的顺序
         *         influenceSessionDefaultVector 循环全部执行后
         *             Axis(1)给出的正确的头部tuple参考对sessionDefaultVector施加了正确影响，
         *             sessionDefaultVector = SDV + Axis(1)HeadTuple
         *
         *     buildSessionDefaultVector 循环第 2 次执行
         *         axisTupleHeader 循环全部执行后
         *             SDV + Axis(1)HeadTuple影响了Axis(1)，Axis(1)继续给出正确的头部tuple参考
         *             SDV + Axis(1)HeadTuple影响了Axis(2)，Axis(2)首次给出了正确的头部tuple参考
         *             SDV + Axis(1)HeadTuple影响了Axis(3)至Axis(N)，但Axis(3)至Axis(N)并未给出正确的头部tuple参考
         *         influenceSessionDefaultVector 循环全部执行后
         *             Axis(1)和Axis(2)给出的正确的头部tuple参考对sessionDefaultVector施加了正确影响，
         *             sessionDefaultVector = SDV + Axis(1)HeadTuple + Axis(2)HeadTuple
         *
         *     ... ...
         *
         *     buildSessionDefaultVector 循环第 N - 1 次执行
         *         axisTupleHeader 循环全部执行后
         *             SDV + Axis(1)HeadTuple ~ Axis(N - 2)HeadTuple影响了Axis(1)至Axis(N - 2)，Axis(1)至Axis(N - 2)继续给出正确的头部tuple参考
         *             SDV + Axis(1)HeadTuple ~ Axis(N - 2)HeadTuple影响了Axis(N - 1)，Axis(N - 1)首次给出了正确的头部tuple参考
         *             SDV + Axis(1)HeadTuple ~ Axis(N - 2)HeadTuple影响了Axis(N)，但Axis(N)并未给出正确的头部tuple参考
         *         influenceSessionDefaultVector 循环全部执行后
         *             Axis(1)至Axis(N - 1)给出的正确的头部tuple参考对sessionDefaultVector施加了正确影响，
         *             sessionDefaultVector = SDV + Axis(1)HeadTuple ~ Axis(N - 1)HeadTuple
         *
         *     buildSessionDefaultVector 循环第 N 次执行
         *         axisTupleHeader 循环全部执行后
         *             SDV + Axis(1)HeadTuple ~ Axis(N - 1)HeadTuple影响了Axis(1)至Axis(N - 1)，Axis(1)至Axis(N - 1)继续给出正确的头部tuple参考
         *             SDV + Axis(1)HeadTuple ~ Axis(N - 1)HeadTuple影响了Axis(N)，Axis(N)首次给出了正确的头部tuple参考
         *         influenceSessionDefaultVector 循环全部执行后
         *             Axis(1)至Axis(N)给出的正确的头部tuple参考对sessionDefaultVector施加了正确影响，
         *             sessionDefaultVector = SDV + Axis(1)HeadTuple ~ Axis(N)HeadTuple
         *         sessionDefaultVector构建完成。
         * 2018/1/31补充结束。
         */
        int cycleIndex = this.axes.size();
        buildSessionDefaultVector:
        while (cycleIndex-- > 0) {

            Tuple tempTuple;
            // Set<DimensionRole> tmpSet;

            axisTupleHeader/*^!?$*/:
            for (Axis axis : this.axes) {

                cn.bgotech.wormhole.olap.mddm.logical.combination.Set axisSet = axis.evolving(sessionDefaultVector);
                if (axisSet.isEmpty()) { // null set
                    continue;
                }

                tempTuple = axisSet.getTuple(0);
                // tmpSet = new HashSet<>(checkRepeatDimensionRoleSet);

                /*try {
                    CollectionUtil.mergeCollection(tmpSet, MemberRole.extract(tempTuple.getMemberRoles()));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new OlapRuntimeException("possible reason: dimension role definition is repeated", e);
                }*/

//                axesHeadTupleMap.put(axis.getCoordPosition(), tuple);
                axisHeadTupleMap.put(axis.getAxisIdx(), tempTuple);
            }
            /**
             * 程序首次执行至此时，每个查询轴的“头部tuple参考”是以“Cube默认多维向量与whereTuple结合”为上下文计算得出。
             *
             * 对于不受上下文影响的固定查询轴，其能够给出正确的“头部tuple参考”(在 'influenceSessionDefaultVector:' 循环处对“会话默认多维向量”施予了正确的影响);
             * 对于受上下文影响的非固定查询轴，由于其他轴的“头部tuple参考”没有对其进行影响，故目前其给出的“头部tuple参考”可能是错误的。
             *
             * 如果 'buildSessionDefaultVector:' 循环再次执行至此，
             * 固定轴(没有使用任何函数，而是直接定义了set)和已给出正确“头部tuple参考”的非固定查询轴已经可以通过会话默认多维向量对“其他非固定轴”进行影响(在 'influenceSessionDefaultVector:' 循环处)，
             * 使得“其他非固定轴”给出的“头部tuple参考”趋向正确(在下一次 'buildSessionDefaultVector:' 循环中的 'axisTupleHeader:' 执行完成时)。
             */
                /* 旧的注释
                 *
                 * 1、第一次执行(步骤一)：
                 *     程序第一次执行到这里时，每个查询轴的“头部tuple参考”是以“Cube对象默认多维向量与whereTuple结合”为上下文计算得出，
                 *     对于不受上下文影响的固定查询轴，其上下文固定不变，没有影响；对于受上下文影响的非固定查询轴，由于其他查询轴的“头部tuple参考”没有对其进行影响，
                 *     目前其给出的“头部tuple参考”可能是错误的。
                 * 2、第二次执行(步骤三)：
                 *     固定查询轴已经可以通过会话默认多维向量对非固定轴进行影响，在非固定查询轴上计算得出的“头部tuple参考”正确。
                 */


            // Constructs the sessionDefaultVector of the current state
            influenceSessionDefaultVector/*^!?$*/:
            for (int i = 0; i < this.axes.size(); i++) {
                sessionDefaultVector = new MultiDimensionalVector
                        (sessionDefaultVector, axisHeadTupleMap.get(axes.get(i).getAxisIdx()), null);
            }
            /**
             * 程序首次执行至此时，非固定轴可能没有给出正确的“头部tuple参考”，构建完成的会话默认多维向量可能存在错误。
             * 固定轴能够给出正确的“头部tuple参考”，故其对“会话默认多维向量”施予了正确的影响，使后者趋于正确。
             * 而这个趋于正确的“会话默认多维向量”将会在下一次 'axisTupleHeader:' 迭代时对非固定轴进行影响。
             *
             * 以某个具有N个轴的MDX查询为例，轴之间最极端的影响关系 ( 被依赖者 -> 依赖者 ) 如下：
             *     cubeDefaultVector + tuple -> axis(1), axis(1) -> axis(2), axis(2) -> axis(3), ... , axis(N - 1) -> axisN
             * 即便如此，'buildSessionDefaultVector:' 的迭代次数至多为查询轴的数量(当前情况下为N)，所以在其最后一次迭代时：
             *     'axisTupleHeader:' 执行完成后，所有轴都能给出正确的“头部tuple参考”;
             *     'influenceSessionDefaultVector:' 执行完成后，正确的“会话默认多维向量”已经构建完成。
             *
             * 当 'buildSessionDefaultVector:' 最后一次迭代结束，所有非固定轴已给出正确的“头部tuple参考”，最后一次构建完成的会话默认多维向量正确。
             */
                /* 旧的注释
                 * 1、第一次执行(步骤二)：
                 *     非固定查询轴可能没有给出正确的“头部tuple参考”，构建完成的会话默认多维向量可能存在错误。
                 *     固定查询轴的“头部tuple参考”改变了会话默认多维向量，可以间接对非固定查询轴进行影响。
                 * 2、第二次执行(步骤四)：
                 *     非固定查询轴已给出正确的“头部tuple参考”，再次构建完成的会话默认多维向量正确。
                 */

        }
        /**
         * 以下情况描述了当MDX查询逻辑存在矛盾时OLAP引擎的执行情况。
         *
         * 当 'buildSessionDefaultVector:' 的某次迭代开始时，“会话默认多维向量”的形态为S1，
         * 此次迭代后，某个查询轴X给出了所谓“正确”的“头部tuple参考”，并对“会话默认多维向量”施予了影响，此时后者的形态为S2。
         *
         * 在后续某次 'buildSessionDefaultVector:' 迭代后，基于形态为S2的“会话默认多维向量”，某查询轴Y也给出了所谓“正确”的“头部tuple参考”，
         * 并影响“会话默认多维向量”致其形态变为S3。
         *
         * 随着 'buildSessionDefaultVector:' 不断迭代，“会话默认多维向量”的形态由S3变为Sn。
         *
         * 在“会话默认多维向量”的形态变为Sn后的某次 'buildSessionDefaultVector:' 迭代时，在 'axisTupleHeader:' 执行后，
         * 基于Sn形态的“会话默认多维向量”导致查询轴X给出的“头部tuple参考”(2018/1/31补充：不一定是不正确的！)与“会话默认多维向量”
         * 为S1形态时X轴给出的“头部tuple参考”不一致(假设导致此问题的原因是Y轴影响了X轴)，
         * 这说明某些查询轴具有循环依赖关系，此种情况下，OLAP引擎仍能够在正常执行(不抛出异常也未出现明显的程序执行问题)MDX查询，
         * 并返回非期望的多维查询结果集(也可能侥幸返回期望的结果集)。
         *
         * 导致此种情况的原因是MDX语句存在逻辑上的矛盾，并非OLAP引擎的BUG。
         */

        // sessionDefaultVector is already right, build all axes.
        for (Axis axis : this.axes) {
            axis.evolving(sessionDefaultVector);
        }

        completed = true;
    }


    @Override
    public Cube getCube() {
//        if (!completed) {
//            logger.warn("be not completed !");
//        }
        return cube;
    }

    @Override
    public MultiDimensionalVector getSessionDefaultVector() {
        if (sessionDefaultVector == null) {
            throw new OlapRuntimeException("Session Default Vector is not built yet.");
        }
        return sessionDefaultVector;
    }

    /**
     * @param selector
     * @return
     */

    public CalculatedMember getCalculatedMember/*^!?$*/(MultiDimensionalDomainSelector selector/*^!?$*/) {

        if (calculatedMemberMap.get(selector) != null) {
            return new CalculatedMember(ctx, selector, calculatedMemberMap.get(selector));
        }

        OlapEngine olap/*^!?$*/ = ctx.getOLAPEngine();
        DimensionRole dr_1 = olap.findUniqueEntity(cube, DimensionRole.class, selector.subSelector(0, 1));
        for (MultiDimensionalDomainSelector s : calculatedMemberMap.keySet()) {
            DimensionRole dr = olap.findUniqueEntity(cube, DimensionRole.class, s.subSelector(0, 1));
            if (dr.getMgId().longValue() == dr_1.getMgId() && selector.subSelector(1, 2).equals(s.subSelector(1, 2))) {
                return new CalculatedMember(ctx, selector, calculatedMemberMap.get(s));
            }
        }

        return null;
    }

    /**
     * 返回命名集合
     *
     * @param customSetName
     * @return
     */

    public SetPE getCustomSet/*^!?$*/(MultiDimensionalDomainSelector customSetName/*^!?$*/) {
        return namedSetMap.get(customSetName);
    }


    public List<Axis> getRealAxes/*^!?$*/() {
        if (!completed) {
            throw new OlapRuntimeException("the axes of this SFP are not complete");
        }
        return axes;
    }
}
