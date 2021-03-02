package cn.bgotech.wormhole.olap.mddm.physical.member;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface DateMember extends UniversalMember {

    // preset.date.dimension.member.global.id.start = 1500_00_00
    int MD_GLOBAL_ID_START = 1500_00_00;

    // preset.date.dimension.member.global.id.end = 2200_14_04
    int MD_GLOBAL_ID_END = 2200_14_04;

    int ROOT_DATE_MEMBER_MG_ID = MD_GLOBAL_ID_START + 15_00;

    List<Long> getFinalDescendantMembersMgIdList();

    enum DateLevelType {
        YEAR,
        HALF_OF_YEAR,
        QUARTER,
        MONTH,
        DAY
    }
}
