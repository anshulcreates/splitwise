package com.college.expensetracker.service;

import com.college.expensetracker.model.Transaction;
import com.college.expensetracker.model.TransactionType;
import com.college.expensetracker.model.User;
import com.college.expensetracker.repository.TransactionRepository;
import com.college.expensetracker.util.DateUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    @Data
    @Builder
    public static class MonthlyReport {
        private int month;
        private int year;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netSavings;
        private List<Transaction> transactions;
        private Map<String, BigDecimal> categoryBreakdown;
        private List<String> categoryLabels;
        private List<BigDecimal> categoryData;
        private List<String> categoryColors;
    }

    @Data
    @Builder
    public static class YearlyReport {
        private int year;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netSavings;
        private List<String> monthLabels;
        private List<BigDecimal> monthlyIncome;
        private List<BigDecimal> monthlyExpense;
    }

    private static final String[] CHART_COLORS = {
        "#4F46E5", "#10B981", "#EF4444", "#F59E0B", "#3B82F6",
        "#8B5CF6", "#EC4899", "#06B6D4", "#84CC16", "#F97316"
    };

    @Transactional(readOnly = true)
    public MonthlyReport generateMonthlyReport(User user, int month, int year) {
        LocalDate start = DateUtils.startOfMonth(year, month);
        LocalDate end = DateUtils.endOfMonth(year, month);

        BigDecimal income = transactionRepository.sumByUserAndTypeAndDateBetween(user, TransactionType.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumByUserAndTypeAndDateBetween(user, TransactionType.EXPENSE, start, end);

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, start, end);

        // Category breakdown for expenses
        List<Object[]> catRows = transactionRepository.sumByCategoryAndUserAndTypeAndDateBetween(
            user, TransactionType.EXPENSE, start, end);

        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        for (int i = 0; i < catRows.size(); i++) {
            String name = (String) catRows.get(i)[0];
            BigDecimal amount = (BigDecimal) catRows.get(i)[1];
            breakdown.put(name, amount);
            labels.add(name);
            data.add(amount);
            colors.add(CHART_COLORS[i % CHART_COLORS.length]);
        }

        return MonthlyReport.builder()
            .month(month)
            .year(year)
            .totalIncome(income)
            .totalExpense(expense)
            .netSavings(income.subtract(expense))
            .transactions(transactions)
            .categoryBreakdown(breakdown)
            .categoryLabels(labels)
            .categoryData(data)
            .categoryColors(colors)
            .build();
    }

    @Transactional(readOnly = true)
    public YearlyReport generateYearlyReport(User user, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndDateBetween(user, TransactionType.INCOME, start, end);
        BigDecimal totalExpense = transactionRepository.sumByUserAndTypeAndDateBetween(user, TransactionType.EXPENSE, start, end);

        List<Object[]> incomeRows = transactionRepository.sumByMonthAndUserAndType(user, TransactionType.INCOME, start, end);
        List<Object[]> expenseRows = transactionRepository.sumByMonthAndUserAndType(user, TransactionType.EXPENSE, start, end);

        Map<Integer, BigDecimal> incomeMap = toMonthMap(incomeRows);
        Map<Integer, BigDecimal> expenseMap = toMonthMap(expenseRows);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> monthlyIncome = new ArrayList<>();
        List<BigDecimal> monthlyExpense = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            labels.add(DateUtils.monthName(m));
            monthlyIncome.add(incomeMap.getOrDefault(m, BigDecimal.ZERO));
            monthlyExpense.add(expenseMap.getOrDefault(m, BigDecimal.ZERO));
        }

        return YearlyReport.builder()
            .year(year)
            .totalIncome(totalIncome)
            .totalExpense(totalExpense)
            .netSavings(totalIncome.subtract(totalExpense))
            .monthLabels(labels)
            .monthlyIncome(monthlyIncome)
            .monthlyExpense(monthlyExpense)
            .build();
    }

    private Map<Integer, BigDecimal> toMonthMap(List<Object[]> rows) {
        Map<Integer, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            // row[0]=year, row[1]=month, row[2]=sum
            map.put(((Number) row[1]).intValue(), (BigDecimal) row[2]);
        }
        return map;
    }

    public List<Integer> getAvailableYears(User user) {
        // Return last 5 years including current
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear; y >= currentYear - 4; y--) {
            years.add(y);
        }
        return years;
    }
}
