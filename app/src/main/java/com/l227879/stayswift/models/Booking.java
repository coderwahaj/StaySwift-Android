package com.l227879.stayswift.models;

public class Booking {
    public String bookingId;
    public String userId;
    public String hotelId;
    public String roomId;
    public String roomCategory;

    public long checkInMs;
    public long checkOutMs;

    public long roomsCount;
    public long pricePerNight;
    public long totalAmount;

    public String status; // upcoming, cancelled, completed
    public long createdAt;

    public Booking() {}
}