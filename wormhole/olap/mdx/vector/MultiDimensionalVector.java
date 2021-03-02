//core_source_code!_yxIwIyyIyfInz_1lll1l

        package cn.bgotech.wormhole.olap.mdx.vector;

import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mddm.physical.member.CalculatedMember;
import cn.bgotech.wormhole.olap.mddm.physical.member.MeasureMember;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;

import java.util.*;

/**
 * 多维向量
 * 表示多维空间逻辑模型中的一个维度成员角色列表，长度固定，每个成员角色所属的维度角色不能改变。
 * 多维向量对象与具体Cube无关，能否在具体Cube上求值取决于多维向量与Cube的维度角色是否一一对应。
 * 维度成员角色列表的顺序决定了计算 {@link CalculatedMember} 时的汇总优先级，
 * 列表中越靠前的成员角色汇总优先级越'高'(也可表述为汇总优先级'远'，较'低'或'近'优先级而言，先进行汇总)。
 *
 * Created by ChenZhiGang on 2017/6/4.
 */
public class
/*^!*/MultiDimensionalVector/*?$*//*_yxIwIyyIyfInz_1lll1l*/ {

    public enum ConstructedType {
        EMBED, // 嵌入式构造
        APPEND // 追加式构造
    }

    private List<MemberRole> mrs;

    /**
     *
     * @param vector
     * @param tuple
     * @param type nullable
     */
    public MultiDimensionalVector(MultiDimensionalVector vector, Tuple tuple, ConstructedType type) {
        this(vector, Arrays.asList(tuple), type);
    }

    public MultiDimensionalVector(MultiDimensionalVector vector, List<Tuple> tupleList, ConstructedType type) {

        // TODO: 当构造类型参数为空时，应有系统配置给出默认的构造类型，系统配置尚未实现，暂时以嵌入式构造作为默认类型
        type = type != null ? type : ConstructedType.EMBED;

        switch (type) {
            case EMBED:
                embedConstructor(vector, tupleList);
                break;
            case APPEND:
                appendConstructor(vector, tupleList);
                break;
            default:
                throw new Error("strange error !");
        }
    }

    public MultiDimensionalVector(List<MemberRole> mrs) {
        this.mrs = mrs;
    }

    public void addMemberRole(MemberRole mr) {
        (mrs = (mrs == null ? new LinkedList<>() : mrs)).add(mr);
    }

    /**
     * 嵌入式构造方法：
     *
     * for (成员角色 mr : tuple) {
     *     if (vector中存在与mr的维度角色相同的成员角色) {
     *         用mr替换vector中的成员角色
     *     } else {
     *         TODO: 理论上vector中必须存在某个成员角色，其对应的维度角色与mr对应的相同
     *     }
     * }
     *
     * @param vector
     * @param tupleList
     */

    private void embedConstructor/*^!?$*/(MultiDimensionalVector vector, List<Tuple> tupleList) {
        mrs = new ArrayList<>(vector.mrs);
        List<MemberRole> tupleMemberRoles;
        for (Tuple t : tupleList) {
            tupleMemberRoles = t.getMemberRoles();
            tupleMemberRolesLoop:
            for (int i = 0; i < tupleMemberRoles.size(); i++) {
                for (int j = 0; j < mrs.size(); j++) {
                    if (tupleMemberRoles.get(i).getDimensionRole().equals(mrs.get(j).getDimensionRole())) {
                        mrs.set(j, tupleMemberRoles.get(i));
                        continue tupleMemberRolesLoop;
                    }
                }
                throw new OlapRuntimeException("Can not find the corresponding MemberRole");
            }
        }

        //mrs = new ArrayList<>(vector.mrs);
        //List<MemberRole> tupleMemberRoles = tuple.getMemberRoles();
        //tupleMemberRolesLoop:
        //for (int i = 0; i < tupleMemberRoles.size(); i++) {
        //    for (int j = 0; j < mrs.size(); j++) {
        //        if (tupleMemberRoles.get(i).getDimensionRole().equals(mrs.get(j).getDimensionRole())) {
        //            mrs.set(j, tupleMemberRoles.get(i));
        //            break tupleMemberRolesLoop;
        //        }
        //    }
        //    throw new OlapRuntimeException("Can not find the corresponding MemberRole");
        //}

// TODO: Delete the comments below at later.
//        for (int i = 0; i < mrs.size(); i++) {
//            tmrs = tuple.getMemberRoles();
//            for (int j = 0; j < tmrs.size(); j++) {
//                if (tuple.getMemberRoles().get(j).getDimensionRole().equals(mrs.get(i).getDimensionRole())) {
//                    mrs.set(i, tmrs.get(j));
//                    break;
//                } else {
//                    throw new OlapRuntimeException("Can not find the corresponding MemberRole");
//                }
//            }
//        }
    }

    /**
     * 追加式构造方法：
     *
     * vector:[{dim1_rA.m?},{dim1_rB.m?},{dim2_rQ.m?},{dim2_rW.m?},{dim2_rE.m?},{dim3_rJ.m?},{dim3_rK.m?},{dim3_rL.m?},{dim4_rX.m?}]
     * tuple: [{dim3_rL.m?},{dim2_rW.m?},{dim4_rX.m?},{dim1_rA.m?}]
     *
     *
     * vector:[			   ,{dim1_rB.m?},{dim2_rQ.m?},			  ,{dim2_rE.m?},{dim3_rJ.m?},{dim3_rK.m?},			  ,			   ]
     * tuple: [{dim3_rL.m?},{dim2_rW.m?},{dim4_rX.m?},{dim1_rA.m?}]
     *
     *
     * vector:[{dim1_rB.m?},{dim2_rQ.m?},{dim2_rE.m?},{dim3_rJ.m?},{dim3_rK.m?}]
     * tuple: 																   [{dim3_rL.m?},{dim2_rW.m?},{dim4_rX.m?},{dim1_rA.m?}]
     *
     *
     * vector:[{dim1_rB.m?},{dim2_rQ.m?},{dim2_rE.m?},{dim3_rJ.m?},{dim3_rK.m?},{dim3_rL.m?},{dim2_rW.m?},{dim4_rX.m?},{dim1_rA.m?}]
     *
     * TODO: 其他情况时，tuple可能包含vector中不存在的维度角色，理论上不应出现此情况？目前暂时先不做校验。
     *
     * @param vector
     * @param tupleList
     */

    private void appendConstructor/*^!?$*/(MultiDimensionalVector vector, List<Tuple> tupleList) {
        mrs = new ArrayList<>(vector.mrs);
        for (Tuple t : tupleList) {
            for (int i = mrs.size() - 1; i >= 0; i--) {
                for (int j = 0; j < t.getMemberRoles().size(); j++) {
                    if (t.getMemberRoles().get(j).getDimensionRole().equals(mrs.get(i).getDimensionRole())) {
                        mrs.remove(i);
                        break;
                    }
                }
            }
            mrs.addAll(t.getMemberRoles());
        }
    }

    @Override
    public String toString() {
        StringBuilder ids = new StringBuilder();
        Member m;
        for (int i = 0; i < mrs.size(); i++) {
            m = mrs.get(i).getMember();
            ids.append(m instanceof CalculatedMember ? CalculatedMember.class.getSimpleName() : m.getMgId());
            if (i < mrs.size() - 1)
                ids.append(",");
        }
        return ids.toString();
    }


    public MemberRole get/*^!?$*/(int i) {
        return mrs.get(i);
    }


    public MemberRole findMemberRole/*^!?$*/(DimensionRole dr) {
        for (MemberRole mr : mrs) {
            if (mr.getDimensionRole().equals(dr)) {
                return mr;
            }
        }
        return null;
    }


    public List<MemberRole> getMemberRoles/*^!?$*/() {
        return mrs; // TODO: Should return an immutable list
    }

    /**
     * @return
     */

    public MemberRole popupMeasure/*^!?$*/() {
        for (int i = 0; i < mrs.size(); i++) {
            if (mrs.get(i).getMember() instanceof MeasureMember) {
                return mrs.remove(i);
            }
        }
        throw new OlapRuntimeException("have no MemberRole belong MeasureDimension");
    }


    public boolean includeCalculatedMembers/*^!?$*/() {
        for (MemberRole mr : mrs) {
            if (mr.getMember() instanceof CalculatedMember) {
                return true;
            }
        }
        return false;
    }


    public boolean includeNonexistentMember/*^!?$*/() {
        for (MemberRole mr : mrs)
            if (Member.NONEXISTENT_MEMBER.equals(mr.getMember()))
                return true;
        return false;
    }

    /**
     *
     * @return
     */

    public CalculatedMember getNearestCalculatedMember/*^!?$*/() throws OlapException {
        for (int i = mrs.size() - 1; i >= 0; i--) {
            if (mrs.get(i).getMember() instanceof CalculatedMember) {
                return (CalculatedMember) mrs.get(i).getMember();
            }
        }
        throw new OlapException("the vector have no CalculatedMember");
    }


    public MultiDimensionalVector sort/*^!?$*/(Comparator<MemberRole> c) {
        Collections.sort(mrs, c);
        return this;
    }


    public boolean hasNonexistentMemberRole/*^!?$*/() {
        for (MemberRole mr : mrs) {
            if (!mr.getMember().isExistentMember()) {
                return true;
            }
        }
        return false;
    }


    public List<MemberRole> getDateMemberRoles/*^!?$*/() {
        throw new OlapRuntimeException("No program available"); // TODO:
//            List<IMemberRole> dateMemberRoles = new ArrayList<IMemberRole>();
//            for (IMemberRole mr : this.mrs) {
//                if (mr.getMember() instanceof DateMember) {
//                    dateMemberRoles.add(mr);
//                }
//            }
//            return dateMemberRoles;
    }
                                                                                                    //        }
                                                                                                    //
//        /**
//         * 是否存在计算公式成员
//         * @return
//         */
//        public boolean isCalculateVector() {
//            for (int i = 0; i < this.mrs.size(); i++) {
//                if (mrs.get(i).getMember() instanceof CustomFormulaMember) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        /**
//         * 如果多维向量中存在自定义维度成员，数据桥对象将调用次方法计算多维向量值
//         * @param brg
//         * @return
//         */
//        public WormholeBasicData vectorValue(WormholeDataBridge brg, Cube cube) {
//            for (int i = mrs.size() - 1; i >= 0 ; i--) {
//                if (mrs.get(i).getMember() instanceof CustomFormulaMember) {
//    //				returnThis = ((CustomFormulaMember)mrs.get(i).getMember()).getExpression().wormholeValue(vcf);
//                    return ((CustomFormulaMember) mrs.get(i).getMember()).getExpression().evaluate(this);
//                }
//            }
//            WormholeLog.WARN("当前多维向量没有自定义维度成员，请查看是否误调用此方法");
//            return brg.vectorValue(cube, this);
//        }
//
//        @Override
//        public int hashCode() {
//            return this.mrs.hashCode();
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null || !(obj instanceof MultidimensionalVector)) {
//                return false;
//            }
//            return this.mrs.equals(((MultidimensionalVector)obj).mrs);
//        }
//
//    }

}