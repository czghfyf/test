package cn.bgotech.analytics.bi.controller.sys;

import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.service.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Deprecated
@Controller
@RequestMapping("/sys_authority")
public class AuthorityController {

    @Autowired
    private SecurityService securityService;

    @GetMapping("/allUsers")
    @ResponseBody
    public List<User> loadAllUsers() {
        List<User> users = securityService.loadAllUsers();
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @GetMapping("/reset_account_password")
    @ResponseBody
    public String resetAccountPassword(@RequestParam("account") String account,
                                       @RequestParam("newPassword") String newPassword) {
        securityService.resetPassword(account, newPassword);
        return "success";
    }
}
