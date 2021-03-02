// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm;

import cn.bgotech.analytics.bi.bean.Bean;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.wormhole.olap.mddm.data.BasicBoolean;
import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mddm.data.BasicNumeric;
import cn.bgotech.wormhole.olap.mddm.data.BasicString;

/**
 * Created by ChenZhiGang on 2018/2/19.
 */
public class MddmAttribute extends Bean {

    private Long mddmGlobalId; // multi-dimensional object's MG_ID

    private String attributeKey;

    private Integer attributeType; // 1: str, 2: num, 3: boolean, 4: MDDM Object, other: invalid

    private String attributeValue;

    public Long getMddmGlobalId() {
        return mddmGlobalId;
    }

    public void setMddmGlobalId(Long mddmGlobalId) {
        this.mddmGlobalId = mddmGlobalId;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }

    public Integer getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(Integer attributeType) {
        this.attributeType = attributeType;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public BasicData convertedToBasicData() {
        switch (attributeType) {
            case 1:
                return new BasicString(attributeValue);
            case 2:
                return new BasicNumeric(Double.parseDouble(attributeValue));
            case 3:
                return "true".equalsIgnoreCase(attributeValue) ? BasicBoolean.TRUE : BasicBoolean.FALSE;
            case 4:
                // TODO: not support multi-dimensional object attribute
                throw new BIRuntimeException("not support multi-dimensional object attribute");
            default:
                throw new BIRuntimeException("attribute type error");
        }
    }
}
