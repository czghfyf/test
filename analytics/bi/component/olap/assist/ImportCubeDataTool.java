package cn.bgotech.analytics.bi.component.olap.assist;

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
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.mddm.CubeDAO;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.analytics.bi.system.config.SystemConfiguration;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.bigdata.MDVectorSet;
import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.imp.text.RawCubeDataPackage;
import cn.bgotech.wormhole.olap.imp.text.VectorLine;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.DateDimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.MeasureDimension;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

@Deprecated
public class ImportCubeDataTool {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CubeDAO cubeDAO;
    // private final DimensionStore dimensionStore;
    // private final HierarchyStore hierarchyStore;
    // private final LevelStore levelStore;
    // private final MemberStore memberStore;

    private Space space;
    private String cubeName;
    private RawCubeDataPackage rawCubeDataPackage;

    private BeanFactory beanFactory;

    private SystemConfiguration sysCfg;


    private Map<UniversalDimensionBean, Map<VectorLine.MemberTrail, UniversalMemberBean>>
            _universalDimensionTrailMemberMap_ = new HashMap<>();

//    public ImportCubeDataTool(Space space, String cubeName, RawCubeDataPackage rawCubeDataPackage,
//                    BeanFactory beanFactory, CubeDAO cubeDAO, DimensionDAO dimensionDAO, HierarchyDAO hierarchyDAO,
//                            LevelDAO levelDAO, MemberDAO memberDAO, SystemConfiguration sysCfg) {
//        this.space = space;
//        this.cubeName = cubeName;
//        this.rawCubeDataPackage = rawCubeDataPackage;
//        this.beanFactory = beanFactory;
//
//        this.cubeDAO = cubeDAO;
//        this.dimensionDAO = dimensionDAO;
//        this.hierarchyDAO = hierarchyDAO;
//        this.levelDAO = levelDAO;
//        this.memberDAO = memberDAO;
//
//        this.sysCfg = sysCfg;
//    }

    public ImportCubeDataTool(Space space, String cubeName, RawCubeDataPackage rawCubeDataPackage,
                              BeanFactory beanFactory, CubeDAO cubeDAO, /*DimensionStore dimensionStore,*/ /*HierarchyStore hierarchyStore,*/
                              /*LevelStore levelStore,*/ /*MemberStore memberStore,*/ SystemConfiguration sysCfg) {
        this.space = space;
        this.cubeName = cubeName;
        this.rawCubeDataPackage = rawCubeDataPackage;
        this.beanFactory = beanFactory;

        this.cubeDAO = cubeDAO;
        // this.dimensionStore = dimensionStore;
        // this.hierarchyStore = hierarchyStore;
        // this.levelStore = levelStore;
        // this.memberStore = memberStore;

        this.sysCfg = sysCfg;
    }

