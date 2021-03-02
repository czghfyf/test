package cn.bgotech.analytics.bi.component.olap;

import cn.bgotech.analytics.bi.bean.mddm.physical.CubeBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.HierarchyBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.LevelBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.DimensionBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.MeasureDimensionBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.UniversalDimensionBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.MeasureMemberBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.MemberBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.UniversalMemberBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.role.DimensionRoleBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.schema.SpaceBean;
import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.cache.CacheService;
import cn.bgotech.analytics.bi.cache.MDDMCacheService;
import cn.bgotech.analytics.bi.cache.SpaceCacheSuite;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.component.olap.assist.OLAPPresetDataBuilder;
import cn.bgotech.analytics.bi.dao.mddm.AttributesDAO;
import cn.bgotech.analytics.bi.dao.mddm.CubeDAO;
import cn.bgotech.analytics.bi.dao.mddm.DimensionDAO;
import cn.bgotech.analytics.bi.dao.mddm.HierarchyDAO;
import cn.bgotech.analytics.bi.dao.mddm.LevelDAO;
import cn.bgotech.analytics.bi.dao.mddm.MemberDAO;
import cn.bgotech.analytics.bi.dao.mddm.SchemaDAO;
import cn.bgotech.analytics.bi.dao.security.SecurityDao;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.analytics.bi.system.config.SystemConfiguration;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.bigdata.MDVectorSet;
import cn.bgotech.wormhole.olap.component.MddmStorageService;
import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.imp.text.RawCubeDataPackage;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.physical.ClassicMDDM;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.MeasureDimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.UniversalDimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeBuildCubeUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeCreateDimensionUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeCreateMemberUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeCreateSpaceUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExecutionUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.MDDM_ExecutionUnit;
import cn.bgotech.wormhole.olap.mdx.auxi.AuxBuildCube;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service("mddm_storage_service")
public class MddmStorageServiceImpl implements MddmStorageService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MDDMCacheService mddmCache;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private SystemConfiguration sysCfg;

    @Autowired
    private CacheService cache;

    @Resource
    private AttributesDAO attributesDAO;

    @Resource
    private SchemaDAO schemaDAO;

    @Resource
    private DimensionDAO dimensionDAO;

    @Resource
    private HierarchyDAO hierarchyDAO;

    @Resource
    private MemberDAO memberDAO;

    @Resource
    private LevelDAO levelDAO;

    @Resource
    private CubeDAO cubeDAO;

    @Resource
    private SecurityDao securityDao;

    @Autowired
    @Qualifier("transactionManager")
    private DataSourceTransactionManager transactionManager;

    @Override
