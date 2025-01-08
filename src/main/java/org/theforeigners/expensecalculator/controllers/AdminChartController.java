package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import org.theforeigners.expensecalculator.database.DBConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class AdminChartController implements Initializable {

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
                    "FROM CATEGORY C " +
                    "LEFT JOIN TRANSACTION T ON T.category_id = C.category_id " +
                    "GROUP BY C.name";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
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
