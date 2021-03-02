// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.member;

import cn.bgotech.wormhole.olap.mddm.physical.member.RegionMember;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public class RegionMemberBean extends UniversalMemberBean implements RegionMember{

    private String regionCode;

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }
}
