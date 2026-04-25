package com.l227879.stayswift.models;

import java.util.ArrayList;

public class Hotel {
    public String hotelId;
    public String ownerUid;
    public String name;
    public String description;
    public String phone;
    public String email;
    public String address;
    public double lat;
    public double lng;
    public ArrayList<String> amenities;
    public String otherAmenities;
    public ArrayList<String> photoUrls;
    public long createdAt;

    public Hotel() {
        // required for Firebase
    }

    public Hotel(String hotelId, String ownerUid, String name, String description, String phone, String email,
                 String address, double lat, double lng, ArrayList<String> amenities, String otherAmenities,
                 ArrayList<String> photoUrls, long createdAt) {
        this.hotelId = hotelId;
        this.ownerUid = ownerUid;
        this.name = name;
        this.description = description;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.amenities = amenities;
        this.otherAmenities = otherAmenities;
        this.photoUrls = photoUrls;
        this.createdAt = createdAt;
    }
}