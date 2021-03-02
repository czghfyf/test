//core_source_code!_yxIwInyIsIhn_1llll1


package cn.bgotech.wormhole.olap.mdx.auxi;

/**
 * Created by ChenZhigang on 2019/7/13.
 */
public class /*^!*/AuxCreateDimension/*?$*//*_yxIwInyIsIhn_1llll1*/ {

    private String dimName;
    private int maxMbrLv;

    public AuxCreateDimension(String _dmName, String _maxMbrLv) {
        dimName = _dmName;
        maxMbrLv = Integer.parseInt(_maxMbrLv);
        if (maxMbrLv < 1)
            throw new RuntimeException("maxMbrLv must be '> 0'");
    }

    public String getDimName() {
        return dimName;
    }

    public int getMaxMbrLv() {
        return maxMbrLv;
    }
}
