// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean;

/**
 * Created by ChenZhiGang on 2017/5/17.
 */
public abstract class CommonBean extends Bean {

    private Long id;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
