// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.json;

import cn.bgotech.analytics.bi.bean.spa.UIJsonObject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by czg on 2017/9/4.
 */
public interface UniversalJsonObjectDAO {

    void save(UIJsonObject jsonObj);

    void update(UIJsonObject jsonObj);

    List<UIJsonObject> queryByType(String type);

    void del(@Param("type") String type, @Param("idList") List<Long> idList);
}
