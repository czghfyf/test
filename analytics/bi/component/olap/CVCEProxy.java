// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.component.olap;

import cn.bgotech.analytics.bi.cache.CacheService;
import cn.bgotech.analytics.bi.component.olap.vce.DataBuf;
import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.analytics.bi.system.config.SystemConfiguration;
import cn.bgotech.wormhole.olap.bigdata.MDVectorSet;
import cn.bgotech.wormhole.olap.bigdata.cache.VectorCache;
import cn.bgotech.wormhole.olap.component.CVCEConstants;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.component.VectorComputingEngine;
import cn.bgotech.wormhole.olap.component.vcexeUnits.InsertMeasureUnit;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNull;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.member.MeasureMember;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import cn.bgotech.wormhole.olap.util.BGTechUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

@Service("c_vce_proxy")
public class CVCEProxy implements VectorComputingEngine {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheService cache;

    @Autowired
    private VectorCache vectorCache;

    @Autowired
    private SystemConfiguration sysCfg;

    @Override
    public void init() {
        logger.info("[OLAP] CVCE proxy init()");
    }

    @Override
    public List<BasicData> vectorValue(Cube cube, List<MultiDimensionalVector> vectors) {

        List<MemberRole> measureMemberRoles = new ArrayList<>();

        for (MultiDimensionalVector mdv : vectors)
            measureMemberRoles.add(mdv.popupMeasure());

        if (!sysCfg.isRedisEnable())
            return _vectorValue_(cube, vectors, measureMemberRoles);

        int i, vSize = vectors.size();
        List<BasicData> ds = new ArrayList<>();
        for (i = 0; i < vSize; i++)
            ds.add(null);
        Double d;
        List<MultiDimensionalVector> _vectors = new LinkedList<>();
        List<MemberRole> _measureMemberRoles = new ArrayList<>();
        for (i = vSize - 1; i >= 0; i--) {
            if (vectorCache.hasMeasureValue(cube.getMgId(), vectors.get(i).getMemberRoles(), measureMemberRoles.get(i))) {
                d = vectorCache.getMeasureValue(cube.getMgId(), vectors.get(i).getMemberRoles(), measureMemberRoles.get(i));
                ds.set(i, d == null ? BasicNull.INSTANCE : new BasicNumeric(d));
            } else {
                _vectors.add(vectors.get(i));
                _measureMemberRoles.add(measureMemberRoles.get(i));
            }
        }

        if (_measureMemberRoles.size() == 0)
            return ds;

        List<BasicData> vceData = _vectorValue_(cube, _vectors, _measureMemberRoles);

        ListIterator<BasicData> lit = vceData.listIterator();
        for (i = 0; i < ds.size(); i++) {
            if (ds.get(i) == null)
                ds.set(i, lit.next());
        }

        new Thread(() -> {
            ListIterator<BasicData> vdLit = vceData.listIterator();
            ListIterator<MultiDimensionalVector> _vLit = _vectors.listIterator();
            ListIterator<MemberRole> _mmrLit = _measureMemberRoles.listIterator();
            BasicData bd__;
            Double d__;
            while (vdLit.hasNext()) {
                bd__ = vdLit.next();
                d__ = BasicNull.INSTANCE.equals(bd__) ? null : ((BasicNumeric) bd__).doubleValue();
                vectorCache.setMeasureValue(cube.getMgId(), _vLit.next().getMemberRoles(), _mmrLit.next(), d__);
            }
        }).start();

        return ds;

    }

