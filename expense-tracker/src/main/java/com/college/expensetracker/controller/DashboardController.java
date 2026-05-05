package com.college.expensetracker.controller;

import com.college.expensetracker.dto.DashboardDto;
import com.college.expensetracker.model.User;
import com.college.expensetracker.service.DashboardService;
import com.college.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        DashboardDto dashboard = dashboardService.buildDashboard(user);

        model.addAttribute("user", user);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("today", LocalDate.now());
        return "dashboard";
    }
}
