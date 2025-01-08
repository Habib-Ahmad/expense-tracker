package org.theforeigners.expensecalculator.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DeleteGroupController {

    @FXML
    private ComboBox<String> groupComboBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    private HashMap<String, Integer> groupsMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadGroupNames();

        cancelButton.setOnAction(e -> closeWindow());
        confirmButton.setOnAction(e -> deleteGroup());
    }

    private void loadGroupNames() {
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT group_id , name FROM GROUPS";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                groupComboBox.getItems().add(rs.getString("name"));
                int group_id = rs.getInt("group_id");
                groupsMap.put(rs.getString("name"), group_id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean verifyGroupAdmin(String groupName) {
        boolean admin = false;
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT admin_id FROM GROUPS where name= ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int adminId = rs.getInt("admin_id");
                admin = adminId == SessionManager.getCurrentUserId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return admin;
    }

    private void deleteGroup() {
        String selectedGroup = groupComboBox.getValue();
        if (selectedGroup == null || selectedGroup.isEmpty()) {
            UtilityMethods.showPopupWarning("Selected any group to delete");
            return;
        }
        if (!verifyGroupAdmin(selectedGroup)) {
            UtilityMethods.showPopupWarning("Permission denied! Only admin can delete group");
            return;
        }
        int group_id = groupsMap.get(selectedGroup);

        try {
            Connection connection = DBConnection.getConnection();
            String groupUsersDeleteQuery = "DELETE FROM GROUP_USERS WHERE group_id =?";
            PreparedStatement statement = connection.prepareStatement(groupUsersDeleteQuery);
            statement.setInt(1, group_id);
            statement.executeUpdate();

            String deleteQuery = "DELETE FROM GROUPS WHERE name = ?";
            PreparedStatement deletePs = connection.prepareStatement(deleteQuery);
            deletePs.setString(1, selectedGroup);
            deletePs.executeUpdate();

            UtilityMethods.showPopup("Group deleted successfully!");
            groupComboBox.getItems().remove(selectedGroup);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
