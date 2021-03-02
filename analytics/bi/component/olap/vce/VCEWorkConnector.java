package cn.bgotech.analytics.bi.component.olap.vce;

import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.wormhole.olap.component.CVCEConstants;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.util.BGTechUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by czg on 2019/3/20.
 */
public class VCEWorkConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(VCEWorkConnector.class);

    public static final ThreadLocal<VCEWorkConnector> THREAD_CURRENT_CONN = new ThreadLocal();

    public static String VCE_ROOT_MASTER_IP;
    public static int VCE_ROOT_MASTER_PORT;

    private static final int CONNECTORS_SIZE = 10;
    private static ArrayBlockingQueue<VCEWorkConnector> CONNECTORS;

    private static final byte[] REAL_TIME_SOCKET_PKG = new byte[12];

    static {
        byte[] bs_ = BGTechUtil.int2byteArray(REAL_TIME_SOCKET_PKG.length);
        for (int i = 0; i < bs_.length; i++)
            REAL_TIME_SOCKET_PKG[i] = bs_[i];
        bs_ = BGTechUtil.int2byteArray(CVCEConstants.RC_1ST);
        for (int i = 0; i < bs_.length; i++)
            REAL_TIME_SOCKET_PKG[i + 4] = bs_[i];
        bs_ = BGTechUtil.int2byteArray(CVCEConstants.SKT_TYPE_REAL_TIME);
        for (int i = 0; i < bs_.length; i++)
            REAL_TIME_SOCKET_PKG[i + 8] = bs_[i];

        CONNECTORS = new ArrayBlockingQueue(CONNECTORS_SIZE);
        try {
            for (int i = 0; i < CONNECTORS_SIZE; i++)
                CONNECTORS.put(new VCEWorkConnector());
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private String ip;
    private int port;
    private Socket vce_socket;
    private DataOutputStream vce_dos;
    private DataInputStream vce_dis;

    private final DataBuf dataBuf = new DataBuf();

    public static VCEWorkConnector openPoolIns(String ip, int port) {
        VCEWorkConnector conn;
        try {
            conn = CONNECTORS.take();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        conn.connect(ip, port);
        return conn;
    }

    public static void closePoolIns(VCEWorkConnector conn) {
        conn.disconnectCVCE();
        try {
            CONNECTORS.put(conn);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public DataBuf getDataBuf() {
        return dataBuf;
    }

    public static List<VCEWorkConnector> parse(String vceWorksCluster, int bufSize) {


        List<VCEWorkConnector> res = new LinkedList<>();
        for (String str : vceWorksCluster.split(";")) {
            String[] ss = str.split(":");
            VCEWorkConnector con = new VCEWorkConnector();
            con.ip = ss[0];
            con.port = Integer.parseInt(ss[1]);
            res.add(con);
        }
        return res;
    }

    public void connect(String ip_, int port_) {
        ip = ip_;
        port = port_;
        connect();
    }

    public void connect() {

        try {
            vce_socket = new Socket(ip, port);
            vce_dis = new DataInputStream(vce_socket.getInputStream());
            vce_dos = new DataOutputStream(vce_socket.getOutputStream());
            vce_dos.write(REAL_TIME_SOCKET_PKG, 0, REAL_TIME_SOCKET_PKG.length);
            vce_dis.read(new byte[32]);
            LOGGER.debug("connect to vce " + ip + ":" + port);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            disconnectCVCE();
            throw new RuntimeException(e);
        }
    }


    private void disconnectCVCE() {

        if (vce_dis != null) {
            try {
                vce_dis.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

        if (vce_dos != null) {
            try {
                vce_dos.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

        if (vce_socket != null) {
            try {
                vce_socket.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

    }

    public void send(VCEExecutableUnit ceu) throws BIException {
        dataBuf.clean();
        dataBuf.add(ceu.generateTCPPackets());
        try {
            sendBytes(dataBuf.getBytes(), 0, dataBuf.effectiveDataLength());
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            disconnectCVCE();
            connect();
        }
    }

    public byte[] send() throws BIException {
        return send(this.dataBuf);
    }

    public byte[] send(DataBuf dBuf) throws BIException {
        int retBytesQty;
        try {
            retBytesQty = sendBytes(dBuf.getBytes(), 0, dBuf.effectiveDataLength());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            disconnectCVCE();
            connect();
            return null;
        }
        byte[] rs = new byte[retBytesQty];
        for (int i = 0; i < retBytesQty; i++) {
            rs[i] = dBuf.getBytes()[i];
        }
        return rs;
    }

    public void sayHello() {
        try {
            byte[] bs = BGTechUtil.int2byteArray(CVCEConstants.REQ_CLI_TEST), bs_ = new byte[8];
            for (int i = 0; i < 4; i++)
                bs_[i] = bs[i];
            sendBytes(bs_, 0, 4);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            disconnectCVCE();
            connect();
        } catch (BIException e) {
            LOGGER.warn(e.getMessage());
            throw new BIRuntimeException("Data buffer is too small! " + e.getMessage());
        }
    }

    private int sendBytes(byte[] bs, int off, int len) throws BIException, IOException {
        if ((off + len) > (bs.length - 4))
            throw new BIException(String.format("Unable to hold data about packet size. " +
                    "[ off = %d ] [ len = %d ] [ bs.length = %d ]", off, len, bs.length));

        for (int i = off + len + 3; i > off + 3; i--)
            bs[i] = bs[i - 4];

        byte[] pkgLenBytes = BGTechUtil.int2byteArray(len + 4);
        for (int i = 0; i < 4; i++)
            bs[off + i] = pkgLenBytes[i];

        vce_dos.write(bs, off, len + 4);
        return vce_dis.read(bs);

    }

    public void test() {
        byte[] bytes = dataBuf.getBytes();
        byte[] bs = BGTechUtil.int2byteArray(8);
        for (int i = 0; i < 4; i++) {
            bytes[i] = bs[i];
        }
        bs = BGTechUtil.int2byteArray(CVCEConstants.REQ_CLI_TEST);
        for (int i = 0; i < 4; i++) {
            bytes[i + 4] = bs[i];
        }
        try {
            vce_dos.write(bytes, 0, 8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int ret = 0;
        try {
            ret = vce_dis.read(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("test VCE node [" + ip + ":" + port + "], return: " + new String(bytes, 0, ret));
    }

    public void masterLinkToWorker(String workerIp, int workerPort) {
        byte[] bs = BGTechUtil.int2byteArray(4 + 4 + 32 + 4);
        for (int i = 0; i < 4; i++) {
            dataBuf.getBytes()[i] = bs[i];
        }

        bs = BGTechUtil.int2byteArray(CVCEConstants.REQ_MASTER_LINK_WORKER);
        for (int i = 0; i < 4; i++) {
            dataBuf.getBytes()[i + 4] = bs[i];
        }

        for (int i = 8; i < 40; i++) {
            dataBuf.getBytes()[i] = 0;
        }
        try {
            bs = workerIp.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        for (int i = 0; i < bs.length; i++) {
            dataBuf.getBytes()[i + 8] = bs[i];
        }
        bs = BGTechUtil.int2byteArray(workerPort);
        for (int i = 0; i < 4; i++) {
            dataBuf.getBytes()[i + 40] = bs[i];
        }
        try {
            vce_dos.write(dataBuf.getBytes(), 0, 4 + 4 + 32 + 4);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        int ret;
        try {
            ret = vce_dis.read(dataBuf.getBytes(), 0, dataBuf.getBytes().length);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        LOGGER.info(String.format("master [%s:%d] link to worker [%s:%d] success [%s]",
                ip, port, workerIp, workerPort, new String(dataBuf.getBytes(), 0, ret)));

    }

    public void send(List<Byte> bs) throws IOException {
        byte[] bytes = dataBuf.getBytes();
        for (int i = 0; i < bs.size(); i++)
            bytes[i + 4] = bs.get(i);
        byte[] temp = BGTechUtil.int2byteArray(bs.size() + 4);
        for (int i = 0; i < temp.length; i++)
            bytes[i] = temp[i];
        vce_dos.write(bytes, 0, bs.size() + 4);
        dataBuf.setIndex(vce_dis.read(bytes, 0, bytes.length));
    }

    public static List<VCEWorkConnector> parseOutVCEWorkConnectors(String vceNodesInfo) {

//        List<VCEWorkConnector.NodeInfo> readyConnectVceNodes = new LinkedList<>();
        String[] ss = vceNodesInfo.split(",");
        List<VCEWorkConnector> ls = new ArrayList<>();
        for (String hostAndPort : ss) {
            String[] host_port = hostAndPort.split(":");
//            VCEWorkConnector.NodeInfo nodeInfo = new VCEWorkConnector.NodeInfo();
            VCEWorkConnector conn = new VCEWorkConnector();
            conn.connect(host_port[0].trim(), Integer.parseInt(host_port[1].trim()));
            ls.add(conn);
//            nodeInfo.setHost();
//            nodeInfo.setPort();
//            readyConnectVceNodes.add(nodeInfo);
        }

        return ls;
//        ThreadLocalTool.setCurrentReadyConnectVCENodes(readyConnectVceNodes);

    }

    public void close() {
        disconnectCVCE();
    }

    public static class NodeInfo {
        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
