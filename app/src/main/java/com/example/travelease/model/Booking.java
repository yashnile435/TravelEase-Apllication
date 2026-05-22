package com.example.travelease.model;

import com.google.firebase.firestore.Exclude;

public class Booking {
    private String bookingId;
    private String userId;
    private String username;
    private String userEmail;
    private String userPhone;
    private String vehicleId;
    private String vehicleName;
    private String pickupDate;
    private String pickupTime;
    private int totalDays;
    private double totalPrice;
    private String bookingNote;
    private String bookingStatus;
    private long createdAt;
    private long updatedAt;
    private String pickupLocation;
    private String dropoffLocation;

    public Booking() {
        // Required empty constructor for Firestore
    }

    public Booking(String bookingId, String userId, String username, String userEmail, String userPhone,
                   String vehicleId, String vehicleName, String pickupDate, String pickupTime, 
                   int totalDays, double totalPrice, String bookingNote, String bookingStatus, 
                   long createdAt, long updatedAt) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.totalDays = totalDays;
        this.totalPrice = totalPrice;
        this.bookingNote = bookingNote;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getPickupDate() { return pickupDate; }
    public void setPickupDate(String pickupDate) { this.pickupDate = pickupDate; }

    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getBookingNote() { return bookingNote; }
    public void setBookingNote(String bookingNote) { this.bookingNote = bookingNote; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Legacy backwards compatibility getters/setters with @Exclude for Firestore

    @Exclude
    public String getId() { return bookingId; }
    
    @Exclude
    public void setId(String id) { this.bookingId = id; }

    @Exclude
    public String getStartDate() { return pickupDate; }

    @Exclude
    public void setStartDate(String startDate) { this.pickupDate = startDate; }

    @Exclude
    public String getEndDate() { 
        return (pickupTime != null && !pickupTime.isEmpty()) ? (pickupDate + " (" + pickupTime + ")") : pickupDate; 
    }

    @Exclude
    public void setEndDate(String endDate) { }

    @Exclude
    public String getStatus() { return bookingStatus; }

    @Exclude
    public void setStatus(String status) { this.bookingStatus = status; }

    @Exclude
    public long getTimestamp() { return createdAt; }

    @Exclude
    public void setTimestamp(long timestamp) { this.createdAt = timestamp; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }
}
