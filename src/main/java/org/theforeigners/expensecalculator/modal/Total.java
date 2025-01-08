package org.theforeigners.expensecalculator.modal;

public class Total {

    private double categoryTotals;
    private double expenseTotal;
    private double incomeTotal;

    public Total(double categoryTotals, double expenseTotal, double incomeTotal) {
        this.categoryTotals = categoryTotals;
        this.expenseTotal = expenseTotal;
        this.incomeTotal = incomeTotal;
    }

    public double getCategoryTotals() {
        return categoryTotals;
    }

    public void setCategoryTotals(double categoryTotals) {
        this.categoryTotals = categoryTotals;
    }

    public double getExpenseTotal() {
        return expenseTotal;
    }

    public void setExpenseTotal(double expenseTotal) {
        this.expenseTotal = expenseTotal;
    }

    public double getIncomeTotal() {
        return incomeTotal;
    }

    public void setIncomeTotal(double incomeTotal) {
        this.incomeTotal = incomeTotal;
    }
}