    /**
     * @param cube
     * @param vectors            doesn't include members related to measure.
     * @param measureMemberRoles
     * @return
     */
    private List<BasicData> _vectorValue_(Cube cube, List<MultiDimensionalVector> vectors,
                                          List<MemberRole> measureMemberRoles) {

        List<BasicData> res = new LinkedList<>();
        int vSize = vectors.size();

        VCEWorkConnector vceConn = VCEWorkConnector.THREAD_CURRENT_CONN.get();

        DataBuf dBuf = vceConn.getDataBuf();
        dBuf.clean();
        dBuf.add(CVCEConstants.REQ_AGG_CALCUL_QUERY); // set request code
        dBuf.add(cube.getMgId().intValue()); // set cube MG_ID
        dBuf.add(vSize); // set the number of queries

        // set reserved space
        long randId = BGTechUtil.genRandomLongValue();
        dBuf.add(randId);
        System.out.println("rand_ID = " + randId);

        dBuf.add(new byte[CVCEConstants.QUEPKG_RESERVED_SPACE_BYTES_SIZE - 8]);

        for (MultiDimensionalVector mdv : vectors)
            for (MemberRole mr : mdv.getMemberRoles())
                dBuf.add(VCEExecutableUnit.expandFull_MG_ID_Path(mr.getMember()));

        byte[] bytes;
        try {
            bytes = vceConn.send();
        } catch (BIException e) {
            logger.error(e.getMessage());
            for (int i = 0; i < vSize; i++)
                res.add(BasicNull.INSTANCE);
            return res;
        }

        // contain the root member
        int measureMembersQuantity = cube.getMeasureDimensionRole().getDimension().getMembers(null).size();

        Double[][] doubles_2D;
        { // a relatively independent program logic
            int d_2d_rows = vSize;
            int d_2d_cols = measureMembersQuantity - 1;
            doubles_2D = new Double[d_2d_rows][d_2d_cols];

            int d_2d_idx = 0;
            int pointer = 0;
            byte[] measureValueBs = new byte[8];

            while (pointer < bytes.length) {
                int current_row = d_2d_idx / d_2d_cols;
                int current_col = d_2d_idx % d_2d_cols;

                byte nullFlag = bytes[pointer]; // 0 - not null, 1 - is null.
                if (nullFlag == 0) { // measure value not null
                    BGTechUtil.memcpy(measureValueBs, bytes, pointer + 1, pointer + 8);
                    double d = BGTechUtil.bytes2Double(measureValueBs);
                    doubles_2D[current_row][current_col] = d;
                }

                d_2d_idx++;
                pointer += 9; // null flag 1 byte and double value 8 bytes
            }
        }

//        Double doubleValue;
        MeasureMember meaMbr;
        int sortedPosition;
        Double tempDouble;
        for (int i = 0; i < vSize; i++) {
            //doubleValue = null;
            meaMbr = (MeasureMember) measureMemberRoles.get(i).getMember();
            sortedPosition = meaMbr.sortedPosition(); // meaMbr is root member when sortedPosition < 0
            if (sortedPosition >= 0) {
                res.add(doubles_2D[i][sortedPosition] == null ? BasicNull.INSTANCE : new BasicNumeric(doubles_2D[i][sortedPosition]));
            } else {
                tempDouble = null;
                for (Double d : doubles_2D[i]) {
                    if (d != null)
                        tempDouble = tempDouble == null ? d : (d + tempDouble);
                }
                res.add(tempDouble == null ? BasicNull.INSTANCE : new BasicNumeric(tempDouble));
            }

////            for (Double[][] dss : d2dList) {
//                if (sortedPosition >= 0) {
//                    if (doubles_2D[i][sortedPosition] != null)
//                        doubleValue = doubleValue == null ? dss[i][sortedPosition] : (dss[i][sortedPosition] + doubleValue);
//                } else { // meaMbr is a root member
//                    for (Double d : dss[i]) {
//                        if (d != null)
//                            doubleValue = doubleValue == null ? d : (d + doubleValue);
//                    }
//                }
////            }
//            res.add(doubleValue == null ? BasicNull.INSTANCE : new BasicNumeric(doubleValue));
        }

        return res;
    }

    @Override
    public boolean existMDVectorSet(String vsFile) {
        return new File(sysCfg.getMdvsFolder() + "/" + vsFile).exists();
    }

    @Override
    public void handleExecutableUnits(VCEWorkConnector vceConn, List<VCEExecutableUnit> vceExecutableUnits) {
        for (VCEExecutableUnit exu : vceExecutableUnits) {
            try {
                vceConn.send(exu);
            } catch (BIException e) {
                logger.error(e.getMessage());
                throw new BIRuntimeException(e);
            }
        }
    }

