package cn.bgotech.wormhole.olap.mddm.data;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public class BasicString implements BasicData {

    private String value;

    public BasicString(String strValue) {
        value = strValue;
    }

    @Override
    public String image() {
        return value;
    }

    @Override
    public Object value() {
        return image();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicString that = (BasicString) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
