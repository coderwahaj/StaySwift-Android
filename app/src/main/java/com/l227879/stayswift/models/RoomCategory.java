package com.l227879.stayswift.models;

import java.util.ArrayList;

public class RoomCategory {
    public String roomId;
    public String hotelId;
    public String category;
    public String description;
    public String bedType;

    public long basePrice;
    public long discountPrice;

    public long totalRooms;
    public long availableRooms;

    public long maxAdults;
    public long maxChildren;

    public long sizeSqft;

    public boolean isActive;

    public java.util.ArrayList<String> amenities;

    public RoomCategory() {}
}