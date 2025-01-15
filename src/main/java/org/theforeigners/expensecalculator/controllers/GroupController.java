package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.*;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.TransactionType;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Date;
import java.util.Objects;

public class GroupController {

    @FXML
    private Button searchButton;
    @FXML
    private TextField usernameField;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField groupNameField;

    @FXML
    private Button removeBtn;

    @FXML
    private Button saveButton;

    @FXML
    private TableColumn<UserCategoryTotals, Double> transactionAmountColumn;

    @FXML
    private TableColumn<UserCategoryTotals, String> transactionCategoryColumn;

    @FXML
    private TableColumn<UserCategoryTotals, Date> transactionDateColumn;

    @FXML
    private TableView<UserCategoryTotals> transactionTable;

    @FXML
    private ListView<User> userListView;

    @FXML
    private MenuItem transactionMenuItem2;

    @FXML
    private MenuItem chartMenuItem;

    @FXML
    private MenuItem groupMenuItem;

    @FXML
    private MenuItem groupMenuItem2;

    @FXML
    private MenuItem todoMenuItem;

    @FXML
    private MenuItem todoMenuItem2;

    @FXML
    private MenuItem transactionMenuItem;

    @FXML
    private MenuItem budgetMenuItem;

    @FXML
    private ListView<UserBudgetData> budgetListView;

    private String groupName = SessionManager.getCurrentGroupName();

