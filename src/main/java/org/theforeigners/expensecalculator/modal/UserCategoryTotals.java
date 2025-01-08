package org.theforeigners.expensecalculator.modal;

public class UserCategoryTotals {
    private String category;
    private String date;
    private double income;
    private double expense;
    private double netAmount;

    public UserCategoryTotals(String category, String date, double netAmount) {
        this.category = category;
        this.date = date;
        this.netAmount = netAmount;
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

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public double getExpense() {
        return expense;
    }

    public void setExpense(double expense) {
        this.expense = expense;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }
}
