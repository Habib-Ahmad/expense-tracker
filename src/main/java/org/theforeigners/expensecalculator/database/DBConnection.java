package org.theforeigners.expensecalculator.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/new_schema";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connection established to MySQL database");
            } catch (SQLException | ClassNotFoundException e) {
                System.err.println("MySQL DBConnection failed: " + e.getMessage());
            }
        }
        return connection;
    }
}
