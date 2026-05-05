package com.college.expensetracker.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtils {

    private DateUtils() {}

    public static LocalDate startOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate endOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1).withDayOfMonth(
            LocalDate.of(year, month, 1).lengthOfMonth()
        );
    }

    public static LocalDate startOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return startOfMonth(now.getYear(), now.getMonthValue());
    }

    public static LocalDate endOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return endOfMonth(now.getYear(), now.getMonthValue());
    }

    public static String monthName(int month) {
        return Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    public static String monthYearLabel(int year, int month) {
        return monthName(month) + " " + year;
    }
}