    private void processUniversalDimensionInfo(String dimensionName, String dimensionRoleName, DataTempStore tempStore, CubeBean cubeBean) {

        logger.info("$>>>>>>>>>>>> import cube data: process UniversalDimensionBean begin, dimension: " + dimensionName + ", role: " + dimensionRoleName);

        // universal dimension
        UniversalDimensionBean udb;
        HierarchyBean hierarchyBean;
        UniversalMemberBean superRoot;

        boolean universalDimensionExist = tempStore.containsUniversalDimension(dimensionName);

        if (universalDimensionExist) {
            udb = tempStore.findUniversalDimensionBean(dimensionName);
            hierarchyBean = tempStore.findUniqueHierarchy(dimensionName);
            superRoot = tempStore.findRootMember(udb);
        } else {
            udb = (UniversalDimensionBean) beanFactory.create(UniversalDimensionBean.class);
            udb.setName(dimensionName);
            udb.setSpaceId(space.getMgId());

            // universal dimension hierarchy
            hierarchyBean = (HierarchyBean) beanFactory.create(HierarchyBean.class);
            udb.setDefaultHierarchyId(hierarchyBean.getMgId());

            /** universal dimension complete */
            tempStore.add(udb);

            hierarchyBean.setDimensionId(udb.getMgId());
            hierarchyBean.setName(udb.defaultHierarchyName());

            // create super root member
            superRoot = (UniversalMemberBean) beanFactory.create(UniversalMemberBean.class);
            superRoot.setName(udb.defaultSuperRootName());
            superRoot.setDimensionId(udb.getMgId());
            superRoot.setSpaceId(space.getMgId());
            /** universal super root member is complete */
            tempStore.add(superRoot);
        }

        // universal dimension role
        DimensionRoleBean dimensionRoleBean = (DimensionRoleBean) beanFactory.create(DimensionRoleBean.class);
        dimensionRoleBean.setName(dimensionRoleName);
        dimensionRoleBean.setCubeId(cubeBean.getMgId());
        dimensionRoleBean.setDimensionId(udb.getMgId());
        /** universal dimension role has been complete */
        tempStore.add(dimensionRoleBean);

        UniversalMemberBean hierarchyDefaultMember
                = generateUniversalMembersAndLevels(udb, dimensionRoleBean, hierarchyBean, superRoot, tempStore);
        if (hierarchyDefaultMember != null) {
            hierarchyBean.setDefaultMemberId(hierarchyDefaultMember.getMgId());
        }

        if (!universalDimensionExist) {
            /** universal hierarchy complete */
            tempStore.add(hierarchyBean);
        }
    }

    private void processPresetDimensionInfo(String dimensionName, String dimensionRoleName, DataTempStore tempStore, CubeBean cubeBean) {
//        for (DimensionBean dimension : dimensionStore.loadAll()) {
//            if (dimension.isPresetDimension() && dimension.getName().equals(dimensionName)) {
//                // preset dimension role
//                DimensionRoleBean dimensionRoleBean = (DimensionRoleBean) beanFactory.create(DimensionRoleBean.class);
//                dimensionRoleBean.setName(dimensionRoleName);
//                dimensionRoleBean.setCubeId(cubeBean.getMgId());
//                dimensionRoleBean.setDimensionId(dimension.getMgId());
//                /** preset dimension role has been complete */
//                tempStore.add(dimensionRoleBean);
//                if (dimension instanceof RegionDimension && !_universalDimensionTrailMemberMap_.containsKey(dimension)) {
////                    RegionDimension regionDimension = (RegionDimension) dimension;
//                    // regionMembers doesn't contain root member
//
//                    Map<VectorLine.MemberTrail, UniversalMemberBean> map = new HashMap();
//
//                    List<Member> regionMembers = dimension.getAllMembers().stream().filter(r -> !r.isRoot()).collect(Collectors.toList());
//                    for (Member m : regionMembers) {
////                        RegionMember regionMember = (RegionMember) m;
////                        List<String> namesPath = m.getFullNamesPath();
//                        map.put(new VectorLine.MemberTrail(m.getFullNamesPath()), (UniversalMemberBean) m);
//                    }
//
//                    _universalDimensionTrailMemberMap_.put((RegionDimensionBean) dimension, map);
//                }
//                return;
//            }
//        }
        throw new BIRuntimeException(String.format("no preset dimension named '%s'", dimensionName));
    }

