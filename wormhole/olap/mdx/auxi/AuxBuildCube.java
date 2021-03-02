//core_source_code!_yxIwInyIsIhn_1lll1l


package cn.bgotech.wormhole.olap.mdx.auxi;

import java.util.List;

/**
 * Created by czg on 2019/7/13.
 */
public class /*^!*/AuxBuildCube/*?$*//*_yxIwInyIsIhn_1lll1l*/ {

    private String cubeName;

    private List<Dim__Role> drsInfo;

    private List<String> measureNames;

    public AuxBuildCube(String _cName, List<Dim__Role> _drList, List<String> _measureNames) {
        cubeName = _cName;
        drsInfo = _drList;
        measureNames = _measureNames;

    }

    public String getCubeName() {
        return cubeName;
    }

    public List<String> getMeasureNames() {
        return measureNames;
    }

    public List<Dim__Role> getDrsInfo() {
        return drsInfo;
    }

    public static class Dim__Role {

        private String dmName, roleName;

        public Dim__Role(String _dmName, String _roleName) {
            dmName = _dmName;
            roleName = _roleName;
        }

        public String getDmName() {
            return dmName;
        }

        public String getRoleName() {
            return roleName;
        }
    }
}
