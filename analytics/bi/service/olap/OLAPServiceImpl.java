package cn.bgotech.analytics.bi.service.olap;

import cn.bgotech.analytics.bi.bean.spa.MDQuerySchema;
import cn.bgotech.analytics.bi.bean.mddm.physical.schema.SpaceBean;
import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.spa.SinglePageAppDAO;
import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.analytics.bi.service.schedule.ScheduleTaskService;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.analytics.bi.util.ImportCubeDataUtils;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.component.MddmStorageService;
import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.MDXQueryResult;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/19.
 */
@Service
public class OLAPServiceImpl implements OLAPService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MddmStorageService mddmStorageService;

    @Autowired
    private ScheduleTaskService scheduleTaskService;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    @Qualifier("olap_engine_instance")
    private OlapEngine olapEngine;

    @Resource
    private SinglePageAppDAO spaDAO;

    @Override
    @Transactional
    public void importCube(String fileType, String cubeName, byte[] fileBytes) throws BIException {

        logger.info("$>>>>>>>>>>>> import cube data: fileType = " + fileType + ", cubeName = " + cubeName + ", " + fileBytes.length + " bytes");

        User currentUser = ThreadLocalTool.getCurrentUser();
        Space space = mddmStorageService.findSpaceByName(currentUser.getName());
        if (space == null) {
            space = (SpaceBean) beanFactory.create(SpaceBean.class);
            ((SpaceBean) space).setName(currentUser.getName());
            mddmStorageService.save(space);
        } else if (mddmStorageService.findCube(space, cubeName) != null) {
            throw new BIRuntimeException("cube[" + cubeName + "] in space[" + space.getName() + "] already exist");
        }

        Space finalSpace = space;
        scheduleTaskService.executeBranchTask("导入多维数据集: " + cubeName, "导入多维数据集: ... " + cubeName, () -> {
            if ("text".equals(fileType)) {
                try {
                    olapEngine.importCube(finalSpace, cubeName, fileBytes);
                } catch (OlapException e) {
                    logger.error(e.getMessage());
                    throw new BIRuntimeException(e);
                }
            } else if ("excel".equals(fileType)) {
                try {
                    olapEngine.importCube(finalSpace, cubeName, ImportCubeDataUtils.excel2text(fileBytes));
                } catch (OlapException | BIException e) {
                    logger.error(e.getMessage());
                    throw new BIRuntimeException(e);
                }
            } else {
                throw new BIRuntimeException("not support " + fileType + " file type.");
            }
        });

    }

    @Override
    public void importMeasure(byte[] fileBytes) {

        ByteArrayInputStream is = new ByteArrayInputStream(fileBytes);
        XSSFWorkbook workbook;
        try {
            workbook = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        XSSFSheet sheet = workbook.getSheetAt(0);

        String spaceName = sheet.getRow(0).getCell(1).getStringCellValue();
        String cubeName = sheet.getRow(1).getCell(1).getStringCellValue();

        XSSFRow row;
        XSSFCell grid;
        String cellStr;
        StringBuilder plainText = new StringBuilder();

        int r = 2, c;
        while ((row = sheet.getRow(r++)) != null) {
            plainText.append("insert [" + spaceName + "]@[" + cubeName + "] (");
            c = 0;
            while ((grid = row.getCell(c++)) != null) {
                cellStr = grid.getStringCellValue();
                if (cellStr.startsWith("{度量}"))
                    break;
                plainText.append((c > 1 ? ", " : "") + cellStr);
            }
            plainText.append(") measures (");
            grid = row.getCell(--c);
            while (grid != null) {
                plainText.append("[" + grid.getStringCellValue().substring(4) + "] = ");
                grid = row.getCell(c + 1);
                plainText.append(grid.getNumericCellValue());
                c += 2;
                grid = row.getCell(c);
                if (grid != null)
                    plainText.append(", ");
            }
            plainText.append(");\n");
        }

        OlapEngine.hold().handleScript(plainText.toString());

    }

    @Override
    public List<Cube> findCubes() {
        Space space = getCurrentSpace();
        return space != null ? getCurrentSpace().cubes() : new ArrayList<>();
    }

    private Space getCurrentSpace() {
        User user = ThreadLocalTool.getCurrentUser();
        if (user == null) {
            throw new BIRuntimeException("current user is not exist");
        }
        return mddmStorageService.findSpaceByName(user.getSpaceName());
    }

    @Override
    public List<DimensionRole> cubeDimensionRoles(long cubeMgId) {
        Cube cube = olapEngine.find(Cube.class, cubeMgId);
        return cube.getDimensionRoles(null);
    }

    @Override
    public Cube findCube(Long cubeMgId) {
        return olapEngine.findCubeByMgId(cubeMgId);
    }


    @Override
    public MDXQueryResult queryMDX(String mdx) {
        return (MDXQueryResult) olapEngine
                .execute(olapEngine.findSpace(ThreadLocalTool.getCurrentUser().getSpaceName()), mdx);
    }

    @Override
    public MDXQueryResult query2table(String withStr, String rowStr, String colStr, String whereStr, long cubeId) {

        Cube cube = olapEngine.findCubeByMgId(cubeId);

        StringBuilder mdx = new StringBuilder();

        mdx.append(withStr).append(" select ").append(colStr).append(" on columns, ").append(rowStr)
                .append(" on rows from &").append(cube.getMgId()).append("[").append(cube.getName()).append("]");

        if (whereStr != null && !whereStr.trim().isEmpty()) {
            mdx.append(" where ").append(whereStr);
        }

        return queryMDX(mdx.toString());
    }

    @Override
    public MDQuerySchema saveMDQuerySchema(MDQuerySchema qs) {
        if (qs.getId() == null) {
            qs.setId(((MDQuerySchema) beanFactory.create(MDQuerySchema.class)).getId());
            new Thread(() -> spaDAO.saveMDQuerySchema(qs)).start();
        } else {
            new Thread(() -> spaDAO.updateMDQuerySchema(qs)).start();
        }
        return qs;
    }

    @Override
    public List<MDQuerySchema> findMDQuerySchemas() {
        return spaDAO.findMDQuerySchemas(getCurrentSpace());
    }

    @Override
    public List<Dimension> findDimensions() {
        return mddmStorageService.findAll(Dimension.class);
    }

}
