package cn.bgotech.analytics.bi.component.olap.assist;

import cn.bgotech.analytics.bi.bean.mddm.physical.HierarchyBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.LevelBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.DateDimensionBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.RegionDimensionBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.UniversalDimensionBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.RegionMemberBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.UniversalMemberBean;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.mddm.DimensionDAO;
import cn.bgotech.analytics.bi.dao.mddm.HierarchyDAO;
import cn.bgotech.analytics.bi.dao.mddm.LevelDAO;
import cn.bgotech.analytics.bi.dao.mddm.MemberDAO;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.DateDimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.RegionDimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.UniversalDimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.RegionMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class OLAPPresetDataBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DimensionDAO dimensionDAO;
    private final HierarchyDAO hierarchyDAO;
    private final LevelDAO levelDAO;
    private final MemberDAO memberDao;

    private BeanFactory beanFactory;

    private RegionMemberBean world = null;
    private RegionMemberBean asia = null;
    private RegionMemberBean china = null;

//    private SpaceBean publicSpace;
//    private SequencesManager sequencesManager;
//    private MDDMPersistentDao mddmPersistentDao;

    private Map<String, RegionMemberBean> codeRegionMap = new HashMap<>();
    private Map<Long, RegionMemberBean> idRegionMap = new HashMap<>();
    private Set<RegionMemberBean> regions = new HashSet<>(); // province、city、county

    public OLAPPresetDataBuilder(BeanFactory beanFactory, DimensionDAO dmDao_, HierarchyDAO hiyDao, MemberDAO mbrDao, LevelDAO lvDao) {
        this.beanFactory = beanFactory;
        this.dimensionDAO = dmDao_;
        this.hierarchyDAO = hiyDao;
        memberDao = mbrDao;
        this.levelDAO = lvDao;
    }

    //    public OLAPPresetDimensionInfoGenerator(SpaceBean publicSpace,
