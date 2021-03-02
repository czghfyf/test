//core_source_code!_yxIwIywIyzIx_1l111l

        package cn.bgotech.wormhole.olap.mdx.profile;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.SelectedByMDDAble;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

public class
/*^!*/CubePE/*?$*//*_yxIwIywIyzIx_1l111l*/ implements EntityPrototype<Cube>, SelectedByMDDAble {

    private ContextAtExecutingMDX ctx;
    private MultiDimensionalDomainSelector mdds;

    public CubePE(ContextAtExecutingMDX ctx, MultiDimensionalDomainSelector mdds) {
        this.ctx = ctx;
        this.mdds = mdds;
    }

    @Override
    public Cube evolving(MultiDimensionalVector v) {
        Space currentThreadSpace = OlapEngine.getCurrentThreadSpace();
        MultiDimensionalDomainSelector.Part cubeInfo = mdds.getPart(0);
        if (currentThreadSpace != null) {
            for (Cube c : currentThreadSpace.cubes()) {
                if (c.getMgId().equals(cubeInfo.getMgId())
                        || (cubeInfo.getMgId() == null && c.getName().equals(cubeInfo.getImage()))) {
                    return c;
                }
            }
            return null;
        }
        throw new OlapRuntimeException("have no Space entity in current thread");
    }

    @Override
    public MultiDimensionalDomainSelector getMDDSelector() {
        return mdds;
    }
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151024
    //    package com.dao.wormhole.core.mdx.production.shell;
    //
    //    //import com.dao.wormhole.core.exception.WromholeRuntimeException;
    //            import com.dao.wormhole.core.mdx.IR.CharacterBlocksToken;
    //            import com.dao.wormhole.core.mdx.parser.context.AnalyticalContext;
    //            import com.dao.wormhole.core.mdx.production.ContextProduction;
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //    //import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //            import com.dao.wormhole.core.model.entity.Cube;
    //            import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //    //import com.dao.wormhole.core.model.vectorspace.MultidimensionalVector;
    //    //import com.dao.wormhole.core.services.ModelService;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-8 下午4:33:20
    //     *
    //     */
    //    public class CubeShell implements WormholeBaseObjectShell, ContextProduction {
    //
    //        private CharacterBlocksToken charBlkTkn = null;
    //
    //        private AnalyticalContext context = null;
    //
    //        /**
    //         * cube ::= cube_name_spec
    //         *
    //         * @param charBlkTkn
    //         */
    //        public CubeShell(CharacterBlocksToken charBlkTkn) {
    //            this.charBlkTkn = charBlkTkn;
    //        }
    //
    //    //	@Override
    //    //	public WormholeBaseEntity hatch(VectorCoordinateFragment vcf) {
    //    //		// 形参{vcf}在这里并不起什么作用
    //    //		if (charBlkTkn.size() == 1) {
    //    //			ModelService modelService = context.getWormholeApp().getModelService();
    //    //			Cube cube = modelService.findCubeByName(charBlkTkn.getCharacterBlock(0));
    //    //			if (cube == null) {
    //    //				throw new /*Wromhole*/RuntimeException("找不到Cube:" + charBlkTkn.getCharacterBlock(0));
    //    //			}
    //    //			return cube;
    //    //		}
    //    //		throw new /*Wromhole*/RuntimeException(charBlkTkn.toString() + "不是合法的Cube名称");
    //    //
    //    ////		Cube cube = (Cube) modelService.findUniqueHyperspaceModelObject(this.charBlkTkn, null); // 对于返回的不是一个Cube对象的情况，请捕捉异常后进行处理
    //    ////		return cube;
    //    //	}
    //
    //        public Cube getCube() {
    //            return context.getWormholeApp().getModelService().findCubeByName(charBlkTkn.getBlock(0).getText());
    //        }
    //
    //        @Override
    //        public void setContext(AnalyticalContext context) {
    //            this.context = context;
    //        }
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
