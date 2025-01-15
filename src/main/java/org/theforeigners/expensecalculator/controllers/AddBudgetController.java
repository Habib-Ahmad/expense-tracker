package org.theforeigners.expensecalculator.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.utilities.CallbackHandler;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;

public class AddBudgetController implements CallbackHandler {
    private Runnable onSuccessCallback;

    @FXML
    private Button cancelButton;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField limitField;

    @FXML
    private Button saveButton;

    private HashMap<String, Integer> categoriesMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadCategories();
        saveButton.setOnAction(_ -> handleSave());
        cancelButton.setOnAction(_ -> handleCancel());
    }

    private void loadCategories() {
        String query = "SELECT category_id, name FROM CATEGORY";

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String categoryName = rs.getString("name");
                int categoryId = rs.getInt("category_id");

                categoryComboBox.getItems().add(categoryName);
                categoriesMap.put(categoryName, categoryId);
            }
        } catch (SQLException e) {
            UtilityMethods.showPopupWarning("Error loading categories: " + e.getMessage());
        }
    }

    private void handleSave() {
        String category = categoryComboBox.getValue();
        String limit = limitField.getText();
        int month = LocalDate.now().getMonthValue();

        if (category == null || category.isEmpty() || limit.isEmpty()) {
            UtilityMethods.showPopupWarning("Please fill all fields.");
            return;
        }

        try {
            Double.parseDouble(limit);
        } catch (NumberFormatException e) {
            UtilityMethods.showPopupWarning("Please enter a valid limit amount.");
            return;
        }

        Integer categoryId = categoriesMap.get(category);
        if (categoryId == null) {
            UtilityMethods.showPopupWarning("Invalid category selected.");
            return;
        }

        try {
            Connection connection = DBConnection.getConnection();
            if (connection == null || connection.isClosed()) {
                UtilityMethods.showPopupWarning("Failed to connect to the database.");
                return;
            }

            connection.setAutoCommit(false);

            int userId = SessionManager.getCurrentUserId();

            String budgetQuery = "INSERT INTO BUDGET (user_id, group_id, month, year, created_at) VALUES (?, NULL, ?, ?, NOW())";
            try (PreparedStatement budgetPs = connection.prepareStatement(budgetQuery,
                    Statement.RETURN_GENERATED_KEYS)) {
                budgetPs.setInt(1, userId);
                budgetPs.setInt(2, month);
                budgetPs.setInt(3, 2025);

                int rowsInserted = budgetPs.executeUpdate();
                if (rowsInserted == 0) {
                    throw new SQLException("Failed to insert budget.");
                }

                ResultSet generatedKeys = budgetPs.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    throw new SQLException("Failed to retrieve budget ID.");
                }
                int budgetId = generatedKeys.getInt(1);

                String budgetItemQuery = "INSERT INTO budget_item (budget_id, category_id, limit_amount) VALUES (?, ?, ?)";
                try (PreparedStatement budgetItemPs = connection.prepareStatement(budgetItemQuery)) {
                    budgetItemPs.setInt(1, budgetId);
                    budgetItemPs.setInt(2, categoryId);
                    budgetItemPs.setDouble(3, Double.parseDouble(limit));

                    int itemRowsInserted = budgetItemPs.executeUpdate();
                    if (itemRowsInserted == 0) {
                        throw new SQLException("Failed to insert budget item.");
                    }
                }

                connection.commit();
                UtilityMethods.showPopup("Budget and budget item added successfully.");
                clearFields();
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
                ((Stage) saveButton.getScene().getWindow()).close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            UtilityMethods.showPopupWarning("Error during budget saving: " + e.getMessage());
        }
    }

    private void handleCancel() {
        clearFields();
        UtilityMethods.closeCurrentWindow(cancelButton);
    }

    private void clearFields() {
        categoryComboBox.setValue(null);
        limitField.clear();
    }

    @Override
    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }
}
