package org.theforeigners.expensecalculator.controllers;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.User;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.*;
import java.util.HashSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AddGroupController {

    @FXML
    private Button cancelButton;

    @FXML
    private TextField groupNameField;

    @FXML
    private Button removeButton;

    @FXML
    private Button saveButton;

    @FXML
    private TextField usernameField;

    @FXML
    private Button searchButton;

    @FXML
    private ListView<User> userListView;

    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<User> selectedUsersList = FXCollections.observableArrayList();
    private HashSet<User> selectedUsers = new HashSet<>();

    public void initialize() {
        cancelButton.setOnAction(event -> handleCancelButtonAction());
        //loadUserData();

        userListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        removeButton.setOnAction(event -> handleRemoveUserButtonAction());
        saveButton.setOnAction(event -> handleSaveButtonAction());
        searchButton.setOnAction(event -> handleSearchButtonAction());

        userListView.setCellFactory(param -> new ListCell<>() {
            private Button removeButton = GlyphsDude.createIconButton(FontAwesomeIcons.REMOVE, "", "20", "0",
                    ContentDisplay.LEFT);

            {
                removeButton
                        .setStyle("-fx-background-color: #ff0000; -fx-text-fill: #ffffff; -fx-border-color: #ff0000;");
                removeButton.setMaxWidth(15);
                removeButton.setMaxHeight(10);
                removeButton.setPrefWidth(15);
                removeButton.setPrefHeight(8);
                removeButton.setOnAction(event -> {
                    User user = getItem();
                    if (user != null) {
                        selectedUsersList.remove(user);
                        userData.remove(user);
                        userListView.getItems().remove(user);
                        UtilityMethods.showPopup("User removed from the group: " + user.getName());
                    }
                });
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                    setText(user.getName());
                }
            }
        });

        userListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

    }

    private void handleCancelButtonAction() {
        UtilityMethods.closeCurrentWindow(cancelButton);
    }

    private void handleRemoveUserButtonAction() {
        User user = userListView.getSelectionModel().getSelectedItem();
        if (user != null) {
            selectedUsersList.remove(user);
            userData.remove(user);
            userListView.getItems().remove(user);
            UtilityMethods.showPopup("User removed from the group!: " + user.getName());
        }
    }

    private void handleSaveButtonAction() {
        String groupName = groupNameField.getText().trim();
        if (groupName.isEmpty()) {
            UtilityMethods.showPopup("Group name is empty!");
            return;
        }

        if (isGroupNameExists(groupName)) {
            UtilityMethods.showPopup("Group name already exists! Please choose another name.");
            return;
        }
        if (userListView.getItems().isEmpty()) {
            UtilityMethods.showPopup("At-least add 1 member to create a group");
            return;
        }

        saveGroupToDb(groupName);
        UtilityMethods.showPopup("Group saved successfully!");
        userListView.getItems().clear();
        groupNameField.clear();
        selectedUsersList.clear();
        userData.clear();
    }

    private void saveGroupToDb(String groupName) {
        int adminId = SessionManager.getCurrentUserId();
        String insertGroupQuery = "INSERT INTO `groups` (name, admin_id) VALUES (?, ?)";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(insertGroupQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, groupName);
            ps.setInt(2, adminId);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int groupId = rs.getInt(1);
                    insertUserToGroup(groupId, adminId);
                    SessionManager.setCurrentGroupId(groupId);
                    SessionManager.setCurrentGroupName(groupName);
                    for (User user : userListView.getItems()) {
                        int userId = user.getId();
                        insertUserToGroup(groupId, userId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertUserToGroup(int groupId, int userId) {
        String insertUserQuery = "INSERT INTO GROUP_USERS (group_id, user_id) VALUES (?, ?)";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(insertUserQuery);
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            UtilityMethods.showPopup("Error saving group:\n" + e.getMessage());
        }
    }

    private void loadUserData() {
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT user_id, name, email FROM USERS WHERE role = ? AND user_id != ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, "USER");
            ps.setInt(2, SessionManager.getCurrentUserId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String name = rs.getString("name");
                String email = rs.getString("email");

                User user = new User(userId, name, email);
                userData.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        userListView.setItems(userData);
    }

    private void handleSearchButtonAction() {
        String searchText = usernameField.getText().trim();
        if (!searchText.isEmpty()) {
            searchUsers(searchText);
        } else {
            UtilityMethods.showPopup("Enter username to search");
            //loadUserData();
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
                if (!userData.contains(user) && !selectedUsersList.contains(user)) {
                    userData.add(user);
                    selectedUsersList.add(user);
                }
            } else {
                UtilityMethods.showPopup("User not found!");
            }
            userListView.setItems(selectedUsersList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isGroupNameExists(String groupName) {
        String query = "SELECT COUNT(*) FROM `groups` WHERE name = ?";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
