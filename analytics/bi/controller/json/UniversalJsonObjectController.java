package cn.bgotech.analytics.bi.controller.json;

import cn.bgotech.analytics.bi.bean.spa.UIJsonObject;
import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.service.json.UniversalJsonObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/universal/json")
public class UniversalJsonObjectController {

    @Autowired
    private UniversalJsonObjectService service;

    @PostMapping("/save")
    public ResponseData save(@RequestBody UIJsonObject jsonObj) {
        return new ResponseData().setData(service.save(jsonObj));
    }

    @GetMapping("/query")
    public ResponseData query(@RequestParam("type") String type) {
        return new ResponseData().setData(service.queryByType(type));
    }

    @GetMapping("/del")
    public ResponseData del(@RequestParam("type") String type, @RequestParam("idArr") String idArr) {
        List<Long> idList = new LinkedList<>();
        for (String idStr : idArr.split(","))
            idList.add(Long.parseLong(idStr));
        service.del(type, idList);
        return new ResponseData().setData("success");
    }
}
