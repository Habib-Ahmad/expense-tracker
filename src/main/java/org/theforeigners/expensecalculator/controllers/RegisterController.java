package org.theforeigners.expensecalculator.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private Button loginBtn;

    @FXML
    private TextField nameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signupBtn;

    @FXML
    private FontAwesomeIcon viewPasswordLbl;

    @FXML
    private FontAwesomeIcon viewPasswordLbl2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        signupBtn.setOnAction(_ -> handleSignUp());
        viewPasswordLbl.setOnMouseClicked(_ -> showPassword());
        viewPasswordLbl2.setOnMouseClicked(_ -> showPassword2());
        loginBtn.setOnAction(_ -> handleLogin());
    }

    public void handleSignUp() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            UtilityMethods.showPopupWarning("Pleas Fill all Fields");
            return;
        }
        if (!UtilityMethods.isValidEmail(email)) {
            UtilityMethods.showPopupWarning("Invalid Email");
            return;
        }

        if (!password.equals(confirmPassword)) {
            UtilityMethods.showPopupWarning("Passwords do not match");
            return;
        }
        if (password.length() < 6) {
            UtilityMethods.showPopupWarning("Password must be at least 6 characters long");
            return;
        }

        try {
            Connection connection = DBConnection.getConnection();
            if (connection == null || connection.isClosed()) {
                UtilityMethods.showPopupWarning("Failed to connect to the database.");
                return;
            }

            String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            try {
                PreparedStatement psCheck = connection.prepareStatement(checkEmailQuery);
                psCheck.setString(1, email);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        UtilityMethods.showPopupWarning("Email is already registered.");
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String query = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, "USER");

                int rowsInserted = ps.executeUpdate();
                if (rowsInserted > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int userId = rs.getInt(1);
                            SessionManager.setCurrentUserId(userId);
                            SessionManager.setCurrentUserName(name);
                            SessionManager.setCurrentUserEmail(email);

                            UtilityMethods.showPopup("Registration successful! Welcome to your dashboard.");
                            UtilityMethods.switchToScene(signupBtn, "UserDashboard");
                            clearFields();
                        }
                    }
                } else {
                    UtilityMethods.showPopupWarning("Registration failed. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showPassword() {
        if (passwordField.getText().isEmpty() || passwordField.getText().isBlank()) {
            return;
        }
        UtilityMethods.showPopup("Your Typed password : " + passwordField.getText());
    }

    public void showPassword2() {
        if (confirmPasswordField.getText().isEmpty() || confirmPasswordField.getText().isBlank()) {
            return;
        }
        UtilityMethods.showPopup("Your Typed password : " + confirmPasswordField.getText());
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleLogin() {
        UtilityMethods.switchToScene(loginBtn, "Login");
    }

}
