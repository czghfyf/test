// PROHIBITION CONFUSION !

package cn.bgotech.wormhole.olap;

import cn.bgotech.analytics.bi.bean.mddm.physical.role.DimensionRoleBean;

import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.wormhole.olap.component.MddmStorageService;
import cn.bgotech.wormhole.olap.component.VCEExecutableUnit;
import cn.bgotech.wormhole.olap.component.VectorComputingEngine;
import cn.bgotech.wormhole.olap.component.vcexeUnits.SyncCubeExeUnit;
import cn.bgotech.wormhole.olap.component.vcexeUnits.DeleteMeasureUnit;
import cn.bgotech.wormhole.olap.component.vcexeUnits.InsertMeasureUnit;
import cn.bgotech.wormhole.olap.component.vcexeUnits.LoadCubeDataExeUnit;
import cn.bgotech.wormhole.olap.component.vcexeUnits.UpdateMeasureUnit;
import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.imp.text.RawCubeDataPackage;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNull;
import cn.bgotech.wormhole.olap.mddm.physical.ClassicMDDM;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.UniversalDimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.MDXQueryResult;
import cn.bgotech.wormhole.olap.mdx.MDXQueryResultSFP;
import cn.bgotech.wormhole.olap.mdx.ResultOfExecuteMDX;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeBuildCubeUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeDeleteMeasuresUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeInsertMeasuresUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeLoadCubeDataUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExeUpdateMeasuresUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.ExecutionUnit;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.MDDManagementAssistant;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.VCE_ExecutionUnit;
import cn.bgotech.wormhole.olap.mdx.auxi.Auxiliary_MDDL_NQ;
import cn.bgotech.wormhole.olap.mdx.parser.WormholeMDXParser;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import cn.bgotech.wormhole.olap.util.BGTechUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by czg on 2017/5/12.
 */
public class OlapEngine {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final ThreadLocal<String> THREAD_CURRENT_SPACE_NAME = new ThreadLocal();

    private static OlapEngine SINGLE_INSTANCE;

    public static String getCurrentThreadSpaceName() {
        return THREAD_CURRENT_SPACE_NAME.get();
    }

    public static void setSingleInstance(OlapEngine singleInstance) {
        if (SINGLE_INSTANCE == null) {
            SINGLE_INSTANCE = singleInstance;
        }
    }

    public static OlapEngine hold() {
        return SINGLE_INSTANCE;
    }

    private static final ThreadLocal<Space> CURRENT_THREAD_SPACE_POOL = new ThreadLocal<>();

    public static void setCurrentThreadSpace(Space space) {
        CURRENT_THREAD_SPACE_POOL.set(space);
    }

    public static Space getCurrentThreadSpace() {
        return CURRENT_THREAD_SPACE_POOL.get();
    }

    private MddmStorageService mddmStorageService; // PROHIBITION CONFUSION !

    private VectorComputingEngine vectorComputingEngine; // PROHIBITION CONFUSION !

    public MddmStorageService getMddmStorageService() {
        return mddmStorageService;
    }

    public VectorComputingEngine getVectorComputingEngine() {
        return vectorComputingEngine;
    }

    public void setMddmStorageService(MddmStorageService mddmStorageService) {
        this.mddmStorageService = mddmStorageService;
    }

    public void setVectorComputingEngine(VectorComputingEngine vectorComputingEngine) {
        this.vectorComputingEngine = vectorComputingEngine;
    }

    /**
     * startup wormhole OLAP engine
     */
    public void startup() {
        logger.info("OLAP engine startup...");
//        mddmStorageService.startup();
        vectorComputingEngine.startup();
    }

    /**
     * initialize OLAP engine
     */
    public void init() {
        mddmStorageService.init();
        vectorComputingEngine.init();
    }

    public void importCube(Space space, String cubeName, byte[] fileBytes) throws OlapException {
        importCube(space, cubeName, RawCubeDataPackage.buildByByteArray(fileBytes));
    }

    public void importCube(Space space, String cubeName, String cubeDataText) throws OlapException {
        importCube(space, cubeName, RawCubeDataPackage.buildByText(cubeDataText));
    }

    public void importCube(Space space, String cubeName, RawCubeDataPackage dataPkg) throws OlapException {

        for (Map<String, String> _map : dataPkg.getUniversalDimensions()) {
            if (mddmStorageService.findUniversalDimension(space, _map.get("DIMENSION")) != null) {
                throw new OlapException("in space[" + space.getName() + "], dimension[" + _map.get("DIMENSION") + "] already exist");
            }
        }

        mddmStorageService.executeImport(space, cubeName, dataPkg); // TODO: Time consuming 720 S, cube data count 200000

        logger.info("$>>>>>>>>>>>> import cube data: import " + cubeName + " data complete");

    }

