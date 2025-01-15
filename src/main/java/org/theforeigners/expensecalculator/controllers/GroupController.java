package org.theforeigners.expensecalculator.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
    private TableColumn<UserBudgetData, String> budgetCategoryColumn;

    @FXML
    private TableColumn<UserBudgetData, Double> budgetLimitColumn;

    @FXML
    private TableColumn<UserBudgetData, Double> budgetSpendColumn;

    @FXML
    private TableView<UserBudgetData> budgetTable;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField groupNameField;

    @FXML
    private Button removeBtn;

    @FXML
    private Button saveButton;

    @FXML
    private TableColumn<Transaction, Integer> transactionAmountColumn;

    @FXML
    private TableColumn<Transaction, String> transactionCategoryColumn;

    @FXML
    private TableColumn<Transaction, Date> transactionDateColumn;

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, String> transactionTypeColumn;

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

    private int groupId = SessionManager.getCurrentGroupId();
    private String groupName = SessionManager.getCurrentGroupName();

    private ObservableList<UserBudgetData> budgetData = FXCollections.observableArrayList();
    private ObservableList<Transaction> transactionData = FXCollections.observableArrayList();
    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<User> groupUsersList = FXCollections.observableArrayList();
    private ObservableList<User> selectedGroupUsersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        userListView.getItems().add(new User(1, "ahmad"));
        budgetCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        budgetLimitColumn.setCellValueFactory(new PropertyValueFactory<>("limit"));
        budgetLimitColumn.setOnEditCommit(event -> {
            UserBudgetData row = event.getRowValue();
            double newLimit = event.getNewValue();
            if (newLimit > 0) {
                row.setLimit(newLimit);
                updateLimitInDatabase(row);
            } else {
                UtilityMethods.showPopupWarning("Amount must be greater than 0");
                budgetTable.refresh();
            }
        });
        budgetSpendColumn.setCellValueFactory(new PropertyValueFactory<>("spend"));
        budgetSpendColumn.setPrefWidth(200);
        budgetSpendColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    UserBudgetData rowData = getTableRow().getItem();
                    double budgetLimit = rowData.getLimit();
                    double spendPercentage = item / budgetLimit;

                    HBox hbox = new HBox();
                    hbox.setSpacing(5);
                    Rectangle progressBar = new Rectangle();
                    progressBar.setHeight(30);
                    progressBar.setWidth(spendPercentage * 100);
                    progressBar.setFill(spendPercentage <= 1 ? Color.LIGHTGREEN : Color.LIGHTCORAL);

                    hbox.getChildren().add(progressBar);
                    hbox.setSpacing(5);

                    Label label = new Label(String.format("%.2f", item));
                    hbox.getChildren().add(label);

                    setGraphic(hbox);
                }
            }
        });

        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(cellData -> {
            Transaction transaction = cellData.getValue();
            return new SimpleStringProperty(transaction.getType().name());
        });
        transactionTypeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(String.valueOf(item), empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("income-cell", "expense-cell");
                } else {
                    setText(item);
                    if (item.equals("INCOME")) {
                        getStyleClass().add("income-cell");
                        getStyleClass().remove("expense-cell");
                    } else {
                        getStyleClass().add("expense-cell");
                        getStyleClass().remove("income-cell");
                    }
                }
            }
        });
        loadBudgetData(groupName);
        loadTransactionData(groupName);
        loadGroupUsers();
        removeBtn.setOnAction(event -> handleRemoveButton());
        cancelButton.setOnAction(event -> handleCancelButton());
        searchButton.setOnAction(event -> handleSearchButtonAction());
        saveButton.setOnAction(event -> {
            saveUpdatedGroupData();
        });
        setupMenuItemActions();
        groupNameField.setEditable(false);
        groupNameField.setText(groupName);
    }

    private void setupMenuItemActions() {
        groupMenuItem.setOnAction(event -> UtilityMethods.switchToScene("AddGroup"));
        groupMenuItem2.setOnAction(event -> UtilityMethods.switchToScene("DeleteGroup"));
        budgetMenuItem.setOnAction(event -> UtilityMethods.switchToScene("AddGroupBudget", () -> {
            loadTransactionData(groupName);
            loadBudgetData(groupName);
        }));
        transactionMenuItem.setOnAction(event -> UtilityMethods.switchToScene("AddGroupTransaction", () -> {
            loadTransactionData(groupName);
            loadBudgetData(groupName);
        }));
        transactionMenuItem2.setOnAction(event -> {
            loadTransactionData(groupName);
            loadBudgetData(groupName);
        });
        todoMenuItem.setOnAction(event -> UtilityMethods.switchToScene("GroupTodoList"));
        todoMenuItem2.setOnAction(event -> UtilityMethods.switchToScene("GroupViewTodo"));
        chartMenuItem.setOnAction(event -> {
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
                    "JOIN `groups` g ON gu.group_id = g.group_id " +
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
            String groupQuery = "SELECT group_id FROM `groups` WHERE name = ?";
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        budgetTable.setItems(budgetData);
    }

    private void loadTransactionData(String groupName) {
        try {
            Connection connection = DBConnection.getConnection();
            String groupQuery = "SELECT group_id FROM `groups` WHERE name = ?";
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

            String transactionQuery = "SELECT t.amount, c.name AS category_name, c.description, t.date, t.split, t.type "
                    +
                    "FROM TRANSACTION t " +
                    "JOIN CATEGORY c ON t.category_id = c.category_id " +
                    "WHERE t.group_id = ? AND MONTH(t.date) = ? AND YEAR(t.date) = ?";
            PreparedStatement ps = connection.prepareStatement(transactionQuery);
            ps.setInt(1, groupId);
            ps.setInt(2, currentMonth);
            ps.setInt(3, currentYear);

            ResultSet rs = ps.executeQuery();

            transactionData.clear();

            if (!rs.isBeforeFirst()) {
                YearMonth previousMonth = YearMonth.now().minusMonths(1);
                int fallbackMonth = previousMonth.getMonthValue();
                int fallbackYear = previousMonth.getYear();

                ps.setInt(2, fallbackMonth);
                ps.setInt(3, fallbackYear);
                rs = ps.executeQuery();
            }

            while (rs.next()) {
                int amount = rs.getInt("amount");
                int splitTransaction = rs.getInt("split");
                String categoryName = rs.getString("category_name");
                String categoryDescription = rs.getString("description");
                Date date = rs.getDate("date");
                String type = rs.getString("type");
                boolean split = splitTransaction == 1;

                TransactionType transactionType = TransactionType.valueOf(type);

                Category category = new Category(categoryName, categoryDescription);
                Transaction transaction = new Transaction(transactionType, amount, date, category.getName(), split,
                        new User(SessionManager.getCurrentUserName(), SessionManager.getCurrentUserEmail()));
                transactionData.add(transaction);
            }

            transactionTable.setItems(transactionData);
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
            String query = "SELECT group_id FROM `groups` WHERE name = ?";
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
            String query = "SELECT admin_id FROM `groups` WHERE name = ?";
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
