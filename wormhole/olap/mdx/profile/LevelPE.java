//core_source_code!_yxIwIywIyzIx_1l1l11

        package cn.bgotech.wormhole.olap.mdx.profile;

import cn.bgotech.wormhole.olap.mddm.physical.role.LevelRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.SelectedByMDDAble;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

public class
/*^!*/LevelPE/*?$*//*_yxIwIywIyzIx_1l1l11*/ implements EntityPrototype<LevelRole>, SelectedByMDDAble {

    private ContextAtExecutingMDX context;
    private MultiDimensionalDomainSelector selector;

    public LevelPE(ContextAtExecutingMDX context, MultiDimensionalDomainSelector selector) {
        this.context = context;
        this.selector = selector;
    }

    @Override
    public LevelRole evolving(MultiDimensionalVector v) {
        return context.getOLAPEngine().findUniqueEntity(context.getCube(), LevelRole.class, selector);
    }

    @Override
    public MultiDimensionalDomainSelector getMDDSelector() {
        return selector;
    }
}
