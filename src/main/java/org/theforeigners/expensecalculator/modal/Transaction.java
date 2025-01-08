package org.theforeigners.expensecalculator.modal;

import org.theforeigners.expensecalculator.utilities.TransactionType;

import java.util.Date;

public class Transaction {

    private TransactionType type;
    private int amount;
    private Date date;
    private Category category;
    private String categoryName;
    private boolean split;
    private User user;

    public Transaction(TransactionType type, int amount, Date date, Category category, boolean split, User user) {
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.split = split;
        this.user = user;
    }

    public Transaction(TransactionType type, int amount, Date date, String categoryName, boolean split, User user) {
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.split = split;
        this.user = user;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
