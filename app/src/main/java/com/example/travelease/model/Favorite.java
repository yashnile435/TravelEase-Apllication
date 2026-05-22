package com.example.travelease.model;

public class Favorite {
    private String id;
    private String userId;
    private String vehicleId;
    private long timestamp;

    public Favorite() {
        // Required empty constructor for Firestore
    }

    public Favorite(String id, String userId, String vehicleId, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
