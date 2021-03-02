package cn.bgotech.wormhole.olap.mddm.physical.dimension;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface DateDimension extends UniversalDimension {

    String[] DEFAULT_LEVEL_NAMES = new String[]{"年度级别", "半年级别", "季度级别", "月份级别", "日期级别"};

    static String defaultName() {
        return "标准日期";
    }

    @Override
    default String defaultHierarchyName() {
        return "标准日期层次结构";
    }

//    @Override
//    DateMember findMemberByFullNamesPath(List<String> fullNamesPath);
}
