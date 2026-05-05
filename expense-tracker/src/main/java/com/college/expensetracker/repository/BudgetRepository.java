package com.college.expensetracker.repository;

import com.college.expensetracker.model.Budget;
import com.college.expensetracker.model.Category;
import com.college.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserAndMonthAndYearOrderByCategoryName(User user, int month, int year);

    Optional<Budget> findByUserAndCategoryAndMonthAndYear(User user, Category category, int month, int year);

    Optional<Budget> findByIdAndUser(Long id, User user);

    boolean existsByUserAndCategoryAndMonthAndYear(User user, Category category, int month, int year);
}
