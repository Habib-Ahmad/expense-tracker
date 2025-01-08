module org.theforeigners.expensecalculator {
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires fontawesomefx;
    requires mysql.connector.j;
    requires javafx.controls;
    requires transitive javafx.graphics;

    opens org.theforeigners.expensecalculator.controllers to javafx.fxml;

    exports org.theforeigners.expensecalculator.controllers;

    opens org.theforeigners.expensecalculator.modal to javafx.base;

    exports org.theforeigners.expensecalculator.main;

    opens org.theforeigners.expensecalculator.main to javafx.fxml;
}