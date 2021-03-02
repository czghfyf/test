package cn.bgotech.analytics.bi.service.schedule;

import cn.bgotech.analytics.bi.bean.schedule.Task;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/9/19.
 */
public interface ScheduleTaskService {

    void executeBranchTask(String taskName, String taskDesc, Runnable fn);

    List<Task> findTasks();

}
