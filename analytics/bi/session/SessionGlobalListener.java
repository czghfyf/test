package cn.bgotech.analytics.bi.session;

//import cn.bgotech.analytics.bi.bean.security.User;
//import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

//import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Enumeration;

/**
 * Created by ChenZhiGang on 2017/7/20.
 */
public class SessionGlobalListener implements HttpSessionListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void sessionCreated(HttpSessionEvent se) {

        logger.debug("SPRING_SECURITY_CONTEXT is " + se.getSession().getAttribute("SPRING_SECURITY_CONTEXT"));

//        logger.debug("session created ........ user name is "
//            + ((SecurityContext) se.getSession().getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication().getName());

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        logger.debug("session destroyed ........ user name is "
                + ((SecurityContext) se.getSession().getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication().getName());

        Enumeration<String> e = se.getSession().getAttributeNames();
        while (e.hasMoreElements()) {
            logger.debug("\t\t>>>>>>>>>>>>>>>>>>> " + e.nextElement());
        }

    }

}
