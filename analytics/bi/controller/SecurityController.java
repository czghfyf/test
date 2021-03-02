package cn.bgotech.analytics.bi.controller;

import cn.bgotech.analytics.bi.bean.security.Role;
import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.service.security.SecurityService;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class SecurityController {

    @Autowired
    private SecurityService securityService;

    @PostMapping("/se_ctrl/createAffiliatedUser")
    @ResponseBody
    public ResponseData createAffiliatedUser(@RequestBody Map params) {
        String affiliated_username = (String) params.get("affiliated_username");
        String affiliated_password = (String) params.get("affiliated_password");
        User masterUser = ThreadLocalTool.getCurrentUser();
        String spaceName = masterUser.getSpaceName();
        securityService.createAffiliatedUser(affiliated_username, affiliated_password, spaceName,
                Role.Type.SPACE_USER.name());
        return ResponseData.SUCCESS_INSTANCE;
    }
}