    @Override
    public void handleExecutableUnits_VCEMNG/*PROHIBITION CONFUSION !*/(List<VCEExecutableUnit> vceExecutableUnits) {
        List<VCEWorkConnector> cs = ThreadLocalTool.getCurrentVCENodeConnectors();
        for (VCEWorkConnector conn : cs)
            handleExecutableUnits(conn, vceExecutableUnits);
    }

    @Override
    public void handleInsertMeasureUnits(VCEWorkConnector vceConn, List<InsertMeasureUnit> units) throws BIException {
        List<Byte> bytes = units.get(0).genTcpPkgBytesList();
        List<Byte> bytes_;
        for (int i = 1; i < units.size(); i++) {
            bytes_ = units.get(i).genTcpPkgBytesList();
            for (int x = 0; x < 8; x++)
                bytes_.remove(0); // remove request code(index 0) and cube MG_ID(index 1)
            bytes.addAll(bytes_);
        }
        DataBuf buf = vceConn.getDataBuf();
        buf.clean();
        buf.add(bytes);
        vceConn.send(buf);
    }

    @Override
    public void handleInsertMeasureUnits_VCEMNG/*PROHIBITION CONFUSION !*/(List<InsertMeasureUnit> units) throws BIException {
        List<VCEWorkConnector> cs = ThreadLocalTool.getCurrentVCENodeConnectors();
        for (VCEWorkConnector conn : cs)
            handleInsertMeasureUnits(conn, units);
    }

    private void _rebuildCubeMemDataStruct(VCEWorkConnector vceConn, Cube c) {
        DataBuf buf;
        buf = vceConn.getDataBuf();
        buf.clean();
        buf.add(CVCEConstants.REQ_REBUI_CUBE_DATA_MEM);
        buf.add(c.getMgId().intValue());
        try {
            vceConn.send();
        } catch (BIException e) {
            logger.error(e.getMessage());
            throw new BIRuntimeException(e);
        }
    }

    @Override
    public void rebuildCubeMemDataStruct_VCEMNG/*PROHIBITION CONFUSION !*/(Cube c) {
        List<VCEWorkConnector> cs = ThreadLocalTool.getCurrentVCENodeConnectors();
        for (VCEWorkConnector conn : cs)
            _rebuildCubeMemDataStruct(conn, c);
    }

    @Override
    public void startup() {
        // connectToCVCEWorks();
        VCEWorkConnector.VCE_ROOT_MASTER_IP = sysCfg.getVceRootMasterHost();
        VCEWorkConnector.VCE_ROOT_MASTER_PORT = sysCfg.getVceRootMasterPort();
    }

    private String _sayHelloToVCE(VCEWorkConnector conn) {
        conn.sayHello();
        return "Don't know the result";

    }

    @Override
    public String sayHelloToVCE_VCEMNG/*PROHIBITION CONFUSION !*/() {
        StringBuilder res = new StringBuilder();
        List<VCEWorkConnector> cs = ThreadLocalTool.getCurrentVCENodeConnectors();
        for (VCEWorkConnector conn : cs)
            res.append(_sayHelloToVCE(conn)).append(",");
        return res.toString();
    }

    @Deprecated
    public MDVectorSet getMDVectorSet(Cube cube) {
        Object vs = cache.getDataSuite(MDVectorSet.class).getObject(MDVectorSet.class, cube);
        if (vs == null) {
            vs = loadMDVectorSet(cube);
            cache.getDataSuite(MDVectorSet.class).setFinalMapping(MDVectorSet.class, o -> cube, vs);
            cache.buildFinalDescendantMembersMapping(cache.getCurrentUserSpaceMDDMDataSuite());
            ((MDVectorSet) vs).buildVectorValueMap();
        }
        return (MDVectorSet) vs;
    }

    private MDVectorSet loadMDVectorSet(Cube c) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(sysCfg.getMdvsFolder() + "/" + MDVectorSet.fileName(c)));
            return (MDVectorSet) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new OlapRuntimeException(e);
        } finally {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new OlapRuntimeException(e);
            }
        }
    }
}