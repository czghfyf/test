package cn.bgotech.analytics.bi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/login.page")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/accessDenied.page")
    public String accessDeniedPage() {
        return "accessDenied";
    }

    @GetMapping("/workbench.page")
    public String workbench() {
        return "workbench";
    }

    @GetMapping("/uploadCubeDataFile.page")
    public String importCubeData(@RequestParam("fileType") String fileType, Model model) {
        model.addAttribute("fileType", fileType);
        return "uploadCubeDataFile";
    }

    @GetMapping("/importMeasureFile.page")
    public String importMeasureFile() {
        return "importMeasureFile";
    }

    @GetMapping("/x-nimda-repus-x.page")
    public String superAdmin() {
        return "_SUPER_ADMIN_";
    }

    @GetMapping("/_admin_sys_mng_")
    public String _admin_sys_mng_() {
        return "_admin_sys_mng_";
    }
}
