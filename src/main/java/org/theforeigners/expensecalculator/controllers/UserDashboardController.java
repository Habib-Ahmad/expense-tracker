package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.UserBudgetData;
import org.theforeigners.expensecalculator.modal.UserCategoryTotals;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.Month;
import java.util.ResourceBundle;

public class UserDashboardController implements Initializable {

    @FXML
    private Label monthlyLeftoverLabel;

    @FXML
    private MenuItem budgetMenuItem;

    @FXML
    private MenuItem chartMenuItem;

    @FXML
    private MenuItem groupMenuItem;

    @FXML
    private MenuItem groupMenuItem2;

    @FXML
    private MenuItem profileMenuItem;

    @FXML
    private MenuItem todoMenuItem;

    @FXML
    private MenuItem todoMenuItem2;

    @FXML
    private MenuItem transactionMenuItem;

    @FXML
    private Button logoutBtn;

    @FXML
    private Label usernameLbl;

    @FXML
    private TableColumn<UserCategoryTotals, String> totalsCategoryColumn;

    @FXML
    private TableColumn<UserCategoryTotals, Double> totalsAmountIncomeColumn;

    @FXML
    private TableColumn<UserCategoryTotals, String> dateColumn;

    @FXML
    private TableView<UserCategoryTotals> totalsTable;

    @FXML
    private ListView<UserBudgetData> budgetListView;

    @FXML
    private Button prevMonthBtn;

    @FXML
    private Button nextMonthBtn;

    @FXML
    private ComboBox<String> monthYearComboBox;

    private LocalDate currentDate = LocalDate.now();

    private final ObservableList<UserBudgetData> budgetList = FXCollections.observableArrayList();
    private final ObservableList<UserCategoryTotals> totalList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameLbl.setText(SessionManager.getCurrentUserName());
        setButtonActions();
        populateMonthYearComboBox(SessionManager.getCurrentUserCreatedAt());
        monthYearComboBox.setOnAction(_ -> updateDataBasedOnSelection());
        prevMonthBtn.setOnAction(_ -> navigateToPreviousMonth());
        nextMonthBtn.setOnAction(_ -> navigateToNextMonth());
        initializeTotalsTable();
        loadUserTotals();
        loadBudgetTableData();
        initializeBudgetListView();
        usernameLbl.setText(SessionManager.getCurrentUserName());
        setButtonActions();
        populateMonthYearComboBox(SessionManager.getCurrentUserCreatedAt());

        // Load leftover once now
        loadMonthlyLeftover();
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

    private void loadMonthlyLeftover() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DBConnection.getConnection();

            String query =
                    "SELECT " +
                            "   SUM(CASE WHEN T.type = 'INCOME' THEN T.amount ELSE 0 END) AS total_incomes, " +
                            "   SUM(CASE WHEN T.type = 'EXPENSE' THEN T.amount ELSE 0 END) AS total_expenses " +
                            "FROM TRANSACTION T " +
                            "WHERE T.user_id = ? " +
                            "  AND T.group_id IS NULL " + // Exclude group transactions
                            "  AND MONTH(T.date) = ? " +
                            "  AND YEAR(T.date) = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, SessionManager.getCurrentUserId());
            preparedStatement.setInt(2, currentDate.getMonthValue());
            preparedStatement.setInt(3, currentDate.getYear());
            resultSet = preparedStatement.executeQuery();

            double totalIncomes = 0;
            double totalExpenses = 0;

            if (resultSet.next()) {
                totalIncomes = resultSet.getDouble("total_incomes");
                totalExpenses = resultSet.getDouble("total_expenses");
            }

