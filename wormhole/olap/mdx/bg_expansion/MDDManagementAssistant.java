package cn.bgotech.wormhole.olap.mdx.bg_expansion;


import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by czg on 2019/3/12.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class MDDManagementAssistant {

    private List<ExecutionUnit> exeUnits = new LinkedList<>();

    public void readyToCreateSpace(String spaceName) {
        exeUnits.add(new ExeCreateSpaceUnit(spaceName));
    }

    public void readyToCreateDimension(String spaceName, String dimensionName, int maxMemberLevel) {
        exeUnits.add(new ExeCreateDimensionUnit(spaceName, dimensionName, maxMemberLevel));
    }

    public void readyToCreateMember(String spaceName, MultiDimensionalDomainSelector memberSelector) {
        exeUnits.add(new ExeCreateMemberUnit(spaceName, memberSelector));
    }

    public List<ExecutionUnit> getExeUnits() {
        return exeUnits;
    }

    public void addExeUnit(ExecutionUnit eu) {
        exeUnits.add(eu);
    }

    public void readyConnectToVCENodes(String nodesInfo) {
        List<VCEWorkConnector.NodeInfo> readyConnectVceNodes = new LinkedList<>();
        String[] ss = nodesInfo.split(",");
        for (String hostAndPort : ss) {
            String[] host_port = hostAndPort.split(":");
            VCEWorkConnector.NodeInfo nodeInfo = new VCEWorkConnector.NodeInfo();
            nodeInfo.setHost(host_port[0].trim());
            nodeInfo.setPort(Integer.parseInt(host_port[1].trim()));
            readyConnectVceNodes.add(nodeInfo);
        }
        ThreadLocalTool.setCurrentReadyConnectVCENodes(readyConnectVceNodes);
    }
}
