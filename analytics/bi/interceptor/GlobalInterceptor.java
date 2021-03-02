// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.interceptor;

import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.dao.security.SecurityDao;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class GlobalInterceptor extends HandlerInterceptorAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("olap_engine_instance")
    private OlapEngine olapEngine;

    @Autowired
    private SecurityDao securityDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        logger.debug("enter");

        Object ssCtx = request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        if (ssCtx != null && ssCtx instanceof SecurityContext) {
            SecurityContext securityCtx = (SecurityContext) ssCtx;
            String userName = securityCtx.getAuthentication().getName();
            User currentUser = ThreadLocalTool.getCurrentUser();
            if (currentUser == null || !currentUser.getName().equalsIgnoreCase(userName)) {
                currentUser = securityDao.loadUserByName(userName);
                currentUser.setPassword(null);
                ThreadLocalTool.setCurrentUser(currentUser);
                logger.debug("set current thread user: " + currentUser);
            }
        }
//        else {
//            throw new Exception("attribute 'SPRING_SECURITY_CONTEXT' " + (ssCtx == null ? "is null" : "not instanceof SecurityContext"));
//        }

        findAndSetCurrentThreadSpace();

//		// logger.debug("accountName = " + accountName);
//		Object credentials = sc.getAuthentication().getCredentials();
//		logger.warn("credentials = " + credentials);
//		WebAuthenticationDetails details = (WebAuthenticationDetails) sc.getAuthentication().getDetails();
//		String remoteAddress = details.getRemoteAddress();
//		logger.error("remoteAddress = " + remoteAddress);
//		String sessionId = details.getSessionId();
//		// logger.info("sessionId = " + sessionId);
//		List<GrantedAuthority> authorities = (List<GrantedAuthority>) sc.getAuthentication().getAuthorities();
//		for (GrantedAuthority ga : authorities) {
//			// logger.info("\tauthority = " + ga.getAuthority());
//		}

        logger.debug("before return");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // TODO
//        super.postHandle(request, response, handler, modelAndView);
        logger.debug("do something later");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // TODO
//        super.afterCompletion(request, response, handler, ex);
        logger.debug("do something later");
    }

    private void findAndSetCurrentThreadSpace() {
        User u = ThreadLocalTool.getCurrentUser();
        if (u != null) {
            Space s = olapEngine.findSpace(u.getSpaceName());
            OlapEngine.setCurrentThreadSpace(s);
        }
    }
}
