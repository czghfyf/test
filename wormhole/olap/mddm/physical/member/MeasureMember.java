package cn.bgotech.wormhole.olap.mddm.physical.member;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface MeasureMember extends Member {

    /**
     * All the leaf members of the measure dimension are sorted by MG_ID from small to large (Location coordinates start from 0),
     * and the position of the current member after sorting is returned.
     * @return
     */
    int sortedPosition();

}
