package cn.bgotech.analytics.bi.controller;

import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.service.ScriptToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ScriptToolController {

    @Autowired
    private ScriptToolService sts;

    @PostMapping("/ScriptTool/exe")
    @ResponseBody
    public ResponseData exe(@RequestBody Map params) {
        Object result;
        try {
            result = sts.executionScript((String) params.get("script"));
        } catch (Exception e) {
            Map<String, Object> errInfo = new HashMap<>();
            errInfo.put("msg", e.getMessage());
            return new ResponseData().setStatus(ResponseData.Status.FAILURE).setData(errInfo);
        }
        return result == null ? ResponseData.SUCCESS_INSTANCE : new ResponseData().setData(result);
    }

}
