package cn.bgotech.wormhole.olap.mdx.bg_expansion;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ChenZhigang on 2019/3/12.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class ExeBuildCubeUnit extends VCE_ExecutionUnit implements MDDM_ExecutionUnit {

//    public String spaceName, cubeName;
    public List<String[]> dm_role_match = new LinkedList<>();
    public List<String> measures = new LinkedList();


//    public void setSpaceName(String spaceName) {
//        this.spaceName = spaceName;
//    }
//
//    public void setCubeName(String cubeName) {
//        this.cubeName = cubeName;
//    }

    public void addDimensionRoleMatcher(String dimensionName, String dimensionRoleName) {
        dm_role_match.add(new String[]{dimensionName, dimensionRoleName});
    }

    public void addMeasureMember(String measureMemberName) {
        measures.add(measureMemberName);
    }
}
