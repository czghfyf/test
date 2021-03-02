package cn.bgotech.analytics.bi.system;

import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.wormhole.olap.OlapEngine;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public final class ThreadLocalTool {

    private static final ThreadLocal<User> THREAD_CURRENT_USER = new ThreadLocal<>();

    private static final ThreadLocal<List<VCEWorkConnector.NodeInfo>> THREAD_CURRENT_VCE_NODES_INFO = new ThreadLocal();

    private static final ThreadLocal<List<VCEWorkConnector>> THREAD_CURRENT_VCE_NODE_CONNS = new ThreadLocal();

    private ThreadLocalTool() {
    }

    public static User getCurrentUser() {
        User u = THREAD_CURRENT_USER.get();
        return u;
    }

    public static void setCurrentUser(User u) {

        User secureUserInfo;

        if (u.getPassword() == null) {
            secureUserInfo = u;
        } else {
            secureUserInfo = new User();
            try {
                BeanUtils.copyProperties(secureUserInfo, u);
                secureUserInfo.setPassword(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        THREAD_CURRENT_USER.set(secureUserInfo);
    }

    public static String threadInfo(Thread thread) {
        if (thread == null) {
            thread = Thread.currentThread();
        }
        return "identityHashCode = [" + System.identityHashCode(thread) + "], " +
                "hashCode = [ " + thread.hashCode() + " ], toString = [ " + thread + " ]";
    }

    public static String getCurrentThreadSpaceName() {
        return getCurrentUser() != null ? getCurrentUser().getName() : OlapEngine.getCurrentThreadSpaceName();
    }

    public static void setCurrentReadyConnectVCENodes(List<VCEWorkConnector.NodeInfo> info) {
        THREAD_CURRENT_VCE_NODES_INFO.set(info);
    }

    public static List<VCEWorkConnector.NodeInfo> getCurrentReadyConnectVCENodes() {
        return THREAD_CURRENT_VCE_NODES_INFO.get();
    }

    public static void setCurrentVCENodeConnectors(List<VCEWorkConnector> cl) {
        THREAD_CURRENT_VCE_NODE_CONNS.set(cl);
    }

    public static List<VCEWorkConnector> getCurrentVCENodeConnectors() {
        return THREAD_CURRENT_VCE_NODE_CONNS.get();
    }
}
