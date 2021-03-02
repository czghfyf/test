package cn.bgotech.analytics.bi.controller.sys.admin;

import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by czg on 2019/5/30.
 */
@Controller
public class SuperAdminController {

    @Autowired
    private SuperAdminService sas;

    @PostMapping("/SAC/createSpaceMasterUser")
    @ResponseBody
    public ResponseData createSpaceMasterUser(@RequestBody Map params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        sas.createSpaceMasterUser(username, password);
        return ResponseData.SUCCESS_INSTANCE;
    }
}
