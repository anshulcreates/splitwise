package com.college.expensetracker.model;

public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    UPI("UPI"),
    NET_BANKING("Net Banking"),
    OTHER("Other");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
