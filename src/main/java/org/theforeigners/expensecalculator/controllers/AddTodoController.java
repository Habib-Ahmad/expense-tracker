package org.theforeigners.expensecalculator.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.*;
import java.util.HashMap;

public class AddTodoController {

    @FXML
    private Button cancelButton;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private CheckBox completedCheckBox;

    @FXML
    private TextField itemNameField;

    @FXML
    private Button saveButton;

    private HashMap<String, Integer> categoryMap = new HashMap<>();

    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> handleSaveTodo());
        cancelButton.setOnAction(event -> UtilityMethods.closeCurrentWindow(cancelButton));
        loadCategoriesFromDatabase();
    }

    private void handleSaveTodo() {
        String itemName = itemNameField.getText();
        String category = categoryComboBox.getValue();

        if (itemName.isEmpty() || category == null) {
            UtilityMethods.showPopupWarning("Please fill in all fields.");
            return;
        }

        boolean isCompleted = completedCheckBox.isSelected();

        int userId = SessionManager.getCurrentUserId();

        try {
            Connection connection = DBConnection.getConnection();
            String query = "INSERT INTO TODO (user_id, item_name, category_id, completed, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP())";
            PreparedStatement ps = connection.prepareStatement(query);
            int categoryId = categoryMap.get(category);

            ps.setInt(1, userId);
            ps.setString(2, itemName);
            ps.setInt(3, categoryId);
            ps.setBoolean(4, isCompleted);

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                UtilityMethods.showPopup("To-Do item saved successfully.");
                itemNameField.clear();
                categoryComboBox.getSelectionModel().clearSelection();
                completedCheckBox.setSelected(false);
            } else {
                UtilityMethods.showPopupWarning("Failed to save To-Do item.");
            }

        } catch (SQLException e) {
            UtilityMethods.showPopupWarning("Database error: " + e.getMessage());
        }
    }

    private void loadCategoriesFromDatabase() {
        String query = "SELECT name, category_id FROM CATEGORY";
        try {
            Connection connection = DBConnection.getConnection();
            if (connection == null || connection.isClosed()) {
                UtilityMethods.showPopupWarning("Failed to connect to the database.");
                return;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String categoryName = resultSet.getString("name");
                int categoryId = resultSet.getInt("category_id");
                categoryComboBox.getItems().add(categoryName);
                categoryMap.put(categoryName, categoryId);
            }
        } catch (SQLException e) {
            UtilityMethods.showPopupWarning("Error loading categories: " + e.getMessage());
        }
    }

}
