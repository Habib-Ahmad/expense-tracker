package org.theforeigners.expensecalculator.utilities;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.util.regex.Pattern;

public class UtilityMethods {

    public static void switchToScene(Node node, String fxmlFile) {
        try {
            Stage stage = (Stage) node.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(UtilityMethods.class.getResource("/views/" + fxmlFile + ".fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.centerOnScreen();
            stage.setTitle("My Expense Tracker");
            stage.show();
            stage.setOnCloseRequest(_ -> {
                System.exit(0);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void switchToScene(String fxmlFile) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(UtilityMethods.class.getResource("/views/" + fxmlFile + ".fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.centerOnScreen();
            stage.setTitle("My Expense Tracker");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void switchToScene(String fxmlFile, Runnable onSuccessCallback) {
        try {
            FXMLLoader loader = new FXMLLoader(UtilityMethods.class.getResource("/views/" + fxmlFile + ".fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Friend Connections Graph");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            Object controller = loader.getController();
            if (controller instanceof CallbackHandler) {
                ((CallbackHandler) controller).setOnSuccessCallback(onSuccessCallback);
            }

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    public static void showPopup(String message) {
        Notifications.create()
                .title("Notification")
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(javafx.util.Duration.seconds(1.5))
                .darkStyle()
                .hideCloseButton()
                .showInformation();
    }

    public static void showPopupWarning(String message) {
        Notifications.create()
                .title("Warning")
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(javafx.util.Duration.seconds(1.5))
                .darkStyle()
                .hideCloseButton()
                .showError();
    }

    public static void closeCurrentWindow(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

}