            double leftover = totalIncomes - totalExpenses;
            // Format however you like:
            monthlyLeftoverLabel.setText(String.format("Current Month Remaining Money: $%.2f", leftover));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // recommended to close resultSet, statement, connection
            try { if (resultSet != null) resultSet.close(); } catch (Exception ignore) {}
            try { if (preparedStatement != null) preparedStatement.close(); } catch (Exception ignore) {}
        }
    }



    private HBox createBudgetCard(UserBudgetData item) {
        HBox card = new HBox();
        card.setSpacing(10);
        card.setStyle(
                "-fx-padding: 10; " +
                        "-fx-background-color: #f8f8f8; " +
                        "-fx-border-color: #ddd; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;"
        );
        card.setPrefHeight(35);

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(3);

        // --- category label ---
        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // --- limit label / textfield setup ---
        Label limitLabel = new Label(String.format("$%.0f", item.getLimit()));
        limitLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");
        TextField limitTextField = new TextField(String.valueOf(item.getLimit()));
        limitTextField.setStyle("-fx-font-size: 16px;");
        limitTextField.setVisible(false);

        limitLabel.setOnMouseClicked(event -> {
            String selectedMonthYear = monthYearComboBox.getSelectionModel().getSelectedItem();
            if (selectedMonthYear != null) {
                String[] parts = selectedMonthYear.split(" ");
                String monthName = parts[0];
                int monthValue = Month.valueOf(monthName.toUpperCase()).getValue();
                // If it's not the current month, do not allow updates
                if (monthValue != LocalDate.now().getMonthValue()) {
                    UtilityMethods.showPopupWarning("Cannot update past budget");
                    return;
                }
            }
            // Double-click
            if (event.getClickCount() == 2) {
                limitLabel.setVisible(false);
                limitTextField.setVisible(true);
                limitTextField.requestFocus();
            }
        });

        limitTextField.setOnAction(_ -> commitLimitEdit(limitTextField, limitLabel, item));
        limitTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitLimitEdit(limitTextField, limitLabel, item);
            }
        });

        detailsBox.getChildren().addAll(categoryLabel, limitLabel, limitTextField);

        // --- progress bar setup ---
        ProgressBar progressBar = new ProgressBar();

        // Compute a fraction in [0,1]
        double progress = item.getLimit() > 0 ? (item.getSpend() / item.getLimit()) : 0.0;
        // Keep it capped at 1.0 so you don't get weird overflows if spending exceeds limit
        progress = Math.max(0, Math.min(1, progress));

        progressBar.setProgress(progress);
        progressBar.setPrefWidth(250);

        // Decide the color
        Color barColor;
        if (item.getSpend() == 0) {
            barColor = Color.GRAY;
        } else {
            barColor = (item.getSpend() <= item.getLimit()) ? Color.LIGHTGREEN : Color.LIGHTCORAL;
        }
        progressBar.setStyle("-fx-accent: " + barColor.toString().replace("0x", "#") + ";");

        // --- percentage label ---
        // IMPORTANT: Only one placeholder in the format string, so pass just one value: progress * 100
        Label spendLabel = new Label(String.format("%.0f%%", progress * 100));
        spendLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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

    private void populateMonthYearComboBox(LocalDate signupDate) {
        ObservableList<String> monthYearList = FXCollections.observableArrayList();
        LocalDate currentDate = LocalDate.now();

        LocalDate date = signupDate.withDayOfMonth(1);
        while (!date.isAfter(currentDate)) {
            String monthYear = date.getMonth() + " " + date.getYear();
            monthYearList.add(monthYear);
            date = date.plusMonths(1);
        }

        monthYearComboBox.setItems(monthYearList);
        monthYearComboBox.getSelectionModel().selectFirst();
    }

    private void navigateToPreviousMonth() {
        currentDate = currentDate.minusMonths(1);
        SessionManager.setSelectedDate(currentDate);
        updateData();
    }

    private void navigateToNextMonth() {
        currentDate = currentDate.plusMonths(1);
        SessionManager.setSelectedDate(currentDate);
        updateData();
    }

    private void updateData() {
        String selectedMonthYear = currentDate.getMonth() + " " + currentDate.getYear();
        monthYearComboBox.getSelectionModel().select(selectedMonthYear);

        loadUserTotals();
        loadBudgetTableData();
    }

    private void updateDataBasedOnSelection() {
        String selectedMonthYear = monthYearComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonthYear != null) {
            String[] parts = selectedMonthYear.split(" ");
            String monthName = parts[0];
            int year = Integer.parseInt(parts[1]);

            int monthValue = Month.valueOf(monthName.toUpperCase()).getValue();
            currentDate = LocalDate.of(year, monthValue, 1);

            SessionManager.setSelectedDate(currentDate);

            loadUserTotals();
            loadBudgetTableData();
        }
    }

    private void initializeTotalsTable() {
        totalsCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        totalsAmountIncomeColumn.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        totalsAmountIncomeColumn.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("income-cell", "expense-cell");
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item >= 0) {
                        setText(String.format("+$%.2f", item));
                        if (!getStyleClass().contains("income-cell")) {
                            getStyleClass().add("income-cell");
                        }
                    } else {
                        setText(String.format("-$%.2f", Math.abs(item)));
                        if (!getStyleClass().contains("expense-cell")) {
                            getStyleClass().add("expense-cell");
                        }
                    }
                }
            }
        });
        totalsTable.setItems(totalList);
    }

    private void setButtonActions() {
        budgetMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("AddBudget", () -> {
            loadBudgetTableData();
            loadUserTotals();
        }));
        transactionMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("AddTransaction", () -> {
            loadBudgetTableData();
            loadUserTotals();
            loadMonthlyLeftover();
        }));

        groupMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("AddGroup"));
        groupMenuItem2.setOnAction(_ -> {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setIconified(true);

            // UtilityMethods.switchToScene("ManageGroup");
            UtilityMethods.switchToScene("GroupSelection");
        });
        todoMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("ManageTodoList"));
        todoMenuItem2.setOnAction(_ -> UtilityMethods.switchToScene("UserViewTodo"));
        profileMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("ViewProfile"));
        chartMenuItem.setOnAction(_ -> UtilityMethods.switchToScene("UserDashboardChart"));
        logoutBtn.setOnAction(_ -> {
            UtilityMethods.switchToScene(logoutBtn, "Login");
            UtilityMethods.showPopup("Successfully logged out!");
        });
    }

    private void loadUserTotals() {
        Connection connection;
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        try {
            connection = DBConnection.getConnection();

            int currentMonth = currentDate.getMonthValue();
            int currentYear = currentDate.getYear();

            String query = "SELECT " +
                    "C.name AS category, " +
                    "T.date AS date, " +
                    "SUM(CASE WHEN T.type = 'INCOME' THEN T.amount ELSE 0 END) - " +
                    "SUM(CASE WHEN T.type = 'EXPENSE' THEN T.amount ELSE 0 END) AS net_amount " +
                    "FROM TRANSACTION T " +
                    "LEFT JOIN CATEGORY C ON T.category_id = C.category_id " +
                    "WHERE T.user_id = ? AND MONTH(T.date) = ? AND YEAR(T.date) = ? AND T.group_id IS NULL " +
                    "GROUP BY T.category_id, T.date";
            // "ORDER BY T.date DESC;";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, SessionManager.getCurrentUserId());
            preparedStatement.setInt(2, currentMonth);
            preparedStatement.setInt(3, currentYear);

            resultSet = preparedStatement.executeQuery();

            ObservableList<UserCategoryTotals> data = FXCollections.observableArrayList();

            if (!resultSet.isBeforeFirst()) {
                preparedStatement.close();
                resultSet.close();

                LocalDate previousMonthDate = currentDate.minusMonths(1);
                int previousMonth = previousMonthDate.getMonthValue();
                int previousYear = previousMonthDate.getYear();

                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, SessionManager.getCurrentUserId());
                preparedStatement.setInt(2, previousMonth);
                preparedStatement.setInt(3, previousYear);

                resultSet = preparedStatement.executeQuery();
            }

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                String date = resultSet.getString("date");
                double netAmount = resultSet.getDouble("net_amount");

                data.add(new UserCategoryTotals(category, date, netAmount));
            }

            totalsCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            totalsAmountIncomeColumn.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
            totalsTable.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBudgetTableData() {
        Connection connection;
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        try {
            connection = DBConnection.getConnection();

            int currentMonth = currentDate.getMonthValue();
            int currentYear = currentDate.getYear();

            String query = "SELECT " +
                    "B.budget_id, " +
                    "BI.budget_item_id, " +
                    "C.name AS category, " +
                    "BI.limit_amount AS budget_limit, " +
                    "COALESCE(SUM(CASE WHEN T.type = 'EXPENSE' THEN T.amount ELSE 0 END), 0) AS total_spend " +
                    "FROM BUDGET B " +
                    "INNER JOIN BUDGET_ITEM BI ON B.budget_id = BI.budget_id " +
                    "INNER JOIN CATEGORY C ON BI.category_id = C.category_id " +
                    "LEFT JOIN TRANSACTION T ON T.category_id = BI.category_id " +
                    "AND T.user_id = B.user_id " +
                    "AND MONTH(T.date) = B.month " +
                    "AND YEAR(T.date) = B.year " +
                    "AND (T.group_id IS NULL OR T.group_id = B.group_id) " +
                    "WHERE B.user_id = ? AND B.month = ? AND B.year = ? " +
                    "AND B.group_id IS NULL " +
                    "GROUP BY BI.budget_item_id, C.name, BI.limit_amount, B.budget_id";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, SessionManager.getCurrentUserId());
            preparedStatement.setInt(2, currentMonth);
            preparedStatement.setInt(3, currentYear);

            resultSet = preparedStatement.executeQuery();

            ObservableList<UserBudgetData> budgetData = FXCollections.observableArrayList();

            if (!resultSet.isBeforeFirst()) {
                preparedStatement.close();
                resultSet.close();

                LocalDate previousMonthDate = currentDate.minusMonths(1);
                int previousMonth = previousMonthDate.getMonthValue();
                int previousYear = previousMonthDate.getYear();

                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, SessionManager.getCurrentUserId());
                preparedStatement.setInt(2, previousMonth);
                preparedStatement.setInt(3, previousYear);

                resultSet = preparedStatement.executeQuery();
            }

            while (resultSet.next()) {
                int budgetId = resultSet.getInt("budget_id");
                int budgetItemId = resultSet.getInt("budget_item_id");
                String category = resultSet.getString("category");
                double budgetLimit = resultSet.getDouble("budget_limit");
                double totalSpend = resultSet.getDouble("total_spend");

                budgetData.add(new UserBudgetData(budgetId, budgetItemId, category, budgetLimit, totalSpend));
            }
            budgetList.clear();
            budgetList.addAll(budgetData);
            budgetListView.setItems(budgetList);
        } catch (Exception e) {
            e.printStackTrace();
        }
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