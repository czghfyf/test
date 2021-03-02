package cn.bgotech.analytics.bi.cache;

import cn.bgotech.analytics.bi.bean.mddm.physical.CubeBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.HierarchyBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.LevelBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.DimensionBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.MemberBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.role.DimensionRoleBean;
import cn.bgotech.analytics.bi.dao.mddm.CubeDAO;
import cn.bgotech.analytics.bi.dao.mddm.DimensionDAO;
import cn.bgotech.analytics.bi.dao.mddm.HierarchyDAO;
import cn.bgotech.analytics.bi.dao.mddm.LevelDAO;
import cn.bgotech.analytics.bi.dao.mddm.MemberDAO;
import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.HierarchyRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.LevelRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by czg on 2019/3/26.
 * 缓存按Space进行隔离，每个Space中的实体存在于同一个{@link SpaceCacheSuite}实例中。
 * 系统预置地区和日期等数据另行存储。
 */
public class SpaceCacheSuite {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Space space;

    private Map<Integer, Dimension> MG_ID___DIMENSION = new HashMap<>();

    private Map<Integer, HierarchyBean> MG_ID___HIERARCHY = new HashMap<>();

    private Map<String, List<HierarchyBean>> NAME___HIERARCHY = new HashMap<>();

    private Map<Integer, LevelBean> MG_ID___LEVEL = new HashMap<>();

    private Map<Integer, Member> MG_ID___MEMBER = new HashMap<>();

    private Map<String, List<Member>> NAME___MEMBERS = new HashMap<>();

    private Map<String, Cube> NAME___CUBE = new HashMap<>();
    private Map<Integer, Cube> MG_ID___CUBE = new HashMap<>();

    private Map<Integer, List<Member>> MEMBER_MG_ID___CHILDREN = new HashMap<>();

    private Map<Integer, List<DimensionRole>> CUBE_MG_ID___DM_ROLES = new HashMap<>();
    private Map<Integer, DimensionRole> MG_ID___DM_ROLE = new HashMap<>();

    private Map<Integer, List<MemberBean>> DM_MG_ID___MEMBERS = new HashMap<>();

    private MemberDAO memberDao;

    private CubeDAO cubeDAO;

    private DimensionDAO dmDao;

    private HierarchyDAO hiDao;

    private LevelDAO lvDao;

    public SpaceCacheSuite(Space space, MemberDAO md_, CubeDAO cd_, DimensionDAO dd_,
                           HierarchyDAO hiDao_, LevelDAO lvDao_) {
        this.space = space;
        memberDao = md_;
        cubeDAO = cd_;
        dmDao = dd_;
        hiDao = hiDao_;
        lvDao = lvDao_;
    }

