package cn.bgotech.analytics.bi.controller;

import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.service.MDDVCEMngService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class MDDVCEMngController {

    @Autowired
    private MDDVCEMngService mddVceService;

    @PostMapping("/MddVceMng/exeCommand")
    @ResponseBody
    public ResponseData exeCommand(@RequestBody Map params) {
        String command = params.get("command").toString();
        mddVceService.exeCommand(command);
        return ResponseData.SUCCESS_INSTANCE;
    }
}
