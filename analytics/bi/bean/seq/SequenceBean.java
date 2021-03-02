// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.seq;

import cn.bgotech.analytics.bi.bean.Bean;
//import cn.bgotech.analytics.bi.bean.CommonBean;

public class SequenceBean extends Bean {

    private String name;
    private Long currentValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //    public String getSequenceName() {
//        return sequenceName;
//    }

//    public void setSequenceName(String sequenceName) {
//        this.sequenceName = sequenceName;
//    }

    public Long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public String toString() {
        return "SequenceBean{" +
                "name='" + name + '\'' +
                ", currentValue=" + currentValue +
                "} extends " + super.toString();
    }
}
