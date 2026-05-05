package com.college.expensetracker.service;

import com.college.expensetracker.exception.ResourceNotFoundException;
import com.college.expensetracker.model.Budget;
import com.college.expensetracker.model.Category;
import com.college.expensetracker.model.TransactionType;
import com.college.expensetracker.model.User;
import com.college.expensetracker.repository.BudgetRepository;
import com.college.expensetracker.repository.TransactionRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    @Data
    @Builder
    public static class BudgetStatus {
        private Budget budget;
        private BigDecimal spent;
        private BigDecimal remaining;
        private double percentage;
        private String statusColor; // success, warning, danger
    }

    @Transactional(readOnly = true)
    public List<BudgetStatus> getBudgetStatusForMonth(User user, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYearOrderByCategoryName(user, month, year);

        return budgets.stream().map(budget -> {
            BigDecimal spent = transactionRepository.sumByUserAndCategoryAndMonthYear(
                user, budget.getCategory(), month, year);
            BigDecimal remaining = budget.getAmount().subtract(spent);
            double pct = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;
            String color = pct >= 100 ? "danger" : pct >= 70 ? "warning" : "success";

            return BudgetStatus.builder()
                .budget(budget)
                .spent(spent)
                .remaining(remaining)
                .percentage(Math.min(pct, 100))
                .statusColor(color)
                .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public Budget saveBudget(User user, Long categoryId, BigDecimal amount, int month, int year) {
        Category category = categoryService.getCategoryByIdAndUser(categoryId, user);
        if (category.getType() != TransactionType.EXPENSE) {
            throw new IllegalArgumentException("Budgets can only be set for expense categories.");
        }

        Budget existing = budgetRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
            .orElse(null);

        if (existing != null) {
            existing.setAmount(amount);
            return budgetRepository.save(existing);
        }

        return budgetRepository.save(Budget.builder()
            .user(user)
            .category(category)
            .amount(amount)
            .month(month)
            .year(year)
            .build());
    }

    @Transactional
    public void deleteBudget(Long id, User user) {
        Budget budget = budgetRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        budgetRepository.delete(budget);
    }

    @Transactional(readOnly = true)
    public List<Category> getExpenseCategoriesForUser(User user) {
        return categoryService.getCategoriesForUserByType(user, TransactionType.EXPENSE);
    }
}
