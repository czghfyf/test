package cn.bgotech.analytics.bi.service.schedule;

import cn.bgotech.analytics.bi.bean.schedule.Task;
import cn.bgotech.analytics.bi.bean.security.User;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.schedule.TaskDAO;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ChenZhiGang on 2017/9/19.
 */
@Service
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private TaskDAO taskDAO;

    @Autowired
    @Qualifier("transactionManager")
    private DataSourceTransactionManager transactionManager;

    @Override
    public void executeBranchTask(String taskName, String taskDesc, Runnable fn) {

        User user = ThreadLocalTool.getCurrentUser();

        new Thread(() -> {

            ThreadLocalTool.setCurrentUser(user);

            Task task = (Task) beanFactory.create(Task.class);
            task.setName(taskName);
            task.setDescription(taskDesc);
            task.setStartTime(new Date());
            task.changeState(Task.Status.EXECUTING);
            taskDAO.insert(task);

            boolean transactionalSuccess = false;

            DefaultTransactionDefinition transactionDef = new DefaultTransactionDefinition();
            transactionDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDef);
            try {
                fn.run();
                transactionManager.commit(transactionStatus);
                transactionalSuccess = true;
            } catch (RuntimeException e) {
                transactionManager.rollback(transactionStatus);
                logger.error("transactional rollback cause by: " + e.getMessage(), e);
            }

            if (transactionalSuccess) {
                task.changeState(Task.Status.SUCCESS);
                task.setEndTime(new Date());
            } else {
                logger.warn("[task: " + task + "] failed");
                task.changeState(Task.Status.FAILED);
            }

            taskDAO.update(task);

        }).start();

    }

    @Override
    public List<Task> findTasks() {
        return taskDAO.queryAll().stream()
                .filter(t -> t.getCreatorId().equals(ThreadLocalTool.getCurrentUser().getId()))
                .collect(Collectors.toList()); // TODO: 请搞清 collect 方法和 Collectors 类！！！
    }

}