    private ObservableList<UserBudgetData> budgetData = FXCollections.observableArrayList();
    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<User> groupUsersList = FXCollections.observableArrayList();
    private ObservableList<User> selectedGroupUsersList = FXCollections.observableArrayList();
    private final ObservableList<UserBudgetData> budgetList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        transactionCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionAmountColumn.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("income-cell", "expense-cell");
                if (empty || item == null) {
                    setText(null);
                } else {
                    UserCategoryTotals currentTransaction = getTableView().getItems().get(getIndex());
                    String type = String.valueOf(currentTransaction.getType());

                    setText(String.format("$%.2f", item));

                    if ("INCOME".equalsIgnoreCase(type)) {
                        if (!getStyleClass().contains("income-cell")) {
                            getStyleClass().add("income-cell");
                        }
                    } else if ("EXPENSE".equalsIgnoreCase(type)) {
                        if (!getStyleClass().contains("expense-cell")) {
                            getStyleClass().add("expense-cell");
                        }
                    }
                }
            }
        });
        loadBudgetData(groupName);
        loadTransactionData(groupName);
        loadGroupUsers();
        removeBtn.setOnAction(_ -> handleRemoveButton());
        cancelButton.setOnAction(_ -> handleCancelButton());
        searchButton.setOnAction(_ -> handleSearchButtonAction());
        saveButton.setOnAction(_ -> {
            saveUpdatedGroupData();
        });
        setupMenuItemActions();
        groupNameField.setEditable(false);
        groupNameField.setText(groupName);
        initializeBudgetListView();
    }

    private void initializeBudgetListView() {
        budgetListView.setItems(budgetList);
        budgetListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(UserBudgetData item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox card = createBudgetCard(item);
                    setGraphic(card);
                }
            }
        });
    }

    private HBox createBudgetCard(UserBudgetData item) {
        HBox card = new HBox();
        card.setSpacing(8);
        card.setStyle(
                "-fx-padding: 10; -fx-background-color: #f8f8f8; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefHeight(30);
        card.setPrefWidth(300);
        card.setMaxWidth(420);

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(3);

        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label limitLabel = new Label(String.format("%.0f $", item.getLimit()));
        limitLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");
        TextField limitTextField = new TextField(String.valueOf(item.getLimit()));
        limitTextField.setStyle("-fx-font-size: 16px;");
        limitTextField.setVisible(false);

        limitLabel.setOnMouseClicked(event -> {
            boolean count = event.getClickCount() == 2;
            if (count) {
                limitLabel.setVisible(false);
                limitTextField.setVisible(true);
                limitTextField.requestFocus();
            }
        });

        limitTextField.setOnAction(_ -> commitLimitEdit(limitTextField, limitLabel, item));
        limitTextField.focusedProperty().addListener((_, _, newVal) -> {
            if (!newVal) {
                commitLimitEdit(limitTextField, limitLabel, item);
            }
        });

        detailsBox.getChildren().addAll(categoryLabel, limitLabel, limitTextField);

        ProgressBar progressBar = new ProgressBar();
        double progressWidth = item.getSpend() == 0 ? 200 : (item.getSpend() / item.getLimit()) * 200;
        Color barColor;
        if (item.getSpend() == 0) {
            barColor = Color.GRAY;
        } else {
            barColor = item.getSpend() <= item.getLimit() ? Color.LIGHTGREEN : Color.LIGHTCORAL;
        }
        progressBar.setProgress(item.getSpend() / item.getLimit());
        progressBar.setPrefWidth(progressWidth);
        progressBar.setPrefWidth(250);
        progressBar.setStyle("-fx-accent: " + barColor.toString().replace("0x", "#") + ";");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label spendLabel = new Label(String.format("%.0f $     %.0f%%", item.getSpend(),
                (item.getSpend() / item.getLimit()) * 100));
        spendLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");

        VBox progressBox = new VBox(progressBar, spendLabel);
        progressBox.setSpacing(5);

        card.getChildren().addAll(detailsBox, spacer, progressBox);

        return card;
    }

    private void commitLimitEdit(TextField limitTextField, Label limitLabel, UserBudgetData item) {
        try {
            double newLimit = Double.parseDouble(limitTextField.getText());
            if (item.getLimit() == newLimit) {
                limitLabel.setVisible(true);
                limitTextField.setVisible(false);
                return;
            }
            if (newLimit > 0) {
                item.setLimit(newLimit);
                limitLabel.setText(String.format("%.0f $", newLimit));
                updateLimitInDatabase(item);
                budgetListView.refresh();
            } else {
                UtilityMethods.showPopupWarning("Amount must be greater than 0");
            }
        } catch (NumberFormatException e) {
            UtilityMethods.showPopupWarning("Invalid amount entered. Please enter a valid number.");
        } finally {
            limitLabel.setVisible(true);
            limitTextField.setVisible(false);
        }
    }

    private void setupMenuItemActions() {
        groupMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("AddGroup"));
        groupMenuItem2.setOnAction(_ -> UtilityMethods.switchToScene("DeleteGroup"));
        budgetMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("AddGroupBudget", () -> {
            loadTransactionData(groupName);
            loadBudgetData(groupName);
        }));
        transactionMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("AddGroupTransaction", () -> {
            loadTransactionData(groupName);
            loadBudgetData(groupName);
        }));
        transactionMenuItem2.setOnAction(_ -> {
            loadTransactionData(groupName);
            loadBudgetData(groupName);
        });
        todoMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("GroupTodoList"));
        todoMenuItem2.setOnAction(_ -> UtilityMethods.switchToScene("GroupViewTodo"));
        chartMenuItem.setOnAction(_ -> {
            if (groupNameField.getText().isEmpty()) {
                UtilityMethods.showPopupWarning("Select any group 1st");
                return;
            }
            int groupId = getGroupIdByName();
            SessionManager.setCurrentGroupId(groupId);
            UtilityMethods.switchToScene("GroupDashboardChart");
        });
    }

    private void loadGroupUsers() {
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT u.user_id, u.name, u.email " +
                    "FROM USERS u " +
                    "JOIN GROUP_USERS gu ON u.user_id = gu.user_id " +
                    "JOIN GROUPS g ON gu.group_id = g.group_id " +
                    "WHERE g.name = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();

            groupUsersList.clear();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String name = rs.getString("name");
                String email = rs.getString("email");

                User user = new User(userId, name, email);
                groupUsersList.add(user);
            }
            userListView.getItems().clear();
            userListView.setItems(groupUsersList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBudgetData(String groupName) {
        try {
            Connection connection = DBConnection.getConnection();
            String groupQuery = "SELECT group_id FROM GROUPS WHERE name = ?";
            PreparedStatement groupPs = connection.prepareStatement(groupQuery);
            groupPs.setString(1, groupName);
            ResultSet groupRs = groupPs.executeQuery();

            int groupId = -1;
            if (groupRs.next()) {
                groupId = groupRs.getInt("group_id");
            }

            if (groupId == -1) {
                System.out.println("Group not found!");
                return;
            }

            String query = "SELECT c.name AS category_name, bi.limit_amount, b.budget_id, bi.budget_item_id, " +
                    "(SELECT COALESCE(SUM(t.amount), 0) " +
                    " FROM TRANSACTION t " +
                    " WHERE t.category_id = c.category_id AND t.group_id = b.group_id AND t.type = 'EXPENSE') AS total_spend "
                    +
                    "FROM BUDGET b " +
                    "JOIN BUDGET_ITEM bi ON b.budget_id = bi.budget_id " +
                    "JOIN CATEGORY c ON bi.category_id = c.category_id " +
                    "WHERE b.group_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            budgetData.clear();
            while (rs.next()) {
                int budgetId = rs.getInt("budget_id");
                int budgetItemId = rs.getInt("budget_item_id");
                String categoryName = rs.getString("category_name");
                int limitAmount = rs.getInt("limit_amount");
                int totalSpend = rs.getInt("total_spend");

                UserBudgetData budget = new UserBudgetData(budgetId, budgetItemId, categoryName, limitAmount,
                        totalSpend);
                budgetData.add(budget);
            }
            budgetList.clear();
            budgetList.addAll(budgetData);
            budgetListView.setItems(budgetList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTransactionData(String groupName) {
        try {
            Connection connection = DBConnection.getConnection();
            String groupQuery = "SELECT group_id FROM GROUPS WHERE name = ?";
            PreparedStatement groupPs = connection.prepareStatement(groupQuery);
            groupPs.setString(1, groupName);
            ResultSet groupRs = groupPs.executeQuery();

            int groupId = -1;
            if (groupRs.next()) {
                groupId = groupRs.getInt("group_id");
            }

            if (groupId == -1) {
                System.out.println("Group not found!");
                return;
            }

            LocalDate currentDate = LocalDate.now();
            int currentMonth = currentDate.getMonthValue();
            int currentYear = currentDate.getYear();

            String transactionQuery = "SELECT " +
                    "T.transaction_id, " +
                    "C.name AS category_name, " +
                    "T.date AS date, " +
                    "T.amount, " +
                    "T.type, " +
                    "T.split AS is_split " +
                    "FROM TRANSACTION T " +
                    "JOIN CATEGORY C ON T.category_id = C.category_id " +
                    "WHERE T.group_id = ? AND MONTH(T.date) = ? AND YEAR(T.date) = ?";
            PreparedStatement ps = connection.prepareStatement(transactionQuery);
            ps.setInt(1, groupId);
            ps.setInt(2, currentMonth);
            ps.setInt(3, currentYear);

            ResultSet rs = ps.executeQuery();

            ObservableList<UserCategoryTotals> data = FXCollections.observableArrayList();

            if (!rs.isBeforeFirst()) {
                YearMonth previousMonth = YearMonth.now().minusMonths(1);
                int fallbackMonth = previousMonth.getMonthValue();
                int fallbackYear = previousMonth.getYear();

                ps.setInt(2, fallbackMonth);
                ps.setInt(3, fallbackYear);
                rs = ps.executeQuery();
            }

            while (rs.next()) {
                double amount = rs.getDouble("amount");
                String categoryName = rs.getString("category_name");
                String date = rs.getString("date");
                String type = rs.getString("type");
                TransactionType transactionType = TransactionType.valueOf(type);
                data.add(new UserCategoryTotals(categoryName, String.valueOf(date), amount, transactionType));
            }
            transactionCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
            transactionTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleRemoveButton() {
        User user = userListView.getSelectionModel().getSelectedItem();
        if (user == null) {
            UtilityMethods.showPopup("Please select a user to remove.");
            return;
        }
        if (Objects.equals(user.getName(), SessionManager.getCurrentUserName())) {
            UtilityMethods.showPopup("You can remove yourself as (group-admin)!.");
            return;
        }
        String groupName = groupNameField.getText();
        if (!isCurrentUserAdmin(groupName)) {
            UtilityMethods.showPopup("You do not have permission to remove users from this group.");
            return;
        }
        selectedGroupUsersList.remove(user);
        userListView.getItems().remove(user);
        userData.remove(user);
        UtilityMethods.showPopup("User removed from group.");
    }

    private void handleCancelButton() {
        UtilityMethods.closeCurrentWindow(cancelButton);
    }

    private void saveUpdatedGroupData() {
        String groupName = groupNameField.getText();
        if (!isCurrentUserAdmin(groupName)) {
            UtilityMethods.showPopup("You do not have permission to update this group.");
            return;
        }
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT group_id FROM GROUPS WHERE name = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, groupNameField.getText());
            ResultSet rs = ps.executeQuery();

            int groupId = -1;
            if (rs.next()) {
                groupId = rs.getInt("group_id");
            }

            if (groupId == -1) {
                System.out.println("Group not found!");
                return;
            }

            String deleteQuery = "DELETE FROM GROUP_USERS WHERE group_id = ?";
            PreparedStatement deletePs = connection.prepareStatement(deleteQuery);
            deletePs.setInt(1, groupId);
            deletePs.executeUpdate();

            String insertQuery = "INSERT INTO GROUP_USERS (group_id, user_id) VALUES (?, ?)";
            PreparedStatement insertPs = connection.prepareStatement(insertQuery);

            for (User user : userListView.getItems()) {
                insertPs.setInt(1, groupId);
                insertPs.setInt(2, user.getId());
                insertPs.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            UtilityMethods.showPopup("Group updated successfully!");
        }
    }

    private void handleSearchButtonAction() {
        String groupName = groupNameField.getText();
        if (!isCurrentUserAdmin(groupName)) {
            UtilityMethods.showPopup("You do not have permission to add users to this group.");
            return;
        }
        String searchText = usernameField.getText().trim();
        if (!searchText.isEmpty()) {
            searchUsers(searchText);
        } else {
            UtilityMethods.showPopup("User not found!");
        }
    }

    private void searchUsers(String searchText) {
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT user_id, name, email FROM USERS WHERE role = ? AND user_id != ? AND name = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, "USER");
            ps.setInt(2, SessionManager.getCurrentUserId());
            ps.setString(3, searchText);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String name = rs.getString("name");
                String email = rs.getString("email");

                User user = new User(userId, name, email);
                if (!userData.contains(user) && !selectedGroupUsersList.contains(user)) {
                    userData.add(user);
                    selectedGroupUsersList.add(user);
                    if (!userListView.getItems().contains(user))
                        userListView.getItems().add(user);
                }
            } else {
                UtilityMethods.showPopup("User not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isCurrentUserAdmin(String groupName) {
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT admin_id FROM GROUPS WHERE name = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int adminId = rs.getInt("admin_id");
                return adminId == SessionManager.getCurrentUserId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getGroupIdByName() {
        String query = "SELECT group_id FROM GROUPS WHERE name = ?";
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

    private void updateLimitInDatabase(UserBudgetData budgetData) {
        try {
            Connection connection = DBConnection.getConnection();
            String updateQuery = "UPDATE BUDGET_ITEM SET limit_amount = ? WHERE budget_item_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setDouble(1, budgetData.getLimit());
            preparedStatement.setInt(2, budgetData.getBudgetItemId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                UtilityMethods.showPopup("Limited updated successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