//                                            SequencesManager sequencesManager, MDDMPersistentDao mddmPersistentDao) {
//        this.publicSpace = publicSpace;
//        this.sequencesManager = sequencesManager;
//        this.mddmPersistentDao = mddmPersistentDao;
//    }

    /**
     * create RegionDimension and DateDimension, and hierarchy、level、member...
     */
    public void build() {
        /*
         * date dimension
         *     default hierarchy
         *     levels
         *     members
         */
        createSystemPresetDimensionInfo(DateDimension.class);

        /*
         * region dimension
         *     default hierarchy
         *     levels
         *     members
         */
        createSystemPresetDimensionInfo(RegionDimension.class);
    }

    private void createSystemPresetDimensionInfo(Class<? extends UniversalDimension> clazz) {

        UniversalDimensionBean universalDimensionBean; // preset dimension
        HierarchyBean hierarchyBean; // preset dimension default hierarchy
        UniversalMemberBean superRootMember = null; // preset dimension super root member

        if (DateDimension.class.equals(clazz)) {
            logger.info("create " + clazz.getSimpleName() + " and relational");

            universalDimensionBean = (DateDimensionBean) beanFactory.create(DateDimensionBean.class);
            universalDimensionBean.setName(DateDimension.defaultName()); // DimensionBean definition
//            universalDimensionBean.setCommonBinaryFlag(2);

            hierarchyBean = (HierarchyBean) beanFactory.create(HierarchyBean.class);
            hierarchyBean.setName(universalDimensionBean.defaultHierarchyName());
//          hierarchyBean.setDefaultMemberId();
//            hierarchyBean.setDimensionId(universalDimensionBean.getMgId());
//            hierarchyBean.setCommonBinaryFlag(2);

//            universalDimensionBean.setDefaultHierarchyId(hierarchyBean.getMgId());

//                        presetDimBean = new DateDimensionBean();
//                        presetDimBean.setName(DateDimension.NAME);
//
//                        presetHichyBean.setName(DateDimension.DEFAULT_HIERARCHY_NAME); // BasicEntityBean property
//                        presetHichyBean.setDefaultMemberId(-1L);

        } else if (RegionDimension.class.equals(clazz)) {
            logger.info("create " + clazz.getSimpleName() + " and relational");

            universalDimensionBean = (RegionDimensionBean) beanFactory.create(RegionDimensionBean.class);
            universalDimensionBean.setName(RegionDimension.defaultName()); // DimensionBean definition
//            universalDimensionBean.setCommonBinaryFlag(2);

            hierarchyBean = (HierarchyBean) beanFactory.create(HierarchyBean.class);
            hierarchyBean.setName(universalDimensionBean.defaultHierarchyName());
//            hierarchyBean.setDefaultMemberId();
//            hierarchyBean.setDimensionId(universalDimensionBean.getMgId());
//            hierarchyBean.setCommonBinaryFlag(2);

//            universalDimensionBean.setDefaultHierarchyId(hierarchyBean.getMgId());

            RegionMemberBean superRoot_ = (RegionMemberBean) beanFactory.create(RegionMemberBean.class);
            superRoot_.setDimensionId(universalDimensionBean.getMgId());
            superRoot_.setName(universalDimensionBean.defaultSuperRootName());
            superRoot_.setCommonBinaryFlag(2);

            superRootMember = superRoot_;


//                        presetDimBean = new RegionDimensionBean();
//                        presetDimBean.setName(RegionDimension.NAME);
//
//                        regionMemberBean.setRegionCode(null);
//                        superRoot = regionMemberBean;
//                        superRoot.setName("REGION_ROOT");
//
//                        presetHichyBean.setName(RegionDimension.DEFAULT_HIERARCHY_NAME); // BasicEntityBean property

        } else {
            logger.error("unknown system preset dimension class: " + clazz);
            throw new BIRuntimeException("unknown system preset dimension class: " + clazz);
        }

//        presetDimBean.setBinaryControlFlag(0x0);
        universalDimensionBean.setCommonBinaryFlag(2);
        hierarchyBean.setDimensionId(universalDimensionBean.getMgId());
        hierarchyBean.setCommonBinaryFlag(2); // Bean property

        universalDimensionBean.setDefaultHierarchyId(hierarchyBean.getMgId()); // DimensionBean property


        if (DateDimension.class.equals(clazz)) {
            createDateLevels(hierarchyBean);
        } else if (RegionDimension.class.equals(clazz)) {

//            superRoot.setMddmGlobalId(sequencesManager.nextMDDMGlobalId());
//            superRoot.setParentMemberId(-1L);
//            superRoot.setDimensionId(presetDimBean.getMddmGlobalId());
//            superRoot.setHierarchyId(presetHichyBean.getMddmGlobalId());
//            superRoot.setMemberLevel((byte) 0); // Byte
//            superRoot.setBinaryControlFlag(/* binary: 00 */ 0); // Integer
//            superRoot.setCommonBinaryFlag(0x2);


            RegionMemberBean defaultMember = createRegionLevelsAndMembers(hierarchyBean, (RegionMemberBean) superRootMember);
            hierarchyBean.setDefaultMemberId(defaultMember.getMgId());


//                        MemberBean regionDimensionDefaultHierarchyDefaultMember
//                                = createRegionLevelsAndMembers(presetHichyBean, superRoot);
//                        // if region dimension relational, must be a region member id
//                        presetHichyBean.setDefaultMemberId(regionDimensionDefaultHierarchyDefaultMember.getMddmGlobalId());


//            this.mddmPersistentDao.insertMember(superRoot); // save preset dimension super root member
            memberDao.save(superRootMember);
        }

//        this.mddmPersistentDao.insertDimension(presetDimBean); // save preset dimension
//        this.mddmPersistentDao.insertHierarchy(presetHichyBean); // save preset dimension default hierarchy

        dimensionDAO.save(universalDimensionBean);
        hierarchyDAO.save(hierarchyBean);

    }

    private void createDateLevels(HierarchyBean hierarchy) {
        // save levels
        for (int i = 0; i <DateDimension.DEFAULT_LEVEL_NAMES.length; i++) {
            LevelBean lb = (LevelBean) beanFactory.create(LevelBean.class);
            lb.setName(DateDimension.DEFAULT_LEVEL_NAMES[i]);
            lb.setHierarchyId(hierarchy.getMgId());
            lb.setMemberLevel(i + 1);
            lb.setCommonBinaryFlag(2);
            levelDAO.save(lb);
        }
    }

    // only called at the OLAP Engine initialize
    private RegionMemberBean createRegionLevelsAndMembers(HierarchyBean hierarchy, RegionMemberBean superRoot) {
        /*
         * 全球 { level: 1, leaf: false }
         *     六大洲(除亚洲) { level: 2, leaf: true }
         *     亚洲 { level: 2, leaf: false }
         *         中国 { level: 3, leaf: false }
         */
        Object[][] regions = new Object[][] {
                new Object[]{ RegionMember.THE_WORLD,           1, 0}, // { name, member level, BINARY_CONTROL_FLAG }
                new Object[]{ RegionMember.NORTHERN_AMERICA,    2, 1},
                new Object[]{ RegionMember.SOUTH_AMERICA,       2, 1},
                new Object[]{ RegionMember.EUROPE,              2, 1},
                new Object[]{ RegionMember.AFRICA,              2, 1},
                new Object[]{ RegionMember.OCEANIA,             2, 1},
                new Object[]{ RegionMember.ANTARCTICA,          2, 1},
                new Object[]{ RegionMember.ASIA,                2, 0},
                new Object[]{ RegionMember.CHINA,               3, 0}
        };

        List<RegionMemberBean> sevenContinents = new LinkedList<>();

        for (Object[] regionProperties: regions) {
//            RegionMemberBean rmb = new RegionMemberBean();
//            rmb.setMddmGlobalId(sequencesManager.nextMDDMGlobalId());

            RegionMemberBean rmb = (RegionMemberBean) beanFactory.create(RegionMemberBean.class);
            rmb.setName(regionProperties[0].toString());
            rmb.setDimensionId(hierarchy.getDimensionId());
            rmb.setHierarchyId(hierarchy.getMgId());
            rmb.setMemberLevel((Integer) regionProperties[1]);
            rmb.setBinaryControlFlag((Integer) regionProperties[2]);
            rmb.setCommonBinaryFlag(2);


            switch (regionProperties[0].toString()) {
                case RegionMember.CHINA:
                    rmb.setRegionCode("156");
                    china = rmb;
                    break;
                case RegionMember.THE_WORLD:
                    world = rmb;
                    break;
                case RegionMember.ASIA:
                    asia = rmb;
                default: sevenContinents.add(rmb);
            }
        }

        // set parent member mgId(MDDM Global Id)
        world.setParentMemberId(superRoot.getMgId());
        for (RegionMemberBean continent : sevenContinents) {
            continent.setParentMemberId(world.getMgId());
        }
        china.setParentMemberId(asia.getMgId());




//        mddmPersistentDao.insertMember(world);
//        mddmPersistentDao.insertMember(china);
        memberDao.save(world);
        memberDao.save(china);
        for (RegionMemberBean continent : sevenContinents) {
//            mddmPersistentDao.insertMember(continent);
            memberDao.save(continent);
        }

        // save levels
//        String[] levelNames = new String[]{"全球", "大洲", "国家", "省", "市", "区县"};
        for (int i = 0; i <RegionDimension.DEFAULT_LEVEL_NAMES.length; i++) {
            LevelBean lb = (LevelBean) beanFactory.create(LevelBean.class);

//            lb.setMddmGlobalId(sequencesManager.nextMDDMGlobalId());
            lb.setName(RegionDimension.DEFAULT_LEVEL_NAMES[i]);
            lb.setHierarchyId(hierarchy.getMgId());
            lb.setMemberLevel(i + 1);
            lb.setCommonBinaryFlag(2);

//            mddmPersistentDao.insertLevel(lb);
            levelDAO.save(lb);

        }

        // save china regions(province, city, county)
        createChinaAdministrativeRegions(hierarchy/*, china*/);

        return china;
    }


    private void createChinaAdministrativeRegions(HierarchyBean hierarchy/*, RegionMemberBean china*/) {
        String codeAndName;
        String code;
        String name;
        BufferedReader br;
        try {
//            br = new BufferedReader(
//                    new FileReader(
//                            this.getClass().getClassLoader()
//                                    .getResource("init/china-regions.txt").getFile()
//                    )
//                 );

            br = new BufferedReader(new InputStreamReader(new FileInputStream(this.getClass().getClassLoader()
                    .getResource("init/china-regions.txt").getFile()), "UTF-8"));

//            br = Files.newBufferedReader(FileSystems.getDefault().getPath(this.getClass().getClassLoader()
//                    .getResource("init/china-regions.txt").getFile()), Charset.forName("UTF-8"));

            while ((codeAndName = br.readLine()) != null) {
                code = codeAndName.substring(0, 6);
                name = codeAndName.substring(6);
                preProcess(hierarchy, code, name);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new BIRuntimeException(e);
        }

        process();
    }

    private void preProcess(HierarchyBean hierarchyBean, String regionCode, String regionName) {

        RegionMemberBean region = (RegionMemberBean) beanFactory.create(RegionMemberBean.class);

//        region.setMddmGlobalId(sequencesManager.nextMDDMGlobalId());

        region.setName(regionName);

        // region.setParentMemberId();
        region.setDimensionId(hierarchyBean.getDimensionId());
        region.setHierarchyId(hierarchyBean.getMgId());
        // region.setMemberLevel();
        region.setBinaryControlFlag(isLeaf(regionCode) ? 1 : 0);
        // region.setMeasureColumn();

        region.setRegionCode(regionCode);

        region.setCommonBinaryFlag(2);

        codeRegionMap.put(regionCode, region);
        idRegionMap.put(region.getMgId(), region);
        regions.add(region);

    }

    private void process() {

        RegionMemberBean parentRegion;

        // set parentMemberId and memberLevel
        for (RegionMemberBean region : regions) {
            parentRegion = findParentAndGenerateMemberLevel(region);
            region.setParentMemberId(parentRegion.getMgId());
//			region.setMemberLevel(generateMemberLevel(region));
        }

        batchSaveRegions();
    }

    private RegionMemberBean findParentAndGenerateMemberLevel(RegionMemberBean child) {
        int code = Integer.parseInt(child.getRegionCode());
        if (code % 10000 == 0) {
            child.setMemberLevel(4);
            return china;
        } else if (code % 100 == 0) {
            child.setMemberLevel(5);
            return codeRegionMap.get(code / 10000 * 10000 + "");
        } else {
            child.setMemberLevel(6);
            return codeRegionMap.get(code / 100 * 100 + "");
        }
    }

    private void batchSaveRegions() {
//        int count = 1;
        for (RegionMemberBean region : regions) {
//            System.out.println((count++) + "\tSAVE:\n" + region + "\n");
//            this.MDDMPhysicalDAO.insert(region);
//            this.MDDMPhysicalDAO.insertRegionMember(region);
//            mddmPersistentDao.insertMember(region);
            memberDao.save(region);

//			if (Math.random() < 0.01) {
//				throw new RuntimeException("rollback");
//			}

        }

    }

    private boolean isLeaf(String code_) {
        if ("710000".equals(code_) || "810000".equals(code_) || "820000".equals(code_)) {
//			710000台湾省、810000香港特别行政区、820000澳门特别行政区
            return true;
        }
        return Integer.parseInt(code_) % 100 != 0;
    }

}
