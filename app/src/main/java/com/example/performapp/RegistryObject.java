package com.example.performapp;

public class RegistryObject {
    private String id;
    private String name;
    private String address;
    private String description;

    // Конструктор по умолчанию обязателен для Firebase
    public RegistryObject() {
    }

    public RegistryObject(String id, String name, String address, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
    }

    // Геттеры
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String getDescription() {
        return description;
    }

    // Сеттеры
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    // Для корректного отображения в Spinner
    @Override
    public String toString() {
        return name;
    }
}
