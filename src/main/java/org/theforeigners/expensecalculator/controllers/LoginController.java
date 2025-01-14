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
import java.time.LocalDate;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signUpBtn;

    @FXML
    private Button loginButton;

    @FXML
    private FontAwesomeIcon viewPasswordLbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        signUpBtn.setOnAction(_ -> handleSignUp());
        viewPasswordLbl.setOnMouseClicked(_ -> showPassword());
        passwordField.setOnAction(_ -> handleLogin());
        loginButton.setOnAction(_ -> handleLogin());
    }

    public void showPassword() {
        if (passwordField.getText().isEmpty() || passwordField.getText().isBlank()) {
            return;
        }
        UtilityMethods.showPopup("Your Typed password : " + passwordField.getText());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            UtilityMethods.showPopupWarning("Please fill in all fields");
            return;
        }
        if (!UtilityMethods.isValidEmail(email)) {
            UtilityMethods.showPopupWarning("Invalid email address");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT user_id, name, email, role, created_at FROM users WHERE email = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, email);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        String name = rs.getString("name");
                        String userEmail = rs.getString("email");
                        String role = rs.getString("role");
                        LocalDate createdAt = rs.getDate("created_at").toLocalDate();

                        SessionManager.setCurrentUserId(userId);
                        SessionManager.setCurrentUserName(name);
                        SessionManager.setCurrentUserEmail(userEmail);
                        SessionManager.setCurrentUserCreatedAt(createdAt);

                        boolean isAdmin = role.equalsIgnoreCase("ADMIN");

                        if (isAdmin) {
                            UtilityMethods.showPopup("Welcome to Admin Dashboard");
                            UtilityMethods.switchToScene(signUpBtn, "AdminDashboard");
                        } else {
                            UtilityMethods.showPopup("Welcome to User Dashboard");
                            UtilityMethods.switchToScene(signUpBtn, "UserDashboard");
                        }
                    } else {
                        UtilityMethods.showPopupWarning("Invalid email or password.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            UtilityMethods.showPopupWarning("Error during login: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignUp() {
        UtilityMethods.switchToScene(signUpBtn, "Register");
    }
}
