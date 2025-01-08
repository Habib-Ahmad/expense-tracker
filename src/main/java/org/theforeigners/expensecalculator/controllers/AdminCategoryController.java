package org.theforeigners.expensecalculator.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.modal.Category;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AdminCategoryController implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private TextField categoryNameField;

    @FXML
    private TextArea categoryDescription;

    @FXML
    private Button addCategoryBtn;

    @FXML
    private Button clearButton;

    @FXML
    private Button editCategoryBtn;

    @FXML
    private Button deleteCategoryBtn;

    @FXML
    private TableView<Category> categoryTableView;

    @FXML
    private TableColumn<Category, String> categoryNameColumn;

    @FXML
    private TableColumn<Category, String> categoryDescriptionColumn;

    private ObservableList<Category> categoryData = FXCollections.observableArrayList();
    private HashMap<String, Integer> categoriesMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
    }

    private void setupTable() {
        loadCategories();

        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        addCategoryBtn.setOnAction(event -> handleAddCategory());
        editCategoryBtn.setOnAction(event -> handleEditCategory());
        deleteCategoryBtn.setOnAction(event -> handleDeleteCategory());
        clearButton.setOnAction(event -> handleResetFields());

        cancelButton.setOnAction(event -> {
            categoryNameField.getScene().getWindow().hide();
        });
        categoryTableView.setItems(categoryData);

        categoryTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                categoryNameField.setText(newSelection.getName());
                categoryDescription.setText(newSelection.getDescription());
            }
        });
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

    private void handleEditCategory() {
        Category selectedCategory = categoryTableView.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            UtilityMethods.showPopupWarning("Please select a category to edit.");
            return;
        }

        String categoryName = categoryNameField.getText();
        String description = categoryDescription.getText();

        if (categoryName.isEmpty() || description.isEmpty()) {
            UtilityMethods.showPopupWarning("Please enter both category name and description.");
            return;
        }

        try {
            Connection connection = DBConnection.getConnection();
            String query = "UPDATE CATEGORY SET name = ?, description = ? WHERE category_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, categoryName);
            ps.setString(2, description);
            ps.setInt(3, categoriesMap.get(selectedCategory.getName()));

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                UtilityMethods.showPopup("Category updated successfully!");
                loadCategories();
                categoryNameField.clear();
                categoryDescription.clear();
            } else {
                UtilityMethods.showPopupWarning("Failed to update category.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteCategory() {
        Category selectedCategory = categoryTableView.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            UtilityMethods.showPopupWarning("Please select a category to delete.");
            return;
        }

        try {
            Connection connection = DBConnection.getConnection();
            String query = "DELETE FROM CATEGORY WHERE category_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, categoriesMap.get(selectedCategory.getName()));

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                UtilityMethods.showPopup("Category deleted successfully!");
                loadCategories();
            } else {
                UtilityMethods.showPopupWarning("Failed to delete category.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleResetFields() {
        categoryNameField.clear();
        categoryDescription.clear();
    }
}
