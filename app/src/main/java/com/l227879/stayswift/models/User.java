package com.l227879.stayswift.models;

public class User {
    public String name;
    public String email;
    public String role;
    public long createdAt;

    public User() { } // Required for Firebase

    public User(String name, String email, String role, long createdAt) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }
}