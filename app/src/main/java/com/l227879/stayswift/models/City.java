package com.l227879.stayswift.models;

public class City {
    private String name;
    private int imageResId;

    public City(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public int getImageResId() { return imageResId; }
}