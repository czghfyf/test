package cn.bgotech.wormhole.olap.component.vcexeUnits;

import cn.bgotech.wormhole.olap.component.CVCEConstants;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.util.BGTechUtil;
import sun.awt.image.ImageWatched;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ChenZhigang on 2019/3/15.
 */
public class UpdateMeasureUnit implements VCEExecutableUnit {

    Cube cube;
    List<MemberRole> universalMemberRoles;
    Map<MemberRole, Double> measureValueMap;

    @Override
    public byte[] generateTCPPackets() {

        byte[] result = new InsertMeasureUnit(cube, universalMemberRoles, measureValueMap).generateTCPPackets();
        List<Byte> updateFlagBytes = BGTechUtil.int2byteList(CVCEConstants.REQ_UPDATE_MEASURE);
        for (int i = 0; i < updateFlagBytes.size(); i++) {
            result[i] = updateFlagBytes.get(i);
        }
        return result;

//        List<Byte> packets = new LinkedList<>();
//        packets.addAll(BGTechUtil.int2byteList(CVCEConstants.REQ_INSERT_MEASURE));
//
//        packets.addAll(BGTechUtil.int2byteList((int) ((long) cube.getMgId())));
//
////        List<DimensionRole> dmRoles = cube.getDimensionRoles(null);
////        DimensionRole measureDmRole;
////        MemberRole measureMR;
//
//        for (int i = 0; i < universalMemberRoles.size(); i++) {
//            if (universalMemberRoles.get(i).getDimensionRole().getDimension().isMeasureDimension()) {
////                measureMR = universalMemberRoles.remove(i);
//                universalMemberRoles.remove(i);
//                break;
//            }
//        }
//
//        Collections.sort(universalMemberRoles, Comparator.comparingInt(mr -> (int) ((long) mr.getDimensionRole().getMgId())));
//
//        for (MemberRole mr : universalMemberRoles) {
//            packets.addAll(VCEExecutableUnit.expandFull_MG_ID_Path(mr.getMember()));
//        }
//
//        List<Member> measureMembers = (List<Member>) cube.getMeasureDimensionRole().getDimension().getMembers(null);
//        Collections.sort(measureMembers, Comparator.comparingInt(m -> (int) ((long) m.getMgId())));
//        for (Member m : measureMembers) {
//            if (m.isRoot())
//                continue;
//
//            if (measureValueMap.containsKey(m)) {
//                packets.addAll(BGTechUtil.int2byteList(0));
//                packets.addAll(BGTechUtil.double2byteList(measureValueMap.get(m)));
//            } else {
//                packets.addAll(BGTechUtil.int2byteList(1)); // skip the measure member
//
//                packets.addAll(BGTechUtil.int2byteList(1)); // Placeholder, meaningless value, Two ints occupy 8 bytes, which is equivalent to a double
//                packets.addAll(BGTechUtil.int2byteList(1)); // Placeholder, meaningless value, Two ints occupy 8 bytes, which is equivalent to a double
//            }
//        }
//
//        return BGTechUtil.convertByteListInto_byteArray(packets);


    }

    public UpdateMeasureUnit(Cube c, List<MemberRole> uMRs, Map<MemberRole, Double> mvMap) {
        cube = c;
        universalMemberRoles = uMRs;
        measureValueMap = mvMap;
    }

    @Override
    public List<Byte> genTcpPkgBytesList() {
        List<Byte> bs = new LinkedList<>();
        for (byte b : generateTCPPackets()) {
            bs.add(b);
        }
        return bs;
    }


}
