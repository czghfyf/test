package cn.bgotech.wormhole.olap.bigdata.cache;

import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;

import java.util.List;

/**
 * Created by czg on 2018/2/4.
 */
public interface VectorCache {

    boolean hasMeasureValue(long cubeId, List<MemberRole> universalMemberRoles, MemberRole measureMemberRole);

    Double getMeasureValue(long cubeId, List<MemberRole> universalMemberRoles, MemberRole measureMemberRole);

    void setMeasureValue(long cubeId, List<MemberRole> universalMemberRoles, MemberRole measureMemberRole, Double measureValue);

}
