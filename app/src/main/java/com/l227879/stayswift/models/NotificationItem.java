package com.l227879.stayswift.models;

public class NotificationItem {
    public String title;
    public String message;
    public String type;
    public long createdAt;

    public NotificationItem() {}

    public NotificationItem(String title, String message, String type, long createdAt) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
    }
}