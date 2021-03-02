package cn.bgotech.analytics.bi.service.json;

import cn.bgotech.analytics.bi.bean.spa.UIJsonObject;
import cn.bgotech.analytics.bi.component.bean.BeanFactory;
import cn.bgotech.analytics.bi.dao.json.UniversalJsonObjectDAO;
import cn.bgotech.analytics.bi.system.ThreadLocalTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ChenZhiGang on 2017/9/4.
 */
@Service
public class UniversalJsonObjectServiceImpl implements UniversalJsonObjectService {

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private UniversalJsonObjectDAO uJsonObjDAO;

    @Resource
    private UniversalJsonObjectDAO dao;

    @Override
    public UIJsonObject save(UIJsonObject jsonObj) {
        if (jsonObj.getId() == null) { // save new
            jsonObj.setId(((UIJsonObject) beanFactory.create(UIJsonObject.class)).getId());
            uJsonObjDAO.save(jsonObj);
        } else { // update
            uJsonObjDAO.update(jsonObj);
        }
        return jsonObj;
    }

    @Override
    public List<UIJsonObject> queryByType(String type) {
        long currentUserId = ThreadLocalTool.getCurrentUser().getId();
        return uJsonObjDAO.queryByType(type).stream()
                .filter(obj -> obj.getCreatorId() == currentUserId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void del(String type, List<Long> idList) {
        dao.del(type, idList);
    }

}
