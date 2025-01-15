package org.theforeigners.expensecalculator.modal;

import org.theforeigners.expensecalculator.utilities.TransactionType;

public class UserCategoryTotals {
    private String category;
    private String date;
    private double netAmount;
    private TransactionType type;

    public UserCategoryTotals(String category, String date, double netAmount) {
        this.category = category;
        this.date = date;
        this.netAmount = netAmount;
    }

    public UserCategoryTotals(String category, String date, double netAmount, TransactionType type) {
        this.category = category;
        this.date = date;
        this.netAmount = netAmount;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public TransactionType getType() {
        return type;
    }
}
