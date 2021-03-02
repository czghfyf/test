package cn.bgotech.analytics.bi.service;

import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.component.vcexeUnits.SyncCubeExeUnit;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Created by czg on 2019/6/23.
 */
@Service
public class MDDVCEMngService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void exeCommand(String command) {
        String[] cs = command.split(";");
        for (String c : cs) {
            String[] aps = c.split("#");
            if ("conn_test".equalsIgnoreCase(aps[0].trim())) {
                conn_test(aps[1]);
            } else if ("link_mw".equalsIgnoreCase(aps[0].trim())) {
                master_worker_link(aps[1]);
            } else if ("set_root_master".equalsIgnoreCase(aps[0].trim())) {
                setRootMaster(aps[1]);
            } else if ("build_cube_info".equalsIgnoreCase(aps[0].trim())) {
                buildCubeInfo(aps[1]);
            }
        }
    }

    private void buildCubeInfo(String cubeInfo) {
        String[] ss = cubeInfo.split(">");
        String[] ss_0 = ss[0].split("@");
        String[] ss_1 = ss[1].split(":");
        String spaceName = ss_0[0].trim();
        String cubeName = ss_0[1].trim();
        String vceNodeIp = ss_1[0].trim();
        int vceNodePort = Integer.parseInt(ss_1[1].trim());

        Cube cube = OlapEngine.hold().findCube(spaceName, cubeName);
        SyncCubeExeUnit eu = new SyncCubeExeUnit(cube);
        List<Byte> bs = eu.genTcpPkgBytesList();

        VCEWorkConnector conn = VCEWorkConnector.openPoolIns(vceNodeIp, vceNodePort);
        try {
            conn.send(bs);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            VCEWorkConnector.closePoolIns(conn);
        }
    }

    private void setRootMaster(String rootMasterInfo) {
        String[] ipAndPort = rootMasterInfo.split(":");
        VCEWorkConnector.VCE_ROOT_MASTER_IP = ipAndPort[0].trim();
        VCEWorkConnector.VCE_ROOT_MASTER_PORT = Integer.parseInt(ipAndPort[1].trim());
    }

    private void master_worker_link(String linkMWCommand) {
        String[] mwInfo = linkMWCommand.split(">");
        String[] master = mwInfo[0].split(":");
        String[] worker = mwInfo[1].split(":");
        int masterPort = Integer.parseInt(master[1].trim());
        int workerPort = Integer.parseInt(worker[1].trim());
        VCEWorkConnector conn = VCEWorkConnector.openPoolIns(master[0].trim(), masterPort);
        try {
            conn.masterLinkToWorker(worker[0].trim(), workerPort);
        } catch (RuntimeException re) {
            throw re;
        } finally {
            VCEWorkConnector.closePoolIns(conn);
        }
    }

    private void conn_test(String testConnCommand) {
        String[] params = testConnCommand.split(":");
        String ip = params[0].trim();
        int port = Integer.parseInt(params[1].trim());
        VCEWorkConnector conn = VCEWorkConnector.openPoolIns(ip, port);
        try {
            conn.test();
        } catch (RuntimeException re) {
            throw re;
        } finally {
            VCEWorkConnector.closePoolIns(conn);
        }
    }

}
