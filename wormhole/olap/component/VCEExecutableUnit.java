package cn.bgotech.wormhole.olap.component;

import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by czg on 2019/3/15.
 */
public interface VCEExecutableUnit {

    byte[] generateTCPPackets();

    List<Byte> genTcpPkgBytesList();

    static List<Byte> expandFull_MG_ID_Path(Member member) {
        List<Byte> result = new ArrayList<>();

        int maxMemberLevel =  member.getDimension().getMaxMemberLevel();

        if (member.isRoot()) {
            for (int i = 0; i < maxMemberLevel; i++) {
                result.addAll(BGTechUtil.int2byteList(0));
            }
            return result;
        }

        List<Member> membersPath = member.findAncestors();
        membersPath.add(member);
        membersPath.remove(0); // remove Member.NONEXISTENT_MEMBER
        membersPath.remove(0); // remove root member

        for (Member m : membersPath)
            result.addAll(BGTechUtil.int2byteList((int) ((long) m.getMgId())));

        int complementaryLen = maxMemberLevel - membersPath.size();
        if (complementaryLen < 0)
            throw new RuntimeException("Dimension member maximum level value overflow");

        for (int i = 0; i < complementaryLen; i++)
            result.addAll(BGTechUtil.int2byteList(0));

        return result;
    }
}
