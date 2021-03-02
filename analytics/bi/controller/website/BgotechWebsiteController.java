package cn.bgotech.analytics.bi.controller.website;

import cn.bgotech.analytics.bi.bean.security.Role;
import cn.bgotech.analytics.bi.service.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by czg on 2018/9/23.
 * todo 接收官网新注册用户信息的临时性接口，将来会删除
 */
@RestController
@RequestMapping("/fromBgotechWebsiteAction_OIJUHB0908")
public class BgotechWebsiteController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SecurityService securityService;

    @GetMapping("/createGuestUser_HNDRFJ985430ijnxzl")
    public String createGuestUser(HttpServletRequest request,
                                  @RequestParam("username") String username,
                                  @RequestParam("password") String password) {

        logger.debug("create a new user from www.bgotech.cn: [ " + username + "  <<<<>>>>  " + password + " ]");

        String remoteAddr = request.getRemoteAddr();
        if (!"47.93.7.86".equals(remoteAddr)) {
            logger.warn("remote address is " + remoteAddr);
            return "fail";
        }
        logger.debug("create a new user from www.bgotech.cn: [ " + username + "  <<<<>>>>  " + password + " ] ... doing ...");
        securityService.createUser(username, password, Role.Type.SPACE_USER.name());
        logger.debug("create a new user from www.bgotech.cn: [ " + username + "  <<<<>>>>  " + password + " ] ... success");
        return "success";
    }

    @GetMapping("/resetAccPSWD_HNDuhdji8685430ij98H")
    public String resetAccountPassword(HttpServletRequest request,
                                  @RequestParam("username") String username,
                                  @RequestParam("newPassword") String newPassword) {

        logger.debug("resetAccountPassword from www.bgotech.cn: [ " + username + "  <<<<>>>>  " + newPassword + " ]");

        String remoteAddr = request.getRemoteAddr();
        if (!"47.93.7.86".equals(remoteAddr)) {
            logger.warn("remote address is " + remoteAddr);
            return "fail";
        }
        logger.debug("resetAccountPassword from www.bgotech.cn: [ " + username + "  <<<<>>>>  " + newPassword + " ] ... doing ...");
        securityService.resetPassword(username, newPassword);
        logger.debug("resetAccountPassword from www.bgotech.cn: [ " + username + "  <<<<>>>>  " + newPassword + " ] ... success");
        return "success";
    }
}
