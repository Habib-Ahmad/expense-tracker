package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.Todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminTodoController {

    @FXML
    private TableView<Todo> todoTable;

    @FXML
    private TableColumn<Todo, Integer> todoIdColumn;

    @FXML
    private TableColumn<Todo, Integer> userIdColumn;

    @FXML
    private TableColumn<Todo, Integer> groupIdColumn;

    @FXML
    private TableColumn<Todo, String> itemNameColumn;

    @FXML
    private TableColumn<Todo, Boolean> completedColumn;

    @FXML
    private TableColumn<Todo, String> createdAtColumn;

    private ObservableList<Todo> todos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        todoIdColumn.setCellValueFactory(new PropertyValueFactory<>("todoId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        groupIdColumn.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadTodos();
    }

    private void loadTodos() {
        String query = "SELECT * FROM TODO";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int todoId = rs.getInt("todo_id");
                int userId = rs.getInt("user_id");
                int groupId = rs.getInt("group_id");
                String itemName = rs.getString("item_name");
                boolean completed = rs.getBoolean("completed");
                String createdAt = rs.getString("created_at");

                todos.add(new Todo(todoId, userId, groupId, itemName, completed, createdAt));
            }

            todoTable.setItems(todos);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}