    private void processMeasureDimensionInfo(DataTempStore tempStore, CubeBean cubeBean) {

        // measure dimension
        MeasureDimensionBean measureDimensionBean = (MeasureDimensionBean) beanFactory.create(MeasureDimensionBean.class);
//        measureDimensionBean.setBinaryControlFlag(2);
        measureDimensionBean.setName(MeasureDimension.DEFAULT_NAME);
        measureDimensionBean.setSpaceId(space.getMgId());

        HierarchyBean measureDimHierarchy = (HierarchyBean) beanFactory.create(HierarchyBean.class);
        measureDimHierarchy.setName(measureDimensionBean.defaultHierarchyName());
        measureDimHierarchy.setDimensionId(measureDimensionBean.getMgId());
        measureDimensionBean.setDefaultHierarchyId(measureDimHierarchy.getMgId());

        /** measure dimension complete */
        tempStore.add(measureDimensionBean);

        MeasureMemberBean measureSuperRoot = (MeasureMemberBean) beanFactory.create(MeasureMemberBean.class);
        measureSuperRoot.setName(measureDimensionBean.defaultSuperRootName());
        measureSuperRoot.setDimensionId(measureDimensionBean.getMgId());
        measureSuperRoot.setBinaryControlFlag(2);
        measureSuperRoot.setSpaceId(space.getMgId());
        /** measure super root member is completed */
        tempStore.add(measureSuperRoot);

        LevelBean measureLevelBean = (LevelBean) beanFactory.create(LevelBean.class);
        measureLevelBean.setName(Level.generateSimpleName(1));
        measureLevelBean.setHierarchyId(measureDimHierarchy.getMgId());
        measureLevelBean.setMemberLevel(1);
        /** measure level is complete */
        tempStore.add(measureLevelBean);

//      List<MeasureMemberBean> measureMemberBeans = new LinkedList<>();
        for (int i = 0; i < rawCubeDataPackage.getMeasureMemberNameList().size(); i++) {
            MeasureMemberBean mmb = (MeasureMemberBean) beanFactory.create(MeasureMemberBean.class);
            mmb.setName(rawCubeDataPackage.getMeasureMemberNameList().get(i));
            mmb.setDimensionId(measureDimensionBean.getMgId());
            mmb.setBinaryControlFlag(3);
            mmb.setParentMemberId(measureSuperRoot.getMgId());
            mmb.setHierarchyId(measureDimHierarchy.getMgId());
            mmb.setMemberLevel(1);
            mmb.setSpaceId(space.getMgId());
            if (i == 0) {
                measureDimHierarchy.setDefaultMemberId(mmb.getMgId());
                /** measure hierarchy is complete */
                tempStore.add(measureDimHierarchy);
            }
            /** measure member(which not be super root) has been completed */
            tempStore.add(mmb);
        }

        // measure dimension role
        DimensionRoleBean measureDimensionRoleBean = (DimensionRoleBean) beanFactory.create(DimensionRoleBean.class);
        measureDimensionRoleBean.setName(MeasureDimension.DEFAULT_ROLE_NAME);
        measureDimensionRoleBean.setDimensionId(measureDimHierarchy.getDimensionId());
        measureDimensionRoleBean.setCubeId(cubeBean.getMgId());
        /** measure dimension role has been completed */
        tempStore.add(measureDimensionRoleBean);
    }

    public void execute() throws OlapException {

        logger.info("$>>>>>>>>>>>> import cube data: execute !!!");

        CubeBean cubeBean = (CubeBean) beanFactory.create(CubeBean.class);
        while (OlapEngine.hold().getVectorComputingEngine().existMDVectorSet(MDVectorSet.fileName(cubeBean))) {
            cubeBean = (CubeBean) beanFactory.create(CubeBean.class);
        }
        cubeBean.setSpaceId(space.getMgId());
        cubeBean.setName(cubeName);
        /** cube is completed. create a new DataTempStore object. */

        DataTempStore tempStore = new DataTempStore(cubeBean);

        for (int i = 0; i < rawCubeDataPackage.getUniversalDimensions().size(); i++) {

            String dimensionName = rawCubeDataPackage.getUniversalDimensions().get(i).get("DIMENSION");
            String dimensionRoleName = rawCubeDataPackage.getUniversalDimensions().get(i).get("DIMENSION_ROLE");

            if (rawCubeDataPackage.hasPresetDimension(dimensionName)) {
                processPresetDimensionInfo(dimensionName, dimensionRoleName, tempStore, cubeBean);
            } else {
                processUniversalDimensionInfo(dimensionName, dimensionRoleName, tempStore, cubeBean);
            }

        }

        processMeasureDimensionInfo(tempStore, cubeBean);

        tempStore._import();
    }


