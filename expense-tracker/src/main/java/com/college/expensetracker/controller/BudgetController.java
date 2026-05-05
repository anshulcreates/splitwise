package com.college.expensetracker.controller;

import com.college.expensetracker.model.User;
import com.college.expensetracker.service.BudgetService;
import com.college.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(required = false) Integer month,
                       @RequestParam(required = false) Integer year,
                       Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year != null) ? year : now.getYear();

        model.addAttribute("user", user);
        model.addAttribute("budgets", budgetService.getBudgetStatusForMonth(user, m, y));
        model.addAttribute("expenseCategories", budgetService.getExpenseCategoriesForUser(user));
        model.addAttribute("selectedMonth", m);
        model.addAttribute("selectedYear", y);
        model.addAttribute("currentYear", now.getYear());
        return "budgets/list";
    }

    @PostMapping("/save")
    public String saveBudget(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam Long categoryId,
                             @RequestParam BigDecimal amount,
                             @RequestParam int month,
                             @RequestParam int year,
                             RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        try {
            budgetService.saveBudget(user, categoryId, amount, month, year);
            redirectAttributes.addFlashAttribute("successMessage", "Budget saved successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/budgets?month=" + month + "&year=" + year;
    }

    @PostMapping("/delete/{id}")
    public String deleteBudget(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id,
                               @RequestParam(required = false) Integer month,
                               @RequestParam(required = false) Integer year,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        try {
            budgetService.deleteBudget(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Budget removed.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        String params = (month != null && year != null) ? "?month=" + month + "&year=" + year : "";
        return "redirect:/budgets" + params;
    }
}
