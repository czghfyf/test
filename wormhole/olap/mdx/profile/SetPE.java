//core_source_code!_yxIwIywIyzIx_1l1ll1

        package cn.bgotech.wormhole.olap.mdx.profile;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.syntax.fun.set.SetFunction;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class
/*^!*/SetPE/*?$*//*_yxIwIywIyzIx_1l1ll1*/ implements EntityPrototype<Set> {

    private enum TypeConstructorEnum {
        SET__TUPLE,         // set ::= tuple
        SET__MEMBER_MEMBER, // set ::= member ":" member
        SET__SET_LIST,      // set ::= "{" set ( "," set )* "}"
        SET__SET_FUN,       // set ::= set_functions
        SET__SET_NAME_SPEC  // set ::= set_name_spec
    }

    private TypeConstructorEnum typeConstructor;

    private ContextAtExecutingMDX ctx;
    private TuplePE tuplePE;
    private List<SetPE> setPEs;
    private SetFunction setFun;
    
    private MemberPE memberPEBegin;
    private MemberPE memberPEEnd;

    private MultiDimensionalDomainSelector customSetSelector;

    /**
     * set ::= tuple
     * @param ctx
     * @param tuplePE
     */
    public SetPE(ContextAtExecutingMDX ctx, TuplePE tuplePE) {
        this.ctx = ctx;
        this.tuplePE = tuplePE;
        typeConstructor = TypeConstructorEnum.SET__TUPLE;
    }

    /**
     * set ::= "{" set ( "," set )* "}"
     * @param ctx
     * @param setPEs
     */
    public SetPE(ContextAtExecutingMDX ctx, List<SetPE> setPEs) {
        this.ctx = ctx;
        this.setPEs = setPEs;
        typeConstructor = TypeConstructorEnum.SET__SET_LIST;
    }

    /**
     * set ::= set_functions
     * @param ctx
     * @param setFun
     */
    public SetPE(ContextAtExecutingMDX ctx, SetFunction setFun) {
        this.ctx = ctx;
        this.setFun = setFun;
        typeConstructor = TypeConstructorEnum.SET__SET_FUN;
    }

    /**
     * set ::= set_name_spec
     *
     * @param ctx_
     * @param customSetName
     */
    public SetPE(ContextAtExecutingMDX ctx_, MultiDimensionalDomainSelector customSetName) {
        typeConstructor = TypeConstructorEnum.SET__SET_NAME_SPEC;
        ctx = ctx_;
        customSetSelector = customSetName;
    }

    /**
     * set ::= member ":" member
     *
     * @param ctx_
     * @param begin
     * @param end
     */
    public SetPE(ContextAtExecutingMDX ctx_, MemberPE begin, MemberPE end) {
        typeConstructor = TypeConstructorEnum.SET__MEMBER_MEMBER;
        ctx = ctx_;
        memberPEBegin = begin;
        memberPEEnd = end;
    }

    @Override
    public Set evolving(MultiDimensionalVector v) {
        switch (this.typeConstructor) {
            case SET__MEMBER_MEMBER:
                MemberRole beginMemberRole = memberPEBegin.evolving(v);
                MemberRole endMemberRole = memberPEEnd.evolving(v);

                Member bm = beginMemberRole.getMember();
                Member em = endMemberRole.getMember();

                if (!bm.getDimension().equals(em.getDimension())) {
                    throw new RuntimeException("不是同一维度的维度成员:" + bm + "; " + em);
                }
                List<Member> members = (List<Member>) bm.getDimension().getAllMembers();
                if (bm.compareTo(em) >= 0) {
                    Member temp = bm;
                    bm = em;
                    em = temp;
                }
                for (int i = members.size() - 1; i >= 0; i--) {
                    if (members.get(i).compareTo(bm) < 0 || members.get(i).compareTo(em) > 0) {
                        members.remove(i);
                    }
                }
                Collections.sort(members);

                List<MemberRole> memberRoles = new ArrayList<>();
                for (int i = 0; i < members.size(); i++) {
                    memberRoles.add(new MemberRole(endMemberRole.getDimensionRole(), members.get(i)));
                }
                return new Set(memberRoles);
            case SET__SET_FUN:
                return setFun.evolving(v);
            case SET__SET_LIST:
                List<Set> setList = new ArrayList<>();
                for (SetPE setPe : this.setPEs) {
                    setList.add(setPe.evolving(v));
                }
                return new Set(new ArrayList<BasicEntityModel>(setList));
            case SET__TUPLE:
                return new Set(this.tuplePE.evolving(v));
            case SET__SET_NAME_SPEC:
                return ctx.getCustomSet(customSetSelector).evolving(v);
            default:
                throw new OlapRuntimeException("undefined constructor type: " + this.typeConstructor.toString());
        }
    }

}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    package com.dao.wormhole.core.mdx.production.shell;
    //
    //    public class SetShell implements /*CoordinateReferenceDatum,*/ ContextProduction, WormholeBaseObjectShell {
    //
    //    //	@Override
    //    //	public VectorCoordinateFragment getHeadVectorCoordinateFragment() {
    //    ////		switch (this.typeConstructor) {
    //    ////		case SET__MEMBER_MEMBER:
    //    ////			return beginMbrShell.getHeadVectorCoordinateFragment();
    //    ////
    //    ////		default:
    //    ////			throw new /*Wromhole*/RuntimeException("未知的构造方法类型:" + this.typeConstructor.toString());
    //    ////		}
    //    //
    //    //		Set set = (Set) this.hatch(null);
    //    //		if (set != null) {
    //    //			return new VectorCoordinateFragment(set.getTuple(0));
    //    //		}
    //    //		return null;
    //    //	}
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
