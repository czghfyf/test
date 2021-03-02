package cn.bgotech.wormhole.olap.component.vcexeUnits;

import cn.bgotech.analytics.bi.bean.mddm.physical.role.DimensionRoleBean;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.component.CVCEConstants;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ChenZhigang on 2019/3/15.
 */
public class InsertMeasureUnit implements VCEExecutableUnit {


    Cube cube;
    List<MemberRole> universalMemberRoles;
    Map<MemberRole, Double> measureValueMap;

    public InsertMeasureUnit(Cube c, List<MemberRole> uMRs, Map<MemberRole, Double> mvMap) {
        cube = c;
        universalMemberRoles = uMRs;
        measureValueMap = mvMap;
    }

    @Override
    public byte[] generateTCPPackets() {
        return BGTechUtil.convertByteListInto_byteArray(genTcpPkgBytesList());
    }

    @Override
    public List<Byte> genTcpPkgBytesList() {

        List<Byte> packets = new LinkedList<>();
        packets.addAll(BGTechUtil.int2byteList(CVCEConstants.REQ_INSERT_MEASURE));

        packets.addAll(BGTechUtil.int2byteList((int) ((long) cube.getMgId())));

//        List<DimensionRole> dmRoles = cube.getDimensionRoles(null);
//        DimensionRole measureDmRole;
//        MemberRole measureMR;

        Dimension d;
        for (int i = 0; i < universalMemberRoles.size(); i++) {
            d = OlapEngine.hold().findDimensionById
                    (((DimensionRoleBean) universalMemberRoles.get(i).getDimensionRole()).getDimensionId());
            if (d.isMeasureDimension()) {
//                measureMR = universalMemberRoles.remove(i);
                universalMemberRoles.remove(i);
                break;
            }
        }

        Collections.sort(universalMemberRoles, Comparator.comparingInt(mr -> (int) ((long) mr.getDimensionRole().getMgId())));

        for (MemberRole mr : universalMemberRoles) {
            packets.addAll(VCEExecutableUnit.expandFull_MG_ID_Path(mr.getMember()));
        }

        List<Member> measureMembers = (List<Member>) cube.getMeasureDimensionRole().getDimension().getMembers(null);
        Collections.sort(measureMembers, Comparator.comparingLong(m -> m.getMgId()));

        DimensionRole measureDmRole = null;
        for (Iterator<MemberRole> itt = measureValueMap.keySet().iterator(); itt.hasNext(); ) {
            measureDmRole = itt.next().getDimensionRole();
        }

        for (Member m : measureMembers) {
            if (m.isRoot())
                continue;

            Double meaVal = measureValueMap.get(new MemberRole(measureDmRole, m));
            packets.add((byte) (meaVal != null ? 0 : 1));
            packets.addAll(BGTechUtil.double2byteList(meaVal != null ? meaVal : 0));
        }
        return packets;
    }

    public static boolean allAreInsertMeasureUnit(List<VCEExecutableUnit> us) {
        for (VCEExecutableUnit u : us) {
            if (!(u instanceof InsertMeasureUnit))
                return false;
        }
        return true;
    }

    public static List<InsertMeasureUnit> transform(List<VCEExecutableUnit> units) {
        List<InsertMeasureUnit> result = new LinkedList<>();
        for (VCEExecutableUnit u : units)
            result.add((InsertMeasureUnit) u);
        return result;
    }
}
