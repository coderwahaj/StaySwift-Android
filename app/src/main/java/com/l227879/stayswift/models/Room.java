package com.l227879.stayswift.models;

import java.util.ArrayList;

public class Room {
    public String roomId;
    public String hotelId;
    public String category;
    public String description;

    public double basePrice;
    public double discountPrice;

    public int totalRooms;
    public int availableRooms;

    public int maxAdults;
    public int maxChildren;

    public String bedType;
    public int sizeSqft;

    public ArrayList<String> amenities;
    public ArrayList<String> imageUrls;

    public boolean isActive;
    public long createdAt;
    public long updatedAt;

    public Room() {}

    public Room(String roomId, String hotelId, String category, String description,
                double basePrice, double discountPrice,
                int totalRooms, int availableRooms,
                int maxAdults, int maxChildren,
                String bedType, int sizeSqft,
                ArrayList<String> amenities, ArrayList<String> imageUrls,
                boolean isActive, long createdAt, long updatedAt) {
        this.roomId = roomId;
        this.hotelId = hotelId;
        this.category = category;
        this.description = description;
        this.basePrice = basePrice;
        this.discountPrice = discountPrice;
        this.totalRooms = totalRooms;
        this.availableRooms = availableRooms;
        this.maxAdults = maxAdults;
        this.maxChildren = maxChildren;
        this.bedType = bedType;
        this.sizeSqft = sizeSqft;
        this.amenities = amenities;
        this.imageUrls = imageUrls;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}