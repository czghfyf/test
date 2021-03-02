package cn.bgotech.wormhole.olap.mddm.data;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public class BasicNumeric implements BasicData {

    private Double value;

    /**
     * @param sign  '+' or '-'
     * @param value
     */
    public BasicNumeric(String sign, double value) {
        if ("-".equals(sign)) {
            this.value = 0 - value;
        } else if ("+".equals(sign) || "".equals(sign) || sign == null) {
            this.value = value;
        } else {
            throw new OlapRuntimeException("not support sign '" + sign + "'");
        }
    }

    public BasicNumeric(Double v) {
        value = v;
    }

    public Double doubleValue() {
        return value;
    }

    @Override
    public String image() {
        return value.toString();
    }

    @Override
    public Object value() {
        return doubleValue();
    }

}
