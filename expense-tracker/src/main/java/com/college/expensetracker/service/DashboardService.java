package com.college.expensetracker.service;

import com.college.expensetracker.dto.DashboardDto;
import com.college.expensetracker.model.TransactionType;
import com.college.expensetracker.model.User;
import com.college.expensetracker.repository.TransactionRepository;
import com.college.expensetracker.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    private static final String[] CHART_COLORS = {
        "#4F46E5", "#10B981", "#EF4444", "#F59E0B", "#3B82F6",
        "#8B5CF6", "#EC4899", "#06B6D4", "#84CC16", "#F97316"
    };

    @Transactional(readOnly = true)
    public DashboardDto buildDashboard(User user) {
        LocalDate startOfMonth = DateUtils.startOfCurrentMonth();
        LocalDate endOfMonth = DateUtils.endOfCurrentMonth();

        BigDecimal monthlyIncome = transactionRepository.sumByUserAndTypeAndDateBetween(
            user, TransactionType.INCOME, startOfMonth, endOfMonth);
        BigDecimal monthlyExpense = transactionRepository.sumByUserAndTypeAndDateBetween(
            user, TransactionType.EXPENSE, startOfMonth, endOfMonth);

        BigDecimal totalIncome = transactionRepository.sumByUserAndType(user, TransactionType.INCOME);
        BigDecimal totalExpense = transactionRepository.sumByUserAndType(user, TransactionType.EXPENSE);
        BigDecimal balance = totalIncome.subtract(totalExpense);
        BigDecimal savings = monthlyIncome.subtract(monthlyExpense);

        // Pie chart: expense by category this month
        List<Object[]> categoryData = transactionRepository.sumByCategoryAndUserAndTypeAndDateBetween(
            user, TransactionType.EXPENSE, startOfMonth, endOfMonth);

        List<String> catLabels = new ArrayList<>();
        List<BigDecimal> catData = new ArrayList<>();
        List<String> catColors = new ArrayList<>();
        for (int i = 0; i < categoryData.size(); i++) {
            catLabels.add((String) categoryData.get(i)[0]);
            catData.add((BigDecimal) categoryData.get(i)[1]);
            catColors.add(CHART_COLORS[i % CHART_COLORS.length]);
        }

        // Bar chart: last 6 months income vs expense
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        List<Object[]> incomeMonthly = transactionRepository.sumByMonthAndUserAndType(
            user, TransactionType.INCOME, sixMonthsAgo, endOfMonth);
        List<Object[]> expenseMonthly = transactionRepository.sumByMonthAndUserAndType(
            user, TransactionType.EXPENSE, sixMonthsAgo, endOfMonth);

        List<String> monthLabels = buildMonthLabels(6);
        List<BigDecimal> monthlyIncomeData = mapMonthlyData(incomeMonthly, 6);
        List<BigDecimal> monthlyExpenseData = mapMonthlyData(expenseMonthly, 6);

        // Line chart: daily spending this month
        List<Object[]> dailyData = transactionRepository.sumByDayAndUserAndType(
            user, TransactionType.EXPENSE, startOfMonth, endOfMonth);
        List<String> dailyLabels = new ArrayList<>();
        List<BigDecimal> dailySpending = new ArrayList<>();
        for (Object[] row : dailyData) {
            dailyLabels.add(row[0].toString());
            dailySpending.add((BigDecimal) row[1]);
        }

        return DashboardDto.builder()
            .monthlyIncome(monthlyIncome)
            .monthlyExpense(monthlyExpense)
            .currentBalance(balance)
            .monthlySavings(savings)
            .recentTransactions(transactionService.getRecentTransactions(user))
            .expenseCategoryLabels(catLabels)
            .expenseCategoryData(catData)
            .expenseCategoryColors(catColors)
            .monthlyLabels(monthLabels)
            .monthlyIncomeData(monthlyIncomeData)
            .monthlyExpenseData(monthlyExpenseData)
            .dailyLabels(dailyLabels)
            .dailySpendingData(dailySpending)
            .build();
    }

    private List<String> buildMonthLabels(int count) {
        List<String> labels = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = count - 1; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            labels.add(DateUtils.monthYearLabel(month.getYear(), month.getMonthValue()));
        }
        return labels;
    }

    // Maps DB results (year, month, sum) to an ordered list matching the last N months
    private List<BigDecimal> mapMonthlyData(List<Object[]> rows, int count) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            String key = row[0] + "-" + row[1];
            map.put(key, (BigDecimal) row[2]);
        }

        List<BigDecimal> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = count - 1; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String key = month.getYear() + "-" + month.getMonthValue();
            result.add(map.getOrDefault(key, BigDecimal.ZERO));
        }
        return result;
    }
}
