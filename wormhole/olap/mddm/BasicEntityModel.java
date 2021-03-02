package cn.bgotech.wormhole.olap.mddm;

import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface BasicEntityModel extends MultiDimensionalDomainModel {

    /**
     * 判断一个对象是否被Cube引用
     *
     * 例如，
     * [日期]是描述[cube 1]的维度之一，在[日期]维度对象上调用此方法并将[cube 1]作为参数传入，结果将返回true
     * [日期]不是描述[cube 2]的维度之一，在[日期]维度对象上调用此方法并将[cube 2]作为参数传入，结果将返回false
     *
     * @param cube
     * @return
     */
    boolean associatedWith(Cube cube);


    /**
     * TODO 201706111534: Inappropriate method.
     *       This method should be defined on classes that represent classical multidimensional models,
     *       and define the default method on it.
     *       (To be modified later)
     *
     *
     * 参考以下多维表达式
     * select &123[XXXX].&234[XX].&345[X].&456[s].&567AAA.&678AA.&789A on rows, [dim2].[dimMbr1] on columns from [cube1]
     * 如果&123[XXXX].&234[XX].&345[X]是一个已知对象，并且程序已获得其引用，
     * 则调用其对象上的这个方法并传入正确参数({"&456[s]", "&567AAA", "&678AA", "&789A"})，
     * 可以得到&123[XXXX].&234[XX].&345[X].&456[s].&567AAA.&678AA.&789A对象的引用
     *
     * @param segmentedSelector Multidimensional selector fragment
     * @return
     */
    BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector);

}


// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151022
    //    package com.dao.wormhole.core.model.entity;
    //
    //            import com.dao.wormhole.core.mdx.IR.EntityPathBlock;
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //            import com.dao.wormhole.core.model.entity.property.Property;
    //
    //
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //
    //    /**
    //     * 以下对象有可能实现此接口:
    //     *     1.多维立方体	Cube
    //     *     2.维度		Dimension
    //     *     3.层级		Hierarchy
    //     *     4.级别		Level
    //     *     5.维度成员		Member
    //     *     6.维度角色		IDimensionRole
    //     *     7.成员角色		IMemberRole
    //     *     8.标签		Label
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-10 下午4:56:15
    //     *
    //     */
    //    public interface WormholeBaseEntity extends WormholeBaseObject {
    //
    //        public int getWormholeID();
    //
    //        public String getName();
                                                                                                        //
                                                                                                        //        /**
                                                                                                        //         * 判断一个对象是否被Cube引用
                                                                                                        //         *
                                                                                                        //         * 例如，
                                                                                                        //         * [日期]是描述[cube 1]的维度之一，在[日期]维度对象上调用此方法并将[cube 1]作为参数传入，结果将返回true
                                                                                                        //         * [日期]不是描述[cube 2]的维度之一，在[日期]维度对象上调用此方法并将[cube 2]作为参数传入，结果将返回false
                                                                                                        //         *
                                                                                                        //         * @param cube
                                                                                                        //         * @return
                                                                                                        //         */
                                                                                                        //        public boolean byCubeReference(Cube cube);
                                                                                                        //
    //        /**
    //         * 参考以下多维表达式
    //         * select [XXXX].[XX].[X].[s].AAA.AA.A on rows, [dim2].[dimMbr1] on columns from [cube1]
    //         * 如果[XXXX].[XX].[X]是一个已知对象，并且程序已获得其引用，
    //         * 则调用其对象上的这个方法并传入正确参数({"s", "AAA", "AA", "A"})可以得到[XXXX].[XX].[X].[s].AAA.AA.A对象的引用
    //         *
    //         * @deprecated 废弃时间：2015/10/27
    //         * @param path 查询路径
    //         * @return
    //         * @see #selectSingleEntity(List)
    //         */
    //        public WormholeBaseEntity selectWormholeEntityByPath(List<String> path);
                                                                                                        //
                                                                                                        //        /**
                                                                                                        //         * 参考以下多维表达式
                                                                                                        //         * select &123[XXXX].&234[XX].&345[X].&456[s].&567AAA.&678AA.&789A on rows, [dim2].[dimMbr1] on columns from [cube1]
                                                                                                        //         * 如果&123[XXXX].&234[XX].&345[X]是一个已知对象，并且程序已获得其引用，
                                                                                                        //         * 则调用其对象上的这个方法并传入正确参数({"&456[s]", "&567AAA", "&678AA", "&789A"})，
                                                                                                        //         * 可以得到&123[XXXX].&234[XX].&345[X].&456[s].&567AAA.&678AA.&789A对象的引用
                                                                                                        //         *
                                                                                                        //         * @param path 查询路径
                                                                                                        //         * @return
                                                                                                        //         */
                                                                                                        //        public WormholeBaseEntity selectSingleEntity(List<EntityPathBlock> path);
                                                                                                        //
    //        /**
    //         * 返回Wormhole实体对象的全路径名
    //         *
    //         * Cube:	  return [cube名称]
    //         * Dimension: return [维度名称]
    //         * Hierarchy: return [维度名].[hierarchy名]
    //         * Level:	  return [维度名].[hierarchy名].[level名]
    //         * Member:	  return [维度名].[hierarchy名].[全部祖先mbr name].[member name]
    //         * super root member: return [维度名称].[super root member name]
    //         * IDimensionRole: [dimension role name]
    //         * IMemberRole: [dimension role name].[hierarchy名].[全部祖先mbr name].[member name]
    //         *
    //         * @return
    //         */
    //        public String fullPathName();
    //
    //        /**
    //         * 返回此对象的全部属性，若没有属性返回一空列表
    //         *
    //         * @createDate 2015-08-05
    //         * @return 属性列表
    //         */
    //        public List<Property> getProperties();
    //
    //        /**
    //         * 返回指定属性，若无则返回null
    //         *
    //         * @createDate 2015-08-05
    //         * @param key 属性键值
    //         * @return 属性对象
    //         */
    //        public Property getProperty(String key);
    //
    //    //	/**
    //    //	 * 获得此对象的全部属性名
    //    //	 * @return
    //    //	 */
    //    //	public List<String> getAllPropertyNames();
    //
    //    //	public Tuple getTuple(int i);
    //    }

// ??????????????????????????????????????????????????????????????????????????????????????????????????????