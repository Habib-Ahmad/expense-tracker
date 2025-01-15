package org.theforeigners.expensecalculator.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupSelectionController {

    @FXML
    private ComboBox<String> groupNameComboBox;

    @FXML
    private void initialize() {
        loadGroupNames();
        loadAccessibleGroupNames();
    }

    private void loadGroupNames() {
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT name FROM `groups` WHERE admin_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, SessionManager.getCurrentUserId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String groupName = rs.getString("name");
                if (!groupNameComboBox.getItems().contains(groupName)) {
                    groupNameComboBox.getItems().add(groupName);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAccessibleGroupNames() {
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT g.name " +
                    "FROM `groups` g " +
                    "JOIN GROUP_USERS gu ON g.group_id = gu.group_id " +
                    "WHERE gu.user_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, SessionManager.getCurrentUserId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String groupName = rs.getString("name");
                if (!groupNameComboBox.getItems().contains(groupName)) {
                    groupNameComboBox.getItems().add(groupName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getGroupIdByName(String groupName) {
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

    @FXML
    private void onSelectGroup() {
        String selectedGroup = groupNameComboBox.getValue();
        if (selectedGroup == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Group Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a group before proceeding.");
            alert.showAndWait();
        } else {
            int selectedGroupId = getGroupIdByName(selectedGroup);
            SessionManager.setCurrentGroupId(selectedGroupId);
            SessionManager.setCurrentGroupName(selectedGroup);
            System.out.println("Selected Group: " + selectedGroup);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Group Selected");
            alert.setHeaderText(null);
            alert.setContentText("You selected: " + selectedGroup);
            alert.showAndWait();
            Stage stage = (Stage) groupNameComboBox.getScene().getWindow();
            stage.close();
            UtilityMethods.switchToScene("ManageGroup");
        }
    }
}