    Map<UniversalDimensionBean, Set<VectorLine.MemberTrail>> dimensionMemberTrailMap = new HashMap<>();

    // generate universal member data that not contain super root member
    private UniversalMemberBean generateUniversalMembersAndLevels
    (UniversalDimensionBean udb, DimensionRoleBean dimensionRole, HierarchyBean hb,
     UniversalMemberBean superRoot, DataTempStore tempStore) {

        if (!dimensionMemberTrailMap.containsKey(udb)) {
            dimensionMemberTrailMap.put(udb, new HashSet<>());
        }

        int levelDeep = 1;

        Set<VectorLine.MemberTrail> memberTrailSet = new HashSet<>();
        for (VectorLine line : rawCubeDataPackage.getLines()) {
            VectorLine.MemberTrail trail
                    = line.getMemberTrail(rawCubeDataPackage.getDimensionRolePosition(dimensionRole.getName()));
            if (!dimensionMemberTrailMap.get(udb).contains(trail)) {
                memberTrailSet.add(trail);
                dimensionMemberTrailMap.get(udb).add(trail);
                levelDeep = trail.getMemberLevel() > levelDeep ? trail.getMemberLevel() : levelDeep;
                for (VectorLine.MemberTrail ancestor : trail.getAncestorTrails()) {
                    memberTrailSet.add(ancestor);
                    dimensionMemberTrailMap.get(udb).add(ancestor);
                }

            }
        }

        for (int i = 1; i <= levelDeep; i++) {
            if (!tempStore.containsLevel(hb, i)) {
                // create levels
                LevelBean levelBean = (LevelBean) beanFactory.create(LevelBean.class);
                levelBean.setName(Level.generateSimpleName(i));
                levelBean.setHierarchyId(hb.getMgId());
                levelBean.setMemberLevel(i);
                /** create universal level complete */
                tempStore.add(levelBean);
            }
        }

        List<VectorLine.MemberTrail> trailList = new ArrayList<>(memberTrailSet);
        Collections.sort(trailList);

        Map<VectorLine.MemberTrail, UniversalMemberBean> trailMemberMap = new HashMap<>();
        if (!_universalDimensionTrailMemberMap_.containsKey(udb)) {
            _universalDimensionTrailMemberMap_.put(udb, new HashMap<>());
        }

        UniversalMemberBean umb;
        for (int i = 0; i < trailList.size(); i++) {
            umb = (UniversalMemberBean) beanFactory.create(UniversalMemberBean.class);
            umb.setName(trailList.get(i).getMemberName());
            umb.setDimensionId(udb.getMgId());
            umb.setHierarchyId(hb.getMgId());
            umb.setMemberLevel(trailList.get(i).getMemberLevel());
            umb.setSpaceId(space.getMgId());
            trailMemberMap.put(trailList.get(i), umb);
        }
        _universalDimensionTrailMemberMap_.get(udb).putAll(trailMemberMap);
        // _universalDimensionTrailMemberMap_.put(udb, trailMemberMap);

//      trailMemberMapList.add(trailMemberMap); // TODO: what is the "trailMemberMapList" ?

        List<UniversalMemberBean> umbs = new ArrayList<>(trailList.size());

        for (int i = 0; i < trailList.size(); i++) {
            VectorLine.MemberTrail trail = trailList.get(i);
            umb = trailMemberMap.get(trail);
            UniversalMemberBean parent = trail.getParentMemberTrail() == null ?
                    superRoot : _universalDimensionTrailMemberMap_.get(udb).get(trail.getParentMemberTrail());
            umb.setParentMemberId(parent.getMgId());
            parent.setBinaryControlFlag(0); // set flag to mean that it not leaf member
            umbs.add(umb);
        }

        /** universal members are complete */
        tempStore.add(umbs);

        return umbs.isEmpty() ? null : umbs.get(0);
    }

