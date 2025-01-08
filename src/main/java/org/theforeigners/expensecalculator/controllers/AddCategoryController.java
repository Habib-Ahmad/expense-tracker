package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.Category;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AddCategoryController implements Initializable {

    @FXML
    private Button addCategoryBtn;

    @FXML
    private Button cancelButton;

    @FXML
    private TextArea categoryDescription;

    @FXML
    private TextField categoryNameField;

    private ObservableList<Category> categoryData = FXCollections.observableArrayList();
    private HashMap<String, Integer> categoriesMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        addCategoryBtn.setOnAction(event -> handleAddCategory());
        cancelButton.setOnAction(event -> handleCloseButton());
    }

    private void loadCategories() {
        categoryData.clear();
        try {
            Connection connection = DBConnection.getConnection();
            String query = "SELECT category_id, name, description FROM CATEGORY";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                categoriesMap.put(name, id);

                Category category = new Category(name, description);
                categoryData.add(category);
            }

        } catch (SQLException e) {
            UtilityMethods.showPopupWarning("Database error: " + e.getMessage());
        }
    }

    private void handleAddCategory() {
        String categoryName = categoryNameField.getText();
        String description = categoryDescription.getText();

        if (categoryName.isEmpty()) {
            UtilityMethods.showPopupWarning("Please enter a category name.");
            return;
        }

        if (description.isEmpty()) {
            UtilityMethods.showPopupWarning("Please enter a description.");
            return;
        }

        if (categoriesMap.containsKey(categoryName)) {
            UtilityMethods.showPopupWarning("This category already exists!");
            return;
        }

        try {
            Connection connection = DBConnection.getConnection();
            String query = "INSERT INTO CATEGORY (name, description) VALUES (?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, categoryName);
            ps.setString(2, description);

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                UtilityMethods.showPopup("Category added successfully!");
                loadCategories();
                categoryNameField.clear();
                categoryDescription.clear();
            } else {
                UtilityMethods.showPopupWarning("Failed to add category.");
            }

        } catch (SQLException e) {
            UtilityMethods.showPopupWarning("Database error: " + e.getMessage());
        }
    }

    void handleCloseButton() {
        UtilityMethods.closeCurrentWindow(cancelButton);
    }
}
