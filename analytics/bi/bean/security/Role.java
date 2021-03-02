// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.security;

import cn.bgotech.analytics.bi.bean.CommonBean;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class Role extends CommonBean {

    public enum Type {
        CHEP_MAIN_BRAIN, // role of super administrator
        SPACE_USER,
        // SPACE_MANAGER,
    }

}
