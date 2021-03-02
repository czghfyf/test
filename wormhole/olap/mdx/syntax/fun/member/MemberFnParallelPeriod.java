//core_source_code!_yxIwIywIyzIx_1111l

        package cn.bgotech.wormhole.olap.mdx.syntax.fun.member;

import cn.bgotech.wormhole.olap.component.MddmStorageService;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.LevelRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.profile.LevelPE;
import cn.bgotech.wormhole.olap.mdx.profile.MemberPE;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.List;

/**
 * Created by ChenZhiGang on 2018/2/23.
 */
public class
/*^!*/MemberFnParallelPeriod/*?$*//*_yxIwIywIyzIx_1111l*/ implements MemberFunction {

    private ContextAtExecutingMDX context;

    private LevelPE levelPE;

    private Expression moveForwardExp;

    private MemberPE memberPE;

    public MemberFnParallelPeriod(ContextAtExecutingMDX context, LevelPE levelPE, Expression moveForwardExp, MemberPE memberPE) {
        this.context = context;
        this.levelPE = levelPE;
        this.moveForwardExp = moveForwardExp;
        this.memberPE = memberPE;
    }

    @Override
    public MemberRole evolving(MultiDimensionalVector v) {

        LevelRole levelRole;
        BasicNumeric moveForward;
        MemberRole memberRole;

        if (memberPE != null) { // 解析: ParallelPeriod(level, index, member)
            memberRole = memberPE.evolving(v);
            moveForward = (BasicNumeric) moveForwardExp.evaluate(v);
            levelRole = (LevelRole) levelPE.evolving(v);
        } else if (moveForwardExp != null) { // 解析: ParallelPeriod(level, index)
            moveForward = (BasicNumeric) moveForwardExp.evaluate(v);
            levelRole = (LevelRole) levelPE.evolving(v);
            memberRole = v.findMemberRole(levelRole.getDimensionRole());
        } else if (levelPE != null) { // 解析: ParallelPeriod(level)
            moveForward = new BasicNumeric(1D);
            levelRole = (LevelRole) levelPE.evolving(v);
            memberRole = v.findMemberRole(levelRole.getDimensionRole());
        } else { // 解析: ParallelPeriod()
            List<MemberRole> memberRoles = v.getDateMemberRoles();
            if (memberRoles.size() != 1) {
                throw new RuntimeException("Date dimension has more than one role");
            } else {
                memberRole = memberRoles.get(0);
                moveForward = new BasicNumeric(1D);
                levelRole = new LevelRole(memberRole.getDimensionRole(), memberRole.getMember().getLevel().getAboveLevel());
            }
        }
        return executeParallelPeriod(levelRole, moveForward, memberRole);
    }

    private MemberRole executeParallelPeriod(LevelRole lr, BasicNumeric idxNum, MemberRole mr) {

        MddmStorageService modelService = context.getOLAPEngine().getMddmStorageService();

        // 获得在level级别上的member祖先成员
        Member ancestorMember = modelService.ancestorMemberAtLevel(lr.getLevel(), mr.getMember());

        // 获得祖先成员的兄弟成员
        Member ancestorBrother = ancestorMember.getBrotherMember((int) idxNum.doubleValue().doubleValue());

        // 获得当前Member相对于某祖先成员的位置
        List<Integer> relativePosition = mr.getMember().relativePosition(ancestorMember);

        return new MemberRole(lr.getDimensionRole(), ancestorBrother.moveRelativePosition(relativePosition));
    }
}
