package cn.bgotech.wormhole.olap.component;

import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.imp.text.RawCubeDataPackage;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.physical.ClassicMDDM;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.UniversalDimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExecutionUnit;
import cn.bgotech.wormhole.olap.mdx.auxi.AuxBuildCube;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.List;
import java.util.Map;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface MddmStorageService {

    /**
     * initialize method:
     *
     * 1. clean persistence data,
     *    and generate initialization data in persistence store again
     * 2. ...
     * 3. ...
     */
    void init();

    Space findSpaceByName(String name);

    int save(Space space);

    Cube findCube(Space space, String cubeName);

    Cube findCube(String spaceName, String cubeName);

    Dimension findDimension(Long dimensionId);

    UniversalDimension findUniversalDimension(Space space, String dimName);

    void executeImport(Space space, String cubeName, RawCubeDataPackage rawCubeDataPackage) throws OlapException;

    String executeImport(List<ExecutionUnit> mddm_exeUnits);

    Cube findCube(Long cubeId);

    /**
     * find and return all 'cube'
     * @return
     */
    List<Cube> allCubes();

    <E> E findUniqueEntity(Cube cube, Class<E> clazz, MultiDimensionalDomainSelector selector);

    BasicEntityModel findUniqueEntity(Cube cube, MultiDimensionalDomainSelector.Part part);

    DimensionRole findDimensionRole(Cube cube, MultiDimensionalDomainSelector.Part part);


    /**
     * 查询维度的全部维度成员
     *
     * @param dimension
     * @return
     */
    List<? extends Member> findDimensionMembers(Dimension dimension);


    /**
     * TODO: Why abandon this method ?
     *
     * @deprecated
     * @param dimRoleName
     * @param cube
     * @return
     * @see #findDimensionRole(Cube, MultiDimensionalDomainSelector.Part)
     */
    @Deprecated
    DimensionRole findDimensionRole(String dimRoleName, Cube cube);

    /**
     *
     * @param dimension
     * @param part
     * @return return entity which in the (superRoot, hierarchy, level, member) scope
     */
    BasicEntityModel selectSingleEntity(Dimension dimension, MultiDimensionalDomainSelector.Part part);

    BasicEntityModel selectSingleEntity(DimensionRole dr, MultiDimensionalDomainSelector selector);

    /**
     *
     * @param hierarchy
     * @param part
     * @return return entity which in the (level, member) scope. note: not include super root member
     */
    BasicEntityModel selectSingleEntity(Hierarchy hierarchy, MultiDimensionalDomainSelector.Part part);

    /**
     *
     * @param member
     * @param part
     * @return
     */
    Member selectSingleEntity(Member member, MultiDimensionalDomainSelector.Part part);

    /**
     *
     * @param level
     * @param part
     * @return
     */
    Member selectSingleEntity(Level level, MultiDimensionalDomainSelector.Part part);


    /**
     *
     * @param dimension
     * @return
     */
    Member getDefaultMember(Dimension dimension);

    <E> List<E> findAll(Class<E> clazz);

    /**
     *
     * @param hierarchy
     * @param entityName
     * @return level entity or member entity
     * @see #selectSingleEntity(Hierarchy, MultiDimensionalDomainSelector.Part)
     */
    ClassicMDDM selectSingleEntity(Hierarchy hierarchy, String entityName);

    /**
     *
     * @param hierarchyId
     * @param memberLevel
     * @return
     */
    Level getLevel(long hierarchyId, int memberLevel);

    /**
     * query and return all the descendants of this member
     * @param member
     * @return
     */
    List<Member> findMemberDescendants(Member member);

    //
    //        /**
    //         * 返回维度成员的子成员列表
    //         *
    //         * @param member
    //         * @return
    //         */

    /**
     * return the direct descendants of the current member
     * @param member
     * @return
     */
    List<Member> findChildren(Member member);

    List<Member> findMembers(Level level);

    /**
     * TODO: low efficiency!!!
     * @param clazz
     * @param mgId Multi-Dimensional Domain Global ID
     * @param <E>
     * @return
     */
    <E> E find(Class<? extends ClassicMDDM> clazz, Long mgId);

    /**
     * @param clazz
     * @param name Multi-Dimensional Domain Global Entity Name
     * @param <E>
     * @return
     */
    <E> List<E> find(Class<? extends ClassicMDDM> clazz, String name);

    /**
     * find and return the unique measure DimensionRole on the given Cube entity
     * @param cube
     * @return
     */
    DimensionRole findMeasureDimensionRole(Cube cube);

    /**
     * find and return universal DimensionRole list on the given Cube entity
     * @param cube
     * @return
     */
    List<DimensionRole> findUniversalDimensionRoles(Cube cube);

    /**
     *
     * @param id - member's mgId
     * @return
     */
    Member findMemberById(long id);

    /**
     *
     * @param id - dimension's mgId
     * @return
     */
    Dimension findDimensionById(long id);

    /**
     * @param member
     * @param propertyKey - property name
     * @return
     */
    BasicData findProperty(Member member, String propertyKey);

    /**
     * 返回level级别上member的祖先成员
     *
     * @param level
     * @param member
     * @return
     */
    Member ancestorMemberAtLevel(Level level, Member member);

    List<UniversalDimension> getSysPredefinedDimensions();

    List<Dimension> findCubeAllDimensionsFromPersistence(Cube cube);

    List<DimensionRole> findCubeAllDimensionRolesFromPersistence(Cube cube);

    /**
     * 根据维度角色MG_ID查询维度
     * @param mgId
     * @return
     */
    Dimension loadDimensionByRoleMGIDFromPersistence(Long mgId);

    List<Member> loadAllMembersByDimensionMGIDFromPersistence(Long mgId);

    MemberRole findMemberRole(Cube cube, MultiDimensionalDomainSelector selector);

    Member findMemberByMgId(long memberMgId);

    void buildSpaceCacheSuiteWhenIsNo(String spaceName);

    DimensionRole getMeasureDimensionRole(Cube cube);

    List<Map> loadCubeLeafMembersInfo();

    void createDimensions(String spaceName, List<String> dimensionNames, List<Integer> maxMbrLvList);

    void createMembers(String spaceName, List<MultiDimensionalDomainSelector> memberSelectors);

    void buildCubes(String spaceName, List<AuxBuildCube> cubesInfo);

    void rebuildSpaceCache(String spaceName);
}