//  @Transactional
    public void init() {
        new OLAPPresetDataBuilder(beanFactory, dimensionDAO, hierarchyDAO, memberDAO, levelDAO).build();
    }

    @Override
    public Space findSpaceByName(String name) {
        SpaceCacheSuite cacheSuite = mddmCache.getSpaceSuite(name);
        if (cacheSuite != null)
            return cacheSuite.getSpace();
        return schemaDAO.findByName(name);
    }

    @Override
    public int save(Space space) {
        return schemaDAO.save((SpaceBean) space);
    }

    @Override
    public Cube findCube(Space space, String cubeName) {
        return mddmCache.findCube(space, cubeName);
    }

    @Override
    public Cube findCube(String spaceName, String cubeName) {
        return mddmCache.findCube(spaceName, cubeName);
    }

    @Override
    public Dimension findDimension(Long dimensionId) {
        return mddmCache.findDimensionByMgId(dimensionId);
    }

    @Override
    public UniversalDimension findUniversalDimension(Space space, String dimName) {
        List<DimensionBean> all = dimensionDAO.loadAll();
        for (DimensionBean d : all) {
            if (d.isWithinRange(space) && d.getName().equals(dimName) && d instanceof UniversalDimension) {
                return (UniversalDimension) d;
            }
        }
        return null;
    }

    @Override
    public Cube findCube(Long cubeId) {
        for (Cube c : cubeDAO.loadAll())
            if (c.getMgId().equals(cubeId))
                return c;
        return null;
    }

    @Override
    public List<Cube> allCubes() {
        return cubeDAO.loadAll();
    }

    @Override
    public DimensionRole findDimensionRole(Cube cube, MultiDimensionalDomainSelector.Part part) {
        if (part.getMgId() != null/*block.getId() != -1*/) {
            for (DimensionRole dr : cube.getDimensionRoles(null)) {
                if (part.getMgId().equals(dr.getMgId())/*block.getId() == dr.getWormholeID()*/) {
                    return dr;
                }
            }
        } else {
            return findDimensionRole(part.getImage()/*block.getText()*/, cube); // TODO: create a method
            /*for (IDimensionRole dr : cube.getDimensionRoles()) {
                if (dr.getName().equals(block.getText())) {
                    returnOne = dr; break;
                }
            }*/
        }
        return null;
    }

    @Override
    public <E> E findUniqueEntity(Cube cube, Class<E> clazz, MultiDimensionalDomainSelector selector) {

        BasicEntityModel bem = findUniqueEntity(cube, selector.getPart(0));
        if (bem == null) {
            throw new OlapRuntimeException(selector + " represents an unknown entity");
        }
        if (selector.length() == 1) {
            return (E) bem;
        }
        try {
            return (E) bem.selectSingleEntity(selector.clone().removePart(0));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new OlapRuntimeException(e);
        }

    }

    @Override
    public DimensionRole findDimensionRole(String dimRoleName, Cube cube) {
        DimensionRole returnOne = null;
        for (DimensionRole dr : cube.getDimensionRoles(null)) {
            if (dr.getName().equals(dimRoleName)) {
                returnOne = dr;
                break;
            }

        }
        return returnOne;
    }

    @Override
    public BasicEntityModel selectSingleEntity(Dimension dimension, MultiDimensionalDomainSelector.Part part) {
        if (part.getMgId() != null) {
            // 根据ID返回对象，如果对象为null或其类型不在{hierarchy、level、member}范围内，返回null
            // 如果对象属于维度dim，返回对象，否则返回null
            SpaceCacheSuite scs = mddmCache.getSpaceSuite(ThreadLocalTool.getCurrentUser().getSpaceName());
            Member m = scs.findMemberByMgId(part.getMgId().intValue());
            if (m != null)
                return m.getDimension().equals(dimension) ? m : null;
            Hierarchy h = scs.findHierarchyByMgId(part.getMgId());
            if (h != null)
                return h.getDimension().equals(dimension) ? h : null;
            Level l = scs.findLevelByMgId(part.getMgId());
            if (l != null)
                return l.getDimension().equals(dimension) ? l : null;
            return null;
        } else {
            // 优先返回：rootMember
            Member rootMember = dimension.getSuperRootMember();
            if (rootMember.getName().equals(part.getImage())) {
                return rootMember;
            }
//            Hierarchy h = this.getHierarchy(dim, blk.getText());
            List<Hierarchy> hierarchies = dimension.getHierarchies(part.getImage());
            if (hierarchies.size() > 1) {
                throw new RuntimeException("duplicate hierarchies which named '" + part.getImage()
                        + "' in dimension[mgId = " + dimension.getMgId() + "]");
            }
            if (!hierarchies.isEmpty()) {
                return hierarchies.get(0); // 如果根据name在Dimension对象上查找到的Hierarchy对象不为空，返回Hierarchy对象
            }
//            Hierarchy h = dimension.getHierarchies(part.getImage()).get(0);
//            if (h != null) {
//                return h;
//            }

            // TODO: 如果无法确定Hierarchy，以默认Hierarchy为准(默认Hierarchy下的Level及Member相对于其他Hierarchy下的对象会被优先返回)
            return this.selectSingleEntity(dimension.getDefaultHierarchy(), part);


//			return this.selectWormholeEntityByPath(dim.getDefaultHierarchy(), blk.getText()); // blk.getId()已为-1

//			return dim.getDefaultHierarchy().s
//			List<Level> ls = dim.getAllLevels();
//			List<Member> ms = dim.getAllMembers();
        }
    }

    @Override
    public BasicEntityModel selectSingleEntity(DimensionRole dr, MultiDimensionalDomainSelector selector) {
        return mddmCache.selectSingleEntity(dr, selector);
    }

    @Override
    public BasicEntityModel selectSingleEntity(Hierarchy hierarchy, MultiDimensionalDomainSelector.Part part) {
//        int epID = ep.getId();
        if (part.getMgId() != null) {

            List<LevelBean> lvs = mddmCache.findAllLevels();
            for (LevelBean lv : lvs) {
                if (lv.getMgId().equals(part.getMgId())) {
                    return lv.getHierarchy().equals(hierarchy) ? lv : null;
                }
            }

            // 原先逻辑为先取预置日期维度成员，如为空，取当前用户空间中的维度成员，如再为空，取预置维度成员
            Member m = mddmCache.findMemberByMgId(part.getMgId().intValue());

            if (m != null) {
                return m.getHierarchy().equals(hierarchy) ? m : null;
            }
            return null;
        } else {
            return selectSingleEntity(hierarchy, part.getImage());
        }
    }

    /**
     * TODO:
     * If the entities of the same name are too many, you can try to return the nearest one,
     * such as: [H].[123].[ABC] is prioritized than [H].[123].XYZ.[ABC] is returned (when entityName = 'ABC')
     *
     * @param hierarchy
     * @param entityName
     * @return
     */
    @Override
    public ClassicMDDM selectSingleEntity(Hierarchy hierarchy, String entityName) {

//        Space currentSpace = schemaStore.findByName(ThreadLocalTool.getCurrentUser().getName());

//        // 1. 查询Level
//        List<Level> levels = loadLevels(hierarchy, objName);
//        // 2. 查询Member
//        List<Member> members = loadMembers(hierarchy, objName);
//
//        List<WormholeBaseEntity> objects = new ArrayList<WormholeBaseEntity>();
//        objects.addAll(members);
//        objects.addAll(levels);

        List<ClassicMDDM> entities = new ArrayList<>();
        entities.addAll(hierarchy.getLevels().stream().filter(l -> l.getName().equals(entityName)).collect(Collectors.toList()));

        for (Object m : cache.getCurrentUserSpaceMDDMDataSuite()
                .findObjects(Member.class,
                        m -> m instanceof MemberBean && ((MemberBean) m).getName().equals(entityName) && hierarchy.getMgId().equals(((MemberBean) m).getHierarchyId()))) {
            entities.add((ClassicMDDM) m);
        }
//        entities.addAll(cache.getDataSuite().findObjects(Member.class, m -> {
//            return m instanceof MemberBean && hierarchy.getMgId().equals(((MemberBean)m).getHierarchyId());
//        }));

        if (entities.size() == 1) {
            return entities.get(0);
        } else if (entities.size() == 0) {
            return null;
        } else {
            throw new OlapRuntimeException("The return of the entities too much, can not determine which one");
        }

//        if (objects.size() == 1) {
//            return objects.get(0);
//        } else if (objects.size() == 0) {
////			throw new WromholeRuntimeException("没有相关的实体");
//            return null;
//        } else {
//            throw new /*Wromhole*/RuntimeException("返回的对象过多");
//        }
    }

    @Override
    public Level getLevel(long hierarchyId, int memberLevel) {
        return mddmCache.findLevel(hierarchyId, memberLevel);
    }

    @Override
    public List<Member> findMemberDescendants(Member member) {
        List<Member> descendants = new ArrayList<>();
        findMemberDescendants(descendants, member);
        Collections.sort(descendants);
        return descendants;
    }

    @Override
    public List<Member> findChildren(Member member) {
//         return memberStore.loadChildren(member);
        return member.isLeaf() ? new LinkedList<>() : mddmCache.findMemberChildren(member);
    }

    @Override
    public List<Member> findMembers(Level level) {
        return memberDAO.loadMembers((LevelBean) level);
    }

    /**
     * TODO: low efficiency!
     * 存在性能隐患。当以ClassicMDDM.class和一个代表维度成员的ID值为参数调用此方法时，会依次调用cubeStore、dimensionStore、hierarchyStore和levelStore，
     * 当这些Store对象相关方法被调用时，因为mgId表示的是维度成员，所以这些Store对象在访问缓存时必定会得到null，进而对数据库进行查询。
     * 由于在memberStore的load方法被调用之前，已经向数据库发起了大量无效查询，造成了性能的浪费。
     *
     * @param clazz
     * @param mgId  Multi-Dimensional Domain Global ID
     * @param <E>
     * @return
     */
    @Override
    public <E> E find(Class<? extends ClassicMDDM> clazz, Long mgId) {

//        logger.warn("low efficiency!");

//        if (EntityRole.class.isAssignableFrom(clazz)) {
//            throw new RuntimeException("// TODO: do it later."); // TODO: do it later.
//        }

//        ClassicMDDM ce = Cube.class.equals(clazz) ? cubeDAO.loadById(mgId) :
//                (Dimension.class.equals(clazz) ? dimensionDAO.load(mgId) :
//                        (Hierarchy.class.equals(clazz) ? hierarchyDAO.load(mgId) :
//                                (Level.class.equals(clazz) ? levelDAO.loadById(mgId) :
//                                        (Member.class.equals(clazz) ? memberDAO.load(mgId) : null))));
//        ClassicMDDM ce = cubeStore.loadById(mgId);
//        ce = ce != null ? ce : dimensionStore.loadRole(mgId);
//        ce = ce != null ? ce : dimensionStore.load(mgId);
//        ce = ce != null ? ce : hierarchyStore.load(mgId);
//        ce = ce != null ? ce : levelStore.loadById(mgId);
//        ce = ce != null ? ce : memberStore.load(mgId);


        SpaceCacheSuite scs = mddmCache.getSpaceSuite(ThreadLocalTool.getCurrentUser().getSpaceName());
        ClassicMDDM ce;
        do {
            ce = scs.findCubeByMgId(mgId);
            if (ce != null) break;
            ce = scs.findDimensionRoleByMgId(mgId);
            if (ce != null) break;
            ce = scs.findDimensionByMgId(mgId);
            if (ce != null) break;
            ce = scs.findHierarchyByMgId(mgId);
            if (ce != null) break;
            ce = scs.findLevelByMgId(mgId);
            if (ce != null) break;
            ce = scs.findMemberByMgId(mgId.intValue());
        } while (false);

//        logger.debug(clazz.getName() + ".isAssignableFrom(" + ce.getClass().getName() + ") = " + clazz.isAssignableFrom(ce.getClass()));
        if (ce != null && clazz.isAssignableFrom(ce.getClass())) {
            return (E) ce;
        }

        return null;

// TODO: Delete the comments below at later.
//        if (Cube.class.equals(clazz)) {
//            ce = cubeDAO.loadById(mgId);
//        } else if (Dimension.class.equals(clazz)) {
//            ce = dimensionDAO.load(mgId);
//        } else if (Hierarchy.class.equals(clazz)) {
//            ce = hierarchyDAO.load(mgId);
//        } else if (Level.class.equals(clazz)) {
//            ce = levelDAO.loadById(mgId);
//        } else if (Member.class.equals(clazz)) {
//            ce = memberDAO.load(mgId);
//        } else if (Space.class.equals(clazz)) {
//            throw new RuntimeException("// TODO: do it later."); // TODO: do it later.
//        } else if (DimensionRole.class.equals(clazz)) {
//            throw new RuntimeException("// TODO: do it later."); // TODO: do it later.
//        } else if (ClassicMDDM.class.equals(clazz)) {
//            find
//        } else {
//            throw new OlapRuntimeException("not support " + clazz.getName());
//        }
//
//        return (E) ce;

    }

    @Override
    public <E> List<E> find(Class<? extends ClassicMDDM> clazz, String name) {
        if (Cube.class.equals(clazz)) {
            return (List<E>) cubeDAO.loadByName(name);
        } else if (Dimension.class.equals(clazz)) {
            return (List<E>) dimensionDAO.loadByName(name);
        } else if (Hierarchy.class.equals(clazz)) {
            return (List<E>) mddmCache.getSpaceSuite(ThreadLocalTool.getCurrentThreadSpaceName()).findHierarchiesByName(name);
        } else if (Level.class.equals(clazz)) {
            return (List<E>) mddmCache.getSpaceSuite(ThreadLocalTool.getCurrentThreadSpaceName()).findLevelsByName(name);
        } else if (Member.class.equals(clazz)) {
            return (List<E>) memberDAO.loadByName(name);
        } else if (Space.class.equals(clazz)) {
            throw new RuntimeException("// TODO: do it later."); // TODO: do it later.
        } else if (DimensionRole.class.equals(clazz)) {
            throw new RuntimeException("// TODO: do it later."); // TODO: do it later.
        }
        throw new OlapRuntimeException("not support " + clazz.getName());
    }

    @Override
    public DimensionRole findMeasureDimensionRole(Cube cube) {
        List<DimensionRole> drs = mddmCache.getSpaceSuite(((CubeBean) cube).getSpaceId()).getDimensionRoles(cube);
        for (DimensionRole dr : drs) {
            if (dr.getDimension() instanceof MeasureDimension) {
                return dr;
            }
        }
        logger.warn("this cube[mgId = " + cube.getMgId() + "] have no measure dimension role");
        return null;
    }

    @Override
    public List<DimensionRole> findUniversalDimensionRoles(Cube cube) {
        List<DimensionRole> roles = mddmCache.getSpaceSuite(((CubeBean) cube).getSpaceId()).getDimensionRoles(cube);
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getDimension() instanceof MeasureDimension) {
                roles.remove(i);
            }
        }
        return roles;
    }

    @Override
    public Member findMemberById(long id) {
        Object m = cache.getDataSuite(CacheService.CacheNode.SpecialLinkType.SYSTEM_PRESET_DATA).getObject(Member.class, id);
        if (m == null) {
            m = cache.getCurrentUserSpaceMDDMDataSuite().getObject(Member.class, id);
        }
        return (Member) m;
    }

    @Override
    public Dimension findDimensionById(long id) {
        return mddmCache.findDimensionByMgId(id);
//        Object d = cache.getCurrentUserSpaceMDDMDataSuite().getObject(Dimension.class, id);
//        if (d == null) {
//            d = cache.getDataSuite(CacheService.CacheNode.SpecialLinkType.SYSTEM_PRESET_DATA).getObject(Dimension.class, id);
//        }
//        return (Dimension) d;
    }

    @Override
    public BasicData findProperty(Member member, String propertyKey) {
        return attributesDAO.load(member.getMgId(), propertyKey).convertedToBasicData();
    }

