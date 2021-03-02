package cn.bgotech.analytics.bi.service.schedule;

/**
 * Created by ChenZhiGang on 2017/9/19.
 */
public class Schedule {

    private String name;

    private String desc;

    private Runnable runnable;

    public Schedule(String name, String desc, Runnable runnable) {
        this.name = name;
        this.desc = desc;
        this.runnable = runnable;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
