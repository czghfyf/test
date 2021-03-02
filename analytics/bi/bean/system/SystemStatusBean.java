// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.system;

import cn.bgotech.analytics.bi.bean.CommonBean;
import cn.bgotech.analytics.bi.component.system.SystemStatus;

public class SystemStatusBean extends CommonBean {

    private SystemStatus.Value statusValue = SystemStatus.Value.UNKNOWN;

    public String getStatusValue() {
        return statusValue.name();
    }

    public void setStatusValue(String statusValue) {
        this.statusValue = SystemStatus.Value.valueOf(statusValue);
    }
}
