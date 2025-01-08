package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.*;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<UserCategoryTotals> totalsTable;

    @FXML
    private TableColumn<UserCategoryTotals, String> totalsDateColumn;

    @FXML
    private TableColumn<UserCategoryTotals, Double> totalsAmountIncomeColumn;

    @FXML
    private TableColumn<?, ?> totalsCategoryColumn;

    @FXML
    private Label usernameLbl;

    @FXML
    private ListView<UserBudgetData> budgetListView;

    private final ObservableList<UserBudgetData> budgetList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameLbl.setText(SessionManager.getCurrentUserName());
        logoutButton.setOnAction(event -> handleLogoutButton());
        setupTable();
        loadBudgets();
        loadTotals();
        initializeBudgetListView();
    }

    private void setupTable() {
        totalsCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        totalsDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        totalsAmountIncomeColumn.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        totalsAmountIncomeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("income-cell", "expense-cell");
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item >= 0) {
                        setText(String.format("$+%.2f", item));
                        if (!getStyleClass().contains("income-cell")) {
                            getStyleClass().add("income-cell");
                        }
                    } else {
                        setText(String.format("$%.2f", item));
                        if (!getStyleClass().contains("expense-cell")) {
                            getStyleClass().add("expense-cell");
                        }
                    }
                }
            }
        });
    }

    private void initializeBudgetListView() {
        budgetListView.setItems(budgetList);
        budgetListView.setCellFactory(listView -> new ListCell<>() {
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
        card.setSpacing(10);
        card.setStyle(
                "-fx-padding: 10; -fx-background-color: #f8f8f8; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefHeight(35);

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

        limitTextField.setOnAction(event -> commitLimitEdit(limitTextField, limitLabel, item));
        limitTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
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

    private void loadBudgets() {
        ObservableList<UserBudgetData> budgetData = FXCollections.observableArrayList();
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT C.name AS category, " +
                    "SUM(CASE WHEN T.type = 'EXPENSE' THEN T.amount ELSE 0 END) AS total_spend, " +
                    "SUM(DISTINCT CASE WHEN (T.split = 0 OR T.split IS NULL) THEN BI.limit_amount ELSE 0 END) AS total_limit "
                    +
                    "FROM CATEGORY C LEFT JOIN BUDGET_ITEM BI ON C.category_id = BI.category_id " +
                    "LEFT JOIN BUDGET B ON BI.budget_id = B.budget_id LEFT JOIN TRANSACTION T ON T.category_id = C.category_id "
                    +
                    "AND (T.user_id = B.user_id OR T.group_id = B.group_id) WHERE (T.split = 0 OR T.split IS NULL OR T.split = 1) GROUP BY C.name;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double spend = resultSet.getDouble("total_spend");
                double limit = resultSet.getDouble("total_limit");

                budgetData.add(new UserBudgetData(category, limit, spend));
            }
            budgetList.clear();
            budgetList.addAll(budgetData);
            budgetListView.setItems(budgetList);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadTotals() {
        List<UserCategoryTotals> totals = new ArrayList<>();
        String query = "SELECT c.name AS category, " +
                "t.date AS date, " +
                "SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) - " +
                "SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS net_amount " +
                "FROM TRANSACTION t " +
                "JOIN CATEGORY c ON t.category_id = c.category_id " +
                "GROUP BY c.name, t.date";

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String category = rs.getString("category");
                String date = rs.getString("date");
                double netAmount = rs.getDouble("net_amount");

                UserCategoryTotals total = new UserCategoryTotals(category, date, netAmount);
                totals.add(total);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        totalsTable.getItems().setAll(totals);
    }

    private void handleLogoutButton() {
        UtilityMethods.switchToScene(logoutButton, "Login");
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        UtilityMethods.switchToScene("ManageCategories");
    }

    @FXML
    private void handleViewAllUsers(ActionEvent event) {
        UtilityMethods.switchToScene("ManageUsers");
    }

    @FXML
    private void handleViewTodo(ActionEvent event) {
        UtilityMethods.switchToScene("ViewTodo");
    }

    @FXML
    private void handleViewGroup(ActionEvent event) {
        UtilityMethods.switchToScene("AdminGroupView");
    }

    @FXML
    private void handleViewChart(ActionEvent event) {
        UtilityMethods.switchToScene("AdminChartView");
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
