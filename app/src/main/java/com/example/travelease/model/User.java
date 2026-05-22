package com.example.travelease.model;

public class User {
    private String userId;
    private String username;
    private String name; // Kept for backward compatibility
    private String email;
    private String phoneNumber;
    private String profilePhoto;
    private String loginProvider;
    private String role; // customer or admin
    private long createdAt;
    private long updatedAt;
    private long lastLogin;
    private String accountStatus;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String userId, String username, String name, String email, String phoneNumber,
                String profilePhoto, String loginProvider, String role, long createdAt, long updatedAt,
                long lastLogin, String accountStatus) {
        this.userId = userId;
        this.username = username;
        this.name = name != null ? name : username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePhoto = profilePhoto;
        this.loginProvider = loginProvider;
        this.role = role != null ? role : "customer";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
        this.accountStatus = accountStatus;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username; 
        if (this.name == null || this.name.isEmpty()) {
            this.name = username;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        if (this.username == null || this.username.isEmpty()) {
            this.username = name;
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getLoginProvider() { return loginProvider; }
    public void setLoginProvider(String loginProvider) { this.loginProvider = loginProvider; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role != null ? role : "customer"; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    // Legacy getters for backward compatibility
    public String getId() { return userId; }
    public void setId(String id) { this.userId = id; }
    public String getProfileImageUrl() { return profilePhoto; }
    public void setProfileImageUrl(String profileImageUrl) { this.profilePhoto = profileImageUrl; }
}
