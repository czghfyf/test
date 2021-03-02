package cn.bgotech.analytics.bi.controller;

import cn.bgotech.analytics.bi.controller.response.ResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class SessionTimeoutController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String REDIRECT_TARGET = "login.page?redirect=sessionTimeout";

    @GetMapping("/sessionTimeout.action")
    public void sessionTimeoutProcess(HttpServletRequest request, HttpServletResponse response) {

        logger.debug("entry into ... ...");

        String redirectTarget = REDIRECT_TARGET;

        // + ((SecurityContext) se.getSession().getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication().getName());
        HttpSession session = request.getSession();
        if (session == null) {
            logger.debug("session is null");
            redirectTarget += "&session=null";
        } else {
            logger.debug("session is exist");
            Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
            if (securityContext == null) {
                logger.debug("have no user");
                redirectTarget += "&user=null";
            } else if (securityContext instanceof SecurityContext) {
                String userName = ((SecurityContext) securityContext).getAuthentication().getName();
                logger.debug("\tthis user is " + userName);
                redirectTarget += "&user=" + userName;
            } else {
                logger.debug("SPRING_SECURITY_CONTEXT is a object that it's class is " + securityContext.getClass().getName());
                redirectTarget += "&user=_o_t_h_e_r_";
            }
        }


        if ("XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))) {
            response.setHeader("session-status", "timeout");
            PrintWriter outer = null;
            try {
                outer = response.getWriter();

                logger.debug("response session timeout: "
                        + new ObjectMapper().writeValueAsString(new ResponseData().setStatus(ResponseData.Status.SESSION_TIMEOUT)));

                outer.print(new ObjectMapper().writeValueAsString(new ResponseData().setStatus(ResponseData.Status.SESSION_TIMEOUT)));
                outer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                outer.close();
            }


        } else {
            try {
                response.sendRedirect(redirectTarget);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }


    }

}
