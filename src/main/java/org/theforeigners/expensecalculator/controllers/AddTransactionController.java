package org.theforeigners.expensecalculator.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.utilities.CallbackHandler;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.TransactionType;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AddTransactionController implements Initializable, CallbackHandler {
    private Runnable onSuccessCallback;
    @FXML
    private TextField amountField;

    @FXML
    private Button cancelButton;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button saveButton;

    @FXML
    private ComboBox<TransactionType> typeComboBox;

    private HashMap<String, Integer> categoryMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typeComboBox.getItems().setAll(TransactionType.values());
        loadCategoriesFromDatabase();

        saveButton.setOnAction(_ -> handleSaveTransaction());
        cancelButton.setOnAction(_ -> handleCancel());
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

    private void handleSaveTransaction() {
        String amountText = amountField.getText();
        String categoryName = categoryComboBox.getValue();
        TransactionType transactionType = typeComboBox.getValue();
        LocalDate transactionDate = datePicker.getValue();

        if (amountText.isEmpty() || categoryName == null || transactionType == null || transactionDate == null) {
            UtilityMethods.showPopupWarning("Please fill all fields.");
            return;
        }

        double amount = 0;
        Integer categoryId = null;

        try {
            amount = Double.parseDouble(amountText);

            categoryId = categoryMap.get(categoryName);
            if (categoryId == null) {
                UtilityMethods.showPopupWarning("Invalid category selected.");
                return;
            }
        } catch (NumberFormatException e) {
            UtilityMethods.showPopupWarning("Invalid amount. Please enter a valid number.");
        }

        String query = "INSERT INTO TRANSACTION (user_id, category_id, type, amount, date, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);

            int userId = getLoggedInUserId();

            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setString(3, transactionType.name());
            ps.setDouble(4, amount);
            ps.setDate(5, Date.valueOf(transactionDate));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                UtilityMethods.showPopup("Transaction added successfully!");
                clearFields();
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
            } else {
                UtilityMethods.showPopupWarning("Failed to add transaction. Please try again.");
            }

        } catch (SQLException e) {
            UtilityMethods.showPopupWarning("Database error: " + e.getMessage());
        }
    }

    private int getLoggedInUserId() {
        return SessionManager.getCurrentUserId();
    }

    private void clearFields() {
        amountField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        typeComboBox.getSelectionModel().clearSelection();
        datePicker.setValue(null);
    }

    private void handleCancel() {
        UtilityMethods.closeCurrentWindow(cancelButton);
    }

    @Override
    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }
}