    private class DataTempStore {

        private CubeBean cubeBean;

        private List<HierarchyBean> hierarchyBeanList = new ArrayList<>();
        private List<LevelBean> levelBeanList = new ArrayList<>();
        private List<DimensionRoleBean> dimensionRoleBeanList = new ArrayList<>();

        private List<UniversalDimensionBean> universalDimensionBeanList = new ArrayList<>();
        private List<UniversalMemberBean> universalMemberBeanList = new ArrayList<>();

        private MeasureDimensionBean measureDimensionBean;
        private List<MeasureMemberBean> measureMemberBeanList = new ArrayList<>();

        DataTempStore(CubeBean cubeBean) {
            this.cubeBean = cubeBean;
        }

        void add(DimensionBean db) {
            if (db == null) {
                throw new BIRuntimeException("DimensionBean param db is null");
            }
            if (MeasureDimensionBean.class.equals(db.getClass())) {
                measureDimensionBean = (MeasureDimensionBean) db;
            } else if (UniversalDimensionBean.class.equals(db.getClass())) {
                universalDimensionBeanList.add((UniversalDimensionBean) db);
            } else {
                throw new BIRuntimeException("not supported class type: " + db.getClass());
            }
        }

        void add(HierarchyBean hb) {
            hierarchyBeanList.add(hb);
        }

        void add(LevelBean lb) {
            levelBeanList.add(lb);
        }

        void add(List<UniversalMemberBean> mbs) {
            for (UniversalMemberBean umb : mbs) {
                add(umb);
            }
        }

        void add(MemberBean mb) {
            if (mb == null) {
                throw new BIRuntimeException("MemberBean param mb is null");
            }
            if (MeasureMemberBean.class.equals(mb.getClass())) {
                measureMemberBeanList.add((MeasureMemberBean) mb);
            } else if (UniversalMemberBean.class.equals(mb.getClass())) {
                universalMemberBeanList.add((UniversalMemberBean) mb);
            } else {
                throw new BIRuntimeException("not supported class type: " + mb.getClass());
            }
        }

        void add(DimensionRoleBean drb) {
            dimensionRoleBeanList.add(drb);
        }

        void _import() {
            cubeDAO.save(cubeBean);
            for (UniversalDimensionBean udb : universalDimensionBeanList) {
                // dimensionStore.save(udb);
            }
            // dimensionStore.save(measureDimensionBean);
            for (HierarchyBean hb : hierarchyBeanList) {
                // hierarchyStore.save(hb);
            }
            for (LevelBean lb : levelBeanList) {
                // levelStore.save(lb);
            }
            for (MemberBean mb : universalMemberBeanList) {
                // memberStore.save(mb);
            }
            for (MemberBean mb : measureMemberBeanList) {
                // memberStore.save(mb);
            }
            for (DimensionRoleBean drb : dimensionRoleBeanList) {
                // dimensionStore.saveRole(drb);
            }

            createMDVectorSet();

        }

        private UniversalDimensionBean findUniversalDimensionBean(DimensionRoleBean dimensionRole) {
            for (UniversalDimensionBean udb : universalDimensionBeanList) {
                if (dimensionRole.getDimensionId().equals(udb.getMgId())) {
                    return udb;
                }
            }
            return null;
        }

        UniversalDimensionBean findUniversalDimensionBean(String dimensionName) {
            for (UniversalDimensionBean udb : universalDimensionBeanList) {
                if (udb.getName().equals(dimensionName)) {
                    return udb;
                }
            }
            return null;
        }

