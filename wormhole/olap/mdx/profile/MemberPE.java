//core_source_code!_yxIwIywIyzIx_1l1l1l

        package cn.bgotech.wormhole.olap.mdx.profile;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.member.CalculatedMember;
//import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.syntax.fun.member.MemberFunction;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.SelectedByMDDAble;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

public class
/*^!*/MemberPE/*?$*//*_yxIwIywIyzIx_1l1l1l*/ implements EntityPrototype<MemberRole>, SelectedByMDDAble {

    private ContextAtExecutingMDX ctx;
    private MultiDimensionalDomainSelector mbrTrail;
    private MemberFunction memberFun;

    public MemberPE(ContextAtExecutingMDX ctx, MultiDimensionalDomainSelector mbrTrail) {
        this.ctx = ctx;
        this.mbrTrail = mbrTrail;
    }

    public MemberPE(ContextAtExecutingMDX ctx, MemberFunction memberFun) {
        this.ctx = ctx;
        this.memberFun = memberFun;
    }

    @Override
    public MemberRole evolving(MultiDimensionalVector v) {
        if (mbrTrail != null) {
            MemberRole mr = ctx.getOLAPEngine().findUniqueEntity(ctx.getCube(), MemberRole.class, mbrTrail);
            if (mr != null) {
                return mr;
            }
            // current MemberPE object is represents a CalculatedMember object
            CalculatedMember cm = ctx.getCalculatedMember(mbrTrail);
            if (cm != null) {
                return cm.getSelfRole();
            }
            throw new OlapRuntimeException("wrong definition");
        } else if (memberFun != null) {
            return this.memberFun.evolving(v);
        }
        throw new OlapRuntimeException("this MemberPE object is not complete");
    }

    @Override
    public MultiDimensionalDomainSelector getMDDSelector() {
        return mbrTrail;
    }
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151024
    //    package com.dao.wormhole.core.mdx.production.shell;
    //
    //    //import com.dao.wormhole.core.app.WormholeApplication;
    //    //import com.dao.wormhole.core.exception.WromholeRuntimeException;
    //            import com.dao.wormhole.core.mdx.IR.CharacterBlocksToken;
    //    //import com.dao.wormhole.core.mdx.IR.CoordinateReferenceDatum;
    //            import com.dao.wormhole.core.mdx.parser.context.AnalyticalContext;
    //            import com.dao.wormhole.core.mdx.production.ContextProduction;
    //            import com.dao.wormhole.core.mdx.production.MemberFunction;
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //            import com.dao.wormhole.core.model.entity.CustomFormulaMember;
    //    //import com.dao.wormhole.core.model.entity.role.IDimensionRole;
    //            import com.dao.wormhole.core.model.entity.role.IMemberRole;
    //    //import com.dao.wormhole.core.model.entity.IMemberRole;
    //    //import com.dao.wormhole.core.model.entity.MemberRole;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //    //import com.dao.wormhole.core.model.entity.Member;
    //    //import com.dao.wormhole.core.model.entity.compositeObjects.Set;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.services.ModelService;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-7 下午4:21:31
    //     *
    //     */
    //    public class MemberShell implements /*CoordinateReferenceDatum,*/ ContextProduction, WormholeBaseObjectShell {
    //
    //        private AnalyticalContext context = null;
    //        private TypeConstructorEnum typeConstructor = null;
    //
    //        private CharacterBlocksToken memberNameToken = null;
    //
    //        private MemberFunction memberFun = null;
    //
    //        private static enum TypeConstructorEnum {
    //            MEMBER__NAME_SPEC, // member ::= member_name_spec
    //            MEMBER__FUNCTION   // member ::= member_functions
    //            ;
    //        }
    //
    //        /**
    //         * member ::= member_name_spec
    //         *
    //         * @param memberNameToken
    //         */
    //        public MemberShell(CharacterBlocksToken memberNameToken) {
    //            typeConstructor = TypeConstructorEnum.MEMBER__NAME_SPEC;
    //            this.memberNameToken = memberNameToken;
    //        }
    //
    //        /**
    //         * member ::= member_functions
    //         *
    //         * @param memberFun
    //         */
    //        public MemberShell(MemberFunction memberFun) {
    //            typeConstructor = TypeConstructorEnum.MEMBER__FUNCTION;
    //            this.memberFun = memberFun;
    //        }
    //
    //    //	@Override
    //    //	public VectorCoordinateFragment getHeadVectorCoordinateFragment() {
    //    ////		switch (typeConstructor) {
    //    ////		case MEMBER__NAME_SPEC:
    //    ////			Member member = this.context.getCustomDefMember(this.memberNameToken);
    //    ////			if (member == null) {
    //    //////				WormholeApplication wormholeApp = context.getWormholeApp();
    //    ////				ModelService modelService = context.getWormholeApp().getModelService();
    //    ////				member = (Member) modelService.findUniqueWormholeEntity(this.memberNameToken, context.getExecuteCube());
    //    ////			}
    //    ////			if (member == null) { // 如果 member 还是为 null 那么程序就要抛出异常
    //    ////				throw new /*Wromhole*/RuntimeException("无法建造 MemberShell 的默认 vcf, " + this.memberNameToken + " 既不是持久层实体对象也不是计算公式成员");
    //    ////			}
    //    ////			return new VectorCoordinateFragment(member);
    //    ////		case MEMBER__FUNCTION:
    //    ////			return this.memberFun.getHeadVectorCoordinateFragment();
    //    ////		default:
    //    ////			throw new /*Wromhole*/RuntimeException("未知的构造方法类型:" + this.typeConstructor.toString());
    //    ////		}
    //    //
    //    //		IMemberRole mr = (IMemberRole) this.hatch(null);
    //    //		if (mr != null) {
    //    //			return new VectorCoordinateFragment(mr);
    //    //		}
    //    //		return null;
    //    //	}

    //        public CharacterBlocksToken getMemberNameCharacterBlocksToken() {
    //    //		if (typeConstructor == TypeConstructorEnum.MEMBER__NAME_SPEC) {
    //    //			return this.memberNameToken;
    //    //		}
    //    //		return null;
    //
    //            return this.memberNameToken;
    //        }
    //
    //    //	@Override
    //    //	public /*WormholeBaseEntity*/WormholeBaseObject hatch(VectorCoordinateFragment vcf) {
    //    //		switch (typeConstructor) {
    //    //		case MEMBER__NAME_SPEC:
    //    //			ModelService modelService = this.context.getWormholeApp().getModelService();
    //    //			IMemberRole mr = (IMemberRole) modelService.findUniqueWormholeEntity(memberNameToken, this.context.getExecuteCube());
    //    //
    //    //			// 可能是一个自定义计算公式成员
    //    //			CustomFormulaMember cfm = null;
    //    //			if (mr == null) {
    //    //				cfm = this.context.getCustomDefMember(memberNameToken);
    //    //			}
    //    //
    //    //			if (cfm == null) {
    //    //				throw new /*Wromhole*/RuntimeException(memberNameToken.getImage() + " 不代表一个持久层维度成员, 也不是一个自定义计算公式成员");
    //    //			}
    //    //
    //    //			return mr == null ? cfm : mr;
    //    //		case MEMBER__FUNCTION:
    //    //			return this.memberFun.hatch(vcf);
    //    //		default:
    //    //			throw new /*Wromhole*/RuntimeException(this.getClass().getName() + ", 错误的构造方法类型: " + typeConstructor);
    //    //		}
    //    //	}
    //
                                                                                                //        @Override
                                                                                                //        public WormholeBaseObject hatch(MultidimensionalVector vector) {
                                                                                                //            switch (typeConstructor) {
                                                                                                //                case MEMBER__NAME_SPEC:
                                                                                                //                    ModelService modelService = this.context.getWormholeApp().getModelService();
                                                                                                //                    IMemberRole mr = (IMemberRole) modelService.findUniqueWormholeEntity(memberNameToken, this.context.getExecuteCube());
                                                                                                //
                                                                                                //                    if (mr != null) {
                                                                                                //                        return mr;
                                                                                                //                    }
                                                                                                //
                                                                                                //                    // 可能是一个自定义计算公式成员
                                                                                                //                    CustomFormulaMember cfm = this.context.getCustomDefMember(memberNameToken);
                                                                                                //                    if (cfm == null) {
                                                                                                //                        throw new /*Wromhole*/RuntimeException(memberNameToken.getImage() + " 不代表一个持久层维度成员, 也不是一个自定义计算公式成员");
                                                                                                //                    } else {
                                                                                                //                        return cfm.getSelfRole();
                                                                                                //                    }
                                                                                                //
                                                                                                //
                                                                                                //    //			if (mr == null) {
                                                                                                //    //				cfm = this.context.getCustomDefMember(memberNameToken);
                                                                                                //    //				mr = cfm.getSelfRole();
                                                                                                //    //			}
                                                                                                //    //
                                                                                                //    ////			if (mr == null && cfm == null) {
                                                                                                //    ////				throw new /*Wromhole*/RuntimeException(memberNameToken.getImage() + " 不代表一个持久层维度成员, 也不是一个自定义计算公式成员");
                                                                                                //    ////			}
                                                                                                //    //
                                                                                                //    ////			return mr == null ? cfm : mr;
                                                                                                //    //			return mr;
                                                                                                //
                                                                                                //                case MEMBER__FUNCTION:
                                                                                                //                    return this.memberFun.hatch(vector);
                                                                                                //                default:
                                                                                                //                    throw new /*Wromhole*/RuntimeException(this.getClass().getName() + ", 错误的构造方法类型: " + typeConstructor);
                                                                                                //            }
                                                                                                //        }
    //
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
