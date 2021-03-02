//core_source_code!_yxIwInyIsIhn_11l11


package cn.bgotech.wormhole.olap.mdx.auxi;

import cn.bgotech.analytics.bi.bean.mddm.physical.role.DimensionRoleBean;
import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.component.MddmStorageService;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.component.vcexeUnits.*;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by czg on 2019/7/13.
 */
public class /*^!*/Auxiliary_MDDL_NQ/*?$*//*_yxIwInyIsIhn_11l11*/ {

    private String cubeName;

    public enum ActionEnum {
        INSERT,
        UPDATE,
        DELETE
    }

    private ActionEnum action;

    private String _VCENodesInfo;

    private String spaceName;

    private List<AuxCreateDimension> createdDms;

    private List<AuxWriteBackCube> writeBackCubes;

    private List<AuxCreateMember> createMembers;

    private List<AuxBuildCube> buildCubes;

    private List<AuxSyncCube> sync_cubes;

    private List<AuxLoadCubeData> memCubeData;

    private List<AuxCUDMeasure> _CUDMeasures;

    public Object handle_MDDL_NQ() throws BIException {

        boolean rebuildCacheFlag = false;

        if (createdDms != null) {
            handleCreateDimension();
            rebuildCacheFlag = true;
        } else if (createMembers != null) {
            handleCreateMembers();
            rebuildCacheFlag = true;
        } else if (buildCubes != null) {
            handleBuildCubes();
            rebuildCacheFlag = true;
        } else if (sync_cubes != null) {
            handleSyncCubes();
        } else if (memCubeData != null) {
            handleLoadCubeData();
        } else if (_CUDMeasures != null) {
            handleCUDMeasures();
        } else if (writeBackCubes != null) {
            handleWriteBackCubes();
        }

        if (rebuildCacheFlag)
            OlapEngine.hold().getMddmStorageService().rebuildSpaceCache(spaceName);

        return null;
    }

    public void setSpace(String _spaceName) {
        spaceName = _spaceName;
    }

    public void setAction(ActionEnum _act) {
        action = _act;
    }

    public void add(AuxWriteBackCube wbc) {
        (writeBackCubes == null ? writeBackCubes = new ArrayList<>() : writeBackCubes).add(wbc);
    }

    private void handleWriteBackCubes() throws BIException {
        for (VCEWorkConnector conn : VCEWorkConnector.parseOutVCEWorkConnectors(_VCENodesInfo)) {
            for (AuxWriteBackCube w : writeBackCubes)
                conn.send(new WriteBackCubeExeUnit(spaceName, w.getCubeName()));
            conn.close();
        }
    }

    public void add(AuxCreateDimension _cd) {
        (createdDms == null ? createdDms = new ArrayList<>() : createdDms).add(_cd);
    }

    private void handleCreateDimension() {
        List<String> nameList = new LinkedList<>();
        List<Integer> maxMbrLvList = new LinkedList<>();
        for (AuxCreateDimension cd : createdDms) {
            nameList.add(cd.getDimName());
            maxMbrLvList.add(cd.getMaxMbrLv());
        }
        OlapEngine.hold().getMddmStorageService().createDimensions(spaceName, nameList, maxMbrLvList);
    }

    public void add(AuxCreateMember _cm) {
        (createMembers == null ? createMembers = new ArrayList<>() : createMembers).add(_cm);
    }

    private void handleCreateMembers() {
        List<MultiDimensionalDomainSelector> selectors = new LinkedList<>();
        for (AuxCreateMember m : createMembers) {
            selectors.add(m.getMDDMSelector());
        }
        OlapEngine.hold().getMddmStorageService().createMembers(spaceName, selectors);
    }

    public void add(AuxBuildCube _bc) {
        (buildCubes == null ? buildCubes = new ArrayList<>() : buildCubes).add(_bc);
    }

    private void handleBuildCubes() {
        OlapEngine.hold().getMddmStorageService().buildCubes(spaceName, buildCubes);
    }

    public void add(AuxSyncCube _sc) {
        (sync_cubes == null ? sync_cubes = new ArrayList<>() : sync_cubes).add(_sc);
    }

    private void handleSyncCubes() throws BIException {
        for (VCEWorkConnector conn : VCEWorkConnector.parseOutVCEWorkConnectors(_VCENodesInfo)) {
            for (AuxSyncCube asc : sync_cubes)
                conn.send(new SyncCubeExeUnit(OlapEngine.hold().findCube(spaceName, asc.getCubeName())));
            conn.close();
        }
    }

