package com.college.expensetracker.service;

import com.college.expensetracker.exception.ResourceNotFoundException;
import com.college.expensetracker.model.Category;
import com.college.expensetracker.model.TransactionType;
import com.college.expensetracker.model.User;
import com.college.expensetracker.repository.CategoryRepository;
import com.college.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void seedDefaultCategoriesForUser(User user) {
        // Income categories
        String[][] incomeCategories = {
            {"Salary", "bi-cash-coin"},
            {"Freelance", "bi-laptop"},
            {"Business", "bi-briefcase"},
            {"Investment", "bi-graph-up-arrow"},
            {"Gift", "bi-gift"},
            {"Other", "bi-three-dots"}
        };
        for (String[] cat : incomeCategories) {
            categoryRepository.save(Category.builder()
                .user(user)
                .name(cat[0])
                .type(TransactionType.INCOME)
                .icon(cat[1])
                .isDefault(false)
                .build());
        }

        // Expense categories
        String[][] expenseCategories = {
            {"Food", "bi-cup-hot"},
            {"Transport", "bi-car-front"},
            {"Shopping", "bi-bag"},
            {"Bills", "bi-receipt"},
            {"Entertainment", "bi-film"},
            {"Health", "bi-heart-pulse"},
            {"Education", "bi-book"},
            {"Rent", "bi-house"},
            {"Groceries", "bi-cart"},
            {"Other", "bi-three-dots"}
        };
        for (String[] cat : expenseCategories) {
            categoryRepository.save(Category.builder()
                .user(user)
                .name(cat[0])
                .type(TransactionType.EXPENSE)
                .icon(cat[1])
                .isDefault(false)
                .build());
        }
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesForUser(User user) {
        return categoryRepository.findAllByUserOrDefault(user);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesForUserByType(User user, TransactionType type) {
        return categoryRepository.findAllByUserOrDefaultAndType(user, type);
    }

    @Transactional(readOnly = true)
    public List<Category> getUserCustomCategories(User user) {
        return categoryRepository.findByUserOrderByName(user);
    }

    @Transactional(readOnly = true)
    public Category getCategoryByIdAndUser(Long id, User user) {
        return categoryRepository.findByIdAndUserOrDefault(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional
    public Category addCustomCategory(User user, String name, TransactionType type, String icon) {
        if (categoryRepository.existsByUserAndName(user, name)) {
            throw new IllegalArgumentException("Category '" + name + "' already exists.");
        }
        return categoryRepository.save(Category.builder()
            .user(user)
            .name(name)
            .type(type)
            .icon(icon)
            .isDefault(false)
            .build());
    }

    @Transactional
    public Category updateCategory(Long id, User user, String name, String icon) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot edit this category.");
        }
        if (category.isDefault()) {
            throw new IllegalArgumentException("Default categories cannot be edited.");
        }

        category.setName(name);
        category.setIcon(icon);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id, User user) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot delete this category.");
        }
        if (category.isDefault()) {
            throw new IllegalArgumentException("Default categories cannot be deleted.");
        }
        if (transactionRepository.existsByCategory(category)) {
            throw new IllegalArgumentException("Cannot delete category — it is used by existing transactions.");
        }

        categoryRepository.delete(category);
    }
}
