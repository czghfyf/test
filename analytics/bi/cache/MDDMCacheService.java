package cn.bgotech.analytics.bi.cache;

import cn.bgotech.analytics.bi.bean.mddm.physical.LevelBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.MemberBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.role.DimensionRoleBean;
import cn.bgotech.analytics.bi.dao.mddm.CubeDAO;
import cn.bgotech.analytics.bi.dao.mddm.DimensionDAO;
import cn.bgotech.analytics.bi.dao.mddm.HierarchyDAO;
import cn.bgotech.analytics.bi.dao.mddm.LevelDAO;
import cn.bgotech.analytics.bi.dao.mddm.MemberDAO;
import cn.bgotech.analytics.bi.dao.mddm.SchemaDAO;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by czg on 2019/3/26.
 */
@Service
public class MDDMCacheService {

    public static MDDMCacheService SI;

    private Map<Integer, SpaceCacheSuite> spaceMgId_suite_map = new HashMap<>();
    private Map<String, SpaceCacheSuite> spaceName_suite_map = new HashMap<>();

    @Resource
    private MemberDAO memberDao;

    @Resource
    private CubeDAO cubeDAO_;

    @Resource
    private DimensionDAO dimensionDAO;

    @Resource
    private HierarchyDAO hierarchyDAO;

    @Resource
    private LevelDAO levelDAO;

    @Resource
    private SchemaDAO schemaDAO;

    public SpaceCacheSuite getSpaceSuite(Long spaceId) {
        return spaceMgId_suite_map.get(spaceId.intValue());
    }

    public SpaceCacheSuite getSpaceSuite(String spaceName) {
        return spaceName_suite_map.get(spaceName);
    }

    public void rebuildSpaceSuite(Space space) {
        SpaceCacheSuite scs = new SpaceCacheSuite(space, memberDao, cubeDAO_, dimensionDAO, hierarchyDAO, levelDAO);
        scs.build();
        spaceMgId_suite_map.put(space.getMgId().intValue(), scs);
        spaceName_suite_map.put(space.getName(), scs);
    }

    public Member findMemberByMgId(int memberMgId) {
        Member member = null;
        for (SpaceCacheSuite scs : this.spaceMgId_suite_map.values()) {
            Member m = scs.findMemberByMgId(memberMgId);
            if (m != null)
                member = m;
        }
        return member;
    }

    /**
     * @param space 当对应多维模型缓存没有被加载时，构建对应的缓存套件。
     */
    public void buildSpaceCacheSuiteWhenIsNo(Space space) {
        if (!spaceMgId_suite_map.containsKey(space.getMgId().intValue()))
            rebuildSpaceSuite(space);
    }

    public boolean buildSpaceCacheSuiteWhenIsNo(String spaceName) {
        Space s = schemaDAO.findByName(spaceName);
        if (s != null) {
            buildSpaceCacheSuiteWhenIsNo(s);
            return true;
        }
        return false;
    }

    public Cube findCube(String spaceName, String cubeName) {
        Cube c = null;
        Iterator<SpaceCacheSuite> itt = spaceMgId_suite_map.values().iterator();
        while (itt.hasNext()) {
            SpaceCacheSuite scs = itt.next();
            if (scs.getSpace().getName().equals(spaceName))
                c = scs.findCubeByName(cubeName);
        }
        return c;
    }

    public Cube findCube(Space space, String cubeName) {
        SpaceCacheSuite scs = spaceMgId_suite_map.get(space.getMgId().intValue());
        return scs == null ? null : scs.findCubeByName(cubeName);
    }

    public List<Member> findMemberChildren(Member member) {
        SpaceCacheSuite scs = spaceMgId_suite_map.get(((MemberBean) member).getSpaceId().intValue());
        return scs == null ? new LinkedList<>() : scs.findMemberChildren(member);
    }

    public Dimension findDimensionByMgId(long id) {
        Iterator<SpaceCacheSuite> it = spaceMgId_suite_map.values().iterator();
        Dimension d;
        while (it.hasNext()) {
            d = it.next().findDimensionByMgId(id);
            if (d != null)
                return d;
        }
        return null;
    }

//    public MemberRole selectMemberRole(DimensionRole dr, MultiDimensionalDomainSelector s) {
//        DimensionRoleBean bean = (DimensionRoleBean) dr;
//        Iterator<SpaceCacheSuite> itr = spaceMgId_suite_map.values().iterator();
//        SpaceCacheSuite scs = null;
//        while (itr.hasNext())
//            if ((scs = itr.next()).isContainsCube(bean.getCubeId()))
//                break;
//        if (scs == null)
//            throw new RuntimeException("no matched cache suite");
//        return scs.selectMemberRole(dr, s);
//    }

    public BasicEntityModel selectSingleEntity(DimensionRole dr, MultiDimensionalDomainSelector s) {
        DimensionRoleBean bean = (DimensionRoleBean) dr;
        Iterator<SpaceCacheSuite> itr = spaceMgId_suite_map.values().iterator();
        SpaceCacheSuite scs = null;
        while (itr.hasNext())
            if ((scs = itr.next()).isContainsCube(bean.getCubeId()))
                break;
        if (scs == null)
            throw new RuntimeException("no matched cache suite");
        return scs.selectSingleEntity(dr, s);
    }

    public Member findHierarchyDefaultMember(Long hierarchyId) {
        Member member;
        for (SpaceCacheSuite scs : spaceMgId_suite_map.values()) {
            member = scs.findHierarchyDefaultMember(hierarchyId);
            if (member != null)
                return member;
        }
        return null;
    }

    public List<LevelBean> findAllLevels() {
        List<LevelBean> res = new ArrayList<>();
        for (Iterator<SpaceCacheSuite> itr = spaceMgId_suite_map.values().iterator(); itr.hasNext(); )
            res.addAll(itr.next().findAllLevels());
        return res;
    }

    public Level findLevel(long hierarchyId, int memberLevel) {
        for (Iterator<SpaceCacheSuite> itr = spaceMgId_suite_map.values().iterator(); itr.hasNext(); ) {
            SpaceCacheSuite suite = itr.next();
            if (suite.findHierarchyByMgId(hierarchyId) != null) {
                for (LevelBean lv : suite.findAllLevels()) {
                    if (lv.getHierarchyId().longValue() == hierarchyId && lv.getMemberLevel().intValue() == memberLevel)
                        return lv;
                }
            }
        }
        throw new RuntimeException("do not found LevelBean that [ hierarchyId = " + hierarchyId + ", memberLevel = " + memberLevel + " ]");
    }
}
