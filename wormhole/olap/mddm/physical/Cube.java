package cn.bgotech.wormhole.olap.mddm.physical;

import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.EntityRole;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface Cube extends ClassicMDDM {

    MultiDimensionalVector getDefaultVector();

    /**
     * 返回带有角色信息的实体对象，前提是此对象要与调用此方法的cube对象具有关联关系。
     *
     * 如果mgId代表的对象本身就是一个维度角色，且此维度角色与cube关联，返回对象，否则返回null。
     *
     * 如果mgId代表Dimension、Hierarchy、Level、Member对象，
     * 只有在此对象所在维度在cube上只扮演一个角色的情况下，返回用维度角色包装过的角色对象。
     *
     * 如果mgId代表的对象所在维度在cube上不只扮演一个维度角色，抛出异常“无法确定此对象在哪个维度角色上”。
     *
     * 如果mgId代表的对象与cube无关联，返回null。
     *
     * @param mgId
     * @return
     */
    EntityRole getEntityRole(long mgId);

    /**
     * 返回ce相关维度在Cube上扮演的角色列表
     * 如果dimension未与cube关联，返回列表为空
     * 如果dimension为度量维，返回列表长度为1(多维数据集只能存在一个度量维)
     * 如果dimension非度量维，返回列表长度>=1
     *
     * @param ce
     * @return
     */
    List<DimensionRole> findRelationalDimensionRoles(ClassicMDDM ce);


    /**
     * get and return dimension roles which matches the parameter and related this cube,
     * if parameter is null, return all dimension roles.
     *
     * @return
     */
    List<DimensionRole> getDimensionRoles(Dimension dimension);

    /**
     * @param dimension
     * @param dimensionRoleName
     * @return
     */
    default DimensionRole getDimensionRole(Dimension dimension, String dimensionRoleName) {
        for (DimensionRole dr : getDimensionRoles(dimension)) {
            if (dr.getName().equals(dimensionRoleName)) {
                return dr;
            }
        }
        return null;
    }

    default DimensionRole getMeasureDimensionRole() {
        for (DimensionRole dr : getDimensionRoles(null)) {
            if (dr.getDimension().isMeasureDimension())
                return dr;
        }
        throw new RuntimeException(String.format("cube &%d[%s] has no measure dimension (role)", getMgId(), getName()));
    }

    /**
     * dimension 对象是否与 cube 相关联
     * @param dim
     * @return
     */
    boolean isReferenceDim(Dimension dim);

    /**
     * get and return dimensions named 'parameter:name', if 'parameter:name' is null, return all.
     *
     * @param name dimension mae
     * @return
     */
    List<Dimension> getDimensions(String name);

    /**
     * get and return hierarchies named 'parameter:name', if 'parameter:name' is null, return all.
     *
     * @param name hierarchy name
     * @return
     */
    List<Hierarchy> getHierarchies(String name);

    /**
     * If the argument is empty, all levels associated with this cube are returned, otherwise matched by name
     *
     * @param name level name
     * @return
     */
    List<Level> getLevels(String name);

    /**
     * If the argument is empty, all members associated with this cube are returned, otherwise matched by name
     *
     * @param name member name
     * @return
     */
    List<Member> getMembers(String name);
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151022
    //    package com.dao.wormhole.core.model.entity;
    //
    //            import com.dao.wormhole.core.model.entity.role.IDimensionRole;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-13 下午3:40:47
    //     *
    //     */
    //    public /*abstract class*/interface Cube /*implements*/extends WormholeBaseEntity {
    //
    //    //	/**
    //    //	 * @deprecated
    //    //	 * @return
    //    //	 * @see #getName()
    //    //	 */
    //    //	public String getCubeName();

    //    //	/**
    //    //	 * 获得Cube的默认矢量坐标片段
    //    //	 * @return
    //    //	 */
    //    //	public VectorCoordinateFragment getDefaultCubeVCF();
    //
    //        /**
    //         * 获得Cube的默认多维向量
    //         * @return
    //         */
    //        public MultidimensionalVector getDefaultVector();
    //
    //    //	public VectorCoordinateFragment getDefaultCubeVCF() {
    //    //
    //    //		List<Dimension> dimensions = this.getDimensionList();
    //    //		Collections.sort(dimensions);
    //    //
    //    //		Dimension measureDimension = null;
    //    //		for (int i = dimensions.size() - 1; i >=0; i--) {
    //    //			measureDimension = dimensions.get(i);
    //    //			if (measureDimension.isMeasureDimension()) {
    //    //				dimensions.remove(i);
    //    //				dimensions.add(measureDimension);
    //    //				break;
    //    //			}
    //    //		}
    //    //
    //    //		List<Member> members = new ArrayList<Member>();
    //    //		for (int i = 0; i < dimensions.size(); i++) {
    //    //			members.add(dimensions.get(i).getDefaultMember());
    //    //		}
    //    //
    //    //		return new VectorCoordinateFragment(new Tuple(members));
    //    //
    //    //	}
    //
    //        /**
    //         * 返回带有角色信息的实体对象，前提是此对象要与调用此方法的cube对象具有关联关系
    //         *
    //         * 如果id代表的对象本身就是一个维度角色，且此维度角色与cube关联，返回对象，否则返回null
    //         *
    //         * 如果id代表Dimension、Hierarchy、Level、Member对象，
    //         *     只有在此对象所在的维度在cube上只扮演一个角色的情况下，返回用维度角色包装过的角色对象；
    //         * 如果id代表的对象所在维度在cube上不只扮演一个维度角色，抛出异常“无法确定此对象在哪个维度角色上”
    //         * 如果id代表的对象与cube无关联，返回null；
    //         *
    //         * @param id 实体对象主键标识
    //         * @return
    //         * @createdDate 2015/10/30
    //         */
    //        public WormholeBaseEntity findUniqueEntityWithRoleByID(int id);
    //
    //        /**
    //         * 剔除与当前cube无关联的对象
    //         * @param dimensions
    //         * @return
    //         * @createdDate 2015/10/30
    //         */
    //        public List<Dimension> removingIrrelevantDim(List<Dimension> dimensions);
    //
    //        /**
    //         * 剔除与当前cube无关联的对象
    //         * @param hierarchies
    //         * @return
    //         * @createdDate 2015/10/30
    //         */
    //        public List<Hierarchy> removingIrrelevantHhy(List<Hierarchy> hierarchies);
    //
    //        /**
    //         * 剔除与当前cube无关联的对象
    //         * @param levels
    //         * @return
    //         * @createdDate 2015/10/30
    //         */
    //        public List<Level> removingIrrelevantLvl(List<Level> levels);
    //
    //        /**
    //         * 剔除与当前cube无关联的对象
    //         * @param members
    //         * @return
    //         * @createdDate 2015/10/30
    //         */
    //        public List<Member> removingIrrelevantMbr(List<Member> members);
    //
    //        /**
    //         * if (e instanceof 维度角色) {
    //         *     return e 关联  cube ? e : null;
    //         * } else { // e 不是维度角色对象
    //         *     if (对象  e 数据类型在{Dimension、Hierarchy、Level、Member}范围内) {
    //         *         if (e 所属维度在当前cube上只扮演一个角色) {
    //         *             返回维度角色包装过得具体类型角色对象
    //         *         } else if (e 所属维度在当前cube上扮演多个角色) {
    //         *             报错“无法判定e所在维度扮演的角色”
    //         *         } else {
    //         *             return null;
    //         *         }
    //         *     } else {
    //         *         return null;
    //         *     }
    //         * }
    //         *
    //         * @param e
    //         * @return
    //         * @createdDate 2015/10/30
    //         */
    //        public WormholeBaseEntity findUniqueEntityWithRole(WormholeBaseEntity e);
    //
    //        /**
    //         * 返回维度在Cube上扮演的角色列表
    //         * 如果dimension未与cube关联，返回列表为空
    //         * 如果dimension为度量维，返回列表长度为1(多维数据集只能存在一个度量维)
    //         * 如果dimension非度量维，返回列表长度>=1
    //         *
    //         * @param dimension
    //         * @return
    //         */
    //        public List<IDimensionRole> getDimensionRoles(Dimension dimension);
    //
    //    //	private int wormholeID = Integer.MIN_VALUE;
    //    //	private String cubeName = null;
    //
    //    //	public Cube(/*int wormholeID,*/ String cubeName) {
    //    //		this.cubeName = cubeName;
    //    ////		this.wormholeID = wormholeID;
    //    //	}
    //
    //    //	@Override
    //    //	public int getWormholeID() {
    //    ////		return wormholeID;
    //    //		return Integer.MIN_VALUE;
    //    //	}
    //
    //    }
    //
    //    /******************************************************************************************/
    //
    //    //package com.dao.wormhole.core.hyperspace;
    //    //
    //    //import java.util.List;
    //    //
    //    //import com.dao.wormhole.core.exception.WromholeRuntimeException;
    //    //
    //    ///**
    //    // * 立方体
    //    // *
    //    // * @author ChenZhigang
    //    // * @version 创建时间:2014-1-13 下午3:40:47
    //    // *
    //    // */
    //    //public class Cube implements HyperspaceModelObject {
    //    //
    //    //	private String cubeName = null;
    //    //	private int binaryFlag = 0;
    //    //	private String factTableName = null;
    //    //
    //    //	public String getFactTableName() {
    //    //		return factTableName;
    //    //	}
    //    //
    //    //	public Cube(String cubeName, int binaryFlag, String factTableName) {
    //    //		this.cubeName = cubeName;
    //    //		this.binaryFlag = binaryFlag;
    //    //		this.factTableName = factTableName;
    //    //	}
    //    //
    //    //	@Override
    //    //	public boolean byCubeReference(Cube cube) {
    //    //		return this.binaryFlag == cube.binaryFlag;
    //    //	}
    //    //
    //    //	@Override
    //    //	public HyperspaceModelObject selectHyperspaceModelObjectByPath(
    //    //			List<String> path) {
    //    //		throw new WromholeRuntimeException("不要调用此方法class:" + this.getClass().getName() + ", method:selectHyperspaceModelObjectByPath");
    //    //	}
    //    //
    //    //	public int getBinaryFlag() {
    //    //		return binaryFlag;
    //    //	}
    //    //
    //    //}

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
