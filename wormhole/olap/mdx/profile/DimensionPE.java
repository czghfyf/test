//core_source_code!_yxIwIywIyzIx_1l11ll

        package cn.bgotech.wormhole.olap.mdx.profile;

import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.SelectedByMDDAble;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

public class
/*^!*/DimensionPE/*?$*//*_yxIwIywIyzIx_1l11ll*/ implements EntityPrototype<DimensionRole>, SelectedByMDDAble {

    private ContextAtExecutingMDX ctx;
    private MultiDimensionalDomainSelector mdds;

    public DimensionPE(ContextAtExecutingMDX ctx, MultiDimensionalDomainSelector mdds) {
        this.ctx = ctx;
        this.mdds = mdds;
    }

    @Override
    public MultiDimensionalDomainSelector getMDDSelector() {
        return mdds;
    }

    @Override
    public DimensionRole evolving(MultiDimensionalVector v) {

        // TODO 涉及到维度角色,可能需要修改相关逻辑
        return ctx.getOLAPEngine().findUniqueEntity(ctx.getCube(), DimensionRole.class, mdds);
                                                                                        //    // TODO 涉及到维度角色,可能需要修改相关逻辑
                                                                                        //    ModelService modelService = context.getWormholeApp().getModelService();
                                                                                        //    IDimensionRole dr =
                                                                                        //            (IDimensionRole) modelService.findUniqueWormholeEntity(dimName_CharBloken, context.getExecuteCube());
                                                                                        //    return dr;
    }
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151024
    //    package com.dao.wormhole.core.mdx.production.shell;
    //
    //            import com.dao.wormhole.core.mdx.IR.CharacterBlocksToken;
    //            import com.dao.wormhole.core.mdx.parser.context.AnalyticalContext;
    //            import com.dao.wormhole.core.mdx.production.ContextProduction;
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //            import com.dao.wormhole.core.model.entity.role.IDimensionRole;
    //    //import com.dao.wormhole.core.model.entity.Dimension;
    //    //import com.dao.wormhole.core.model.entity.IDimensionRole;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //            import com.dao.wormhole.core.services.ModelService;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-7 下午4:57:01
    //     *
    //     */
    //    public class DimensionShell implements ContextProduction, WormholeBaseObjectShell {
    //
    //        private AnalyticalContext context = null;
    //        private CharacterBlocksToken dimName_CharBloken = null;
    //
    //        /**
    //         * dimension ::= dimension_name_spec
    //         *
    //         * @param dimName_CharBloken
    //         */
    //        public DimensionShell(CharacterBlocksToken dimName_CharBloken) {
    //            this.dimName_CharBloken = dimName_CharBloken;
    //        }
    //
    //    //	@Override
    //    //	public WormholeBaseEntity hatch(VectorCoordinateFragment vcf) {
    //    //		ModelService modelService = context.getWormholeApp().getModelService();
    //    ////		Dimension dimension = (Dimension) modelService.findUniqueWormholeEntity(dimName_CharBloken, context.getExecuteCube());
    //    //		IDimensionRole dr = null;
    //    //		try {
    //    //			dr = (IDimensionRole) modelService.findUniqueWormholeEntity(dimName_CharBloken, context.getExecuteCube());
    //    //		} catch (RuntimeException e) {
    //    //			throw e;
    //    //		}
    //    ////		return dimension;
    //    //		return dr;
    //    //	}
                                                                                                    //
                                                                                                    //        @Override
                                                                                                    //        public WormholeBaseObject hatch(MultidimensionalVector vector) { // TODO 涉及到维度角色,可能需要修改相关逻辑
                                                                                                    //            ModelService modelService = context.getWormholeApp().getModelService();
                                                                                                    //            IDimensionRole dr =
                                                                                                    //                    (IDimensionRole) modelService.findUniqueWormholeEntity(dimName_CharBloken, context.getExecuteCube());
                                                                                                    //            return dr;
                                                                                                    //        }
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
