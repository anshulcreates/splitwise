package com.college.expensetracker.dto;

import com.college.expensetracker.model.PaymentMethod;
import com.college.expensetracker.model.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDto {

    private Long id;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount can have at most 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
