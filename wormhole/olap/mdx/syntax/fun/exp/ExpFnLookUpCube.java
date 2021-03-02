//core_source_code!_yxIwIywIyzIx_1ll1l1

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.exp;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.SFPOfExecuteMDX;
import cn.bgotech.wormhole.olap.mdx.parser.ParseException;
import cn.bgotech.wormhole.olap.mdx.parser.WormholeMDXParser;
import cn.bgotech.wormhole.olap.mdx.profile.CubePE;
import cn.bgotech.wormhole.olap.mdx.profile.TuplePE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by ChenZhiGang on 2018/2/17.
 */
public class
/*^!*/ExpFnLookUpCube/*?$*//*_yxIwIywIyzIx_1ll1l1*/ implements ExpressionFunction {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ContextAtExecutingMDX ctx;

    private Expression cubeExp;
    private CubePE cubePE;

    private boolean parseExpFlag = true;
    private Expression exp;

    private Expression specifiedContextExp;
    private TuplePE tuplePE;

    // for parse MDX fragment in observedCube scope
    private ContextAtExecutingMDX observedCubeContext;

    public ExpFnLookUpCube(ContextAtExecutingMDX ctx) {
        this.ctx = ctx;
    }

    public void setCubeExp(Expression exp) {
        cubeExp = exp;
    }

    public void setCubePE(CubePE cubePe) {
        cubePE = cubePe;
    }

    public void setParseExpFlag(boolean b) {
        parseExpFlag = b;
    }

    public void setExp(Expression _exp) {
        exp = _exp;
    }

    public void setSpecifiedContextExp(Expression exp) {
        specifiedContextExp = exp;
    }

    public void setTuplePE(TuplePE tuplePe) {
        tuplePE = tuplePe;
    }

    /**
     * @param outsideVector - The context MultiDimensionalVector of the external MDX when the LookUpCube function is called
     * @return
     */
    @Override
    public BasicData evaluate(MultiDimensionalVector outsideVector) {

        Cube cube = _findCube(outsideVector);

        observedCubeContext = new ContextAtExecutingMDX(OlapEngine.hold()).set(new SFPOfExecuteMDX() {
            @Override
            public Cube getCube() {
                return cube;
            }

            @Override
            public MultiDimensionalVector getSessionDefaultVector() {
                return null;
            }
        });

        MultiDimensionalVector currentCubeVector = mappingVector(outsideVector, cube);

        Tuple specifiedContextTuple = _generateSpecifiedContextTuple(outsideVector, currentCubeVector);

        if (specifiedContextTuple != null) {
            currentCubeVector = new MultiDimensionalVector(currentCubeVector, specifiedContextTuple, null);
        }

        if (parseExpFlag) {
            WormholeMDXParser parser4exp = new WormholeMDXParser(exp.evaluate(outsideVector).image(), OlapEngine.hold());
            try {
                exp = parser4exp.expression(observedCubeContext);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
                throw new OlapRuntimeException(e);
            }
        }

        return exp.evaluate(currentCubeVector);

    }

    private Tuple _generateSpecifiedContextTuple
            (MultiDimensionalVector outsideVector, MultiDimensionalVector currentCubeVector) {

        if (specifiedContextExp != null) {
            WormholeMDXParser parser4tuple =
                    new WormholeMDXParser(specifiedContextExp.evaluate(outsideVector).image(), OlapEngine.hold());
            try {
                tuplePE = parser4tuple.tuple(observedCubeContext);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
                throw new OlapRuntimeException(e);
            }
        }
        if (tuplePE != null) {
            return tuplePE.evolving(currentCubeVector);
        }
        return null;

    }

    private Cube _findCube(MultiDimensionalVector outsideVector) {
        if (cubeExp != null) {
            return (Cube) OlapEngine.hold().find(Cube.class, cubeExp.evaluate(outsideVector).image()).get(0);
        } else { // cubePE != null
            return cubePE.evolving(null);
        }
    }

    /**
     * 使用外部上下文的多维向量，对observedCube默认vector进行设置
     * 按聚集优先级由高至低对outsideVector进行遍历，如果遍历点MemberRole所在的DimensionRole未与observedCube关联则将其忽略，
     * 如果关联到observedCube则将其作为最低优先级替换到observedCube默认vector上。
     * <p>
     * 关联规则：// TODO: 修改为更智能的关联规则判定逻辑
     * 1.维度角色名相同
     * 2.维度相同
     * <p>
     * 示例：
     * observedCube默认vector: [ {dim1_rC.m1}, {dim1_rB.m4}, {dim3_rA.m5} ]
     * outsideVector: [ {dim1_rA.m1} #无关联#, {dim1_rB.m2} #替换dim1_rB.m4#, {dim2_rC.m3} #无关联# ]
     * 返回: [ {dim1_rC.m1}, {dim3_rA.m5}, {dim1_rB.m2} ]
     *
     * @param outsideVector - The context MultiDimensionalVector of the external MDX when the LookUpCube function is called
     * @param observedCube  - Cube specified in the LookUpCube function
     * @return
     */
    private MultiDimensionalVector mappingVector(MultiDimensionalVector outsideVector, Cube observedCube) {

        List<MemberRole> ovMemberRoles = outsideVector.getMemberRoles();

        List<MemberRole> observedCubeMRs = observedCube.getDefaultVector().getMemberRoles();

        for (int i = ovMemberRoles.size() - 1; i >= 0; i--) {
            MemberRole mr = ovMemberRoles.get(i);
            DimensionRole relatedDimensionRole = observedCube.getDimensionRole(mr.getDimensionRole().getDimension(), mr.getDimensionRole().getName());
            if (relatedDimensionRole != null) {
                for (int j = 0; j < observedCubeMRs.size(); j++) {
                    if (observedCubeMRs.get(j).getDimensionRole().equals(relatedDimensionRole)) {
                        observedCubeMRs.remove(j);
                        break;
                    }
                }
                observedCubeMRs.add(new MemberRole(relatedDimensionRole, mr.getMember()));
            }
        }

        return new MultiDimensionalVector(observedCubeMRs);
    }

}
