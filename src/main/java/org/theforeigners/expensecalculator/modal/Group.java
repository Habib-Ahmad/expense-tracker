package org.theforeigners.expensecalculator.modal;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String name;
    private List<User> users;
    private Budget budget;
    private Transaction transaction;
    private Total total;
    private Todo todo;

    public Group(String name, ArrayList<User> users) {
        this.name = name;
        this.users = users;
    }

    public Group(String name, ArrayList<User> users, Budget budget, Transaction transaction, Total total, Todo todo) {
        this.name = name;
        this.users = users;
        this.budget = budget;
        this.transaction = transaction;
        this.total = total;
        this.todo = todo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Total getTotal() {
        return total;
    }

    public void setTotal(Total total) {
        this.total = total;
    }

    public Todo getTodo() {
        return todo;
    }

    public void setTodo(Todo todo) {
        this.todo = todo;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }
}
