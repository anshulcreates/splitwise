package com.college.expensetracker.controller;

import com.college.expensetracker.dto.TransactionDto;
import com.college.expensetracker.model.*;
import com.college.expensetracker.service.CategoryService;
import com.college.expensetracker.service.TransactionService;
import com.college.expensetracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "transactionDate") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        TransactionType transactionType = null;
        if (type != null && !type.isBlank()) {
            try { transactionType = TransactionType.valueOf(type.toUpperCase()); } catch (Exception ignored) {}
        }

        Page<Transaction> transactions = transactionService.getTransactionsPage(
            user, transactionType, categoryId, startDate, endDate, keyword, page, sortBy, sortDir);
        List<Category> categories = categoryService.getAllCategoriesForUser(user);

        model.addAttribute("user", user);
        model.addAttribute("transactions", transactions);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalElements", transactions.getTotalElements());
        // Pass filter values back for form persistence
        model.addAttribute("filterType", type);
        model.addAttribute("filterCategoryId", categoryId);
        model.addAttribute("filterStartDate", startDate);
        model.addAttribute("filterEndDate", endDate);
        model.addAttribute("filterKeyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        return "transactions/list";
    }

    @GetMapping("/add")
    public String addForm(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(required = false) String type,
                          Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        TransactionDto dto = new TransactionDto();
        dto.setTransactionDate(LocalDate.now());
        if (type != null) {
            try { dto.setType(TransactionType.valueOf(type.toUpperCase())); } catch (Exception ignored) {}
        }

        model.addAttribute("user", user);
        model.addAttribute("dto", dto);
        model.addAttribute("incomeCategories", categoryService.getCategoriesForUserByType(user, TransactionType.INCOME));
        model.addAttribute("expenseCategories", categoryService.getCategoriesForUserByType(user, TransactionType.EXPENSE));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("formAction", "/transactions/add");
        model.addAttribute("pageTitle", "Add Transaction");
        return "transactions/form";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails userDetails,
                      @Valid @ModelAttribute("dto") TransactionDto dto,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("incomeCategories", categoryService.getCategoriesForUserByType(user, TransactionType.INCOME));
            model.addAttribute("expenseCategories", categoryService.getCategoriesForUserByType(user, TransactionType.EXPENSE));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("formAction", "/transactions/add");
            model.addAttribute("pageTitle", "Add Transaction");
            return "transactions/form";
        }
        try {
            transactionService.addTransaction(user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction added successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/transactions";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long id, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        Transaction transaction = transactionService.getTransactionByIdAndUser(id, user);
        TransactionDto dto = transactionService.toDto(transaction);

        model.addAttribute("user", user);
        model.addAttribute("dto", dto);
        model.addAttribute("incomeCategories", categoryService.getCategoriesForUserByType(user, TransactionType.INCOME));
        model.addAttribute("expenseCategories", categoryService.getCategoriesForUserByType(user, TransactionType.EXPENSE));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("formAction", "/transactions/edit/" + id);
        model.addAttribute("pageTitle", "Edit Transaction");
        return "transactions/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long id,
                       @Valid @ModelAttribute("dto") TransactionDto dto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("incomeCategories", categoryService.getCategoriesForUserByType(user, TransactionType.INCOME));
            model.addAttribute("expenseCategories", categoryService.getCategoriesForUserByType(user, TransactionType.EXPENSE));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("formAction", "/transactions/edit/" + id);
            model.addAttribute("pageTitle", "Edit Transaction");
            return "transactions/form";
        }
        try {
            transactionService.updateTransaction(id, user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/transactions";
    }

    @PostMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        try {
            transactionService.deleteTransaction(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/transactions";
    }
}
