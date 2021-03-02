package cn.bgotech.wormhole.olap.mddm.physical.dimension;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.ClassicMDDM;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface Dimension extends ClassicMDDM/*PhysicalEntity*/ {

    default String defaultHierarchyName() {
        return getName() + "_DH";
    }

    default String defaultSuperRootName() {
        // return getName() + "_ROOT";
        return getName() + "汇总";
    }

    /**
     * 获得默认维度成员
     * @return
     */
    Member getDefaultMember();

    /**
     * 获得维度下全部维度成员
     * @return
     */
    List<? extends Member> getAllMembers();

    Hierarchy getDefaultHierarchy();

    /**
     * get and return hierarchies named 'parameter:name', if 'parameter:name' is null, return all.
     *
     * @param name hierarchy name
     * @return
     */
    List<Hierarchy> getHierarchies(String name);

    /**
     *
     * @return
     */
    Member getSuperRootMember();

    boolean isMeasureDimension();

    /**
     * if name is null, return all members.
     * if name is not null, return members who name match the parameter.
     * @param name
     * @return
     */
    List<? extends Member> getMembers(String name);

    boolean isPresetDimension();

    default Member findMemberByFullNamesPath(List<String> fullNamesPath) {
        throw new OlapRuntimeException("Please call this method on the implementation class!");
    }

    Integer getMaxMemberLevel();
}


// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151022
    //    package com.dao.wormhole.core.model.entity;
    //            import java.util.List;
    //    /**
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-14 上午10:45:23
    //     */
    //    public /*abstract class*/interface Dimension /*implements*/extends WormholeBaseEntity, Comparable<Dimension> {
    //
    //
    //        /**
    //         * 判断维度对象是否是度量维
    //         *
    //         * @return true 是度量维； false 非度量维
    //         */
    //        public /*abstract*/ boolean isMeasureDimension();
    //
                                                                                                    //        public /*abstract*/ Hierarchy getDefaultHierarchy();
                                                                                                    //
                                                                                                    //        /**
                                                                                                    //         * 获得默认维度成员
                                                                                                    //         * @return
                                                                                                    //         */
                                                                                                    //        public Member getDefaultMember();
                                                                                                    //
                                                                                                    //        /**
                                                                                                    //         * 返回此维度的superRoot成员
                                                                                                    //         * @return
                                                                                                    //         */
                                                                                                    //        public Member getSuperRootMember();
                                                                                                    //
                                                                                                    //        /**
                                                                                                    //         * 获得维度下全部维度成员
                                                                                                    //         * @return
                                                                                                    //         */
                                                                                                    //        public List<Member> getAllMembers();
    //    }
    //

// ????????????????????????????????????????????????????????????????????????????????????????????
