package com.example.performapp;

public class User {
    private String id;
    private String login;
    private String password;
    private boolean dispatcher;

    public User() {
    }

    public User(String id, String login, String password, boolean dispatcher) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.dispatcher = dispatcher;
    }

    // Геттеры
    public String getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isDispatcher() {
        return dispatcher;
    }

    // Сеттеры
    public void setId(String id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDispatcher(boolean dispatcher) {
        this.dispatcher = dispatcher;
    }
}
