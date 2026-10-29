package com.library.lms.controller;

import com.library.lms.service.LogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogsController {

    private final LogService logService;

    public LogsController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("logs", logService.list());
        return "logs";
    }
}
