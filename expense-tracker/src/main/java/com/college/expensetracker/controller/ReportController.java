package com.college.expensetracker.controller;

import com.college.expensetracker.model.User;
import com.college.expensetracker.service.ReportService;
import com.college.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    @GetMapping("/monthly")
    public String monthly(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(required = false) Integer month,
                          @RequestParam(required = false) Integer year,
                          Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year != null) ? year : now.getYear();

        ReportService.MonthlyReport report = reportService.generateMonthlyReport(user, m, y);

        model.addAttribute("user", user);
        model.addAttribute("report", report);
        model.addAttribute("selectedMonth", m);
        model.addAttribute("selectedYear", y);
        model.addAttribute("availableYears", reportService.getAvailableYears(user));
        return "reports/monthly";
    }

    @GetMapping("/yearly")
    public String yearly(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(required = false) Integer year,
                         Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        int y = (year != null) ? year : LocalDate.now().getYear();

        ReportService.YearlyReport report = reportService.generateYearlyReport(user, y);

        model.addAttribute("user", user);
        model.addAttribute("report", report);
        model.addAttribute("selectedYear", y);
        model.addAttribute("availableYears", reportService.getAvailableYears(user));
        return "reports/yearly";
    }

    @GetMapping("/category")
    public String categoryReport(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(required = false) Long categoryId,
                                 Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        // Category-specific report is handled on the categories list page via redirect
        return "redirect:/categories";
    }
}
