// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.security.logout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by ChenZhiGang on 2017/7/19.
 */
public class UserLogoutSuccessHandler implements LogoutSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String loginPage;

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        logger.debug("logout success ... ... user is " + authentication.getName());

        response.sendRedirect(loginPage);

    }

}
