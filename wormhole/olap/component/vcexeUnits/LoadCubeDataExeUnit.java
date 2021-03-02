package cn.bgotech.wormhole.olap.component.vcexeUnits;

import cn.bgotech.analytics.bi.cache.MDDMCacheService;
import cn.bgotech.wormhole.olap.component.CVCEConstants;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by czg on 2019/7/3.
 */
public class LoadCubeDataExeUnit implements VCEExecutableUnit {

    private String spaceName, cubeName;

    public LoadCubeDataExeUnit(String spaceName, String cubeName) {
        this.spaceName = spaceName;
        this.cubeName = cubeName;
    }

    @Override
    public byte[] generateTCPPackets() {
        List<Byte> list = genTcpPkgBytesList();
        byte[] bs = new byte[list.size()];
        for (int i = 0; i < bs.length; i++) {
            bs[i] = list.get(i);
        }
        return bs;
    }

    @Override
    public List<Byte> genTcpPkgBytesList() {
        List<Byte> bytes = new ArrayList<>();
        byte[] byteArr = BGTechUtil.int2byteArray(CVCEConstants.REQ_REBUI_CUBE_DATA_MEM);
        for (byte b : byteArr)
            bytes.add(b);
        MDDMCacheService.SI.buildSpaceCacheSuiteWhenIsNo(spaceName);
        Cube cube = MDDMCacheService.SI.findCube(spaceName, cubeName);
        byteArr = BGTechUtil.int2byteArray(cube.getMgId().intValue());
        for (byte b : byteArr)
            bytes.add(b);
        return bytes;
    }

}
