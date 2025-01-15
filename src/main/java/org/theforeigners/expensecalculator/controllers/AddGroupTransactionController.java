package org.theforeigners.expensecalculator.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.User;
import org.theforeigners.expensecalculator.utilities.CallbackHandler;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.TransactionType;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class AddGroupTransactionController implements Initializable, CallbackHandler {

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
    private List<User> participants = new ArrayList<>();

    private int loggedInUserId = SessionManager.getCurrentUserId();
    private String groupName = SessionManager.getCurrentGroupName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typeComboBox.getItems().setAll(TransactionType.values());
        loadCategoriesFromDatabase();
        saveButton.setOnAction(_ -> handleSaveTransaction());
        cancelButton.setOnAction(_ -> handleCancel());
        loadGroupMembers();
    }

    private void loadCategoriesFromDatabase() {
        String query = "SELECT name, category_id FROM CATEGORY";
        try {
            Connection connection = DBConnection.getConnection();
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
        boolean split = false;

        if (amountText.isEmpty() || categoryName == null || transactionType == null || transactionDate == null
                || groupName == null) {
            UtilityMethods.showPopupWarning("Please fill in all required fields.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                UtilityMethods.showPopupWarning("Amount must be a positive value.");
                return;
            }

            Integer categoryId = categoryMap.get(categoryName);
            if (categoryId == null) {
                UtilityMethods.showPopupWarning("Invalid category selected.");
                return;
            }

            int groupId = getGroupIdByName(groupName);
            if (groupId == 0) {
                UtilityMethods.showPopupWarning("Invalid group selected.");
                return;
            }
            if (transactionType.equals(TransactionType.EXPENSE) && split) {
                if (participants.isEmpty()) {
                    UtilityMethods.showPopupWarning("No participants available for splitting the transaction.");
                    return;
                }
                double splitAmount = amount / (participants.size() + 1);
                for (User participant : participants) {
                    saveTransaction(participant.getId(), categoryId, transactionType, splitAmount, transactionDate,
                            groupId, true);
                }
                saveTransaction(loggedInUserId, categoryId, transactionType, splitAmount, transactionDate, groupId,
                        true);
                UtilityMethods.showPopup("Split expense transactions added successfully!");
            } else {
                saveTransaction(loggedInUserId, categoryId, transactionType, amount, transactionDate, groupId, false);

                if (transactionType.equals(TransactionType.INCOME)) {
                    UtilityMethods.showPopup("Income transaction added successfully!");
                } else {
                    UtilityMethods.showPopup("Expense transaction added successfully!");
                }
            }
            clearFields();
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
        } catch (NumberFormatException e) {
            UtilityMethods.showPopupWarning("Invalid amount entered. Please enter a valid numeric value.");
        }
    }

    private void saveTransaction(int userId, int categoryId, TransactionType type, double amount, LocalDate date,
            int groupId, boolean isSplit) {
        String query = "INSERT INTO TRANSACTION (user_id, category_id, type, amount, date, split, created_at, group_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setString(3, type.name());
            ps.setDouble(4, amount);
            ps.setDate(5, Date.valueOf(date));
            ps.setBoolean(6, isSplit);
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(8, groupId);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getGroupIdByName(String groupName) {
        String query = "SELECT group_id FROM `groups` WHERE name = ?";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("group_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadGroupMembers() {

        String query = "SELECT u.user_id, u.name ,u.email FROM USERS u " +
                "JOIN GROUP_USERS gu ON u.user_id = gu.user_id " +
                "JOIN `groups` g ON gu.group_id = g.group_id " +
                "WHERE g.name = ? and u.user_id != ?";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, groupName);
            ps.setInt(2, SessionManager.getCurrentUserId());
            ResultSet rs = ps.executeQuery();

            participants.clear();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("name");
                String email = rs.getString("email");
                participants.add(new User(userId, username, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
