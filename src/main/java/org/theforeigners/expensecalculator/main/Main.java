package org.theforeigners.expensecalculator.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/Login.fxml")));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(String.valueOf(getClass().getResource("/style/styles.css")));
            stage.setTitle("My Expense Tracker");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
