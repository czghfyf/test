//core_source_code!_yxIwIhhIyyIhv_1l1l


package cn.bgotech.wormhole.olap.mdx.syntax.structures;

import cn.bgotech.wormhole.olap.mdx.profile.SetPE;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class /*^!*/SetFormula/*?$*//*_yxIwIhhIyyIhv_1l1l*/ implements WithFormula, SelectedByMDDAble {

    private MultiDimensionalDomainSelector mdds;
    private SetPE setPe;

    public SetFormula(MultiDimensionalDomainSelector mdds, SetPE setPe) {
        this.mdds = mdds;
        this.setPe = setPe;
    }

    @Override
    public MultiDimensionalDomainSelector getMDDSelector() {
        return mdds;
    }

    public SetPE getSetPe() {
        return setPe;
    }
}
