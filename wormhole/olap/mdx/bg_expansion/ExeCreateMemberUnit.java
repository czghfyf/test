package cn.bgotech.wormhole.olap.mdx.bg_expansion;

import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

/**
 * Created by ChenZhigang on 2019/3/12.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class ExeCreateMemberUnit implements MDDM_ExecutionUnit {

    public String spaceName;

    // [dimensionName].[lv1memberName].[lv2memberName].[lv3memberName].[lv4memberName]
    public MultiDimensionalDomainSelector memberSelector;

    public ExeCreateMemberUnit(String spaceName, MultiDimensionalDomainSelector memberSelector) {
        this.spaceName = spaceName;
        this.memberSelector = memberSelector;
    }

    @Override
    public String getSpaceName() {
        return spaceName;
    }
}
