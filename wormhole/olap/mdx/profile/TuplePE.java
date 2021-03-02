//core_source_code!_yxIwIywIyzIx_1l1lll

        package cn.bgotech.wormhole.olap.mdx.profile;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class
/*^!*/TuplePE/*?$*//*_yxIwIywIyzIx_1l1lll*/ implements EntityPrototype<Tuple> {

    private List<MemberPE> memberPEs;

    public TuplePE(List<MemberPE> memberPEs) {
        this.memberPEs = memberPEs;
    }

    public TuplePE(MemberPE mbrPe) {
        this(new LinkedList<>(Arrays.asList(new MemberPE[]{mbrPe})));
    }

    @Override
    public Tuple evolving(MultiDimensionalVector v) {
        List<MemberRole> memberRoles = new ArrayList<>();
        for (MemberPE mpe : memberPEs) {
            memberRoles.add(mpe.evolving(v));
        }
        return new Tuple(memberRoles);
    }
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151024
    //    package com.dao.wormhole.core.mdx.production.shell;

    //    //import com.dao.wormhole.core.exception.WormholeException;
    //    //import com.dao.wormhole.core.exception.WromholeRuntimeException;
    //    //import com.dao.wormhole.core.mdx.IR.CharacterBlocksToken;
    //    //import com.dao.wormhole.core.mdx.IR.CoordinateReferenceDatum;
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //    //import com.dao.wormhole.core.model.entity.Dimension;
    //    //import com.dao.wormhole.core.model.entity.IMemberRole;
    //    //import com.dao.wormhole.core.model.entity.Member;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.entity.compositeObjects.Tuple;
    //            import com.dao.wormhole.core.model.entity.role.IMemberRole;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-7 下午4:22:23
    //     *
    //     */
    //    public class TupleShell implements /*CoordinateReferenceDatum,*/ WormholeBaseObjectShell {
    //
    //        private List<MemberShell> memberShellList = null;
    //    //	private MemberShell memberShell = null;
    //
    //        private TypeConstructorEnum typeConstructor = null;
    //
    //        private static enum TypeConstructorEnum {
    //            TUPLE__MEMBER,
    //            TUPLE__MEMBER_LIST
    //            ;
    //        }
    //
    //        public TypeConstructorEnum getTypeConstructor() {
    //            return typeConstructor;
    //        }
    //
    //        /**
    //         * tuple ::= "(" member ( "," member )* ")"
    //         *
    //         * @param memberShellList
    //         */
    //        public TupleShell(List<MemberShell> memberShellList) {
    //            this.typeConstructor = TypeConstructorEnum.TUPLE__MEMBER_LIST;
    //            this.memberShellList = memberShellList;
    //        }
    //
    //        /**
    //         * tuple ::= member
    //         *
    //         * @param ms
    //         */
    //        public TupleShell(MemberShell ms) {
    //            this.typeConstructor = TypeConstructorEnum.TUPLE__MEMBER;
    //            memberShellList = new ArrayList<MemberShell>();
    //            memberShellList.add(ms);
    //        }
    //
    //    //	@Override
    //    //	public VectorCoordinateFragment getHeadVectorCoordinateFragment() {
    //    //		List<VectorCoordinateFragment> fragments = new ArrayList<VectorCoordinateFragment>();
    //    //		for (MemberShell memberShell : this.memberShellList) {
    //    //			VectorCoordinateFragment vcf = memberShell.getHeadVectorCoordinateFragment();
    //    //			fragments.add(vcf);
    //    //		}
    //    //
    //    //		VectorCoordinateFragment returnThis = new VectorCoordinateFragment(fragments);
    //    //
    //    ////		// 校验重复
    //    ////		Set<Dimension> dimsSet = new HashSet<Dimension>();
    //    ////		for (int i = 0; i < returnThis.getMembers().size(); i++) {
    //    ////
    //    //////			List<Member> mmss = returnThis.getMembers();
    //    //////			Member mbrIndex_i = mmss.get(i);
    //    //////			Dimension ddiimm = mbrIndex_i.getDimension();
    //    ////
    //    ////			if (dimsSet.contains(returnThis.getMembers().get(i).getDimension())) {
    //    ////				throw new /*Wromhole*/RuntimeException("构成Tuple有重复的维度[" + returnThis.getMembers().get(i).getDimension().getName() + "]");
    //    ////			} else {
    //    ////				dimsSet.add(returnThis.getMembers().get(i).getDimension());
    //    ////			}
    //    ////		}
    //    //
    //    //		return returnThis;
    //    //	}
    //
    //    //	/**
    //    //	 * @deprecated 被< ANNOTATION_SET : "@set" >代替
    //    //	 * @return
    //    //	 */
    //    //	public CharacterBlocksToken getCharacterBlocksToken() { // TODO 这是什么方法？？？
    //    //		if (this.typeConstructor == TypeConstructorEnum.TUPLE__MEMBER) {
    //    //			return memberShellList.get(0).getMemberNameCharacterBlocksToken();
    //    //		}
    //    //		return null;
    //    //	}
    //
    //    //	@Override
    //    //	public /*WormholeBaseEntity*/WormholeBaseObject hatch(VectorCoordinateFragment vcf) {
    //    ////		List<Member> members = new ArrayList<Member>();
    //    //		List<IMemberRole> mrs = new ArrayList<IMemberRole>();
    //    //		switch (typeConstructor) {
    //    //		case TUPLE__MEMBER:
    //    //
    //    //		case TUPLE__MEMBER_LIST:
    //    //			for (MemberShell ms : memberShellList) {
    //    //				/*members.add((Member) ms.hatch(vectorCoordinateFragment));*/
    //    //				mrs.add((IMemberRole) ms.hatch(vcf));
    //    //			}
    //    //			return new Tuple(mrs/*members*/);
    //    //		default:
    //    //			throw new /*Wromhole*/RuntimeException("没有匹配上的构造方法[" + typeConstructor + "]");
    //    //		}
    //    //	}
    //
                                                                                                    //        @Override
                                                                                                    //        public WormholeBaseObject hatch(MultidimensionalVector vector) {
                                                                                                    //            List<IMemberRole> mrs = new ArrayList<IMemberRole>();
                                                                                                    //            switch (typeConstructor) {
                                                                                                    //                case TUPLE__MEMBER:
                                                                                                    //
                                                                                                    //                case TUPLE__MEMBER_LIST:
                                                                                                    //                    for (MemberShell ms : memberShellList) {
                                                                                                    //                        WormholeBaseObject wbo = ms.hatch(vector);
                                                                                                    //                        IMemberRole imr = (IMemberRole) wbo;
                                                                                                    //                        mrs.add(imr);
                                                                                                    //                    }
                                                                                                    //                    return new Tuple(mrs);
                                                                                                    //                default:
                                                                                                    //                    throw new RuntimeException("没有匹配上的构造方法[" + typeConstructor + "]");
                                                                                                    //            }
                                                                                                    //        }
    //
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
