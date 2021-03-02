package cn.bgotech.wormhole.olap.component.vcexeUnits;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.component.CVCEConstants;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SyncCubeExeUnit implements VCEExecutableUnit {

    private Cube cube;

    public SyncCubeExeUnit(Cube c) {
        cube = c;
    }

    @Override
    public byte[] generateTCPPackets() {
        return BGTechUtil.convertByteListInto_byteArray(genTcpPkgBytesList());
    }

    @Override
    public List<Byte> genTcpPkgBytesList() {


        List<Byte> packets = new LinkedList<>();
        packets.addAll(BGTechUtil.int2byteList(CVCEConstants.REQ_SYNC_CUBE));

        packets.addAll(BGTechUtil.int2byteList((int) ((long) cube.getMgId())));

        List<Dimension> dimensions =
                OlapEngine.hold().getMddmStorageService().findCubeAllDimensionsFromPersistence(cube);

//        List<Dimension> dimensions = cube.getDimensions(null);
        packets.addAll(BGTechUtil.int2byteList(dimensions.size() - 1));
        Dimension measureDm = null;
        for (Dimension d : dimensions) {
            if (d.isMeasureDimension()) {
                measureDm = d;
                continue;
            }
            packets.addAll(BGTechUtil.int2byteList((int) ((long) d.getMgId())));
            packets.addAll(BGTechUtil.int2byteList(d.getMaxMemberLevel()));
        }

        List<DimensionRole> dmRoles =
                OlapEngine.hold().getMddmStorageService().findCubeAllDimensionRolesFromPersistence(cube);

        packets.addAll(BGTechUtil.int2byteList(dmRoles.size() - 1));
        for (DimensionRole r : dmRoles) {
            Dimension dm = OlapEngine.hold().getMddmStorageService().loadDimensionByRoleMGIDFromPersistence(r.getMgId());
            if (dm.isMeasureDimension())
                continue;
            packets.addAll(BGTechUtil.int2byteList((int) ((long) r.getMgId())));
            packets.addAll(BGTechUtil.int2byteList((int) ((long) dm.getMgId())));
        }

        List<Member> meaMbrs = //(List<MeasureMember>) measureDm.getAllMembers();
                OlapEngine.hold().getMddmStorageService().loadAllMembersByDimensionMGIDFromPersistence(measureDm.getMgId());

        packets.addAll(BGTechUtil.int2byteList(meaMbrs.size() - 1));
        for (Member mm : meaMbrs) {
            if (mm.isRoot())
                continue;
            packets.addAll(BGTechUtil.int2byteList((int) ((long) mm.getMgId())));
        }

        List<Map> cubeLeafMembersMap = OlapEngine.hold().loadCubeLeafMembersInfo();
        for (Map m : cubeLeafMembersMap) {
            if (!cube.getMgId().equals(m.get("CUBE_MG_ID")))
                continue;
            packets.addAll(BGTechUtil.int2byteList(((Long) m.get("DIMENSION_ROLE_MG_ID")).intValue()));
            packets.addAll(BGTechUtil.int2byteList(((Long) m.get("LEAF_MEMBERS_COUNT")).intValue()));
        }

        return packets;
    }
}
