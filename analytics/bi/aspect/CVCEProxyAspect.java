package cn.bgotech.analytics.bi.aspect;

import cn.bgotech.analytics.bi.component.olap.vce.VCEWorkConnector;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Aspect
@Component
public class CVCEProxyAspect {

    @Pointcut("execution(public * cn.bgotech.analytics.bi.component.olap.CVCEProxy.*_VCEMNG(..))")
    public void pointcutExpression() {

    }

    @Before("pointcutExpression()")
    public void beforeMethod(JoinPoint joinPoint) {
        List<VCEWorkConnector.NodeInfo> nis = ThreadLocalTool.getCurrentReadyConnectVCENodes();
        if (nis == null || nis.isEmpty())
            return;
        List<VCEWorkConnector> cl = new LinkedList<>();
        for (VCEWorkConnector.NodeInfo ni : nis) {
            cl.add(VCEWorkConnector.openPoolIns(ni.getHost(), ni.getPort()));
        }
        ThreadLocalTool.setCurrentVCENodeConnectors(cl);
    }

    @After("pointcutExpression()")
    public void afterMethod(JoinPoint joinPoint) {
        List<VCEWorkConnector> cl = ThreadLocalTool.getCurrentVCENodeConnectors();
        if (cl == null || cl.isEmpty())
            return;
        for (VCEWorkConnector conn : cl)
            VCEWorkConnector.closePoolIns(conn);
    }

//    @AfterReturning(value = "pointcutExpression()", returning = "result")
//    public void afterReturnMethod(JoinPoint joinPoint, Object result) {
//    }

//    @AfterThrowing(value = "pointcutExpression()", throwing = "ex")
//    public void afterThrowingMethod(JoinPoint joinPoint, Exception ex) {
//    }

}
