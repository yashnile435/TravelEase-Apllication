package com.example.travelease.model;

import com.google.firebase.firestore.Exclude;

public class Vehicle implements java.io.Serializable {
    private String id;
    private String name;
    private String description;
    private String imageBase64;
    private String imageUrl;
    private double order;
    private int passengers;
    private int pricePerKmAC;
    private int pricePerKmNonAC;
    private String status;
    private String transmission;
    private String fuelType;

    public Vehicle() {
        // Required empty constructor for Firestore
    }

    public Vehicle(String id, String name, String description, String imageBase64, String imageUrl, 
                   double order, int passengers, int pricePerKmAC, int pricePerKmNonAC, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageBase64 = imageBase64;
        this.imageUrl = imageUrl;
        this.order = order;
        this.passengers = passengers;
        this.pricePerKmAC = pricePerKmAC;
        this.pricePerKmNonAC = pricePerKmNonAC;
        this.status = status;
    }

    public Vehicle(String id, String name, String description, String imageBase64, String imageUrl, 
                   double order, int passengers, int pricePerKmAC, int pricePerKmNonAC, String status,
                   String transmission, String fuelType) {
        this(id, name, description, imageBase64, imageUrl, order, passengers, pricePerKmAC, pricePerKmNonAC, status);
        this.transmission = transmission;
        this.fuelType = fuelType;
    }

    // Getters and Setters for Required Fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getOrder() { return order; }
    public void setOrder(double order) { this.order = order; }

    public int getPassengers() { return passengers; }
    public void setPassengers(int passengers) { this.passengers = passengers; }

    public int getPricePerKmAC() { return pricePerKmAC; }
    public void setPricePerKmAC(int pricePerKmAC) { this.pricePerKmAC = pricePerKmAC; }

    public int getPricePerKmNonAC() { return pricePerKmNonAC; }
    public void setPricePerKmNonAC(int pricePerKmNonAC) { this.pricePerKmNonAC = pricePerKmNonAC; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Compatibility Getters and Setters for Existing UI
    @Exclude
    public String getBrand() { return name != null ? name : ""; }
    @Exclude
    public void setBrand(String brand) { this.name = brand; }

    @Exclude
    public String getModel() { return ""; }
    @Exclude
    public void setModel(String model) {}

    @Exclude
    public String getCategory() { return "Luxury"; }
    @Exclude
    public void setCategory(String category) {}

    @Exclude
    public double getRating() { return 5.0; }
    @Exclude
    public void setRating(double rating) {}

    @Exclude
    public int getReviewsCount() { return 1; }
    @Exclude
    public void setReviewsCount(int reviewsCount) {}

    @Exclude
    public double getDailyPrice() { return pricePerKmAC; }
    @Exclude
    public void setDailyPrice(double dailyPrice) { this.pricePerKmAC = (int) dailyPrice; }

    @Exclude
    public int getSeats() { return passengers; }
    @Exclude
    public void setSeats(int seats) { this.passengers = seats; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    @Exclude
    public String getAcceleration() { return "5.2 Seconds"; }
    @Exclude
    public void setAcceleration(String acceleration) {}

    @Exclude
    public boolean getIsAvailable() { return "available".equalsIgnoreCase(status); }
    @Exclude
    public void setIsAvailable(boolean available) { this.status = available ? "available" : "unavailable"; }
}
