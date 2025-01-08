package org.theforeigners.expensecalculator.modal;

import java.math.BigDecimal;

public class BudgetItem {

    private int id;
    private int budgetId;
    private Category category;
    private BigDecimal limit;

    public BudgetItem() {
    }

    public BudgetItem(int budgetId, Category category, BigDecimal limit) {
        this.budgetId = budgetId;
        this.category = category;
        this.limit = limit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }
}
