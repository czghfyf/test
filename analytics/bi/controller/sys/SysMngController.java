package cn.bgotech.analytics.bi.controller.sys;

import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.wormhole.olap.OlapEngine;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Deprecated
@Controller
@RequestMapping("/sys_mng_controller")
public class SysMngController {

    @PostMapping("/exe_script")
    @ResponseBody
    public ResponseData exeScript(@RequestBody Map params) {
        String result = OlapEngine.hold().handleScript(params.get("script_str").toString());
        if ("success".equals(result)) {
            return new ResponseData();
        } else{
            Map<String, String> msg = new HashMap<>();
            msg.put("msg", result);
            return new ResponseData().setStatus(ResponseData.Status.FAILURE).setData(msg);
        }
    }
}
