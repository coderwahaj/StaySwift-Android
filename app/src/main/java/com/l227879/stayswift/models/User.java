package com.l227879.stayswift.models;

public class User {
    public String name;
    public String email;
    public String role;
    public String phone;
    public long createdAt;

    public User() { }

    public User(String name, String email, String role, String phone, long createdAt) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.createdAt = createdAt;
    }
}