        private UniversalMemberBean searchUniversalMemberBeanByTrail(DimensionRoleBean dimensionRoleBean, VectorLine.MemberTrail memberTrail) {
            Dimension dimension = findUniversalDimensionBean(dimensionRoleBean);
            if (dimension == null) {
                dimension = dimensionRoleBean.getDimension(); // predefined dimension
            }
//            // RegionDimension
//            if (!(dimension instanceof DateDimension)) {
//                result = _universalDimensionTrailMemberMap_.get(dimension).get(memberTrail);
//            }
//            // DateDimension
//            result = (UniversalMemberBean) ((DateDimension) dimension).findMemberByFullNamesPath(memberTrail.toList());

            UniversalMemberBean result = dimension instanceof DateDimension
                    ? (UniversalMemberBean) ((DateDimension) dimension).findMemberByFullNamesPath(memberTrail.toList()) // DateDimension
                    : _universalDimensionTrailMemberMap_.get(dimension).get(memberTrail);

            if (!result.isLeaf()) {
                throw new BIRuntimeException(String.format
                        ("UniversalMemberBean [mgId = %d, name = %s] is not a leaf member", result.getMgId(), result.getName()));
            }

            return result;
        }

        private void createMDVectorSet() {

            List<VectorLine> lines = rawCubeDataPackage.getLines();
            MDVectorSet vs = new MDVectorSet(cubeBean, lines.size());

            long[] universalDimensionMemberArray;
            double[] measureValueArray;
            int nullMeasureFlag = 0;
            for (VectorLine vl : lines) {
                universalDimensionMemberArray = new long[dimensionRoleBeanList.size() - 1]; // exclude measure dimension role
                for (int j = 0; j < universalDimensionMemberArray.length; j++) {
                    universalDimensionMemberArray[j] = searchUniversalMemberBeanByTrail(dimensionRoleBeanList.get(j), vl.getMemberTrail(j)).getMgId();
                }
                measureValueArray = new double[measureMemberBeanList.size() - 1]; // exclude super root member
                for (int k = 0; k < measureValueArray.length; k++) {
                    if (vl.getMeasureValue(k) != null) {
                        measureValueArray[k] = vl.getMeasureValue(k);
                    } else {
                        nullMeasureFlag = (1 << (measureValueArray.length - k - 1)) | nullMeasureFlag;
                    }
                }
                vs.addVector(universalDimensionMemberArray, measureValueArray, nullMeasureFlag);
            }

//            File mdvs = new File(sysCfg.getMdvsFolder() + "/cube-" + cubeBean.getMgId() + ".mdvs");
            File mdvs = new File(sysCfg.getMdvsFolder() + "/" + MDVectorSet.fileName(cubeBean));
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(new FileOutputStream(mdvs));
                oos.writeObject(vs);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new BIRuntimeException(e);
            } finally {
                try {
                    oos.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    throw new BIRuntimeException(e);
                }
            }
        }

        boolean containsUniversalDimension(String dimensionName) {
            for (UniversalDimensionBean udb : universalDimensionBeanList) {
                if (udb.getName().equals(dimensionName)) {
                    return true;
                }
            }
            return false;
        }

        HierarchyBean findUniqueHierarchy(String dimensionName) {
            UniversalDimensionBean dimension = findUniversalDimensionBean(dimensionName);
            for (HierarchyBean h : hierarchyBeanList) {
                if (dimension.getMgId().equals(h.getDimensionId())) {
                    return h;
                }
            }
            return null;
        }

        UniversalMemberBean findRootMember(UniversalDimensionBean udb) {
            for (UniversalMemberBean member : universalMemberBeanList) {
                if (member.getMemberLevel() == 0 && member.getDimensionId().equals(udb.getMgId())) {
                    return member;
                }
            }
            return null;
        }

        boolean containsLevel(HierarchyBean hierarchy, int levelValue) {
            for (LevelBean level : levelBeanList) {
                if (level.getHierarchyId().equals(hierarchy.getMgId()) && level.getLevelValue() == levelValue) {
                    return true;
                }
            }
            return false;
        }
    }

}
