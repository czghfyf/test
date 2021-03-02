package cn.bgotech.wormhole.olap.mdx.bg_expansion;

/**
 * Created by ChenZhigang on 2019/3/12.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class ExeCreateSpaceUnit implements MDDM_ExecutionUnit{

    public String name;

    public ExeCreateSpaceUnit(String name) {
        this.name = name;
    }

    @Override
    public String getSpaceName() {
        return name;
    }
}
