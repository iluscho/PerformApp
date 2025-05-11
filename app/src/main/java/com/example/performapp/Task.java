package com.example.performapp;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String taskDate;
    private String acceptanceDate;
    private String completionDate;
    private String address;
    private String comment;
    private String organization;
    private String status;
    private String workerName;

    public Task() {
        this.completionDate = "";
        this.workerName = "";
        this.status = TaskStatus.PENDING.name();
    }

    public Task(String id, String taskDate, String acceptanceDate, String address,
                String comment, String organization, TaskStatus status) {
        this.id = id;
        this.taskDate = taskDate;
        this.acceptanceDate = acceptanceDate;
        this.address = address;
        this.comment = comment;
        this.organization = organization;
        this.status = status.name();
        this.workerName = "";
        this.completionDate = "";
    }

    public String getId() { return id; }
    public String getTaskDate() { return taskDate; }
    public String getAcceptanceDate() { return acceptanceDate; }
    public String getCompletionDate() { return completionDate; }
    public String getAddress() { return address; }
    public String getComment() { return comment; }
    public String getOrganization() { return organization; }

    public TaskStatus getStatus() {
        if (status == null || status.trim().isEmpty()) {
            return TaskStatus.PENDING;
        }
        try {
            return TaskStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return TaskStatus.PENDING;
        }
    }

    public String getWorkerName() {
        return workerName != null ? workerName : "";
    }

    public void setId(String id) { this.id = id; }
    public void setTaskDate(String taskDate) { this.taskDate = taskDate; }
    public void setAcceptanceDate(String acceptanceDate) { this.acceptanceDate = acceptanceDate; }
    public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }
    public void setAddress(String address) { this.address = address; }
    public void setComment(String comment) { this.comment = comment; }
    public void setOrganization(String organization) { this.organization = organization; }

    public void setStatus(TaskStatus status) {
        this.status = (status != null)
                ? status.name().toUpperCase()
                : TaskStatus.PENDING.name().toUpperCase();
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName != null ? workerName : "";
    }
}