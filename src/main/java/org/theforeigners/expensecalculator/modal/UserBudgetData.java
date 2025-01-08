package org.theforeigners.expensecalculator.modal;

public class UserBudgetData {
    private int budgetId;
    private int budgetItemId;
    private String category;
    private double limit;
    private double spend;

    public UserBudgetData(String category, double limit, double spend) {
        this.category = category;
        this.limit = limit;
        this.spend = spend;
    }

    public UserBudgetData(int budgetId, int budgetItemId, String category, double limit, double spend) {
        this.budgetId = budgetId;
        this.budgetItemId = budgetItemId;
        this.category = category;
        this.limit = limit;
        this.spend = spend;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public int getBudgetItemId() {
        return budgetItemId;
    }

    public void setBudgetItemId(int budgetItemId) {
        this.budgetItemId = budgetItemId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public double getSpend() {
        return spend;
    }

    public void setSpend(double spend) {
        this.spend = spend;
    }

    @Override
    public String toString() {
        return "UserBudgetData{" +
                "category='" + category + '\'' +
                ", limit=" + limit +
                ", spend=" + spend +
                '}';
    }
}
