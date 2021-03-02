package cn.bgotech.analytics.bi.cache;

import cn.bgotech.analytics.bi.dao.mddm.*;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.DateDimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ChenZhiGang on 2017/6/20.
 *
 * @deprecated 过于复杂，即将被新缓存服务{@link MDDMCacheService}代替，目前采取两套缓存并行的临时方案。2019/3/27
 */
@Deprecated
@Component
public class CacheByCollectionInSameJVM implements CacheService {

    private static CacheService SINGLE_INSTANCE;

    public static void setCacheInstance(CacheService singleInstance) {
        if (SINGLE_INSTANCE == null) {
            SINGLE_INSTANCE = singleInstance;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private DimensionDAO dimensionDAO;

    @Resource
    private HierarchyDAO hierarchyDAO;

    @Resource
    private LevelDAO levelDAO;

    @Resource
    private MemberDAO memberDAO;

    @Resource
    private SchemaDAO schemaDAO;

    private final Map<Object, CacheNode> topCacheNodeMap = new HashMap<>();


    @Override
    public void rebuildMDDMDataSuite(Space space) {
        topCacheNodeMap.remove(space);
        createMDDMDataSuite(space);
    }

    @Override
    public void createMDDMDataSuite(Space space) {

        if (topCacheNodeMap.containsKey(space)) {
            logger.debug("top cache node '" + space.getName() + "' is exist already");
            return;
            // throw new BIRuntimeException("top cache node '" + dataSuiteKey + "' is exist already");
        }

        // Space space = olap().findSpace(dataSuiteKey);

//        if (space == null) {
//            logger.debug("There is no cache suite named " + dataSuiteKey);
//            return;
//        }

        CacheService.CacheNode cacheTopNode = new CacheNode(space);

        topCacheNodeMap.put(space, cacheTopNode);

        List<Cube> cubes = space.cubes();
        /** cacheTopNode -> Cube */
        cacheTopNode.setMapping(Cube.class, c -> ((Cube) c).getMgId(), cubes);

        List<DimensionRole> dimensionRoles;
        for (Cube cube : cubes) {

            dimensionRoles = dimensionDAO.loadRoles(cube);

            /** cacheTopNode -> Cube -> DimensionRole */
            cacheTopNode.getCacheNode(Cube.class, cube.getMgId()).setFinalMapping(DimensionRole.class,
                    dr -> ((DimensionRole) dr).getMgId(), dimensionRoles);

            /** cacheTopNode -> DimensionRole */
            cacheTopNode.setFinalMapping(DimensionRole.class, dr -> ((DimensionRole) dr).getMgId(), dimensionRoles);
        }

        _setDimensionSuite(cacheTopNode, space.getAllDimensions());
        logger.debug("create '" + space.getName() + "' data suite complete");


    }

    private void _setDimensionSuite(CacheNode cacheNode, List<Dimension> dimensions) {
        /** cacheNode -> Dimension */
        cacheNode.setMapping(Dimension.class, d -> ((Dimension) d).getMgId(), dimensions);

        List<Hierarchy> hierarchies;
        List<Level> levels;
        List<Member> members;
        Member rootMember;
        for (Dimension dimension : dimensions) {

            if (!(dimension instanceof DateDimension)) {
                // rootMember = dimension.getSuperRootMember();
                try {
                    rootMember = memberDAO.loadByParams("parentMemberId", -1, "dimensionId", dimension.getMgId()).get(0);
                    /** cacheNode -> Dimension -> root member object */
                    cacheNode.getCacheNode(Dimension.class, dimension.getMgId())
                            .setFinalMapping(CacheNode.SpecialLinkType.ROOT_MEMBER,
                                    m -> ((Member) m).getMgId(), Arrays.asList(rootMember));
                    /** cacheNode -> Member (root member) */
                    cacheNode.setFinalMapping(Member.class, m -> ((Member) m).getMgId(), rootMember);
                } catch (OlapException e) {
                    logger.error(e.getMessage(), e);
                    throw new OlapRuntimeException(e);
                }

            }

            // hierarchies = dimension.getHierarchies(null);
            hierarchies = hierarchyDAO.selectByDimensionId(dimension.getMgId());

            /** cacheNode -> Hierarchy */
            cacheNode.setMapping(Hierarchy.class, h -> ((Hierarchy) h).getMgId(), hierarchies);


            /** cacheNode -> Dimension -> Hierarchy */
            cacheNode.getCacheNode(Dimension.class, dimension.getMgId())
                    .setMapping(Hierarchy.class, h -> ((Hierarchy) h).getMgId(), hierarchies);

            for (Hierarchy hierarchy : hierarchies) {

                // levels = hierarchy.getLevels();
                levels = levelDAO.selectByHierarchyId(hierarchy.getMgId());

                /** cacheNode -> Level */
                cacheNode.setFinalMapping(Level.class, l -> ((Level) l).getMgId(), levels);

                /** cacheNode -> Hierarchy -> Level */
                cacheNode.getCacheNode(Hierarchy.class, hierarchy.getMgId())
                        .setFinalMapping(Level.class, l -> ((Level) l).getMgId(), levels);

                /** cacheNode -> Dimension -> Hierarchy -> Level */
                cacheNode.getCacheNode(Dimension.class, dimension.getMgId())
                        .getCacheNode(Hierarchy.class, hierarchy.getMgId())
                        .setMapping(Level.class, l -> ((Level) l).getMgId(), levels);


                if (!(dimension instanceof DateDimension)) {
                    for (Level level : levels) {
                        members = level.getMembers();
                        /** cacheNode -> Dimension -> Hierarchy -> Level -> Member */
                        cacheNode.getCacheNode(Dimension.class, dimension.getMgId())
                                .getCacheNode(Hierarchy.class, hierarchy.getMgId())
                                .getCacheNode(Level.class, level.getMgId())
                                .setFinalMapping(Member.class, m -> ((Member) m).getMgId(), members);
                        /** cacheNode -> Member */
                        cacheNode.setFinalMapping(Member.class, m -> ((Member) m).getMgId(), members);
                    }
                }
            }
        }
    }

    @Override
    public CacheNode getDataSuite(Object dataSuiteKey) {
        if (!topCacheNodeMap.containsKey(dataSuiteKey)) {
            createDataSuite(dataSuiteKey);
        }
        return topCacheNodeMap.get(dataSuiteKey);
    }

    @Override
    public CacheNode getCurrentUserSpaceMDDMDataSuite() {
        Space s = schemaDAO.findByName(ThreadLocalTool.getCurrentThreadSpaceName());
        return topCacheNodeMap.get(s);
    }

    private synchronized void createDataSuite(Object dataSuiteKey) {
        if (!topCacheNodeMap.containsKey(dataSuiteKey)) {
            topCacheNodeMap.put(dataSuiteKey, new CacheNode(dataSuiteKey));
        }
    }

    @Override
    public void systemPresetDataInit() {

        long currentTimeMillis = System.currentTimeMillis();

        CacheNode sysPresetDataCacheTopNode = new CacheNode(CacheNode.SpecialLinkType.SYSTEM_PRESET_DATA);
        topCacheNodeMap.put(CacheNode.SpecialLinkType.SYSTEM_PRESET_DATA, sysPresetDataCacheTopNode);

//        List<Dimension> sysPresetDimensions = new LinkedList<>();
        List<Dimension> sysPresetDimensions = dimensionDAO.loadAll()
                .stream().filter(d -> d.isPresetDimension()).collect(Collectors.toList());

        _setDimensionSuite(sysPresetDataCacheTopNode, sysPresetDimensions);

        buildFinalDescendantMembersMapping(getDataSuite(CacheNode.SpecialLinkType.SYSTEM_PRESET_DATA));

        logger.info("elapsed time: " + (System.currentTimeMillis() - currentTimeMillis) + " ms");
    }

    @Override
    public void buildFinalDescendantMembersMapping(CacheNode dataSuite) {
        Map<Long, List<Long>> finalDescendantMembersMap = new HashMap<>();
        Member member;
        for (Object m : dataSuite.findObjects(Member.class)) {
            if ((member = (Member) m).isLeaf()) {
                for (Member ancestor : member.findAncestors()) {
                    if (!finalDescendantMembersMap.containsKey(ancestor.getMgId())) {
                        finalDescendantMembersMap.put(ancestor.getMgId(), new LinkedList<>());
                    }
                    finalDescendantMembersMap.get(ancestor.getMgId()).add(member.getMgId());
                }
            }
        }
        dataSuite.setFinalMapping(CacheNode.SpecialLinkType.FINAL_DESCENDANT_MEMBERS_MAPPING,
                x -> CacheNode.SpecialLinkType.FINAL_DESCENDANT_MEMBERS_MAPPING, finalDescendantMembersMap);
    }

    @Override
    public void buildSpaceCacheSuiteWhenIsNo(Space space) {
        if (!topCacheNodeMap.containsKey(space))
            rebuildMDDMDataSuite(space);
    }

}
