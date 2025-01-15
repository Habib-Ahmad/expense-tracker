package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.utilities.SessionManager;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class UserChartController implements Initializable {

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private PieChart pieChart;

    @FXML
    private CategoryAxis categoryAxis;

    @FXML
    private NumberAxis numberAxis;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadBarChartData();
        loadPieChartData();
    }

    private void loadBarChartData() {
        barChart.setTitle("Spending vs Limit by Category");

        categoryAxis.setLabel("Category");
        numberAxis.setLabel("Amount");

        XYChart.Series<String, Number> spendingSeries = new XYChart.Series<>();
        spendingSeries.setName("Spend");

        XYChart.Series<String, Number> limitSeries = new XYChart.Series<>();
        limitSeries.setName("Limit");

        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT C.name AS category, " +
                    "SUM(CASE WHEN T.type = 'EXPENSE' THEN T.amount ELSE 0 END) AS total_spend, " +
                    "BI.limit_amount AS budget_limit " +
                    "FROM BUDGET B " +
                    "INNER JOIN BUDGET_ITEM BI ON B.budget_id = BI.budget_id " +
                    "INNER JOIN CATEGORY C ON BI.category_id = C.category_id " +
                    "LEFT JOIN TRANSACTION T ON T.category_id = C.category_id AND T.user_id = B.user_id AND MONTH(T.date) = B.month AND YEAR(T.date) = B.year "
                    +
                    "WHERE B.user_id = ? " +
                    "GROUP BY C.name, BI.limit_amount";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, SessionManager.getCurrentUserId());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double spend = resultSet.getDouble("total_spend");
                double limit = resultSet.getDouble("budget_limit");

                spendingSeries.getData().add(new XYChart.Data<>(category, spend));
                limitSeries.getData().add(new XYChart.Data<>(category, limit));
            }

            barChart.getData().addAll(spendingSeries, limitSeries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPieChartData() {
        pieChart.setTitle("Category Spending Distribution");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT C.name AS category, " +
                    "SUM(CASE WHEN T.type = 'EXPENSE' THEN T.amount ELSE 0 END) AS total_spend " +
                    "FROM TRANSACTION T " +
                    "INNER JOIN CATEGORY C ON T.category_id = C.category_id " +
                    "WHERE T.user_id = ? " +
                    "GROUP BY C.name";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, SessionManager.getCurrentUserId());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double spend = resultSet.getDouble("total_spend");

                pieChartData.add(new PieChart.Data(category, spend));
            }

            pieChart.setData(pieChartData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}