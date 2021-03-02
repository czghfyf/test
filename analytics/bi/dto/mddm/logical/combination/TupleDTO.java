package cn.bgotech.analytics.bi.dto.mddm.logical.combination;

import cn.bgotech.analytics.bi.dto.mddm.physical.role.MemberRoleDTO;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/7/13.
 */
public class TupleDTO {

    private Tuple tuple;

    public TupleDTO(Tuple tuple) {
        this.tuple = tuple;
    }

    public List<MemberRoleDTO> getMemberRoles() {
        List<MemberRoleDTO> memberRoleDTOs = new ArrayList<>();
        for (MemberRole mr : tuple.getMemberRoles()) {
            memberRoleDTOs.add(new MemberRoleDTO(mr));
        }
        return memberRoleDTOs;
    }

}
