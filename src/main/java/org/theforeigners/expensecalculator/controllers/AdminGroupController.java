package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.*;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.UserGroupRecord;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminGroupController implements Initializable {

    @FXML
    private TableView<UserGroupRecord> tableView;

    @FXML
    private TableColumn<UserGroupRecord, Integer> groupIdColumn;

    @FXML
    private TableColumn<UserGroupRecord, String> groupNameColumn;

    @FXML
    private TableColumn<UserGroupRecord, Integer> userIdColumn;

    @FXML
    private TableColumn<UserGroupRecord, String> userNameColumn;

    @FXML
    private PieChart groupDistributionChart;

    private final ObservableList<UserGroupRecord> userGroupData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableView();
        loadUserGroupData();
        loadGroupDistributionChart();
    }

    private void initializeTableView() {
        groupIdColumn.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        groupNameColumn.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));

        tableView.setItems(userGroupData);
    }

    private void loadUserGroupData() {
        String query = "SELECT G.group_id, G.name AS group_name, U.user_id, U.name AS user_name " +
                "FROM GROUPS G " +
                "LEFT JOIN GROUP_USERS GU ON G.group_id = GU.group_id " +
                "LEFT JOIN USERS U ON GU.user_id = U.user_id " +
                "ORDER BY G.group_id, U.user_id";

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int groupId = rs.getInt("group_id");
                String groupName = rs.getString("group_name");
                int userId = rs.getInt("user_id");
                String userName = rs.getString("user_name");

                userGroupData.add(new UserGroupRecord(groupId, groupName, userId, userName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGroupDistributionChart() {
        groupDistributionChart.setTitle("Group User Distribution");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        String query = "SELECT G.name AS group_name, COUNT(GU.user_id) AS user_count " +
                "FROM GROUPS G " +
                "LEFT JOIN GROUP_USERS GU ON G.group_id = GU.group_id " +
                "GROUP BY G.name";

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String groupName = rs.getString("group_name");
                int userCount = rs.getInt("user_count");

                pieChartData.add(new PieChart.Data(groupName, userCount));
            }

            groupDistributionChart.setData(pieChartData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
