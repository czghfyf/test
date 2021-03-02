package cn.bgotech.analytics.bi.component.olap;

import cn.bgotech.analytics.bi.cache.CacheService;
import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.analytics.bi.system.config.SystemConfiguration;
import cn.bgotech.wormhole.olap.bigdata.MDVectorSet;
import cn.bgotech.wormhole.olap.bigdata.cache.VectorCache;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.component.VectorComputingEngine;
import cn.bgotech.wormhole.olap.component.vcexeUnits.InsertMeasureUnit;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNull;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

@Deprecated
@Service("vector_computing_engine")
public class VectorComputingEngineImpl implements VectorComputingEngine {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheService cache;

    @Autowired
    private SystemConfiguration sysCfg;

    @Autowired
    private VectorCache vectorCache;

    @Override
    public void init() {
        logger.info("[OLAP] vector computing engine init()");
    }

    @Override
    public List<BasicData> vectorValue(Cube cube, List<MultiDimensionalVector> vectors) {
        List<BasicData> result = new ArrayList<>();
        MDVectorSet vs = getMDVectorSet(cube);
        Double[] doubleValues;


        doubleValues = new Double[vectors.size()];
        List<Integer> tempPositions = new LinkedList<>();
        List<MultiDimensionalVector> tempVectors = new LinkedList<>();
        for (int i = 0; i < vectors.size(); i++) {
            MultiDimensionalVector v = vectors.get(i);
            if (!v.hasNonexistentMemberRole()) {
                tempPositions.add(i);
                tempVectors.add(v);
            }
        }
        if (!tempVectors.isEmpty()) {
            Map<Long, List<Long>> finalDescendantMembersMap = (Map<Long, List<Long>>) cache.getCurrentUserSpaceMDDMDataSuite()
                    .getObject(CacheService.CacheNode.SpecialLinkType.FINAL_DESCENDANT_MEMBERS_MAPPING,
                            CacheService.CacheNode.SpecialLinkType.FINAL_DESCENDANT_MEMBERS_MAPPING);

            if (sysCfg.getCachePreDataOnStart()) {
                finalDescendantMembersMap.putAll((Map<Long, List<Long>>) cache.getDataSuite(CacheService.CacheNode.SpecialLinkType.SYSTEM_PRESET_DATA)
                        .getObject(CacheService.CacheNode.SpecialLinkType.FINAL_DESCENDANT_MEMBERS_MAPPING,
                                CacheService.CacheNode.SpecialLinkType.FINAL_DESCENDANT_MEMBERS_MAPPING));
            }

            Double[] tempDoubleValues = vs.setVectorCache(vectorCache).evaluate(tempVectors, finalDescendantMembersMap);
            for (int i = 0; i < tempDoubleValues.length; i++) {
                doubleValues[tempPositions.get(i)] = tempDoubleValues[i];
            }
        }


        for (int i = 0; i < doubleValues.length; i++) {
            result.add(doubleValues[i] != null ? new BasicNumeric(null, doubleValues[i]) : BasicNull.INSTANCE);
        }
        return result;
    }

    @Override
    public boolean existMDVectorSet(String vsFile) {
        return new File(sysCfg.getMdvsFolder() + "/" + vsFile).exists();
    }

    @Override
    public void handleExecutableUnits_VCEMNG(List<VCEExecutableUnit> vceExecutableUnits) {
        throw new RuntimeException("Can't run this method.");
    }

    @Override
    public void startup() {

    }

    @Override
    public String sayHelloToVCE_VCEMNG() {
        throw new RuntimeException("Can't run this method.");
    }

    @Override
    public void handleInsertMeasureUnits_VCEMNG(List<InsertMeasureUnit> units) {
        throw new RuntimeException("Can't run this method.");
    }

    @Override
    public void rebuildCubeMemDataStruct_VCEMNG(Cube c) {
        throw new RuntimeException("Can't run this method.");
    }

    @Override
    public void handleInsertMeasureUnits(VCEWorkConnector vceConn, List<InsertMeasureUnit> units) throws BIException {
        throw new BIException("do not call this method");
    }

    @Override
    public void handleExecutableUnits(VCEWorkConnector vceConn, List<VCEExecutableUnit> vceExecutableUnits) {
        throw new RuntimeException("do not call this method");
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
