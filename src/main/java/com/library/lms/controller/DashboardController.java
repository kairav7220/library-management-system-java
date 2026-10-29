package com.library.lms.controller;

import com.library.lms.service.LogService;
import com.library.lms.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final StatsService statsService;
    private final LogService logService;

    public DashboardController(StatsService statsService, LogService logService) {
        this.statsService = statsService;
        this.logService = logService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("totalBooks", statsService.totalBooks());
        model.addAttribute("totalMembers", statsService.totalMembers());
        model.addAttribute("issuedCount", statsService.issuedCount());
        model.addAttribute("overdueCount", statsService.overdueCount());
        model.addAttribute("revenue", statsService.revenue());
        model.addAttribute("recentLogs", logService.list().stream().limit(8).toList());
        return "dashboard";
    }
}
