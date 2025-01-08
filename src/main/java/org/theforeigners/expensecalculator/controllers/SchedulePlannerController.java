package org.theforeigners.expensecalculator.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.theforeigners.expensecalculator.database.DBConnection;
import org.theforeigners.expensecalculator.utilities.SessionManager;
import org.theforeigners.expensecalculator.utilities.UtilityMethods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class SchedulePlannerController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthYearLabel;

    @FXML
    private Button prevMonthButton, nextMonthButton, saveNotesButton, openSettingsButton;

    @FXML
    private TextArea notesTextArea;

    private YearMonth currentMonth;
    private Map<LocalDate, String> notesMap;
    private LocalDate selectedDate;

    private String weekdayColor = "#000000";
    private String weekendColor = "#FF0000";
    private String selectedDateColor = "#ADD8E6";
    private String dateWithNoteColor = "#FFECD1";
    private int fontSize = 20;
    private boolean isDarkMode = false;

    @FXML
    public void initialize() {
        currentMonth = YearMonth.now();
        notesMap = new HashMap<>();
        updateCalendar();
        loadNotesForCurrentUser();

        prevMonthButton.setOnAction(event -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });

        nextMonthButton.setOnAction(event -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });

        saveNotesButton.setOnAction(event -> {
            if (selectedDate != null) {
                saveNotesToDatabase(selectedDate, notesTextArea.getText());
                UtilityMethods.showPopup("Note saved successfully!");
            }
        });

        openSettingsButton.setOnAction(event -> openSettingsPopup());
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        monthYearLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());

        String[] weekdays = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int i = 0; i < weekdays.length; i++) {
            Label weekdayLabel = new Label(weekdays[i]);
            weekdayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: " + fontSize + "px;");
            if (i == 0 || i == 6) {
                weekdayLabel.setStyle(weekdayLabel.getStyle() + "-fx-text-fill: " + weekendColor + ";");
            } else {
                weekdayLabel.setStyle(weekdayLabel.getStyle() + "-fx-text-fill: " + weekdayColor + ";");
            }
            calendarGrid.add(weekdayLabel, i, 0);
        }

        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            StackPane cell = new StackPane();
            enableDragAndDrop(cell, date);
            String defaultStyle = "-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: " +
                    (isDarkMode ? "#444444;" : "#f9f9f9;");
            cell.setStyle(defaultStyle);

            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-weight: bold;");

            if (date.getDayOfWeek().getValue() % 7 == 0 || date.getDayOfWeek().getValue() % 7 == 6) { // Weekend
                dayLabel.setStyle(dayLabel.getStyle() + "-fx-text-fill: " + weekendColor + ";");
            } else {
                dayLabel.setStyle(dayLabel.getStyle() + "-fx-text-fill: " + weekdayColor + ";");
            }

            if (notesMap.containsKey(date)) {
                cell.setStyle(cell.getStyle() + "-fx-background-color: " + dateWithNoteColor + ";");
                Tooltip tooltip = new Tooltip("Day: " + date.getDayOfWeek().name() + "\nNote: " + notesMap.get(date));
                Tooltip.install(cell, tooltip);
            } else {
                Tooltip tooltip = new Tooltip("Day: " + date.getDayOfWeek().name());
                Tooltip.install(cell, tooltip);
            }

            cell.setOnMouseEntered(event -> cell.setStyle(cell.getStyle() + "-fx-background-color: #d3d3f7;"));

            cell.setOnMouseExited(event -> {
                if (selectedDate != null && selectedDate.equals(date)) {
                    cell.setStyle(defaultStyle + "-fx-background-color: " + selectedDateColor + ";");
                } else {
                    cell.setStyle(defaultStyle + "-fx-background-color: " +
                            (notesMap.containsKey(date) ? dateWithNoteColor : (isDarkMode ? "#444444;" : "#f9f9f9;")));
                }
            });

            cell.setOnMouseClicked(event -> {
                if (selectedDate != null) {
                    updateCalendar();
                }
                selectedDate = date;
                cell.setStyle(defaultStyle + "-fx-background-color: " + selectedDateColor + ";");
                notesTextArea.setText(notesMap.getOrDefault(date, ""));
            });

            cell.getChildren().add(dayLabel);
            calendarGrid.add(cell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private void openSettingsPopup() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initOwner(openSettingsButton.getScene().getWindow());
        settingsStage.setTitle("Settings");

        VBox settingsBox = new VBox(10);
        settingsBox.setStyle("-fx-padding: 20;");

        Label lightDarkLabel = new Label("Theme:");
        ToggleGroup themeGroup = new ToggleGroup();
        RadioButton lightModeButton = new RadioButton("Light");
        lightModeButton.setToggleGroup(themeGroup);
        lightModeButton.setSelected(!isDarkMode);
        RadioButton darkModeButton = new RadioButton("Dark");
        darkModeButton.setToggleGroup(themeGroup);
        darkModeButton.setSelected(isDarkMode);

        lightModeButton.setOnAction(event -> {
            isDarkMode = false;
            updateCalendar();
        });

        darkModeButton.setOnAction(event -> {
            isDarkMode = true;
            updateCalendar();
        });

        Label fontSizeLabel = new Label("Font Size:");
        Spinner<Integer> fontSizeSpinner = new Spinner<>(12, 36, fontSize);
        fontSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            fontSize = newVal;
            updateCalendar();
        });

        Label weekdayColorLabel = new Label("Weekday Color:");
        ColorPicker weekdayColorPicker = new ColorPicker(Color.valueOf(weekdayColor));
        weekdayColorPicker.setOnAction(event -> {
            weekdayColor = toHexString(weekdayColorPicker.getValue());
            updateCalendar();
        });

        Label weekendColorLabel = new Label("Weekend Color:");
        ColorPicker weekendColorPicker = new ColorPicker(Color.valueOf(weekendColor));
        weekendColorPicker.setOnAction(event -> {
            weekendColor = toHexString(weekendColorPicker.getValue());
            updateCalendar();
        });

        Label selectedDateColorLabel = new Label("Selected Date Color:");
        ColorPicker selectedDateColorPicker = new ColorPicker(Color.valueOf(selectedDateColor));
        selectedDateColorPicker.setOnAction(event -> {
            selectedDateColor = toHexString(selectedDateColorPicker.getValue());
            updateCalendar();
        });

        Label dateWithNoteColorLabel = new Label("Date With Note Color:");
        ColorPicker dateWithNoteColorPicker = new ColorPicker(Color.valueOf(dateWithNoteColor));
        dateWithNoteColorPicker.setOnAction(event -> {
            dateWithNoteColor = toHexString(dateWithNoteColorPicker.getValue());
            updateCalendar();
        });

        settingsBox.getChildren().addAll(
                lightDarkLabel, lightModeButton, darkModeButton,
                fontSizeLabel, fontSizeSpinner,
                weekdayColorLabel, weekdayColorPicker,
                weekendColorLabel, weekendColorPicker,
                selectedDateColorLabel, selectedDateColorPicker,
                dateWithNoteColorLabel, dateWithNoteColorPicker);

        Scene scene = new Scene(settingsBox, 350, 450);
        settingsStage.setScene(scene);
        settingsStage.centerOnScreen();
        settingsStage.show();
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void enableDragAndDrop(StackPane cell, LocalDate date) {
        cell.setOnDragDetected(event -> {
            Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(date.toString());
            db.setContent(content);
            event.consume();
        });

        cell.setOnDragOver(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        cell.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String droppedDate = db.getString();
                notesMap.put(date, notesMap.remove(LocalDate.parse(droppedDate)));
                updateCalendar();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void loadNotesForCurrentUser() {
        notesMap.clear();
        int currentUserId = SessionManager.getCurrentUserId();

        String query = "SELECT note_date, content FROM NOTES WHERE user_id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, currentUserId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                LocalDate noteDate = resultSet.getDate("note_date").toLocalDate();
                String content = resultSet.getString("content");
                notesMap.put(noteDate, content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCalendar();
    }

    private void saveNotesToDatabase(LocalDate date, String note) {
        int currentUserId = SessionManager.getCurrentUserId();
        String insertOrUpdateQuery = "INSERT INTO NOTES (user_id, note_date, content) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE content = VALUES(content)";
        String deleteQuery = "DELETE FROM NOTES WHERE user_id = ? AND note_date = ?";

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = note.isEmpty()
                    ? connection.prepareStatement(deleteQuery)
                    : connection.prepareStatement(insertOrUpdateQuery);

            if (note.isEmpty()) {
                statement.setInt(1, currentUserId);
                statement.setDate(2, java.sql.Date.valueOf(date));
            } else {
                statement.setInt(1, currentUserId);
                statement.setDate(2, java.sql.Date.valueOf(date));
                statement.setString(3, note);
            }
            statement.executeUpdate();

            if (note.isEmpty()) {
                notesMap.remove(date);
            } else {
                notesMap.put(date, note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCalendar();
    }
}
