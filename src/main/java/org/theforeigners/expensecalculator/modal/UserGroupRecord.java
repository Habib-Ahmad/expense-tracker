package org.theforeigners.expensecalculator.modal;

public class UserGroupRecord {
    private final int groupId;
    private final String groupName;
    private final int userId;
    private final String userName;

    public UserGroupRecord(int groupId, String groupName, int userId, String userName) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.userId = userId;
        this.userName = userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getUserName() {
        return userName;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getUserId() {
        return userId;
    }

}