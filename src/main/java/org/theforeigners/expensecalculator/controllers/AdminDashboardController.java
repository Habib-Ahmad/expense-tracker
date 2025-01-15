package org.theforeigners.expensecalculator.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private Button logoutButton;

    @FXML
    private Label usernameLbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameLbl.setText(SessionManager.getCurrentUserName());
        logoutButton.setOnAction(_ -> handleLogoutButton());
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
    private void handleViewGroup(ActionEvent event) {
        UtilityMethods.switchToScene("AdminGroupView");
    }

    @FXML
    private void handleViewChart(ActionEvent event) {
        UtilityMethods.switchToScene("AdminChartView");
    }

}
