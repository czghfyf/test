package cn.bgotech.wormhole.olap.component.vcexeUnits;

import cn.bgotech.wormhole.olap.component.CVCEConstants;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.member.MeasureMember;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Created by ChenZhigang on 2019/3/15.
 */
public class DeleteMeasureUnit implements VCEExecutableUnit {
    Cube cube;
    List<MemberRole> universalMemberRoles;
    List<MemberRole> preDeleteMeasures;

    public DeleteMeasureUnit(Cube c, List<MemberRole> mrs, List<MemberRole> pre_delete_measures_ls) {
        cube = c;
        universalMemberRoles = mrs;
        preDeleteMeasures = pre_delete_measures_ls;
    }

    @Override
    public byte[] generateTCPPackets() {
        return BGTechUtil.convertByteListInto_byteArray(genTcpPkgBytesList());
    }

    @Override
    public List<Byte> genTcpPkgBytesList() {
        List<Byte> packets = new ArrayList<>();
        packets.addAll(BGTechUtil.int2byteList(CVCEConstants.REQ_DELETE_MEASURE));
        packets.addAll(BGTechUtil.int2byteList((int) ((long) cube.getMgId())));
        for (MemberRole mr : universalMemberRoles) {
            packets.addAll(VCEExecutableUnit.expandFull_MG_ID_Path(mr.getMember()));
        }
//        byte[] result = new InsertMeasureUnit(cube, universalMemberRoles, new HashMap<>()).generateTCPPackets();
//        for (byte bt : result) {
//            packets.add(bt);
//        }
        Set<MeasureMember> delMbrSet = new HashSet<>(); //new HashSet<>(preDeleteMeasures);
        ListIterator<MemberRole> lit = preDeleteMeasures.listIterator();
        while (lit.hasNext())
            delMbrSet.add((MeasureMember) lit.next().getMember());
        List<Member> measureMembers = (List<Member>) cube.getMeasureDimensionRole().getDimension().getMembers(null);
        Collections.sort(measureMembers, Comparator.comparingLong(m -> m.getMgId()));
        for (Member m : measureMembers) {
            if (!m.isRoot())
                packets.add(delMbrSet.contains(m) ? ((byte) 0) : ((byte) 1));
        }
        return packets;
    }
}