    public Space findSpace(String name) {
        return mddmStorageService.findSpaceByName(name);
    }

    public ResultOfExecuteMDX execute(Space space, String mdx) {
        Object sfp;
        try {
            sfp = new WormholeMDXParser(mdx, this).execute();
        } catch (cn.bgotech.wormhole.olap.mdx.parser.ParseException e) {
            e.printStackTrace();
            throw new OlapRuntimeException(e);
        }
        if (!(sfp instanceof MDXQueryResultSFP)) {
            throw new OlapRuntimeException("undefined subclass of SFPOfExecuteMDX: " + sfp.getClass().getName());
        }

        return new MDXQueryResult((MDXQueryResultSFP) sfp).transform();
    }

    /**
     * @param cube
     * @param clazz
     * @param selector
     * @param <E>
     * @return
     * @deprecated todo 确定一下此方法返回的对象是否都带有角色信息？如果是，此方法名称有问题，不能通过方法名得知其返回对象均属于角色。
     */
    public <E> E findUniqueEntity(Cube cube, Class<E> clazz, MultiDimensionalDomainSelector selector) {
        return mddmStorageService.findUniqueEntity(cube, clazz, selector);
    }

    public <E> E find(Class<? extends ClassicMDDM> clazz, Long mgId) {
        return mddmStorageService.find(clazz, mgId);
    }

    public <E> List<E> find(Class<? extends ClassicMDDM> clazz, String name) {
        return mddmStorageService.find(clazz, name);
    }

    public List<? extends Member> findDimensionMembers(Dimension dimension) {
        return mddmStorageService.findDimensionMembers(dimension);
    }

    public BasicEntityModel selectSingleEntity(Dimension dimension, MultiDimensionalDomainSelector.Part part) {
        return mddmStorageService.selectSingleEntity(dimension, part);
    }

    public BasicEntityModel selectSingleEntity(Hierarchy hierarchy, MultiDimensionalDomainSelector.Part part) {
        return mddmStorageService.selectSingleEntity(hierarchy, part);
    }

    public Member selectSingleEntity(Member member, MultiDimensionalDomainSelector.Part part) {
        return mddmStorageService.selectSingleEntity(member, part);
    }

    public Member selectSingleEntity(Level level, MultiDimensionalDomainSelector.Part part) {
        return mddmStorageService.selectSingleEntity(level, part);
    }

    public <E> List<E> findAll(Class<E> clazz) {
        return mddmStorageService.findAll(clazz);
    }

    public Level getLevel(long hierarchyId, int memberLevel) {
        return mddmStorageService.getLevel(hierarchyId, memberLevel);
    }

    /**
     * query and return all the descendants of this member
     *
     * @param member
     * @return
     */
    public List<Member> findMemberDescendants(Member member) {
        return mddmStorageService.findMemberDescendants(member);
    }

    public List<Member> findMembers(Level level) {
        return mddmStorageService.findMembers(level);
    }

    public DimensionRole findMeasureDimensionRole(Cube cube) {
        return mddmStorageService.findMeasureDimensionRole(cube);
    }

    public List<DimensionRole> findUniversalDimensionRoles(Cube cube) {
        return mddmStorageService.findUniversalDimensionRoles(cube);
    }

    public Member findMemberById(long id) {
        return mddmStorageService.findMemberById(id);
    }

    public Member findMemberByMgId(long memberMgId) {
        return mddmStorageService.findMemberByMgId(memberMgId);
    }

    public Dimension findDimensionById(long id) {
        return mddmStorageService.findDimensionById(id);
    }

    public BasicData findProperty(Member member, String propertyKey) {
        return mddmStorageService.findProperty(member, propertyKey);
    }

    public List<UniversalDimension> getSysPredefinedDimensions() {
        return mddmStorageService.getSysPredefinedDimensions();
    }

    public List<BasicData> vectorValue(Cube cube, List<MultiDimensionalVector> vectors) {

        BasicData[] basicDataArr = new BasicData[vectors.size()];
        List<MultiDimensionalVector> noFormulaMemberVectors = new ArrayList<>();
        MultiDimensionalVector v;

        for (int i = 0; i < vectors.size(); i++) {
            v = vectors.get(i);
            if (v.includeNonexistentMember()) {
                basicDataArr[i] = BasicNull.INSTANCE;
            } else if (v.includeCalculatedMembers()) {
                try {
                    // get vector value
                    basicDataArr[i] = v.getNearestCalculatedMember().getExpression().evaluate(v);
                } catch (OlapException e) {
                    logger.error(e.getMessage(), e);
                    throw new OlapRuntimeException(e);
                }
            } else {
                noFormulaMemberVectors.add(v);
            }
        }

        if (!noFormulaMemberVectors.isEmpty()) {
            List<BasicData> basicDatas = vectorComputingEngine.vectorValue(cube, noFormulaMemberVectors);
            int j = 0;
            for (int i = 0; i < basicDataArr.length; i++) {
                if (basicDataArr[i] == null) {
                    basicDataArr[i] = basicDatas.get(j++);
                    if (j == basicDatas.size()) {
                        break;
                    }
                }
            }
        }

        return Arrays.asList(basicDataArr);
    }

