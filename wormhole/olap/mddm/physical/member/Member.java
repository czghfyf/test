package cn.bgotech.wormhole.olap.mddm.physical.member;

import cn.bgotech.wormhole.olap.mddm.physical.ClassicMDDM;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface Member extends ClassicMDDM, Comparable<Member> {

    Member NONEXISTENT_MEMBER = new NonexistentMember();

    Member getParent();

    Dimension getDimension();

    boolean isRoot();

    Hierarchy getHierarchy();

    /**
     * if this one is super root member, return null
     *
     * @return
     */
    Level getLevel();


    /**
     * 根据 descendantId 找到 Member对象，如果 Member对象是当前调用此方法的成员对象的后代，返回 Member 对象
     * 否则返回 null
     *
     * @param descendantId
     * @return
     */
    Member findDescendantMember(long descendantId);

    /**
     * 获得 member 全部后代成员列表，并用 descendantName 过滤
     * 保留列表中 level 最高的成员
     * 如果列表长度为 1，返回 get(0)
     * 如果列表长度为 0，返回 null
     * 如果列表长度大于 1，报错 "当前维度成员具有多个距离最近且名为 descendantName 的后代成员，无法确定选择哪个"
     *
     * @param descendantName
     * @return
     */
    Member findNearestDescendantMember(String descendantName);

    /**
     * if m is ancestor of the current object, return true
     * @param m
     * @return
     */
    boolean hasAncestor(Member m);

    default List<Member> findAncestors() {
        if (getParent() != null) {
            List<Member> pas = getParent().findAncestors();
            pas.add(getParent());
            return pas;
        }
        return new ArrayList<>();
    }

    boolean isMeasure();

    boolean isLeaf();

    /**
     * 获得向前offset个位置的兄弟成员
     * offset > 0，往前找
     * offset = 0，自己
     * offset < 0，往后找
     *
     * @param offset
     * @return
     */
    Member getBrotherMember(int offset);

    @Override
    Member selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector);

    /**
     * If this method returns false, that the current dimension member does not exist,
     * the nonexistent member will be used, for example: find the parent of the root member
     * @return
     */
    default boolean isExistentMember() {
        return true;
    }

    /**
     * 获得当前维度成员相对于某祖先成员的位置
     *
     * Maaa
     *  ├──Mddd
     *  │   ├──Mkkk
     *  │   └──Mlll
     *  └──Mggg
     *      ├──Mooo
     *      └──Mppp
     *
     * 以上面的维度成员树为例，Mooo.relativePosition(Maaa) 返回的List为{1, 0}
     *
     * @param ancestorMember
     * @return
     */
    List<Integer> relativePosition(Member ancestorMember);

    /**
     * 在当前成员上往后位移，得到指定位置上的后代成员
     * Maaa
     *  ├──Mddd
     *  │   ├──Mkkk
     *  │   └──Mlll
     *  └──Mggg
     *      ├──Mooo
     *      └──Mppp
     * 以上面的维度成员树为例，
     * Maaa.moveRelativePosition(List{1, 1}) 返回的Member为Mppp
     *
     * @param relativePosition
     * @return
     */
    Member moveRelativePosition(List<Integer> relativePosition);
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    package com.dao.wormhole.core.model.entity;
    //
    //    public /*abstract class*/interface Member /*implements*/extends Comparable<Member>, WormholeBaseEntity {
                                                                                            //
                                                                                            //        public /*abstract*/ boolean isMeasureMember();
                                                                                            //
                                                                                            //        public /*abstract*/ boolean isSuperRootMember();
    //
    //        public boolean isCustomFormulaMember();
                                                                                            //
                                                                                            //        /**
                                                                                            //         * superRoot成员返回null
                                                                                            //         * @return
                                                                                            //         */
                                                                                            //        public /*abstract*/ Hierarchy getHierarchy();
                                                                                            //
                                                                                            //
                                                                                            //        /**
                                                                                            //         * superRoot成员返回null
                                                                                            //         * @return
                                                                                            //         */
                                                                                            //        public /*abstract*/ Level getLevel();
                                                                                            //
    //        public /*abstract*/ boolean isLeaf();
    //
    //        //	public abstract Dimension getDimension();
    //        public /*abstract*/ Dimension getDimension();
    //
    //        public /*abstract*/ Member getParentMember();
    //
    //        /**
    //         * 返回当前维度成员的子级成员
    //         * @return
    //         */
    //        public List<Member> getChildren();
    //
                                                                                        //        /**
                                                                                        //         * 根据desdntMbrId找到Member对象，如果Member对象是当前调用此方法的成员对象的后代，返回Member对象
                                                                                        //         * 否则返回null
                                                                                        //         *
                                                                                        //         * @param desdntMbrId
                                                                                        //         * @return
                                                                                        //         * @createdDate 2015/10/30
                                                                                        //         */
                                                                                        //        public Member findDescendantMember(int desdntMbrId);
                                                                                        //
                                                                                        //        /**
                                                                                        //         * 获得member全部后代成员列表，并用desdntMbrName过滤
                                                                                        //         * 保留列表中level最高的成员
                                                                                        //         * 如果列表长度为1，返回get(0)
                                                                                        //         * 如果列表长度为0，返回null
                                                                                        //         * 如果列表长度大于1，报错“当前维度成员具有多个距离最近且名为desdntMbrName的后代成员，无法确定选择哪个”
                                                                                        //         *
                                                                                        //         * @param desdntMbrName
                                                                                        //         * @return
                                                                                        //         * @createdDate 2015/10/30
                                                                                        //         */
                                                                                        //        public Member findNearestDescendantMember(String desdntMbrName);
                                                                                        //
    //    }
// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
