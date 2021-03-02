// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.security.authentication;

import cn.bgotech.analytics.bi.component.olap.MddmStorageServiceImpl;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.wormhole.olap.OlapEngine;

import cn.bgotech.wormhole.olap.component.MddmStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by ChenZhiGang on 2017/7/17.
 */
public class UserLoginSuccessHandler implements AuthenticationSuccessHandler {

//    @Autowired
//    private SecurityStore securityStore; // Impossible to get securityStore, securityStore is null

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String defaultTargetUrl;

//    public void setSecurityStore(SecurityStore securityStore) {
//        this.securityStore = securityStore;
//    }

    public void setDefaultTargetUrl(String defaultTargetUrl) {
        this.defaultTargetUrl = defaultTargetUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        logger.debug("authentication success ... ... user is " + authentication.getName());

        MddmStorageService service = OlapEngine.hold().getMddmStorageService();

        if (service == null) {
            throw new BIRuntimeException("###  the multi-dimensional storage service is null !!!  ###");
        }

        if (!(service instanceof MddmStorageServiceImpl)) {
            throw new BIRuntimeException("### not supporting the service category " + service.getClass().getName() + " !!!  ###");
        }

        MddmStorageServiceImpl service_ = (MddmStorageServiceImpl) service;

        if (ThreadLocalTool.getCurrentUser() == null) {
            logger.debug("thread current user is null, now loading user by name '" + authentication.getName() + "'");
        }

//        // After successful login, put the current user inside to the current thread
//        User user = securityStore.loadUserByName(authentication.getName()); // Impossible to get securityStore, securityStore is null
//        user.setPassword(null);
//        ThreadLocalTool.setCurrentUser(user);

        service_.spaceCacheInit(authentication.getName());

        response.sendRedirect(defaultTargetUrl);

    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }

}
