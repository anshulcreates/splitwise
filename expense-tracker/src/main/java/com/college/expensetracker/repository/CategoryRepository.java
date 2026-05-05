package com.college.expensetracker.repository;

import com.college.expensetracker.model.Category;
import com.college.expensetracker.model.TransactionType;
import com.college.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // All categories visible to a user: their own + all defaults
    @Query("SELECT c FROM Category c WHERE c.user = :user OR c.isDefault = true ORDER BY c.name")
    List<Category> findAllByUserOrDefault(@Param("user") User user);

    // Filtered by type
    @Query("SELECT c FROM Category c WHERE (c.user = :user OR c.isDefault = true) AND c.type = :type ORDER BY c.name")
    List<Category> findAllByUserOrDefaultAndType(@Param("user") User user, @Param("type") TransactionType type);

    // Only user's custom categories
    List<Category> findByUserOrderByName(User user);

    // Only default categories
    List<Category> findByIsDefaultTrueOrderByName();

    // Check if category belongs to user or is default (for security)
    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user = :user OR c.isDefault = true)")
    java.util.Optional<Category> findByIdAndUserOrDefault(@Param("id") Long id, @Param("user") User user);

    boolean existsByUserAndName(User user, String name);
}
