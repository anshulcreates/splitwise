package com.college.expensetracker.dto;

import com.college.expensetracker.model.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardDto {

    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal currentBalance;
    private BigDecimal monthlySavings;

    private List<Transaction> recentTransactions;

    // Chart data
    private List<String> expenseCategoryLabels;
    private List<BigDecimal> expenseCategoryData;
    private List<String> expenseCategoryColors;

    private List<String> monthlyLabels;
    private List<BigDecimal> monthlyIncomeData;
    private List<BigDecimal> monthlyExpenseData;

    private List<String> dailyLabels;
    private List<BigDecimal> dailySpendingData;
}
