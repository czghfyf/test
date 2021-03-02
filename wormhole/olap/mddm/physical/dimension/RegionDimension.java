package cn.bgotech.wormhole.olap.mddm.physical.dimension;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface RegionDimension extends UniversalDimension {

    String[] DEFAULT_LEVEL_NAMES = new String[]{"全球", "大洲", "国家", "省", "市", "区县"};

    static String defaultName() {
        return "标准地区";
    }

    @Override
    default String defaultSuperRootName() {
        return "REGION_ROOT";
    }

    @Override
    default String defaultHierarchyName() {
        return "标准地区层次结构"; // 行政划分
    }

}
