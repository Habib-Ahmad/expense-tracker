package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.User;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.*;
import java.util.Optional;

public class ManageUsersController {

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        ObservableList<String> roles = FXCollections.observableArrayList("Admin", "User");
        roleComboBox.setItems(roles);
        loadUsers();
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                usernameField.setText(newSelection.getName());
                emailField.setText(newSelection.getEmail());
                roleComboBox.getSelectionModel().select(newSelection.getRole());
            }
        });
    }

    private void loadUsers() {
        String query = "SELECT * FROM USERS where role = ?";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, "USER");
            ResultSet rs = stmt.executeQuery();

            usersList.clear();

            while (rs.next()) {
                String username = rs.getString("name");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String role = rs.getString("role");
                User user = new User(username, email, password, role);
                usersList.add(user);
            }

            usersTable.setItems(usersList);
        } catch (SQLException e) {
            UtilityMethods.showPopupWarning("Error loading users: " + e.getMessage());
        }
    }

    public void handleAddUser(ActionEvent event) {
        UtilityMethods.switchToScene("AddUser");
    }

    public void handleEditUser(ActionEvent event) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            usernameField.setText(selectedUser.getName());
            emailField.setText(selectedUser.getEmail());
        } else {
            showAlert("No user selected", "Please select a user to edit.", AlertType.WARNING);
        }
    }

    public void handleDeleteUser(ActionEvent event) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Are you sure you want to delete this user?");
            alert.setContentText("This action cannot be undone.");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                deleteUserFromDatabase(selectedUser);
                usersList.remove(selectedUser);
            }
        } else {
            showAlert("No user selected", "Please select a user to delete.", AlertType.WARNING);
        }
    }

    public void handleUpdateUser(ActionEvent event) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            String updatedUsername = usernameField.getText();
            String updatedPassword = passwordField.getText();
            String updatedRole = roleComboBox.getSelectionModel().getSelectedItem();

            String query = "UPDATE USERS SET name = ?, role= ?, password = ? WHERE name = ?";
            try {
                Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(query);

                ps.setString(1, updatedUsername);
                ps.setString(2, updatedRole);
                ps.setString(3, updatedPassword);
                ps.setString(4, selectedUser.getName());

                ps.executeUpdate();
                UtilityMethods.showPopup("User updated successfully.");
                loadUsers();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleResetFields(ActionEvent event) {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    private void deleteUserFromDatabase(User user) {
        String query = "DELETE FROM USERS WHERE name = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getName());
            ps.executeUpdate();
            UtilityMethods.showPopup("User deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
