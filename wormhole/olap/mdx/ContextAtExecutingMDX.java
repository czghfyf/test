//core_source_code!_yxIwIyyIyfInz_1ll1

package cn.bgotech.wormhole.olap.mdx;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.member.CalculatedMember;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

public class
/*^!*/ContextAtExecutingMDX/*?$*//*_yxIwIyyIyfInz_1ll1*/ {

    private SFPOfExecuteMDX sfp/*^!?$*/;

    private OlapEngine olapEngine/*^!?$*/;

    public ContextAtExecutingMDX(OlapEngine olapEngine/*^!?$*/) {
        this.olapEngine = olapEngine;
    }


    public ContextAtExecutingMDX set/*^!?$*/(SFPOfExecuteMDX sfp/*^!?$*/) {
        this.sfp = sfp;
        return this;
    }


    public OlapEngine getOLAPEngine/*^!?$*/() {
        return olapEngine;
    }


    public Cube getCube/*^!?$*/() {
        return sfp.getCube();
    }

    /**
     * @param selector
     * @return
     */

    public CalculatedMember getCalculatedMember/*^!?$*/(MultiDimensionalDomainSelector selector/*^!?$*/) {
        if (sfp instanceof MDXQueryResultSFP) {
            return ((MDXQueryResultSFP) sfp).getCalculatedMember(selector);
        }
        return null;
    }

    /**
     * 返回命名集合
     *
     * @param customSetName
     * @return
     */

    public SetPE getCustomSet/*^!?$*/(MultiDimensionalDomainSelector customSetName/*^!?$*/) {
        if (sfp instanceof MDXQueryResultSFP) {
            return ((MDXQueryResultSFP) sfp).getCustomSet(customSetName);
        }
        return null;
    }

}