//    @Override
//    public void startup() {
//        TODO:
//    }

    private void findMemberDescendants(List<Member> memberContainer, Member member) {
        if (member.isLeaf())
            return;
        List<Member> children = findChildren(member);
        memberContainer.addAll(children);
        for (Member m : children) {
            findMemberDescendants(memberContainer, m);
        }
    }

//    private List<? extends ClassicMDDM> loadMembers(Hierarchy hierarchy, String memberName) {
////        Map<String, Object> params = new HashMap<>();
////        params.put("hierarchyId", hierarchy.getMgId());
////        params.put("memberName", memberName);
//        try {
//            return memberStore.loadByParams("hierarchyId", hierarchy.getMgId(), "memberName", memberName);
//        } catch (OlapException e) {
//            e.printStackTrace();
//            throw new OlapRuntimeException(e);
//        }
////        List<Member> members = new ArrayList<>();
////        Member m;
////        for (Iterator<Map.Entry<Long, Member>> i$ = membersMap.entrySet().iterator(); i$.hasNext();) {
////            Map.Entry<Long, Member> memberEntry = i$.next();
////            m = memberEntry.getValue();
////            if (m.getName().equals(memberName) && m.getHierarchy().equals(hierarchy)) {
////                members.add(m);
////            }
////        }
////        return members;
//    }

    @Override
    public Member selectSingleEntity(Member member, MultiDimensionalDomainSelector.Part part) {
        return part.getMgId() != null ?
                member.findDescendantMember(part.getMgId()) // 根据指定ID返回当前成员的后代成员
                : member.findNearestDescendantMember(part.getImage()); // 根据名称返回距离当前成员最近的后代成员
    }

    @Override
    public Member selectSingleEntity(Level level, MultiDimensionalDomainSelector.Part part) {
        Member member;
        if (part.getMgId() == null) {
            // 根据ID查找维度成员，若不是root成员且level相同，返回成员，否则返回null
//            Member m = this.findMemberByID(pathSegment.getId());
//            member = find(Member.class, part.getMgId());

            // 原先逻辑为先取预置日期维度成员，如为空，取当前用户空间中的维度成员，如再为空，取预置维度成员
            member = mddmCache.findMemberByMgId(part.getMgId().intValue());

            if ((!member.isRoot()) && member.getLevel().equals(level)) {
                return member;
            }
        } else {
            member = level.findUniqueNamedMember(part.getImage()); // 找到Level中命名唯一的维度成员
        }
        return member;
    }

    @Override
    public Member getDefaultMember(Dimension dimension) {
        // return dimension.getSuperRootMember();
        // return dimension.getDefaultHierarchy().getDefaultMember();
        return dimension.isMeasureDimension() ?
                mddmCache.findHierarchyDefaultMember(((DimensionBean) dimension).getDefaultHierarchyId())
                : dimension.getSuperRootMember();
    }

    @Override
    public <E> List<E> findAll(Class<E> clazz) {
        if (Cube.class.equals(clazz)) {
            return (List<E>) cubeDAO.loadAll();
        } else if (Dimension.class.equals(clazz)) {
            return (List<E>) dimensionDAO.loadAll();
        } else if (Hierarchy.class.equals(clazz)) {
            return (List<E>) hierarchyDAO.loadByName(null);
        } else if (Level.class.equals(clazz)) {
            return (List<E>) mddmCache.findAllLevels();
        } else if (Member.class.equals(clazz)) {
            return (List<E>) memberDAO.loadByName(null);
        } else if (Space.class.equals(clazz)) {
            throw new RuntimeException("// TODO: do it later."); // TODO: do it later.
        } else if (DimensionRole.class.equals(clazz)) {
            throw new RuntimeException("// TODO: do it later."); // TODO: do it later.
        }
        throw new OlapRuntimeException("not support " + clazz.getName());
    }

    /**
     * if (part start with like "&123456") {
     * BasicEntityModel e = getById(123456) // 直接按ID取对象
     * switch (e的对象类型)
     * case DimensionRole: if (关联cube) { return e; } else { 报错“不相关的维度角色” }
     * case Dimension:
     * if (维度在cube上只扮演一个角色) {
     * return e 扮演的维度角色
     * } else {
     * 报错“维度在cube上没有扮演角色或角色不唯一”
     * }
     * case Hierarchy:
     * case Level:
     * case Member:
     * Dimension dim // 取得对象所属的维度对象
     * if (dim在cube上只扮演一个角色) {
     * DimensionRole dimRole // 获得dim在cube上扮演的唯一角色
     * 返回Hierarchy、Level或Member对应的维度对象
     * } else {
     * 报错“e所在维度在cube上没有扮演角色或角色不唯一”
     * }
     * default: 报错“错误的对象类型” + e.getClass
     * } else { // part in ("something", "[something]")
     * <p>
     * 根据part.image查询同名对象列表：
     * <p>
     * 1、维度角色列表：如果存在名称相同且匹配cube的角色，return 维度角色; 如果不存在，执行第2步。
     * <p>
     * 2、维度列表：// 查询cube上以part.image为名的维度对象列表
     * if (返回0个维度) {
     * 执行第3步。
     * } else if (返回1个维度) {
     * if (维度在cube上只扮演一个角色) {
     * return 维度角色
     * } else {
     * 报错“维度在cube上没有扮演角色或角色不唯一”
     * }
     * } else { // 返回的维度多于一个
     * 报错“维度不等同于维度角色，不可能返回多个相同维度”
     * }
     * <p>
     * 3、hierarchy列表：
     * if (hierarchy列表长度为0) {
     * 执行第4步
     * } else {
     * 删除列表中所属维度与cube无关联的hierarchy
     * if (剩余列表长度为0) {
     * 执行第4步
     * } else if (剩余列表长度为1) {
     * if (hierarchy所属的维度在cube上只扮演一个角色) {
     * return new HierarchyRole()
     * } else {
     * 报错“hierarchy的维度在cube上没有扮演角色或角色不唯一”
     * }
     * } else {
     * 报错“表示多个Hierarchy”
     * }
     * }
     * <p>
     * 4、level列表：
     * if (level列表长度为0) {
     * 执行第5步
     * } else {
     * 删除列表中所属维度与cube无关联的level
     * if (剩余列表长度为0) {
     * 执行第5步
     * } else if (剩余列表长度为1) {
     * if (level所属的维度在cube上只扮演一个角色) {
     * return new LevelRole()
     * } else {
     * 报错“level的维度在cube上没有扮演角色或角色不唯一”
     * }
     * } else {
     * 报错“表示多个level”
     * }
     * }
     * <p>
     * 5、member列表：
     * if (member列表长度为0) {
     * 报错“找不到同名的对象”
     * } else {
     * 删除列表中所属维度与cube无关联的member
     * if (剩余列表长度为1) {
     * if (member所属的维度在cube上只扮演一个角色) { // 调用相关方法
     * return new MemberRole()
     * } else {
     * 报错“member的维度在cube上没有扮演角色或角色不唯一”
     * }
     * } else {
     * 报错“表示0或多个member”
     * }
     * }
     * <p>
     * }
     */
    @Override
    public BasicEntityModel findUniqueEntity(Cube cube, MultiDimensionalDomainSelector.Part part) {
        /**
         * if (part start with like "&123456") {
         *     // step 0.
         *     return cube.getEntityRole(123456) // 返回cube中带角色的唯一对象
         * } else {
         *     根据part.image查询对象列表
         *     // step 1.
         *     DimensionRole list:
         *         如果列表中有与cube关联的维度角色则返回
         *         否则继续往下执行
         *     // step 2.
         *     Dimension list:
         *         list = cube.removingIrrelevant(list) // 用cube过滤列表，将与cube无关联的对象删除
         *         如果列表为空，执行下一步；如果列表长度大于1，报错“名称可表示多个对象”
         *         return cube.getEntityRole(dimension) // 返回cube中带角色的唯一对象
         *     // step 3.
         *     Hierarchy list:
         *         list = cube.removingIrrelevant(list) // 用cube过滤列表，将与cube无关联的对象删除
         *         如果列表为空，执行下一步；如果列表长度大于1，报错“名称可表示多个对象”
         *         return cube.getEntityRole(hierarchy) // 返回cube中带角色的唯一对象
         *     // step 4.
         *     Level list:
         *         list = cube.removingIrrelevant(list) // 用cube过滤列表，将与cube无关联的对象删除
         *         如果列表为空，执行下一步；如果列表长度大于1，报错“名称可表示多个对象”
         *         return cube.getEntityRole(level) // 返回cube中带角色的唯一对象
         *     // step 5.
         *     Member List:
         *         list = cube.removingIrrelevant(list) // 用cube过滤列表，将与cube无关联的对象删除
         *         如果列表为空，执行下一步；如果列表长度大于1，报错“名称可表示多个对象”
         *         return cube.getEntityRole(member) // 返回cube中带角色的唯一对象
         *
         *     查无对象：报错
         * }
         */
        if (part.getMgId() != null) {
            // step 0.
            return cube.getEntityRole(part.getMgId()); // TODO: replace: return cube.findUniqueEntityWithRoleByID(pathBlock.getId());
        } else {

            /** step 1. **/
            // Collection<DimensionRole> dimensionRoles = findDimensionRoles(part.getImage()); // TODO: replace: this.findDimensionRolesByName(pathBlock.getText());
            Collection<DimensionRole> dimensionRoles = cube.getDimensionRoles(null);
            for (DimensionRole dr : dimensionRoles) {
                if (dr.getName().equals(part.getImage())) { // TODO: replace: if (namedDimRoles.get(i).byCubeReference(cube)) {
                    return dr;
                }
            }

            /** step 2. **/
//            List<Dimension> dimensions = cube.extractRelated(loadDimensions(part.getImage())); // TODO: replace: dimensions = cube.removingIrrelevantDim(dimensions);
            List<Dimension> dimensions = cube.getDimensions(part.getImage());
            if (dimensions.size() == 1) {
//                return cube.getEntityRole(dimensions.get(0));
//                return new DimensionRoleBean(cube, dimensions.get(0));
                return cube.getDimensionRoles(dimensions.get(0)).get(0);
            } else if (dimensions.size() > 1) {
                throw new OlapRuntimeException("Multiple dimensions have the same name");
            }

            /** step 3. **/
//            List<Hierarchy> hierarchies = cube.extractRelated(loadHierarchies(part.getImage()));
            // TODO: replace: List<Hierarchy> hierarchies = this.loadHierarchiesByName(pathBlock.getText());
            //                hierarchies = cube.removingIrrelevantHhy(hierarchies);
            List<Hierarchy> hierarchies = cube.getHierarchies(part.getImage());
            if (hierarchies.size() == 1) { // If there is only one hierarchy, its corresponding dimension and dimension role must be only one.
//                return cube.getEntityRole(hierarchies.get(0));
                return cube.getEntityRole(hierarchies.get(0).getMgId());
//                    return cube.getDimensionRoles(hierarchies.get(0).getDimension()).get(0);
            } else if (hierarchies.size() > 1) {
                throw new OlapRuntimeException("Multiple hierarchies have the same name");
            }

            /** step 4. **/
//            List<Level> levels = cube.extractRelated(loadLevels(part.getImage()));
            List<Level> levels = cube.getLevels(part.getImage());
            if (levels.size() == 1) {
//                return cube.getEntityRole(levels.get(0));
                return cube.getEntityRole(levels.get(0).getMgId());
//                    return cube.getDimensionRoles(levels.get(0).getDimension()).get(0);
            } else if (levels.size() > 1) {
                throw new OlapRuntimeException("Multiple levels have the same name");
            }


            /** step 5. **/
//            List<Member> members = cube.extractRelated(loadMembers(part.getImage()));
            List<Member> members = cube.getMembers(part.getImage());
            if (members.size() == 1) {
//                return cube.getEntityRole(members.get(0));
                return cube.getEntityRole(members.get(0).getMgId());
//                return cube.getDimensionRoles(members.get(0).getDimension()).get(0);
            } else if (members.size() > 1) {
                throw new OlapRuntimeException("Multiple members have the same name");
            }
            throw new OlapRuntimeException("no named " + part.getImage() + " entity in cube[" + cube.getName() + "] space");

        }
    }


    @Override
    public void executeImport(Space space, String cubeName, RawCubeDataPackage rawCubeDataPackage) throws OlapException {
//        new ImportCubeDataTool(space, cubeName, rawCubeDataPackage, beanFactory,
//                cubeDAO, dimensionStore, hierarchyStore, levelStore, memberStore, sysCfg).execute();
//        cache.rebuildMDDMDataSuite(space);
//        mddmCache.rebuildSpaceSuite(space);
    }

    @Override
    public String executeImport(List<ExecutionUnit> exeUnits) {
        DefaultTransactionDefinition transactionDef = new DefaultTransactionDefinition();
        transactionDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDef);
        Set<Space> preRefreshedCacheSpace = new HashSet<>();
        try {
            for (ExecutionUnit eu : exeUnits) {
                if (!(eu instanceof MDDM_ExecutionUnit))
                    continue;
                handleExeUnit((MDDM_ExecutionUnit) eu, preRefreshedCacheSpace);
            }
            transactionManager.commit(transactionStatus);
            for (Iterator<Space> itor = preRefreshedCacheSpace.iterator(); itor.hasNext(); ) {
                Space s_ = itor.next();
                cache.rebuildMDDMDataSuite(s_);
                mddmCache.rebuildSpaceSuite(s_);
            }

            return "success";
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            logger.error("transactional rollback cause by: " + e.getMessage(), e);
            return e.getMessage();
        }
    }

    private void handleExeUnit(MDDM_ExecutionUnit eu, Set<Space> preRefreshedCacheSpace) {
        if (eu instanceof ExeBuildCubeUnit) {
            handleExeUnit((ExeBuildCubeUnit) eu, preRefreshedCacheSpace);
            return;
        } else if (eu instanceof ExeCreateSpaceUnit) {
            handleExeUnit((ExeCreateSpaceUnit) eu, preRefreshedCacheSpace);
            return;
        } else if (eu instanceof ExeCreateDimensionUnit) {
            handleExeUnit((ExeCreateDimensionUnit) eu, preRefreshedCacheSpace);
            return;
        } else if (eu instanceof ExeCreateMemberUnit) {
            handleExeUnit((ExeCreateMemberUnit) eu, preRefreshedCacheSpace);
            return;
        }
        throw new RuntimeException("do not match class: " + eu.getClass().getName());
    }

    private void handleExeUnit(ExeBuildCubeUnit eu, Set<Space> preRefreshedCacheSpace) {

        SpaceBean space = (SpaceBean) findSpaceByName(eu.spaceName);
        if (space == null)
            throw new RuntimeException("space [" + eu.spaceName + "] not exist.");
        preRefreshedCacheSpace.add(space);

        List<CubeBean> cubes = cubeDAO.loadByName(eu.cubeName);
        for (int i = 0; i < cubes.size(); i++) {
            CubeBean c = cubes.get(i);
            if (c.getSpaceId().equals(space.getMgId()))
                throw new RuntimeException("cube [" + eu.cubeName + "] in space [" + eu.spaceName + "] already exist.");
        }
        CubeBean cubeBean = (CubeBean) beanFactory.create(CubeBean.class);
        while (OlapEngine.hold().getVectorComputingEngine().existMDVectorSet(MDVectorSet.fileName(cubeBean))) {
            cubeBean = (CubeBean) beanFactory.create(CubeBean.class);
        }
        cubeBean.setSpaceId(space.getMgId());
        cubeBean.setName(eu.cubeName);
        cubeDAO.save(cubeBean);

        List<DimensionBean> dimensions = dimensionDAO.loadAll();
        goto_flag_1:
        for (int j = 0; j < eu.dm_role_match.size(); j++) {
            String existDimensionName = eu.dm_role_match.get(j)[0];
            for (int i = 0; i < dimensions.size(); i++) {
                DimensionBean dm = dimensions.get(i);
                if (existDimensionName.equals(dm.getName()) && space.getMgId().equals(dm.getSpaceId())) {
                    DimensionRoleBean dimensionRoleBean = (DimensionRoleBean) beanFactory.create(DimensionRoleBean.class);
                    dimensionRoleBean.setName(eu.dm_role_match.get(j)[1]);
                    dimensionRoleBean.setCubeId(cubeBean.getMgId());
                    dimensionRoleBean.setDimensionId(dm.getMgId());
                    dimensionDAO.saveRole(dimensionRoleBean);
                    continue goto_flag_1;
                }
            }
            throw new RuntimeException("No dimension [" + existDimensionName + "] defined in space [" + space.getName() + "]");
        }


        // create cube's measure dimension
        MeasureDimensionBean measureDimensionBean = (MeasureDimensionBean) beanFactory.create(MeasureDimensionBean.class);
//        measureDimensionBean.setBinaryControlFlag(2);
        measureDimensionBean.setName(MeasureDimension.DEFAULT_NAME);
        measureDimensionBean.setSpaceId(space.getMgId());
        measureDimensionBean.setMaxMemberLevel(1);

        // create cube's measure dimension hierarchy
        HierarchyBean measureDimHierarchy = (HierarchyBean) beanFactory.create(HierarchyBean.class);
        measureDimHierarchy.setName(measureDimensionBean.defaultHierarchyName());
        measureDimHierarchy.setDimensionId(measureDimensionBean.getMgId());
        measureDimensionBean.setDefaultHierarchyId(measureDimHierarchy.getMgId());

        /** measure dimension complete */
        dimensionDAO.save(measureDimensionBean);

        // create cube's measure dimension root member
        MeasureMemberBean measureSuperRoot = (MeasureMemberBean) beanFactory.create(MeasureMemberBean.class);
        measureSuperRoot.setName(measureDimensionBean.defaultSuperRootName());
        measureSuperRoot.setDimensionId(measureDimensionBean.getMgId());
        measureSuperRoot.setBinaryControlFlag(2);
        measureSuperRoot.setSpaceId(space.getMgId());
        /** measure super root member is completed */
        memberDAO.save(measureSuperRoot);

        // create cube's measure dimension hierarchy level
        LevelBean measureLevelBean = (LevelBean) beanFactory.create(LevelBean.class);
        measureLevelBean.setName(Level.generateSimpleName(1));
        measureLevelBean.setHierarchyId(measureDimHierarchy.getMgId());
        measureLevelBean.setMemberLevel(1);
        /** measure level is complete */
        levelDAO.save(measureLevelBean);

        // create cube's measure dimension members
        for (String meaName : eu.measures) {
            MeasureMemberBean mmb = (MeasureMemberBean) beanFactory.create(MeasureMemberBean.class);
            mmb.setName(meaName);
            mmb.setDimensionId(measureDimensionBean.getMgId());
            mmb.setBinaryControlFlag(3);
            mmb.setParentMemberId(measureSuperRoot.getMgId());
            mmb.setHierarchyId(measureDimHierarchy.getMgId());
            mmb.setMemberLevel(1);
            mmb.setSpaceId(space.getMgId());
            measureDimHierarchy.setDefaultMemberId(mmb.getMgId());
            /** measure member(which not be super root) has been completed */
            memberDAO.save(mmb);
        }

        /** measure hierarchy is complete */
        hierarchyDAO.save(measureDimHierarchy);

        // create cube's measure dimension role
        DimensionRoleBean measureDimensionRoleBean = (DimensionRoleBean) beanFactory.create(DimensionRoleBean.class);
        measureDimensionRoleBean.setName(MeasureDimension.DEFAULT_ROLE_NAME);
        measureDimensionRoleBean.setDimensionId(measureDimHierarchy.getDimensionId());
        measureDimensionRoleBean.setCubeId(cubeBean.getMgId());
        /** measure dimension role has been completed */
        dimensionDAO.saveRole(measureDimensionRoleBean);


    }

    private void handleExeUnit(ExeCreateSpaceUnit eu, Set<Space> preRefreshedCacheSpace) {
        SpaceBean space = schemaDAO.findByName(eu.name);
        if (space != null)
            throw new RuntimeException("space [" + eu.name + "] already exist.");

        space = (SpaceBean) beanFactory.create(SpaceBean.class);
        space.setName(eu.name);
        schemaDAO.save(space);
        preRefreshedCacheSpace.add(space);
    }

    private void handleExeUnit(ExeCreateDimensionUnit eu, Set<Space> preRefreshedCacheSpace) {

        SpaceBean space = (SpaceBean) findSpaceByName(eu.spaceName);
        if (space == null)
            throw new RuntimeException("space [" + eu.spaceName + "] not exist.");
        preRefreshedCacheSpace.add(space);

        List<DimensionBean> dimensions = dimensionDAO.loadByName(eu.dimensionName);
        DimensionBean dmBean;
        for (int i = 0; i < dimensions.size(); i++) {
            dmBean = dimensions.get(i);
            if (space.getMgId().equals(dmBean.getSpaceId()) && dmBean.getName().equals(eu.dimensionName))
                throw new RuntimeException("dimension [" + eu.dimensionName + "] already exist in space [" + eu.spaceName + "].");
        }

        dmBean = (UniversalDimensionBean) beanFactory.create(UniversalDimensionBean.class);
        HierarchyBean hieBean = (HierarchyBean) beanFactory.create(HierarchyBean.class);
        MemberBean rootMember = (UniversalMemberBean) beanFactory.create(UniversalMemberBean.class);

        dmBean.setName(eu.dimensionName);
        dmBean.setSpaceId(space.getMgId());
        dmBean.setDefaultHierarchyId(hieBean.getMgId());
        dmBean.setMaxMemberLevel(eu.maxMemberLevel);
        dimensionDAO.save(dmBean);
        /** universal dimension complete */

        rootMember.setName(dmBean.defaultSuperRootName());
        rootMember.setDimensionId(dmBean.getMgId());
        rootMember.setSpaceId(dmBean.getSpaceId());
        memberDAO.save(rootMember);
        /** universal super root member is complete */

        hieBean.setDimensionId(dmBean.getMgId());
        hieBean.setName(dmBean.defaultHierarchyName());
        hieBean.setDefaultMemberId(rootMember.getMgId());
        hierarchyDAO.save(hieBean);
        /** universal hierarchy complete */
    }

    private void handleExeUnit(ExeCreateMemberUnit eu, Set<Space> preRefreshedCacheSpace) {

        SpaceBean space = (SpaceBean) findSpaceByName(eu.spaceName);
        if (space == null)
            throw new RuntimeException("space [" + eu.spaceName + "] not exist.");
        preRefreshedCacheSpace.add(space);

        String dimensionName = eu.memberSelector.getPart(0).getImage();
        List<DimensionBean> dimensions = dimensionDAO.loadByName(dimensionName);
        if (dimensions.isEmpty())
            throw new RuntimeException("no dimension [" + dimensionName + "] be named '" + dimensionName + "'");
        DimensionBean dmBean = null;
        for (int i = 0; i < dimensions.size(); i++) {
            dmBean = dimensions.get(i);
            if (space.getMgId().equals(dmBean.getSpaceId()) && dmBean.getName().equals(dimensionName))
                break;
            dmBean = null;
        }
        if (dmBean == null)
            throw new RuntimeException("no dimension [" + dimensionName + "] exist in space [" + eu.spaceName + "].");

        HierarchyBean hieBean = hierarchyDAO.load(dmBean.getDefaultHierarchyId());

//        MemberBean attrs = new MemberBean();
//        attrs.setAllAttributesNull();
//
//        attrs.setDimensionId(dmBean.getMgId());
//        attrs.setMemberLevel(-1);

        // first, the parent member is root member.
        MemberBean parent;
        try {
            parent = memberDAO.loadByParams("dimensionId", dmBean.getMgId(), "memberLevel", -1).get(0);
        } catch (OlapException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }

        UniversalMemberBean newMember = null;
        for (int lv = 1; lv < eu.memberSelector.length(); lv++) {
//            attrs.setAllAttributesNull();
//            attrs.setName(eu.memberSelector.getPart(lv).getImage());
//            attrs.setParentMemberId(parent.getMgId());

            MemberBean paramBean = new MemberBean();
            paramBean.setName(eu.memberSelector.getPart(lv).getImage());
            paramBean.setParentMemberId(parent.getMgId());
            List<MemberBean> members = memberDAO.loadByNameAndParentMemberId(paramBean);
//            try {
//                members = memberDAO.loadByParams("name", eu.memberSelector.getPart(lv).getImage(), "parentMemberId", parent.getMgId());
//            } catch (OlapException e) {
//                logger.error(e.getMessage());
//                throw new RuntimeException(e);
//            }
            if (members.size() > 1) {
                throw new RuntimeException("Data is confusing. [parent id = " + parent.getMgId() + "] [name = " + eu.memberSelector.getPart(lv).getImage() + "]");
            } else if (members.size() == 1) {
                if (lv == eu.memberSelector.length() - 1)
                    throw new RuntimeException("member " + eu.memberSelector + " already exist.");
                parent = members.get(0);
            } else { // members.size() == 0

                parent.setBinaryControlFlag(0); // not measure dimension, and not leaf member

                newMember = (UniversalMemberBean) beanFactory.create(UniversalMemberBean.class);
                newMember.setName(eu.memberSelector.getPart(lv).getImage());
                newMember.setDimensionId(hieBean.getDimensionId());
                newMember.setHierarchyId(hieBean.getMgId());
                newMember.setMemberLevel(lv);
                newMember.setSpaceId(dmBean.getSpaceId());
                newMember.setParentMemberId(parent.getMgId());
                newMember.setBinaryControlFlag(1); // not measure dimension. is leaf member.

                memberDAO.update(parent);
                memberDAO.save(parent = newMember);

            }
        }

        // create level bean and insert into database when the level not exist
        LevelBean lvBean;
        for (int i = 1; i <= newMember.getMemberLevel(); i++) {
            lvBean = levelDAO.load(newMember.getHierarchyId(), i);
            if (lvBean != null)
                continue;
            // lvBean is null
            lvBean = (LevelBean) beanFactory.create(LevelBean.class);
            lvBean.setName(Level.generateSimpleName(i));
            lvBean.setHierarchyId(newMember.getHierarchyId());
            lvBean.setMemberLevel(i);
            levelDAO.save(lvBean);
        }

    }

    @Override
    public List<MemberBean> findDimensionMembers(Dimension dimension) {

        return mddmCache.getSpaceSuite(((DimensionBean) dimension).getSpaceId()).getMembers(dimension);

//        List<MemberBean> dimensionMembers = new ArrayList<>();
//        for (Object m : (dimension.isPresetDimension() ? cache.getDataSuite(CacheService.CacheNode.SpecialLinkType.SYSTEM_PRESET_DATA) : cache.getCurrentUserSpaceMDDMDataSuite())
//                .findObjects(Member.class,
//                        m -> m instanceof MemberBean && ((MemberBean) m).getDimensionId().equals(dimension.getMgId()))) {
//            dimensionMembers.add((MemberBean) m);
//        }
//        return dimensionMembers;


//        try {
//            for (Object m : cache.getDataSuite()
//                    .findObjects(Member.class,
//                            m -> m instanceof MemberBean && ((MemberBean)m).getDimensionId().equals(dimension.getMgId()))) {
//                dimensionMembers.add((MemberBean) m);
//            }
//            return dimensionMembers;
//        } catch (OlapException e) {
//            e.printStackTrace();
//            throw new OlapRuntimeException(e);
//        }


//        List<Member> members = new ArrayList<>();
//        for (Iterator<Map.Entry<Long, Member>> itt = membersMap.entrySet().iterator(); itt.hasNext();) {
//            Map.Entry<Long, Member> entry = itt.next();
//            if (entry.getValue().getDimension().getMgId().equals(dimension.getMgId())) {
//                members.add(entry.getValue());
//            }
//        }
//        Collections.sort(members);
//        return members;
    }

    /**
     * load all multi-dimensional model data about user named {userName}, then put they into cache
     *
     * @param userName
     */
    public void spaceCacheInit(String userName) {
        User u = securityDao.loadUserByName(userName);
        if (u.getSpaceName() != null) {
            Space space = schemaDAO.findByName(u.getSpaceName());
            cache.createMDDMDataSuite(space);
            mddmCache.buildSpaceCacheSuiteWhenIsNo(space);
        }
    }

    @Override
    public Member ancestorMemberAtLevel(Level level, Member member) {
        Level currentLevel = member.getLevel();
        if (!level.getDimension().equals(currentLevel.getDimension())) {
            throw new RuntimeException("无法找到另一个维度上的祖先成员");
        }

        if (currentLevel.getLevelValue() < level.getLevelValue()) {
            return null;
        } else if (currentLevel.getLevelValue() == level.getLevelValue()) {
            return member;
        } else {
            Member ancestor;
            do {
                ancestor = member.getParent();
                member = ancestor;
            } while (level.getLevelValue() < ancestor.getLevel().getLevelValue());
            return ancestor;
        }
    }

    @Override
    public List<UniversalDimension> getSysPredefinedDimensions() {
        List<UniversalDimension> result = new LinkedList<>();
        for (DimensionBean db : dimensionDAO.loadAll()) {
            if (db.isPresetDimension()) {
                result.add((UniversalDimension) db);
            }
        }
        return result;
    }

    @Override
    public List<Dimension> findCubeAllDimensionsFromPersistence(Cube cube) {
        Set<Dimension> set = new HashSet<>();
        for (DimensionRole dr : dimensionDAO.loadRoles(cube)) {
            set.add(dimensionDAO.load(((DimensionRoleBean) dr).getDimensionId()));
        }
        return new LinkedList<>(set);
    }

    @Override
    public List<DimensionRole> findCubeAllDimensionRolesFromPersistence(Cube cube) {
        return dimensionDAO.loadRoles(cube);
    }

    @Override
    public Dimension loadDimensionByRoleMGIDFromPersistence(Long mgId) {
        return dimensionDAO.loadDimensionByRoleMGIDFromPersistence(mgId);
    }

    @Override
    public List<Member> loadAllMembersByDimensionMGIDFromPersistence(Long dmMgId) {
        try {
            List<Member> res = new LinkedList<>();
            for (MemberBean mBean : memberDAO.loadByParams()) {
                if (mBean.getDimensionId().equals(dmMgId))
                    res.add(mBean);
            }
            return res;
        } catch (OlapException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public MemberRole findMemberRole(Cube cube, MultiDimensionalDomainSelector selector) {
        SpaceCacheSuite spaceCache = mddmCache.getSpaceSuite(((CubeBean) cube).getSpaceId());
        return spaceCache.findMemberRole(cube, selector);
    }

    @Override
    public Member findMemberByMgId(long memberMgId) {
        return mddmCache.findMemberByMgId((int) memberMgId);
    }

    @Override
    public void buildSpaceCacheSuiteWhenIsNo(String spaceName) {
        SpaceCacheSuite cacheSuite = mddmCache.getSpaceSuite(spaceName);
        if (cacheSuite != null)
            return;
        Space space = schemaDAO.findByName(spaceName);
        mddmCache.buildSpaceCacheSuiteWhenIsNo(space);
        cache.buildSpaceCacheSuiteWhenIsNo(space);
    }

    @Override
    public DimensionRole getMeasureDimensionRole(Cube cube) {
        SpaceCacheSuite spaceCache = mddmCache.getSpaceSuite(((CubeBean) cube).getSpaceId());
        return spaceCache.getMeasureDimensionRole(cube);
    }

    @Override
    public List<Map> loadCubeLeafMembersInfo() {
        return schemaDAO.cubeLeafMembersMap();
    }

    @Override
    public void createDimensions(String spaceName, List<String> dimensionNames, List<Integer> maxMbrLvList) {

        SpaceBean space = (SpaceBean) findSpaceByName(spaceName);
        if (space == null)
            throw new RuntimeException("space [" + spaceName + "] not exist.");

        Set<String> dimNameSet = new HashSet<>(dimensionNames);
        if (dimensionNames.size() != dimNameSet.size())
            throw new RuntimeException("Duplicate names in the dimension that will be created.");

        List<DimensionBean> existsDimensions = new LinkedList<>();
        for (String dn : dimensionNames)
            existsDimensions.addAll(dimensionDAO.loadByName(dn));

        for (DimensionBean dmBean : existsDimensions) {
            if (!dmBean.getSpaceId().equals(space.getMgId()))
                continue;
            if (dimNameSet.contains(dmBean.getName()))
                throw new RuntimeException("dimension [" + dmBean.getName() + "] already exist in space [" + spaceName + "].");
        }

        _transactionProcessing_(() -> {

            for (int i = 0; i < dimensionNames.size(); i++) {
                String newDimName = dimensionNames.get(i);
                int maxMbrLv = maxMbrLvList.get(i);

                DimensionBean dmBean = (UniversalDimensionBean) beanFactory.create(UniversalDimensionBean.class);
                HierarchyBean hieBean = (HierarchyBean) beanFactory.create(HierarchyBean.class);
                MemberBean rootMember = (UniversalMemberBean) beanFactory.create(UniversalMemberBean.class);

                dmBean.setName(newDimName);
                dmBean.setSpaceId(space.getMgId());
                dmBean.setDefaultHierarchyId(hieBean.getMgId());
                dmBean.setMaxMemberLevel(maxMbrLv);
                dimensionDAO.save(dmBean);
                /** universal dimension complete */

                rootMember.setName(dmBean.defaultSuperRootName());
                rootMember.setDimensionId(dmBean.getMgId());
                rootMember.setSpaceId(dmBean.getSpaceId());
                memberDAO.save(rootMember);
                /** universal super root member is complete */

                hieBean.setDimensionId(dmBean.getMgId());
                hieBean.setName(dmBean.defaultHierarchyName());
                hieBean.setDefaultMemberId(rootMember.getMgId());
                hierarchyDAO.save(hieBean);
                /** universal hierarchy complete */
            }

        });
    }

    @Override
    public void createMembers(String spaceName, List<MultiDimensionalDomainSelector> memberSelectors) {

        SpaceBean space = (SpaceBean) findSpaceByName(spaceName);
        if (space == null)
            throw new RuntimeException("space [" + spaceName + "] not exist.");

        _transactionProcessing_(() -> {

            for (MultiDimensionalDomainSelector mbrSelector : memberSelectors) {

                String dimensionName = mbrSelector.getPart(0).getImage();
                List<DimensionBean> dimensions = dimensionDAO.loadByName(dimensionName);
                if (dimensions.isEmpty())
                    throw new RuntimeException(dimensionName + " dimension not exist");

                DimensionBean dmBean = null;
                for (DimensionBean _bean : dimensions) {
                    if (space.getMgId().equals(_bean.getSpaceId()) && _bean.getName().equals(dimensionName)) {
                        dmBean = _bean;
                        break;
                    }
                }

                if (dmBean == null)
                    throw new RuntimeException("no dimension [" + dimensionName + "] exist in space [" + spaceName + "].");

                HierarchyBean hieBean = hierarchyDAO.load(dmBean.getDefaultHierarchyId());

                // first, the parent member is root member.
                MemberBean parent;
                try {
                    parent = memberDAO.loadByParams("dimensionId", dmBean.getMgId(), "memberLevel", -1).get(0);
                } catch (OlapException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                }

                UniversalMemberBean newMember = null;
                for (int lv = 1; lv < mbrSelector.length(); lv++) {

                    MemberBean paramBean = new MemberBean();
                    paramBean.setName(mbrSelector.getPart(lv).getImage());
                    paramBean.setParentMemberId(parent.getMgId());
                    List<MemberBean> members = memberDAO.loadByNameAndParentMemberId(paramBean);

                    if (members.size() > 1) {
                        throw new RuntimeException("Data is confusing. [parent id = " + parent.getMgId() + "] [name = " + mbrSelector.getPart(lv).getImage() + "]");
                    } else if (members.size() == 1) {
                        if (lv == mbrSelector.length() - 1)
                            throw new RuntimeException("member " + mbrSelector + " already exist.");
                        parent = members.get(0);
                    } else { // members.size() == 0
                        parent.setBinaryControlFlag(0); // not measure dimension, and not leaf member
                        memberDAO.update(parent);
                        newMember = (UniversalMemberBean) beanFactory.create(UniversalMemberBean.class);
                        newMember.setName(mbrSelector.getPart(lv).getImage());
                        newMember.setDimensionId(hieBean.getDimensionId());
                        newMember.setHierarchyId(hieBean.getMgId());
                        newMember.setMemberLevel(lv);
                        newMember.setSpaceId(dmBean.getSpaceId());
                        newMember.setParentMemberId(parent.getMgId());
                        newMember.setBinaryControlFlag(1); // not measure dimension. is leaf member.
                        memberDAO.save(parent = newMember);
                    }
                }

                // create level bean and insert into database when the level not exist
                LevelBean lvBean;
                for (int i = 1; i <= newMember.getMemberLevel(); i++) {
                    lvBean = levelDAO.load(newMember.getHierarchyId(), i);
                    if (lvBean != null)
                        continue;
                    // lvBean is null
                    lvBean = (LevelBean) beanFactory.create(LevelBean.class);
                    lvBean.setName(Level.generateSimpleName(i));
                    lvBean.setHierarchyId(newMember.getHierarchyId());
                    lvBean.setMemberLevel(i);
                    levelDAO.save(lvBean);
                }
            }
        });
    }

    @Override
    public void buildCubes(String spaceName, List<AuxBuildCube> cubesInfo) {

        SpaceBean space = (SpaceBean) findSpaceByName(spaceName);
        if (space == null)
            throw new RuntimeException("space [" + spaceName + "] not exist.");

        List<Cube> cubes = cubeDAO.loadAll();
        for (Cube _c : cubes) {
            CubeBean c = (CubeBean) _c;
            if (!c.getSpaceId().equals(space.getMgId()))
                continue;
            for (AuxBuildCube abc : cubesInfo) {
                if (c.getName().equals(abc.getCubeName()))
                    throw new RuntimeException("cube [" + c.getName() + "] in space [" + spaceName + "] already exist.");
            }
        }

        _transactionProcessing_(() -> {


            List<DimensionBean> spDms = dimensionDAO.loadAll().stream()
                    .filter(db -> space.getMgId().equals(db.getSpaceId())).collect(Collectors.toList());


            Map<String, DimensionBean> spDmMap = new HashMap<>();
            for (DimensionBean _db : spDms)
                spDmMap.put(_db.getName(), _db);

            for (AuxBuildCube abc : cubesInfo) {

                CubeBean cubeBean = (CubeBean) beanFactory.create(CubeBean.class);
                cubeBean.setSpaceId(space.getMgId());
                cubeBean.setName(abc.getCubeName());
                cubeDAO.save(cubeBean);

                for (AuxBuildCube.Dim__Role dr_ : abc.getDrsInfo()) {
                    DimensionBean dm = spDmMap.get(dr_.getDmName());
                    if (dm == null)
                        throw new RuntimeException("No dimension [" + dr_.getDmName() + "] defined in space [" + spaceName + "]");
                    DimensionRoleBean dimensionRoleBean = (DimensionRoleBean) beanFactory.create(DimensionRoleBean.class);
                    dimensionRoleBean.setName(dr_.getRoleName());
                    dimensionRoleBean.setCubeId(cubeBean.getMgId());
                    dimensionRoleBean.setDimensionId(dm.getMgId());
                    dimensionDAO.saveRole(dimensionRoleBean);
                }

                // create cube's measure dimension
                MeasureDimensionBean measureDimensionBean = (MeasureDimensionBean) beanFactory.create(MeasureDimensionBean.class);
                measureDimensionBean.setName(MeasureDimension.DEFAULT_NAME);
                measureDimensionBean.setSpaceId(space.getMgId());
                measureDimensionBean.setMaxMemberLevel(1);

                // create cube's measure dimension hierarchy
                HierarchyBean measureDimHierarchy = (HierarchyBean) beanFactory.create(HierarchyBean.class);
                measureDimHierarchy.setName(measureDimensionBean.defaultHierarchyName());
                measureDimHierarchy.setDimensionId(measureDimensionBean.getMgId());
                measureDimensionBean.setDefaultHierarchyId(measureDimHierarchy.getMgId());

                /** measure dimension complete */
                dimensionDAO.save(measureDimensionBean);

                // create cube's measure dimension root member
                MeasureMemberBean measureSuperRoot = (MeasureMemberBean) beanFactory.create(MeasureMemberBean.class);
                measureSuperRoot.setName(measureDimensionBean.defaultSuperRootName());
                measureSuperRoot.setDimensionId(measureDimensionBean.getMgId());
                measureSuperRoot.setBinaryControlFlag(2);
                measureSuperRoot.setSpaceId(space.getMgId());
                /** measure super root member is completed */
                memberDAO.save(measureSuperRoot);

                // create cube's measure dimension hierarchy level
                LevelBean measureLevelBean = (LevelBean) beanFactory.create(LevelBean.class);
                measureLevelBean.setName(Level.generateSimpleName(1));
                measureLevelBean.setHierarchyId(measureDimHierarchy.getMgId());
                measureLevelBean.setMemberLevel(1);
                /** measure level is complete */
                levelDAO.save(measureLevelBean);

                // create cube's measure dimension members
                for (String meaName : abc.getMeasureNames()) {
                    MeasureMemberBean mmb = (MeasureMemberBean) beanFactory.create(MeasureMemberBean.class);
                    mmb.setName(meaName);
                    mmb.setDimensionId(measureDimensionBean.getMgId());
                    mmb.setBinaryControlFlag(3);
                    mmb.setParentMemberId(measureSuperRoot.getMgId());
                    mmb.setHierarchyId(measureDimHierarchy.getMgId());
                    mmb.setMemberLevel(1);
                    mmb.setSpaceId(space.getMgId());
                    measureDimHierarchy.setDefaultMemberId(mmb.getMgId());
                    /** measure member(which not be super root) has been completed */
                    memberDAO.save(mmb);
                }

                /** measure hierarchy is complete */
                hierarchyDAO.save(measureDimHierarchy);

                // create cube's measure dimension role
                DimensionRoleBean measureDimensionRoleBean = (DimensionRoleBean) beanFactory.create(DimensionRoleBean.class);
                measureDimensionRoleBean.setName(MeasureDimension.DEFAULT_ROLE_NAME);
                measureDimensionRoleBean.setDimensionId(measureDimHierarchy.getDimensionId());
                measureDimensionRoleBean.setCubeId(cubeBean.getMgId());
                /** measure dimension role has been completed */
                dimensionDAO.saveRole(measureDimensionRoleBean);
            }
        });
    }

    @Override
    public void rebuildSpaceCache(String spaceName) {
        Space s_ = schemaDAO.findByName(spaceName);
        cache.rebuildMDDMDataSuite(s_);
        mddmCache.rebuildSpaceSuite(s_);
    }

    private void _transactionProcessing_(Runnable rab) {
        DefaultTransactionDefinition transactionDef = new DefaultTransactionDefinition();
        transactionDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDef);
        try {
            rab.run();
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            logger.error(e.getMessage());
            throw e;
        }
    }
}
