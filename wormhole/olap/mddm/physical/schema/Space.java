package cn.bgotech.wormhole.olap.mddm.physical.schema;

import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.PhysicalEntity;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface Space extends PhysicalEntity {

    /**
     * return all cubes which belong this space
     * @return
     */
    List<Cube> cubes();

    /**
     * return all dimensions which belong this space
     * @return
     */
    List<Dimension> getAllDimensions();

}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    package cn.bgotech.prophet.mddm.schema;
    //    public interface Space extends BasicEntity {
    //        Cube findCubeByName(String cubeName);
    //        GeneralDimension findGeneralDimensionByName(String dimName);
    //    }
// ????????????????????????????????????????????????????????????????????????????????????????????

