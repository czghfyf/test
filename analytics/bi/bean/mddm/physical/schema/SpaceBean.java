// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.schema;

import cn.bgotech.analytics.bi.bean.mddm.MddmBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.CubeBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.DimensionBean;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class SpaceBean extends MddmBean implements Space {

    @Deprecated
    private Integer binaryControlFlag;

    @Deprecated
    public Integer getBinaryControlFlag() {
        return binaryControlFlag;
    }

    @Deprecated
    public void setBinaryControlFlag(Integer binaryControlFlag) {
        this.binaryControlFlag = binaryControlFlag;
    }

    @Override
    public boolean isWithinRange(Space space) {
        return this.equals(space);
    }

    @Override
    public List<Cube> cubes() {
        List<Cube> cubes = OlapEngine.hold().getMddmStorageService().allCubes();
        for (int i = cubes.size() - 1; i >= 0; i--) {
            if (!cubes.get(i).isWithinRange(this)) {
                cubes.remove(i);
            }
        }
        return cubes;
    }

    @Override
    public List<Dimension> getAllDimensions() {
        List<Dimension> dimensions = OlapEngine.hold().findAll(Dimension.class);
        for (int i = 0; i < dimensions.size(); i++) {
            if (!getMgId().equals(((DimensionBean) dimensions.get(i)).getSpaceId())) {
                dimensions.remove(i--);
            }
        }
        return dimensions;
    }

    @Override
    public boolean associatedWith(Cube cube) {
        return ((CubeBean) cube).getSpaceId().equals(getMgId());
    }

    /**
     * TODO: Inappropriate method details are achieved. see TODO 201706111534
     *
     * @param segmentedSelector Multidimensional selector fragment
     * @return
     */
    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        return null;
    }
}

