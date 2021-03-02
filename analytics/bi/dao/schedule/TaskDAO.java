// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.schedule;

import cn.bgotech.analytics.bi.bean.schedule.Task;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/9/19.
 */
public interface TaskDAO {

    void insert(Task task);

    void update(Task task);

    List<Task> queryAll();
}