    public Member findMember(Member ancestor, MultiDimensionalDomainSelector descendant) {
        List<Member> members = findMembers(ancestor, descendant.getPart(0));
        if (descendant.length() == 1) {
            if (members.size() > 1)
                throw new RuntimeException("too many members.");
            return members.size() < 1 ? null : members.get(0);
        } else { // descendant.length() > 1
            MultiDimensionalDomainSelector clone;
            try {
                clone = descendant.clone().removePart(0);
            } catch (CloneNotSupportedException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
            List<Member> res = new LinkedList<>();
            for (Member m : members) {
                Member tmp = findMember(m, clone);
                if (tmp != null)
                    res.add(tmp);
            }
            if (res.size() > 1)
                throw new RuntimeException("too many members.");
            return res.size() < 1 ? null : res.get(0);
        }
    }

    public List<Member> findMembers(Member ancestor, MultiDimensionalDomainSelector.Part part) {

        List<Member> rt = new LinkedList<>();

        if (part.getMgId() != null) {
            int mgId = (int) ((long) part.getMgId());
            Member m = MG_ID___MEMBER.get(mgId);
            if (m != null && m.hasAncestor(ancestor))
                rt.add(m);
        } else {
            List<Member> ms = findMembersByName(part.getImage());
            for (Member m : ms) {
                if (hasAncestor(m, ancestor))
                    rt.add(m);

            }
        }

        return rt;
    }

    /**
     * if _a is the ancestor of _m, return true, otherwise return false
     *
     * @param _m
     * @param _a
     * @return
     */
    public boolean hasAncestor(Member _m, Member _a) {
        MemberBean m = (MemberBean) _m, a = (MemberBean) _a;
        if (a.getMemberLevel() >= m.getMemberLevel())
            return false;
        do {
            m = (MemberBean) MG_ID___MEMBER.get(m.getParentMemberId().intValue());
            if (m.getMgId().equals(a.getMgId()))
                return true;
        } while (!m.isRoot());
        return false;
    }

    public Member findMember(Dimension dm, MultiDimensionalDomainSelector mbrSel) {
        return findMember(dm.getSuperRootMember(), mbrSel);
    }

    public DimensionRole findDimensionRole(Cube cube, MultiDimensionalDomainSelector.Part drPart) {
        for (DimensionRole dr : CUBE_MG_ID___DM_ROLES.get(cube.getMgId().intValue())) {
            if (dr.getMgId().equals(drPart.getMgId()) || dr.getName().equals(drPart.getImage()))
                return dr;
        }
        return null;
    }

    public MemberRole findMemberRole(Cube cube, MultiDimensionalDomainSelector selector) {

        if (selector.length() == 1)
            return findMemberRole(cube, selector.getPart(0));

        MultiDimensionalDomainSelector cloneSelector;
        try {
            cloneSelector = selector.clone();
        } catch (CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        DimensionRole dmRole = findDimensionRole(cube, selector.getPart(0));
        if (dmRole != null)
            return findMemberRole(dmRole, cloneSelector.removePart(0));

        // todo handle HierarchyRole and LevelRole

        MemberRole ancestor = findMemberRole(cube, selector.getPart(0));
        return findMemberRole(ancestor, cloneSelector.removePart(0));
    }

    public MemberRole findMemberRole(MemberRole ancestor, MultiDimensionalDomainSelector descendant) {
        Member descendantMember = findMember(ancestor.getMember(), descendant);
        return descendantMember == null ? null : new MemberRole(ancestor.getDimensionRole(), descendantMember);
    }

    public MemberRole findMemberRole(DimensionRole dmRole, MultiDimensionalDomainSelector selector) {
        Member m = findMember(MG_ID___DIMENSION.get(((DimensionRoleBean) dmRole).getDimensionId().intValue()), selector);
        if (m != null)
            return new MemberRole(dmRole, m);
        return null;
    }

    public MemberRole findMemberRole(Cube cube, MultiDimensionalDomainSelector.Part part) {
        Long mgId = part.getMgId();
        if (mgId != null)
            return findMemberRole(cube, mgId);
        List<Member> members = findMembersByName(part.getImage());
        for (Member m : members) {
            MemberRole mr = findMemberRole(cube, m.getMgId());
            if (mr != null)
                return mr;
        }
        return null;
    }

    private List<Member> findMembersByName(String mbrName) {
        return NAME___MEMBERS.containsKey(mbrName) ? NAME___MEMBERS.get(mbrName) : new LinkedList<>();
    }

    public MemberRole findMemberRole(Cube cube, Long memberMgId) {
        Member m = MG_ID___MEMBER.get((int) ((long) memberMgId));
        List<DimensionRole> dmRoles = cube.getDimensionRoles(m.getDimension());
        if (dmRoles.size() == 1)
            return new MemberRole(dmRoles.get(0), m);
        if (dmRoles.size() > 1)
            throw new RuntimeException("Too many dimension roles.");
        return null; // dmRoles.size() == 0
    }

    public void build() {
        List<MemberBean> spaceMembers;
        try {
            spaceMembers = memberDao.loadByParams("spaceId", space.getMgId().longValue());
        } catch (OlapException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        for (MemberBean m : spaceMembers) {
            MG_ID___MEMBER.put(m.getMgId().intValue(), m);
            if (!NAME___MEMBERS.containsKey(m.getName()))
                NAME___MEMBERS.put(m.getName(), new LinkedList<>());
            NAME___MEMBERS.get(m.getName()).add(m);
        }

        // init name cube mapping
        List<Cube> cubes = cubeDAO.loadAll();
        for (Cube c : cubes) {
            if (((CubeBean) c).getSpaceId().equals(space.getMgId())) {
                List<DimensionRole> dr_s = dmDao.loadRoles(c);
                NAME___CUBE.put(c.getName(), c);
                MG_ID___CUBE.put(c.getMgId().intValue(), c);
                CUBE_MG_ID___DM_ROLES.put(c.getMgId().intValue(), dr_s);
                for (DimensionRole _dr_ : dr_s)
                    MG_ID___DM_ROLE.put(_dr_.getMgId().intValue(), _dr_);
            }
        }

        // init MEMBER_MG_ID___CHILDREN
        for (Member m : spaceMembers) {
            MemberBean bean = (MemberBean) m;
            if (bean.isRoot())
                continue;
            if (!MEMBER_MG_ID___CHILDREN.containsKey(bean.getParentMemberId().intValue()))
                MEMBER_MG_ID___CHILDREN.put(bean.getParentMemberId().intValue(), new ArrayList<>());
            MEMBER_MG_ID___CHILDREN.get(bean.getParentMemberId().intValue()).add(m);
        }

        // build MG_ID___DIMENSIONS
        List<DimensionBean> dbs = dmDao.loadAll();
        for (DimensionBean d : dbs) {
            if (space.getMgId().equals(d.getSpaceId()))
                MG_ID___DIMENSION.put(d.getMgId().intValue(), d);
        }

        // build DM_MG_ID___MEMBERS
        for (MemberBean b : spaceMembers) {
            if (!DM_MG_ID___MEMBERS.containsKey(b.getDimensionId().intValue()))
                DM_MG_ID___MEMBERS.put(b.getDimensionId().intValue(), new ArrayList<>());
            DM_MG_ID___MEMBERS.get(b.getDimensionId().intValue()).add(b);
        }

        // build MG_ID___HIERARCHY
        List<HierarchyBean> hbs = hiDao.loadByName(null);
        for (HierarchyBean hb : hbs)
            if (MG_ID___DIMENSION.containsKey(hb.getDimensionId().intValue())) {
                MG_ID___HIERARCHY.put(hb.getMgId().intValue(), hb);
                if (!NAME___HIERARCHY.containsKey(hb.getName()))
                    NAME___HIERARCHY.put(hb.getName(), new ArrayList<>());
                NAME___HIERARCHY.get(hb.getName()).add(hb);
            }

        // build MG_ID___LEVEL
        List<LevelBean> lbs = lvDao.loadByName(null);
        for (LevelBean lb : lbs)
            if (MG_ID___HIERARCHY.containsKey(lb.getHierarchyId().intValue()))
                MG_ID___LEVEL.put(lb.getMgId().intValue(), lb);
    }

    public Member findMemberByMgId(int memberMgId) {
        return MG_ID___MEMBER.get(memberMgId);
    }

    public Cube findCubeByName(String cubeName) {
        return NAME___CUBE.get(cubeName);
    }

    public List<Member> findMemberChildren(Member member) {
        return MEMBER_MG_ID___CHILDREN.get(member.getMgId().intValue());
    }

    public Space getSpace() {
        return space;
    }

    public List<MemberBean> getMembers(Dimension d) {
        List<MemberBean> res = new ArrayList<>();
        res.addAll(DM_MG_ID___MEMBERS.get(d.getMgId().intValue()));
        return res;
    }

    public List<DimensionRole> getDimensionRoles(Cube cube) {
        List<DimensionRole> res = new ArrayList<>();
        res.addAll(CUBE_MG_ID___DM_ROLES.get(cube.getMgId().intValue()));
        return res;
    }

    public DimensionRole getMeasureDimensionRole(Cube cube) {
        List<DimensionRole> drs = CUBE_MG_ID___DM_ROLES.get(cube.getMgId().intValue());
        if (drs == null)
            return null;
        Dimension d;
        for (DimensionRole dr : drs) {
            d = MG_ID___DIMENSION.get(((DimensionRoleBean) dr).getDimensionId().intValue());
            if (d.isMeasureDimension())
                return dr;
        }
        throw new RuntimeException("cube has no measure dimension (role).");
    }

    public Dimension findDimensionByMgId(long id) {
        return MG_ID___DIMENSION.get((int) id);
    }

    public boolean isContainsCube(Long cubeId) {
        return CUBE_MG_ID___DM_ROLES.containsKey(cubeId.intValue());
    }

//    public MemberRole selectMemberRole(DimensionRole dr, MultiDimensionalDomainSelector s) {
//        if (s.length() < 1)
//            throw new RuntimeException("selector len < 1");
//        if (s.length() == 1) {
////            return selectMemberRole(dr, s.getPart(0));
//        }
//    }

    public BasicEntityModel selectSingleEntity(Dimension d, Long mgId) {

        // 根据ID返回对象，如果对象为null或其类型不在{hierarchy、level、member}范围内，返回null
        // 如果对象属于维度，返回对象，否则返回null

        Member m = MG_ID___MEMBER.get(mgId.intValue());
        if (m != null)
            return ((MemberBean) m).getDimensionId().equals(d.getMgId()) ? m : null;

        Hierarchy h = MG_ID___HIERARCHY.get(mgId.intValue());
        if (h != null)
            return ((HierarchyBean) h).getDimensionId().equals(d.getMgId()) ? h : null;

        Level l = MG_ID___LEVEL.get(mgId.intValue());
        if (l != null)
            return MG_ID___HIERARCHY.get(((LevelBean) l).getHierarchyId().intValue()).getDimensionId()
                    .equals(d.getMgId()) ? l : null;

        return null;
    }

    public BasicEntityModel selectSingleEntity(Dimension d, String image) {

//        // 优先返回：rootMember
//        Member rootMember = dimension.getSuperRootMember();
//        if (rootMember.getName().equals(part.getImage())) {
//            return rootMember;
//        }

        for (Member m : NAME___MEMBERS.get(image))
            if (((MemberBean) m).getDimensionId().equals(d.getMgId()))
                return m;

////            Hierarchy h = this.getHierarchy(dim, blk.getText());
//        List<Hierarchy> hierarchies = dimension.getHierarchies(part.getImage());
//        if (hierarchies.size() > 1) {
//            throw new RuntimeException("duplicate hierarchies which named '" + part.getImage()
//                    + "' in dimension[mgId = " + dimension.getMgId() + "]");
//        }
//        if (!hierarchies.isEmpty()) {
//            return hierarchies.get(0); // 如果根据name在Dimension对象上查找到的Hierarchy对象不为空，返回Hierarchy对象
//        }

        Iterator<HierarchyBean> lit = MG_ID___HIERARCHY.values().iterator();
        HierarchyBean hb;
        while (lit.hasNext())
            if ((hb = lit.next()).getDimensionId().equals(d.getMgId()) && image.equals(hb.getName()))
                return hb;


//            Hierarchy h = dimension.getHierarchies(part.getImage()).get(0);
//            if (h != null) {
//                return h;
//            }

        // TODO: 如果无法确定Hierarchy，以默认Hierarchy为准(默认Hierarchy下的Level及Member相对于其他Hierarchy下的对象会被优先返回)
        return selectSingleEntity(MG_ID___HIERARCHY.get(((DimensionBean) d).getDefaultHierarchyId().intValue()), image);


//			return this.selectWormholeEntityByPath(dim.getDefaultHierarchy(), blk.getText()); // blk.getId()已为-1

//			return dim.getDefaultHierarchy().s
//			List<Level> ls = dim.getAllLevels();
//			List<Member> ms = dim.getAllMembers();

    }

    public BasicEntityModel selectSingleEntity(HierarchyBean hb, String image) {
        for (Member m : NAME___MEMBERS.get(image))
            if (((MemberBean) m).getHierarchyId().equals(hb.getMgId()) && image.equals(m.getName()))
                return m;
        Iterator<LevelBean> it = MG_ID___LEVEL.values().iterator();
        LevelBean lb;
        while (it.hasNext())
            if ((lb = it.next()).getHierarchyId().equals(hb.getMgId()) && image.equals(lb.getName()))
                return lb;
        return null;
    }

    public BasicEntityModel selectSingleEntity(Dimension d, MultiDimensionalDomainSelector.Part p) {
        return p.getMgId() != null ? selectSingleEntity(d, p.getMgId()) : selectSingleEntity(d, p.getImage());
    }

    public BasicEntityModel selectSingleEntity(Dimension d, MultiDimensionalDomainSelector s) {
        /*
         * BasicEntityModel entity = null;
         * if (当前维度对象.segmentedSelector[0] is superRootMember) {
         *     entity = superRootMember
         * } else if (当前维度对象.segmentedSelector[0] is Hierarchy) {
         *     entity = Hierarchy
         * } else if (当前维度对象.segmentedSelector[0] is Level) {
         *     entity = Level
         * } else if (当前维度对象.segmentedSelector[0] is not superRootMember) {
         *     entity = not superRootMember
         * }
         * if (segmentedSelector length == 1) {
         *     return entity
         * } else {
         *     return entity.selectSingleEntity(segmentedSelector[1... end])
         * }
         */
        BasicEntityModel e = selectSingleEntity(d, s.getPart(0));
        return e == null ? e :
                (s.length() == 1 ? e : selectSingleEntity(e, s.subSelector(1)));
    }

    public BasicEntityModel selectSingleEntity(BasicEntityModel e, MultiDimensionalDomainSelector s) {
        if (e instanceof MemberBean)
            return selectSingleEntity((MemberBean) e, s);
        if (e instanceof HierarchyBean)
            return selectSingleEntity((HierarchyBean) e, s);
        if (e instanceof LevelBean)
            return selectSingleEntity((LevelBean) e, s);
        return null;
    }

    public BasicEntityModel selectSingleEntity(MemberBean mb, MultiDimensionalDomainSelector s) {
        BasicEntityModel e = selectSingleEntity(mb, s.getPart(0));
        return e == null ? e : (s.length() == 1 ? e : selectSingleEntity((MemberBean) e, s.removePart(0)));
    }

    public BasicEntityModel selectSingleEntity(MemberBean mb, MultiDimensionalDomainSelector.Part part) {
        if (part.getMgId() != null) {
            MemberBean pm = (MemberBean) MG_ID___MEMBER.get(part.getMgId().intValue());
            return pm == null ? null : (hasAncestor(pm, mb) ? pm : null);
        } else {
            for (Member bean : NAME___MEMBERS.get(part.getImage()))
                if (hasAncestor(bean, mb))
                    return bean;
        }
        return null;
    }

    public BasicEntityModel selectSingleEntity(HierarchyBean hb, MultiDimensionalDomainSelector s) {
        BasicEntityModel e = selectSingleEntity(hb, s.getPart(0));
        if (e == null || s.length() == 1)
            return e;
        if (e instanceof MemberBean)
            return selectSingleEntity((MemberBean) e, s.removePart(0));
        if (e instanceof LevelBean)
            return selectSingleEntity((LevelBean) e, s.removePart(0));
        return null;
    }

    public BasicEntityModel selectSingleEntity(HierarchyBean hb, MultiDimensionalDomainSelector.Part part) {
        if (part.getMgId() != null) {
            MemberBean m = (MemberBean) MG_ID___MEMBER.get(part.getMgId().intValue());
            if (m.getHierarchyId().equals(hb.getMgId()))
                return m;
            LevelBean l = MG_ID___LEVEL.get(part.getMgId().intValue());
            if (l.getHierarchyId().equals(hb.getMgId()))
                return l;
        } else {
            for (Member bean : NAME___MEMBERS.get(part.getImage()))
                if (((MemberBean) bean).getHierarchyId().equals(hb.getMgId()))
                    return bean;
            Iterator<LevelBean> itt = MG_ID___LEVEL.values().iterator();
            LevelBean lb;
            while (itt.hasNext())
                if ((lb = itt.next()).getHierarchyId().equals(hb.getMgId()))
                    return lb;
        }
        return null;
    }

    public BasicEntityModel selectSingleEntity(LevelBean lb, MultiDimensionalDomainSelector s) {
        BasicEntityModel e = selectSingleEntity(lb, s.getPart(0));
        return e == null ? e : (s.length() == 1 ? e : selectSingleEntity((MemberBean) e, s.removePart(0)));
    }

    public BasicEntityModel selectSingleEntity(LevelBean lb, MultiDimensionalDomainSelector.Part part) {
        if (part.getMgId() != null) {
            MemberBean pm = (MemberBean) MG_ID___MEMBER.get(part.getMgId().intValue());
            if (pm != null
                    && pm.getHierarchyId().equals(lb.getHierarchyId())
                    && pm.getMemberLevel().equals(lb.getMemberLevel()))
                return pm;
        } else {
            for (Member bean : NAME___MEMBERS.get(part.getImage()))
                if (((MemberBean) bean).getHierarchyId().equals(lb.getHierarchyId())
                        && ((MemberBean) bean).getMemberLevel().equals(lb.getMemberLevel()))
                    return bean;
        }
        return null;
    }

    public BasicEntityModel selectSingleEntity(DimensionRole dr, MultiDimensionalDomainSelector s) {
        Dimension d = MG_ID___DIMENSION.get(((DimensionRoleBean) dr).getDimensionId().intValue());
        BasicEntityModel e = selectSingleEntity(d, s);
        if (e instanceof Hierarchy) {
            return new HierarchyRole(dr, ((Hierarchy) e));
        } else if (e instanceof Level) {
            return new LevelRole(dr, ((Level) e));
        } else if (e instanceof Member) {
            return new MemberRole(dr, ((Member) e));
        }/* else {
            throw new RuntimeException("Illegal class [" + e.getClass().getName() + "]");
        }*/
        return null;
    }

    public Member findHierarchyDefaultMember(Long hierarchyId) {
        HierarchyBean h = MG_ID___HIERARCHY.get(hierarchyId.intValue());
        return h == null ? null : MG_ID___MEMBER.get(h.getDefaultMemberId().intValue());
    }

    public Cube findCubeByMgId(Long mgId) {
        return MG_ID___CUBE.get(mgId.intValue());
    }

    public DimensionRole findDimensionRoleByMgId(Long mgId) {
        return MG_ID___DM_ROLE.get(mgId.intValue());
    }

    public HierarchyBean findHierarchyByMgId(Long mgId) {
        return MG_ID___HIERARCHY.get(mgId.intValue());
    }

    public LevelBean findLevelByMgId(Long mgId) {
        return MG_ID___LEVEL.get(mgId.intValue());
    }

    public List<HierarchyBean> findHierarchiesByName(String name) {
        List<HierarchyBean> res = new ArrayList<>();
        if (name == null)
            res.addAll(MG_ID___HIERARCHY.values());
        else if (NAME___HIERARCHY.containsKey(name))
            res.addAll(NAME___HIERARCHY.get(name));
        return res;
    }

    public List<LevelBean> findAllLevels() {
        return new LinkedList<>(MG_ID___LEVEL.values());
    }

    public List<LevelBean> findLevelsByName(String name) {
        List<LevelBean> res = new ArrayList<>();
        for (Iterator<LevelBean> it = MG_ID___LEVEL.values().iterator(); it.hasNext(); ) {
            LevelBean lv = it.next();
            if (lv.getName().equals(name))
                res.add(lv);
        }
        return res;


    }
}
