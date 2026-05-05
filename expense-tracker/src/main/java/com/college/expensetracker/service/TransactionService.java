package com.college.expensetracker.service;

import com.college.expensetracker.dto.TransactionDto;
import com.college.expensetracker.exception.ResourceNotFoundException;
import com.college.expensetracker.model.*;
import com.college.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    @Transactional
    public Transaction addTransaction(User user, TransactionDto dto) {
        Category category = categoryService.getCategoryByIdAndUser(dto.getCategoryId(), user);

        if (category.getType() != dto.getType()) {
            throw new IllegalArgumentException("Category type does not match transaction type.");
        }

        Transaction transaction = Transaction.builder()
            .user(user)
            .category(category)
            .type(dto.getType())
            .amount(dto.getAmount())
            .description(dto.getDescription())
            .transactionDate(dto.getTransactionDate())
            .paymentMethod(dto.getPaymentMethod())
            .build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Long id, User user, TransactionDto dto) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        Category category = categoryService.getCategoryByIdAndUser(dto.getCategoryId(), user);

        if (category.getType() != dto.getType()) {
            throw new IllegalArgumentException("Category type does not match transaction type.");
        }

        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setCategory(category);
        transaction.setDescription(dto.getDescription());
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setPaymentMethod(dto.getPaymentMethod());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionByIdAndUser(Long id, User user) {
        return transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsPage(User user, TransactionType type, Long categoryId,
                                                  LocalDate startDate, LocalDate endDate,
                                                  String keyword, int page, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, 10, sort);

        Category category = null;
        if (categoryId != null) {
            try {
                category = categoryService.getCategoryByIdAndUser(categoryId, user);
            } catch (ResourceNotFoundException ignored) {}
        }

        String kw = (keyword != null && keyword.isBlank()) ? null : keyword;

        return transactionRepository.findWithFilters(user, type, category, startDate, endDate, kw, pageable);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions(User user) {
        return transactionRepository.findTop5ByUserOrderByTransactionDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsBetween(User user, LocalDate start, LocalDate end) {
        return transactionRepository.findByUserAndDateBetween(user, start, end);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByCategory(User user, Category category) {
        return transactionRepository.findByUserAndCategory(user, category);
    }

    public TransactionDto toDto(Transaction t) {
        TransactionDto dto = new TransactionDto();
        dto.setId(t.getId());
        dto.setType(t.getType());
        dto.setAmount(t.getAmount());
        dto.setCategoryId(t.getCategory().getId());
        dto.setDescription(t.getDescription());
        dto.setTransactionDate(t.getTransactionDate());
        dto.setPaymentMethod(t.getPaymentMethod());
        return dto;
    }
}
