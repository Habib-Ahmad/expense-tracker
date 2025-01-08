package org.theforeigners.expensecalculator.modal;

import java.util.List;

public class Budget {

    private int id;
    private int userId;
    private int groupId;
    private int month;
    private int year;
    private List<BudgetItem> budgetItems;

    public Budget() {
    }

    public Budget(int id, int userId, int month, int year, List<BudgetItem> budgetItems) {
        this(userId, month, year, budgetItems);
        this.id = id;
    }

    public Budget(int id, int month, int year, List<BudgetItem> budgetItems, int groupId) {
        this(month, year, budgetItems, groupId);
        this.id = id;
    }

    public Budget(int userId, int month, int year, List<BudgetItem> budgetItems) {
        this.userId = userId;
        this.month = month;
        this.year = year;
        this.budgetItems = budgetItems;
    }

    public Budget(int month, int year, List<BudgetItem> budgetItems, int groupId) {
        this.month = month;
        this.year = year;
        this.budgetItems = budgetItems;
        this.groupId = groupId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<BudgetItem> getBudgetItems() {
        return budgetItems;
    }

    public void setBudgetItems(List<BudgetItem> budgetItems) {
        this.budgetItems = budgetItems;
    }
}
