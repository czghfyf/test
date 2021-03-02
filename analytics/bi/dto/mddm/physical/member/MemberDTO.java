package cn.bgotech.analytics.bi.dto.mddm.physical.member;

import cn.bgotech.analytics.bi.bean.mddm.physical.member.DateMemberBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.MemberBean;
import cn.bgotech.wormhole.olap.mddm.physical.member.CalculatedMember;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/6/29.
 */
public class MemberDTO {

    private Member member;

    public static List<MemberDTO> transform(List<Member> members) {
        List<MemberDTO> result = new ArrayList<>();
        for (Member m : members) {
            result.add(new MemberDTO(m));
        }
        return result;
    }

    public MemberDTO(Member member) {
        this.member = member;
    }

    public long getMgId() {
        return member instanceof CalculatedMember ? 0 : member.getMgId();
    }

    public String getName() {
        return member.getName();
    }

    public boolean isMeasure() {
        return member.isMeasure();
    }

    public Long getParentId() {
        Member parent = member.getParent();
        return parent != null ? parent.getMgId() : null;
    }

    public Long getDimensionId() {
        return member == Member.NONEXISTENT_MEMBER ? 0 : member.getDimension().getMgId();
    }

    public int getLevelValue() {
        if (member instanceof DateMemberBean)
            return ((DateMemberBean) member).getMemberLevel();
        if (member.isRoot() || member instanceof CalculatedMember || member == Member.NONEXISTENT_MEMBER)
            return 0;
        Integer lv = ((MemberBean) member).getMemberLevel();
        if (lv != null)
            return lv.intValue();
        throw new RuntimeException("member level is null");
    }

    public boolean isCalculatedMember() {
        return member instanceof CalculatedMember;
    }

}
