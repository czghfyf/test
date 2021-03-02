package cn.bgotech.analytics.bi.component;

/**
 * Created by ChenZhiGang on 2017/6/1.
 */
public class BaseComponent { // TODO: what is this ?
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    package cn.bgotech.prophet.bi.component;
    //
    //            import cn.bgotech.prophet.bi.exception.BIRuntimeException;
    //            import org.springframework.beans.BeansException;
    //            import org.springframework.context.ApplicationContext;
    //            import org.springframework.context.ApplicationContextAware;
    //
    //    /**
    //     * Created by ChenZhiGang on 2017/5/6.
    //     */
    //    public abstract class BaseComponent implements ApplicationContextAware {
    //
    //        private boolean complete = false;
    //
    //        private ApplicationContext applicationContext;
    //
    //        @Override
    //        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    //            if (complete) {
    //                throw new BIRuntimeException("applicationContext is already set into");
    //            }
    //            if (applicationContext == null) {
    //                throw new BIRuntimeException("applicationContext must be not null");
    //            }
    //            this.applicationContext = applicationContext;
    //            complete = true;
    //        }
    //
    //        public ApplicationContext getSpringAppContext() {
    //            return applicationContext;
    //        }
    //    }

// ?????????????????????????????????????????????????????????????????????????