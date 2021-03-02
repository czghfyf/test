package cn.bgotech.wormhole.olap.component;

import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.wormhole.olap.component.vcexeUnits.InsertMeasureUnit;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 * The vector calculation engine is not responsible for the calculation of custom formula members.
 */
public interface VectorComputingEngine {

    /**
     * initialize method:
     * 1. clean all fact data
     * 2. ...
     * 3. ...
     */
    void init();

    default BasicData vectorValue(Cube cube, MultiDimensionalVector vector) {
        return vectorValue(cube, Arrays.asList(vector)).get(0);
    }

    /**
     * @param cube
     * @param vectors Cannot contain calculation formula members, otherwise an exception will be thrown.
     * @return
     */
    List<BasicData> vectorValue(Cube cube, List<MultiDimensionalVector> vectors);

    boolean existMDVectorSet(String vsFile);

    void handleExecutableUnits_VCEMNG(List<VCEExecutableUnit> vceExecutableUnits);

    void startup();

    String sayHelloToVCE_VCEMNG();

    void handleInsertMeasureUnits_VCEMNG(List<InsertMeasureUnit> units) throws BIException;

    void rebuildCubeMemDataStruct_VCEMNG(Cube c);

    void handleInsertMeasureUnits(VCEWorkConnector vceConn, List<InsertMeasureUnit> units) throws BIException;

    void handleExecutableUnits(VCEWorkConnector vceConn, List<VCEExecutableUnit> vceExecutableUnits);
}
