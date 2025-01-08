package org.theforeigners.expensecalculator.modal;

public class Item {
    private int month;
    private int year;
    private String category;
    private double limitAmount;

    public Item(int month, int year, String category, double limitAmount) {
        this.month = month;
        this.year = year;
        this.category = category;
        this.limitAmount = limitAmount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }
}