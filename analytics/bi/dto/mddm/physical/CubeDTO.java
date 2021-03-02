package cn.bgotech.analytics.bi.dto.mddm.physical;

import cn.bgotech.wormhole.olap.mddm.physical.Cube;

/**
 * Created by ChenZhiGang on 2017/6/1.
 */
public class CubeDTO {

    private Cube cube;

    public CubeDTO(Cube cube) {
        this.cube = cube;
    }

    public long getMgId() {
        return cube.getMgId();
    }

    public String getName() {
        return cube.getName();
    }

}
