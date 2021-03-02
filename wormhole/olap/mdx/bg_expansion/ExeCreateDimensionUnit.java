package cn.bgotech.wormhole.olap.mdx.bg_expansion;

/**
 * Created by ChenZhigang on 2019/3/12.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class ExeCreateDimensionUnit implements MDDM_ExecutionUnit {

    public String spaceName;
    public String dimensionName;
    public int maxMemberLevel;

    public ExeCreateDimensionUnit(String spaceName, String dimensionName, int maxLv) {
        this.spaceName = spaceName;
        this.dimensionName = dimensionName;
        maxMemberLevel = maxLv;
    }

    @Override
    public String getSpaceName() {
        return spaceName;
    }
}
