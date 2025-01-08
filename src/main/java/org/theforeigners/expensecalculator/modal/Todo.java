package org.theforeigners.expensecalculator.modal;

public class Todo {
    private int todoId;
    private int userId;
    private int groupId;
    private String itemName;
    private boolean completed;
    private String createdAt;

    public Todo(int todoId, int userId, int groupId, String itemName, boolean completed, String createdAt) {
        this.todoId = todoId;
        this.userId = userId;
        this.groupId = groupId;
        this.itemName = itemName;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public Todo(int todoId, int userId, String itemName, boolean completed, String createdAt) {
        this.todoId = todoId;
        this.userId = userId;
        this.itemName = itemName;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public int getTodoId() {
        return todoId;
    }

    public void setTodoId(int todoId) {
        this.todoId = todoId;
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
