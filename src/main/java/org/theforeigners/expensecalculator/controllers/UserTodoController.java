package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.Todo;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserTodoController {

    @FXML
    private TableView<Todo> todoTable;

    @FXML
    private TableColumn<Todo, Integer> todoIdColumn;

    @FXML
    private TableColumn<Todo, Integer> userIdColumn;

    @FXML
    private TableColumn<Todo, String> itemNameColumn;

    @FXML
    private TableColumn<Todo, Boolean> completedColumn;

    @FXML
    private TableColumn<Todo, String> createdAtColumn;

    @FXML
    private Button markButton;

    @FXML
    private Button cancelButton;

    private ObservableList<Todo> todos = FXCollections.observableArrayList();

    private int userId = SessionManager.getCurrentUserId();

    @FXML
    public void initialize() {
        todoIdColumn.setCellValueFactory(new PropertyValueFactory<>("todoId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadTodosForUser(userId);
        markButton.setOnAction(event -> handleMarkButton());
        cancelButton.setOnAction(event -> handleCancelButton());
    }

    private void handleCancelButton() {
        UtilityMethods.closeCurrentWindow(cancelButton);
    }

    private void loadTodosForUser(int userId) {
        String query = "SELECT * FROM TODO WHERE user_id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int todoId = rs.getInt("todo_id");
                String itemName = rs.getString("item_name");
                boolean completed = rs.getBoolean("completed");
                String createdAt = rs.getString("created_at");

                todos.add(new Todo(todoId, userId, itemName, completed, createdAt));
            }

            todoTable.setItems(todos);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleMarkButton() {
        Todo selectedTodo = todoTable.getSelectionModel().getSelectedItem();
        if (selectedTodo != null) {
            int todoId = selectedTodo.getTodoId();
            String query = "UPDATE TODO SET completed = ? WHERE todo_id = ?";
            try {
                Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setBoolean(1, true);
                ps.setInt(2, todoId);
                ps.executeUpdate();
                todos.clear();
                loadTodosForUser(userId);
                UtilityMethods.showPopup("Todo marked as completed successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            UtilityMethods.showPopupWarning("select a todo to mark as complete");
        }
    }
}
