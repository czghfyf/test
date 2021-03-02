// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.mddm;

import cn.bgotech.analytics.bi.bean.mddm.physical.CubeBean;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/17.
 */
public interface CubeDAO {

    List<Cube> loadAll();

    int save(CubeBean cubeBean);

    CubeBean loadById(Long cubeId);

    List<CubeBean> loadByName(String name);

}
