package cn.bgotech.wormhole.olap.component.vcexeUnits;

import cn.bgotech.analytics.bi.cache.MDDMCacheService;
import cn.bgotech.wormhole.olap.component.CVCEConstants;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhigang on 2019/7/30.
 */
public class WriteBackCubeExeUnit implements VCEExecutableUnit {

    private String space, cubeName;

    public WriteBackCubeExeUnit(String s, String c) {
        space = s;
        cubeName = c;
    }

    @Override
    public byte[] generateTCPPackets() {
        return BGTechUtil.convertByteListInto_byteArray(genTcpPkgBytesList());
    }

    @Override
    public List<Byte> genTcpPkgBytesList() {
        List<Byte> bytes = new ArrayList<>();
        byte[] byteArr = BGTechUtil.int2byteArray(CVCEConstants.REQ_REBUI_CUBEDATAFILE_FROMMEM);
        for (byte b : byteArr)
            bytes.add(b);

        MDDMCacheService.SI.buildSpaceCacheSuiteWhenIsNo(space);
        Cube cube = MDDMCacheService.SI.findCube(space, cubeName);

        byteArr = BGTechUtil.int2byteArray(cube.getMgId().intValue());
        for (byte b : byteArr)
            bytes.add(b);
        return bytes;

    }
}
