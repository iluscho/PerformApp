package com.example.performapp;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String taskDate;
    private String acceptanceDate;
    private String address;
    private String comment;
    private String organization;
    private TaskStatus status;

    public Task(String id, String taskDate, String acceptanceDate, String address, String comment, String organization, TaskStatus status) {
        this.id = id;
        this.taskDate = taskDate;
        this.acceptanceDate = acceptanceDate;
        this.address = address;
        this.comment = comment;
        this.organization = organization;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getTaskDate() { return taskDate; }
    public String getAcceptanceDate() { return acceptanceDate; }
    public String getAddress() { return address; }
    public String getComment() { return comment; }
    public String getOrganization() { return organization; }
    public TaskStatus getStatus() { return status; }

    // Setters
    public void setAcceptanceDate(String acceptanceDate) { this.acceptanceDate = acceptanceDate; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
