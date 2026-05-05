package com.college.expensetracker.controller;

import com.college.expensetracker.model.Category;
import com.college.expensetracker.model.TransactionType;
import com.college.expensetracker.model.User;
import com.college.expensetracker.service.CategoryService;
import com.college.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        List<Category> all = categoryService.getUserCustomCategories(user);

        model.addAttribute("user", user);
        model.addAttribute("incomeCategories", all.stream()
            .filter(c -> c.getType() == TransactionType.INCOME).collect(Collectors.toList()));
        model.addAttribute("expenseCategories", all.stream()
            .filter(c -> c.getType() == TransactionType.EXPENSE).collect(Collectors.toList()));
        return "categories/list";
    }

    @GetMapping("/add")
    public String addForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", userService.getUserByUsername(userDetails.getUsername()));
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Add Category");
        model.addAttribute("formAction", "/categories/add");
        return "categories/form";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails userDetails,
                      @RequestParam String name,
                      @RequestParam String type,
                      @RequestParam(required = false, defaultValue = "bi-tag") String icon,
                      RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        try {
            TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
            categoryService.addCustomCategory(user, name, transactionType, icon);
            redirectAttributes.addFlashAttribute("successMessage", "Category added successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long id, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        Category category = categoryService.getCategoryByIdAndUser(id, user);

        if (category.isDefault() || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/categories";
        }

        model.addAttribute("user", user);
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", "Edit Category");
        model.addAttribute("formAction", "/categories/edit/" + id);
        return "categories/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long id,
                       @RequestParam String name,
                       @RequestParam(required = false, defaultValue = "bi-tag") String icon,
                       RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        try {
            categoryService.updateCategory(id, user, name, icon);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        try {
            categoryService.deleteCategory(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }
}
