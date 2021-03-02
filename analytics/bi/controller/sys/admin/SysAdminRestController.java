package cn.bgotech.analytics.bi.controller.sys.admin;

import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.dto.DTOUtils;
import cn.bgotech.analytics.bi.service.olap.OLAPService;
import cn.bgotech.analytics.bi.service.security.SecurityService;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by ChenZhiGang on 2017/11/30.
 * @deprecated 方法移到 SuperAdminController 类
 */
@Deprecated
@RestController
@RequestMapping("/sys_admin_rest")
public class SysAdminRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SecurityService securityService;

    @Autowired
    private OLAPService olapService;

    @PostMapping("/ecl")
    public ResponseData executeCommandLines(@RequestBody Map params) {
        
        String commandLines = params.get("commandLines").toString();

        if (commandLines.startsWith("--MDX:")) {
            return executeMDX(commandLines);
        }

        String[] commands = commandLines.split("\\s+");

        if ("user".equalsIgnoreCase(commands[0])) {
            /**
             * "user + 'user name' 'password' 'role name'"
             */
            if (commands.length != 5) {
                return new ResponseData().setStatus(ResponseData.Status.FAILURE)
                        .setData("Incomplete command: " + commandLines);
            }
            if ("+".equals(commands[1])) {
                try {
                    securityService.createUser(commands[2], commands[3], commands[4]);
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                    return new ResponseData().setStatus(ResponseData.Status.FAILURE).setData(e.getMessage());
                }
            } else {
                return new ResponseData().setStatus(ResponseData.Status.FAILURE)
                        .setData("Invalid command: " + commandLines);
            }
        } else {
            return new ResponseData().setStatus(ResponseData.Status.FAILURE)
                    .setData("Invalid command: " + commandLines);
        }

        return new ResponseData();

    }

    private ResponseData executeMDX(String mdx) {

//        MDXQueryResult queryResult = (MDXQueryResult) olapEngine
//                .execute(olapEngine.findSpace(ThreadLocalTool.getCurrentUser().getName()), mdx);

        Map<String, Object> result = new HashedMap();
        try {
            result.put("_2D_PIVOT_GRID", DTOUtils.convertTo2DPivotGrid(olapService.queryMDX(mdx)));
            return new ResponseData().setData(result);
        } catch (RuntimeException re) {
            logger.error(re.getMessage(), re);
            return new ResponseData().setStatus(ResponseData.Status.FAILURE).setData(re.getMessage());
        }

    }

}
