package com.college.expensetracker.repository;

import com.college.expensetracker.model.Category;
import com.college.expensetracker.model.Transaction;
import com.college.expensetracker.model.TransactionType;
import com.college.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUser(Long id, User user);

    // Recent transactions for dashboard
    List<Transaction> findTop5ByUserOrderByTransactionDateDescCreatedAtDesc(User user);

    // All transactions for a user (paginated)
    Page<Transaction> findByUser(User user, Pageable pageable);

    // Sum by type and date range
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndTypeAndDateBetween(@Param("user") User user, @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // All-time sum by type (for balance calculation)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    BigDecimal sumByUserAndType(@Param("user") User user, @Param("type") TransactionType type);

    // Category breakdown (for pie chart)
    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.transactionDate BETWEEN :start AND :end GROUP BY t.category.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumByCategoryAndUserAndTypeAndDateBetween(@Param("user") User user, @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // Monthly totals (for bar chart — last N months)
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.transactionDate BETWEEN :start AND :end GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> sumByMonthAndUserAndType(@Param("user") User user, @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // Daily totals (for line chart)
    @Query("SELECT t.transactionDate, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.transactionDate BETWEEN :start AND :end GROUP BY t.transactionDate ORDER BY t.transactionDate")
    List<Object[]> sumByDayAndUserAndType(@Param("user") User user, @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // Transactions in date range for reports
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :start AND :end ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndDateBetween(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // Search with filters (paginated)
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:keyword IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.category.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Transaction> findWithFilters(@Param("user") User user,
                                      @Param("type") TransactionType type,
                                      @Param("category") Category category,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);

    // For category report
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.category = :category ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndCategory(@Param("user") User user, @Param("category") Category category);

    // Check if any transactions use a category (before delete)
    boolean existsByCategory(Category category);

    // Sum spent for a category in a given month (for budget tracking)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.category = :category AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal sumByUserAndCategoryAndMonthYear(@Param("user") User user, @Param("category") Category category, @Param("month") int month, @Param("year") int year);
}
