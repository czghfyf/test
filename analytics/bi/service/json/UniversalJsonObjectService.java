package cn.bgotech.analytics.bi.service.json;

import cn.bgotech.analytics.bi.bean.spa.UIJsonObject;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/9/4.
 */
public interface UniversalJsonObjectService {

    UIJsonObject save(UIJsonObject jsonObj);

    List<UIJsonObject> queryByType(String type);

    void del(String type, List<Long> idList);
}
