package cn.bgotech.analytics.bi.controller.schedule;

import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.service.schedule.ScheduleTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/schedule")
public class ScheduleRestController {

    @Autowired
    private ScheduleTaskService service;

    @GetMapping("/tasks")
    public ResponseData tasks() {
        return new ResponseData().setData(service.findTasks());
    }

}
