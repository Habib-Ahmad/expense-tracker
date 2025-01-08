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

public class GroupViewTodoController {

    @FXML
    private TableView<Todo> todoTable;

    @FXML
    private TableColumn<Todo, Integer> todoIdColumn;

    @FXML
    private TableColumn<Todo, Integer> groupIdColumn;

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

    private int groupId;

    @FXML
    public void initialize() {
        todoIdColumn.setCellValueFactory(new PropertyValueFactory<>("todoId"));
        groupIdColumn.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadTodosForUser(groupId);
        markButton.setOnAction(event -> handleMarkButton());
        cancelButton.setOnAction(event -> handleCancelButton());
    }

    private void handleCancelButton() {
        UtilityMethods.closeCurrentWindow(cancelButton);
    }

    private void loadTodosForUser(int groupId) {
        String query = "SELECT * FROM TODO WHERE group_id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int todoId = rs.getInt("todo_id");
                String itemName = rs.getString("item_name");
                boolean completed = rs.getBoolean("completed");
                String createdAt = rs.getString("created_at");

                todos.add(new Todo(todoId, groupId, itemName, completed, createdAt));
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
                loadTodosForUser(groupId);
                UtilityMethods.showPopup("Todo marked as completed successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