    public void add(AuxCUDMeasure _cud) {
        (_CUDMeasures == null ? _CUDMeasures = new ArrayList<>() : _CUDMeasures).add(_cud);
    }

    private void handleCUDMeasures() throws BIException {

        // 19-07-15 10:30 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        MddmStorageService mdmSvr = OlapEngine.hold().getMddmStorageService();

//        String spaceName = exeUnits.get(0).getSpaceName();
//        THREAD_CURRENT_SPACE_NAME.set(spaceName);

        mdmSvr.buildSpaceCacheSuiteWhenIsNo(spaceName);

        Cube cube = mdmSvr.findCube(spaceName, cubeName);

//        VCE_ExecutionUnit veu;
//        Cube cube;
        List<VCEExecutableUnit> vceExecutableUnitList = new ArrayList<>();

        for (AuxCUDMeasure cud : _CUDMeasures) {
//        for (ExecutionUnit eu : exeUnits) {

            // veu = (VCE_ExecutionUnit) eu;


            List<MemberRole> mrs = new LinkedList<>();
            for (MultiDimensionalDomainSelector selector : cud.getMbrSelectors()) {
                mrs.add(mdmSvr.findMemberRole(cube, selector));
            }

            DimensionRole measureDR = mdmSvr.getMeasureDimensionRole(cube);
            Dimension meaDm = mdmSvr.findDimensionById(((DimensionRoleBean) measureDR).getDimensionId());
            Member measureRoot = meaDm.getSuperRootMember();
            MultiDimensionalDomainSelector.Part meaRootPart
                    = new MultiDimensionalDomainSelector.Part("" + measureRoot.getMgId(), null);

            Map<MemberRole, Double> insert_update_meaMbrRoleValueMap = new HashMap<>();
            List<MemberRole> delete_meaMbrRoles = new LinkedList<>();

            for (Iterator<AuxCUDMeasure.MeasureInfo> it = cud.getMeasures().listIterator(); it.hasNext(); ) {
                AuxCUDMeasure.MeasureInfo meaInfo = it.next();
                MultiDimensionalDomainSelector tempSelector = new MultiDimensionalDomainSelector(Arrays.asList(meaRootPart));
                tempSelector.append(new MultiDimensionalDomainSelector.Part(null, meaInfo.getName()));

                MemberRole meRo = (MemberRole) mdmSvr.selectSingleEntity(measureDR, tempSelector);

                if (ActionEnum.DELETE.equals(action))
                    delete_meaMbrRoles.add(meRo);
                else
                    insert_update_meaMbrRoleValueMap.put(meRo, meaInfo.getValue());
            }

            if (ActionEnum.INSERT.equals(action))
                vceExecutableUnitList.add(new InsertMeasureUnit(cube, mrs, insert_update_meaMbrRoleValueMap));
            else if (ActionEnum.UPDATE.equals(action))
                vceExecutableUnitList.add(new UpdateMeasureUnit(cube, mrs, insert_update_meaMbrRoleValueMap));
            else if (ActionEnum.DELETE.equals(action))
                vceExecutableUnitList.add(new DeleteMeasureUnit(cube, mrs, delete_meaMbrRoles));

        }


        List<VCEWorkConnector> connectors = VCEWorkConnector.parseOutVCEWorkConnectors(_VCENodesInfo);
        try {
            if (ActionEnum.INSERT.equals(action)) {
                for (VCEWorkConnector c : connectors)
                    OlapEngine.hold().getVectorComputingEngine().handleInsertMeasureUnits(c, InsertMeasureUnit.transform(vceExecutableUnitList));
            } else {
                for (VCEWorkConnector c : connectors)
                    OlapEngine.hold().getVectorComputingEngine().handleExecutableUnits(c, vceExecutableUnitList);
                // OlapEngine.hold().getVectorComputingEngine().handleExecutableUnits_VCEMNG(vceExecutableUnitList);
            }


//            if (ActionEnum.INSERT.equals(action))
//                handleCUDMeasures_insert(connectors);
//            else if (ActionEnum.UPDATE.equals(action))
//                handleCUDMeasures_update(connectors);
//            else if (ActionEnum.DELETE.equals(action))
//                handleCUDMeasures_delete(connectors);
        } catch (RuntimeException re) {
            throw re;
        } catch (BIException e) {
            throw e;
        } finally {
            for (VCEWorkConnector c : connectors)
                c.close();
        }


//        VectorComputingEngine vectorComputingEngine = OlapEngine.hold().getVectorComputingEngine();
//        if (InsertMeasureUnit.allAreInsertMeasureUnit(vceExecutableUnitList))
//            try {
//                vectorComputingEngine.handleInsertMeasureUnits_VCEMNG(InsertMeasureUnit.transform(vceExecutableUnitList));
//            } catch (BIException e) {
//                logger.error(e.getMessage());
//                return e.getMessage();
//            }
//        else
//            vectorComputingEngine.handleExecutableUnits_VCEMNG(vceExecutableUnitList);


        // 19-07-15 10:30 ??????????????????????????????


    }

//    private void handleCUDMeasures_insert(List<VCEWorkConnector> connectors) {
//        Cube cube = OlapEngine.hold().findCube(spaceName, cubeName);
//        MddmStorageService mdmSvr = OlapEngine.hold().getMddmStorageService();
//        List<InsertMeasureUnit> ins = new ArrayList<>();
//        for (AuxCUDMeasure cud : _CUDMeasures) {
//            List<MemberRole> mrs = new ArrayList<>();
//            Map<MemberRole, Double> insMeasuresMap = new HashMap<>();
//            for (MultiDimensionalDomainSelector selector : cud.getMbrSelectors()) {
//                mrs.add(mdmSvr.findMemberRole(cube, selector));
//            }
//
//            for (Iterator<AuxCUDMeasure.MeasureInfo> it = cud.getMeasures().listIterator(); it.hasNext(); ) {
//                AuxCUDMeasure.MeasureInfo meaInfo = it.next();
//                MultiDimensionalDomainSelector tmpSel = new MultiDimensionalDomainSelector(Arrays.asList(meaRootPart));
//            }
//
//            for (Iterator<Map.Entry<String, Double>> itt = nameValMap.entrySet().iterator(); itt.hasNext(); ) {
//                Map.Entry<String, Double> entry = itt.next();
//                MultiDimensionalDomainSelector tempSelector = new MultiDimensionalDomainSelector(Arrays.asList(meaRootPart));
//                tempSelector.append(new MultiDimensionalDomainSelector.Part(null, entry.getKey()));
////                    MemberRole meRo = (MemberRole) measureDR.selectSingleEntity(tempSelector);
//                MemberRole meRo = (MemberRole) mddmStorageService.selectSingleEntity(measureDR, tempSelector);
//                insMeasuresMap.put(meRo, entry.getValue());
//            }
//
//
//        }
//
//        new InsertMeasureUnit(cube, mrs, insMeasuresMap)
//        for (VCEWorkConnector conn : VCEWorkConnector.parseOutVCEWorkConnectors(_VCENodesInfo)) {
//            for (AuxCUDMeasure cud : _CUDMeasures) {
//                if (ActionEnum.INSERT.equals(action)) {
//
//                } else if (ActionEnum.UPDATE.equals(action)) {
//
//                } else if (ActionEnum.DELETE.equals(action)) {
//
//                } else {
//
//                }
//                // conn.send(new LoadCubeDataExeUnit(spaceName, cubeName));
//            }
//            conn.close();
//        }
//    }

//    private void handleCUDMeasures_update(List<VCEWorkConnector> connectors) {
//
//    }

//    private void handleCUDMeasures_delete(List<VCEWorkConnector> connectors) {
//
//    }

    public void add(AuxLoadCubeData _lcd) {
        (memCubeData == null ? memCubeData = new ArrayList<>() : memCubeData).add(_lcd);
    }

    private void handleLoadCubeData() throws BIException {
        for (VCEWorkConnector conn : VCEWorkConnector.parseOutVCEWorkConnectors(_VCENodesInfo)) {
            for (AuxLoadCubeData lcd : memCubeData) {
                conn.send(new LoadCubeDataExeUnit(spaceName, lcd.getCubeName()));
            }
            conn.close();
        }
    }

    public void setVCENodesInfo(String vce_nsi) {
        _VCENodesInfo = vce_nsi;
    }

    public void setCube(String _cubeName) {
        cubeName = _cubeName;
    }

}
