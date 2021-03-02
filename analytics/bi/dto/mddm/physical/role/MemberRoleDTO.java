package cn.bgotech.analytics.bi.dto.mddm.physical.role;

import cn.bgotech.analytics.bi.dto.mddm.physical.member.MemberDTO;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;

/**
 * Created by ChenZhiGang on 2017/7/13.
 */
public class MemberRoleDTO {

    private MemberRole memberRole;

    public MemberRoleDTO(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public DimensionRoleDTO getDimensionRole() {
        return new DimensionRoleDTO(memberRole.getDimensionRole());
    }

    public MemberDTO getMember() {
        return new MemberDTO(memberRole.getMember());
    }

}