    public void openListeningService(int port) {
        new Thread(() -> {
            ServerSocket ss;
            try {
                ss = new ServerSocket(port);
            } catch (IOException e) {
                logger.error(e.getMessage());
                return;
            }
            while (true) {
                Socket socket;
                try {
                    socket = ss.accept();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    continue;
                }
                new Thread(() -> {
                    DataInputStream dis = null;
                    DataOutputStream dos = null;
                    boolean normalState = true;
                    try {
                        dis = new DataInputStream(socket.getInputStream());
                        dos = new DataOutputStream(socket.getOutputStream());
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        normalState = false;
                    }
                    byte[] buf = new byte[1024 * 1024 * 8]; // 8M
                    int len;
                    while (normalState) {
                        String retMessage;
                        try {
                            len = BGTechUtil.readAllBytes(buf, dis);
                            if (len <= 0)
                                break;
                            retMessage = this.handleScript(new String(buf, 0, len, "UTF-8"));
                            if (!"success".equalsIgnoreCase(retMessage)) {
                                retMessage = "handle Script error: " + retMessage;
                            }
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                            normalState = false;
                            retMessage = "Socket error: " + e.getMessage();
                        }

                        try {
                            dos.write(retMessage.getBytes("UTF-8"));
                            dos.flush();
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                            normalState = false;
                        }
                    }
                    try {
                        dis.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                    try {
                        dos.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }).start();
            }
//            ss.close(); // TODO close the ServerSocket
        }).start();
    }

    private String handleCommand(String[] ss) {

        if ("RB_CUBE_DATA_MEM".equals(ss[1].trim())) {
            String spaceName = ss[2].trim();
            String cubeName = ss[3].trim();
            vectorComputingEngine.rebuildCubeMemDataStruct_VCEMNG(mddmStorageService.findCube(spaceName, cubeName));
        }

        return "success";
    }

    public String handleScript(String script) {

//        if (script.startsWith("<[COMMAND]>"))
//            return handleCommand(script.split("!"));

        if ("hello".equals(script))
            return vectorComputingEngine.sayHelloToVCE_VCEMNG();

        Object result;
        try {
            result = new WormholeMDXParser(script, this).execute();
        } catch (cn.bgotech.wormhole.olap.mdx.parser.ParseException e) {
            logger.error(e.getMessage());
            return e.getMessage();
        }

        if (result instanceof MDDManagementAssistant) {
            return handleMngAssistant((MDDManagementAssistant) result);
        } else if (result instanceof Auxiliary_MDDL_NQ) {
            try {
                ((Auxiliary_MDDL_NQ) result).handle_MDDL_NQ();
            } catch (BIException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
            return "success";
        } else {
            logger.warn("Cannot execute this statement");
            return "Cannot execute this statement";
        }
    }

    public String handleMngAssistant(MDDManagementAssistant mma) {
        String res = mddmStorageService.executeImport(mma.getExeUnits());
        if ("success".equalsIgnoreCase(res)) {
            return handleVCEExeUnits(mma.getExeUnits());
        } else {
            return "Error when creating a multidimensional model: " + res;
        }
    }

    private String handleVCEExeUnits(List<ExecutionUnit> exeUnits) {

        String spaceName = exeUnits.get(0).getSpaceName();
        THREAD_CURRENT_SPACE_NAME.set(spaceName);

        mddmStorageService.buildSpaceCacheSuiteWhenIsNo(spaceName);

        VCE_ExecutionUnit veu;
        Cube cube;

        List<VCEExecutableUnit> vceExecutableUnitList = new LinkedList<>();

        for (ExecutionUnit eu : exeUnits) {
            if (!(eu instanceof VCE_ExecutionUnit))
                continue;
            if (eu instanceof ExeBuildCubeUnit) {
                cube = (mddmStorageService.findCube(mddmStorageService.findSpaceByName(((ExeBuildCubeUnit) eu).spaceName), ((ExeBuildCubeUnit) eu).cubeName));
                vceExecutableUnitList.add(new SyncCubeExeUnit(cube));
                continue;
            }
            veu = (VCE_ExecutionUnit) eu;
            cube = mddmStorageService.findCube(mddmStorageService.findSpaceByName(veu.spaceName), veu.cubeName);
            List<MemberRole> mrs = new LinkedList<>();
            for (MultiDimensionalDomainSelector selector : veu.tupleInfo) {
//                mrs.add(mddmStorageService.findUniqueEntity(cube, MemberRole.class, selector));
                mrs.add(mddmStorageService.findMemberRole(cube, selector));
            }
            DimensionRole measureDR = mddmStorageService.getMeasureDimensionRole(cube);
            Dimension meaDm = mddmStorageService.findDimensionById(((DimensionRoleBean) measureDR).getDimensionId());
            Member measureRoot = meaDm.getSuperRootMember();
            MultiDimensionalDomainSelector.Part meaRootPart
                    = new MultiDimensionalDomainSelector.Part("" + measureRoot.getMgId(), null);
            if (veu instanceof ExeInsertMeasuresUnit) {
                Map<String, Double> nameValMap = ((ExeInsertMeasuresUnit) veu).getMeasureValMap();
                Map<MemberRole, Double> insMeasuresMap = new HashMap<>();
                for (Iterator<Map.Entry<String, Double>> itt = nameValMap.entrySet().iterator(); itt.hasNext(); ) {
                    Map.Entry<String, Double> entry = itt.next();
                    MultiDimensionalDomainSelector tempSelector = new MultiDimensionalDomainSelector(Arrays.asList(meaRootPart));
                    tempSelector.append(new MultiDimensionalDomainSelector.Part(null, entry.getKey()));
//                    MemberRole meRo = (MemberRole) measureDR.selectSingleEntity(tempSelector);
                    MemberRole meRo = (MemberRole) mddmStorageService.selectSingleEntity(measureDR, tempSelector);
                    insMeasuresMap.put(meRo, entry.getValue());
                }
                vceExecutableUnitList.add(new InsertMeasureUnit(cube, mrs, insMeasuresMap));
            } else if (veu instanceof ExeUpdateMeasuresUnit) {
                Map<String, Double> nameValMap = ((ExeUpdateMeasuresUnit) veu).getNameValMap();
                Map<MemberRole, Double> pre_update_measures_mp = new HashMap<>();
                for (Iterator<Map.Entry<String, Double>> itt = nameValMap.entrySet().iterator(); itt.hasNext(); ) {
                    Map.Entry<String, Double> entry = itt.next();
                    MultiDimensionalDomainSelector tempSelector = new MultiDimensionalDomainSelector(Arrays.asList(meaRootPart));
                    tempSelector.append(new MultiDimensionalDomainSelector.Part(null, entry.getKey()));
                    MemberRole meRo = (MemberRole) mddmStorageService.selectSingleEntity(measureDR, tempSelector);
                    pre_update_measures_mp.put(meRo, entry.getValue());
                }
                vceExecutableUnitList.add(new UpdateMeasureUnit(cube, mrs, pre_update_measures_mp));
            } else if (veu instanceof ExeDeleteMeasuresUnit) {
                List<String> delMeasures = ((ExeDeleteMeasuresUnit) veu).getMeasureMembers();
                List<MemberRole> pre_delete_measures_ls = new LinkedList<>();
                for (String str : delMeasures) {
                    MultiDimensionalDomainSelector tempSelector = new MultiDimensionalDomainSelector(Arrays.asList(meaRootPart));
                    tempSelector.append(new MultiDimensionalDomainSelector.Part(null, str));
                    MemberRole meRo = (MemberRole) mddmStorageService.selectSingleEntity(measureDR, tempSelector);
                    pre_delete_measures_ls.add(meRo);
                }
                vceExecutableUnitList.add(new DeleteMeasureUnit(cube, mrs, pre_delete_measures_ls));
            } else if (veu instanceof ExeLoadCubeDataUnit) {
                ExeLoadCubeDataUnit loadCU = (ExeLoadCubeDataUnit) veu;
                vceExecutableUnitList.add(new LoadCubeDataExeUnit(loadCU.getSpaceName(), loadCU.cubeName));
            }
        }

        if (vceExecutableUnitList.isEmpty())
            return "success";

        if (InsertMeasureUnit.allAreInsertMeasureUnit(vceExecutableUnitList))
            try {
                vectorComputingEngine.handleInsertMeasureUnits_VCEMNG(InsertMeasureUnit.transform(vceExecutableUnitList));
            } catch (BIException e) {
                logger.error(e.getMessage());
                return e.getMessage();
            }
        else
            vectorComputingEngine.handleExecutableUnits_VCEMNG(vceExecutableUnitList);

        return "success";
    }

    public Cube findCubeByMgId(Long cubeMgId) {
        return mddmStorageService.findCube(cubeMgId);
    }

    public List<Map> loadCubeLeafMembersInfo() {
        return mddmStorageService.loadCubeLeafMembersInfo();
    }

    public Cube findCube(String spaceName, String cubeName) {
        return mddmStorageService.findCube(findSpace(spaceName), cubeName);
    }
}
