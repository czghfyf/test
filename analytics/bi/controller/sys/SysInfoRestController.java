package cn.bgotech.analytics.bi.controller.sys;

import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.dto.security.UserDTO;
import cn.bgotech.analytics.bi.service.security.SecurityService;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rest/sys")
public class SysInfoRestController {

    @Autowired
    private SecurityService securityService;

    @GetMapping("/currentUser")
    public ResponseData currentUser() {
        return new ResponseData().setData(new UserDTO(ThreadLocalTool.getCurrentUser()));
    }

    @GetMapping("/currentSpaceUsers")
    public ResponseData currentSpaceUsers() {
        List<UserDTO> res = new ArrayList<>();
        for (User u : securityService.loadAllUsers())
            if (ThreadLocalTool.getCurrentUser().getSpaceName().equals(u.getSpaceName()))
                res.add(new UserDTO(u));
        return new ResponseData().setData(res);
    }

    @GetMapping("/isAdminUser")
    @ResponseBody
    public String isAdminUser() {
        return securityService.isAdminUser(ThreadLocalTool.getCurrentUser()) ? "Y" : "N";
    }
}
