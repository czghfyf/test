// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm;

import cn.bgotech.analytics.bi.bean.Bean;
import cn.bgotech.wormhole.olap.mddm.physical.PhysicalEntity;

//import java.util.Date;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public abstract class MddmBean extends Bean implements PhysicalEntity {

//    private Long mddmGlobalId;
    private Long mgId; // MDDM Global Id
    private String name;

    public Long getMgId() {
        return mgId;
    }

    public void setMgId(Long mgId) {
        this.mgId = mgId;
    }

//    public Long getMddmGlobalId() {
//        return mddmGlobalId;
//    }

//    public void setMddmGlobalId(Long mddmGlobalId) {
//        this.mddmGlobalId = mddmGlobalId;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MddmBean)) {
            return false;
        }
        MddmBean other = (MddmBean) obj;
        return mgId.equals(other.mgId);
    }

    @Override
    public int hashCode() {
        return getMgId().hashCode();
    }


    public void setAllAttributesNull() {
        mgId = null;
        name = null;
        super.setAllAttributesNull();
    }
}

