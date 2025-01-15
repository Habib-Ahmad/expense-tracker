package org.theforeigners.expensecalculator.utilities;

import java.time.LocalDate;

public class SessionManager {
    private static int CurrentUserId;
    private static String CurrentUserEmail;
    private static String CurrentUserName;
    private static int CurrentGroupId;
    private static String CurrentGroupName;
    private static LocalDate CurrentUserCreatedAt;
    private static LocalDate selectedDate;

    public static void setSelectedDate(LocalDate date) { selectedDate = date;}

    public static LocalDate getSelectedDate() { return selectedDate; }

    public static int getCurrentUserId() {
        return CurrentUserId;
    }

    public static void setCurrentUserId(int currentUserId) {
        CurrentUserId = currentUserId;
    }

    public static String getCurrentUserEmail() {
        return CurrentUserEmail;
    }

    public static void setCurrentUserEmail(String currentUserEmail) {
        CurrentUserEmail = currentUserEmail;
    }

    public static String getCurrentUserName() {
        return CurrentUserName;
    }

    public static void setCurrentUserName(String currentUserName) {
        CurrentUserName = currentUserName;
    }

    public static int getCurrentGroupId() {
        return CurrentGroupId;
    }

    public static void setCurrentGroupId(int currentGroupId) {
        CurrentGroupId = currentGroupId;
    }

    public static String getCurrentGroupName() {
        return CurrentGroupName;
    }

    public static void setCurrentGroupName(String currentGroupName) {
        CurrentGroupName = currentGroupName;
    }

    public static LocalDate getCurrentUserCreatedAt() {
        return CurrentUserCreatedAt;
    }

    public static void setCurrentUserCreatedAt(LocalDate currentUserCreatedAt) {
        CurrentUserCreatedAt = currentUserCreatedAt;
    }
}
