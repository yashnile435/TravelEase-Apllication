package com.example.travelease.util;

import com.example.travelease.model.Booking;
import com.example.travelease.model.Favorite;
import com.example.travelease.model.User;
import com.example.travelease.model.Vehicle;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {
    private static FirebaseHelper instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getDb() { return db; }
    public FirebaseStorage getStorage() { return storage; }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public CollectionReference getUsersCollection() { return db.collection("users"); }
    public CollectionReference getVehiclesCollection() { return db.collection("vehicles"); }
    public CollectionReference getBookingsCollection() { return db.collection("bookings"); }
    public CollectionReference getFavoritesCollection() { return db.collection("favorites"); }

    // Seed mock data if database is blank
    public void initializeMockDataIfEmpty() {
        getVehiclesCollection().limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                seedInitialVehicles();
            }
        });
    }

    private void seedInitialVehicles() {
        List<Vehicle> initialList = new ArrayList<>();
        initialList.add(new Vehicle(
                "v1",
                "Mercedes-Benz S-Class",
                "The Mercedes-Benz S-Class redefines the premium sedan segment with its unparalleled blend of performance, technology, and sheer comfort. Designed for the discerning traveler, it features a handcrafted interior with sustainable vegan leather and authentic wood accents.",
                "",
                "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?q=80&w=800&auto=format&fit=crop",
                1.0,
                5,
                185,
                160,
                "available"
        ));
        initialList.add(new Vehicle(
                "v2",
                "Range Rover Velar",
                "The Range Rover Velar redefines the midsize luxury SUV category, offering refined power, an advanced terrain response system, and a floating glass infotainment cockpit for effortless exploration in complete comfort.",
                "",
                "https://images.unsplash.com/photo-1606016159991-dfe4f2746ad5?q=80&w=800&auto=format&fit=crop",
                2.0,
                5,
                145,
                130,
                "available"
        ));
        initialList.add(new Vehicle(
                "v3",
                "Tesla Model 3",
                "The Tesla Model 3 delivers sustainable high-performance urban mobility. Equipped with Autopilot assistance, instant torque acceleration, and high-efficiency battery packs, it is the ultimate compact cruiser.",
                "",
                "https://images.unsplash.com/photo-1614162692292-7ac56d7f7f1e?q=80&w=800&auto=format&fit=crop",
                3.0,
                5,
                95,
                80,
                "available"
        ));

        for (Vehicle v : initialList) {
            getVehiclesCollection().document(v.getId()).set(v);
        }
    }

    // Save profile details to Firestore - fully customized with all required fields (defaulting to customer role)
    public Task<Void> registerNewUser(String userId, String username, String email, String phoneNumber) {
        return registerNewUser(userId, username, email, phoneNumber, "customer");
    }

    // Save profile details with a specific role
    public Task<Void> registerNewUser(String userId, String username, String email, String phoneNumber, String role) {
        long now = System.currentTimeMillis();
        User user = new User(
                userId,
                username,
                username,
                email,
                phoneNumber != null ? phoneNumber : "",
                "", // profilePhoto starts empty
                "Email",
                role != null ? role : "customer",
                now, // createdAt
                now, // updatedAt
                now, // lastLogin
                "Active" // accountStatus
        );
        return getUsersCollection().document(userId).set(user);
    }

    // Handles Google Sign-In user checking, creating, and updating lastLogin (retains existing role)
    public Task<Void> handleGoogleSignInUser(String userId, String displayName, String email, String photoUrl) {
        long now = System.currentTimeMillis();
        return getUsersCollection().document(userId).get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // User already exists, only update lastLogin and updatedAt (preserves role)
                return getUsersCollection().document(userId).update(
                        "lastLogin", now,
                        "updatedAt", now
                );
            } else {
                // User does not exist, create a new document
                User user = new User(
                        userId,
                        displayName,
                        displayName,
                        email,
                        "", // phone number not available from Google Sign-In GSO by default
                        photoUrl != null ? photoUrl : "",
                        "Google",
                        "customer", // default role = customer
                        now, // createdAt
                        now, // updatedAt
                        now, // lastLogin
                        "Active" // accountStatus
                );
                return getUsersCollection().document(userId).set(user);
            }
        });
    }

    // Update user login timestamp on successful email login
    public Task<Void> updateUserLoginTimestamp(String userId, String email) {
        long now = System.currentTimeMillis();
        return getUsersCollection().document(userId).get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                return getUsersCollection().document(userId).update(
                        "lastLogin", now,
                        "updatedAt", now
                );
            } else {
                // Fallback: If profile document is somehow missing, recreate it
                String fallbackName = email != null ? email.split("@")[0] : "Traveler";
                User user = new User(
                        userId,
                        fallbackName,
                        fallbackName,
                        email != null ? email : "",
                        "", // phone
                        "", // photo
                        "Email",
                        "customer", // default role = customer
                        now, // createdAt
                        now, // updatedAt
                        now, // lastLogin
                        "Active" // accountStatus
                );
                return getUsersCollection().document(userId).set(user);
            }
        });
    }

    // Save profile details to Firestore (Legacy 5-argument method updated for the new model)
    public Task<Void> saveUserProfile(String userId, String name, String email, String profilePhoto, String loginProvider) {
        long now = System.currentTimeMillis();
        User user = new User(
                userId,
                name,
                name,
                email,
                "", // phoneNumber
                profilePhoto != null ? profilePhoto : "",
                loginProvider != null ? loginProvider : "Email",
                "customer", // default role = customer
                now, // createdAt
                now, // updatedAt
                now, // lastLogin
                "Active" // accountStatus
        );
        return getUsersCollection().document(userId).set(user);
    }

    // Backwards-compatible overload for legacy screen profile saves
    public Task<Void> saveUserProfile(String userId, String name, String email) {
        return saveUserProfile(userId, name, email, "", "Email");
    }

    // Get user details
    public Task<DocumentSnapshot> getUserProfile(String userId) {
        return getUsersCollection().document(userId).get();
    }

    // Check favorite status
    public Task<Boolean> checkIsFavorite(String userId, String vehicleId) {
        return getFavoritesCollection()
                .whereEqualTo("userId", userId)
                .whereEqualTo("vehicleId", vehicleId)
                .get()
                .continueWith(task -> !task.getResult().isEmpty());
    }

    // Toggle favorite status
    public Task<Void> toggleFavorite(String userId, String vehicleId) {
        return getFavoritesCollection()
                .whereEqualTo("userId", userId)
                .whereEqualTo("vehicleId", vehicleId)
                .get()
                .continueWithTask(task -> {
                    if (task.getResult().isEmpty()) {
                        String id = getFavoritesCollection().document().getId();
                        Favorite fav = new Favorite(id, userId, vehicleId, System.currentTimeMillis());
                        return getFavoritesCollection().document(id).set(fav);
                    } else {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        return getFavoritesCollection().document(docId).delete();
                    }
                });
    }

    // Save Booking
    public Task<Void> saveBooking(Booking booking) {
        String bookingId = getBookingsCollection().document().getId();
        booking.setBookingId(bookingId);
        booking.setCreatedAt(System.currentTimeMillis());
        return getBookingsCollection().document(bookingId).set(booking);
    }

    public static String formatRupees(double amount) {
        try {
            java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("en", "IN"));
            if (amount % 1 == 0) {
                formatter.setMaximumFractionDigits(0);
            } else {
                formatter.setMinimumFractionDigits(2);
                formatter.setMaximumFractionDigits(2);
            }
            return "₹ " + formatter.format(amount);
        } catch (Exception e) {
            if (amount % 1 == 0) {
                return String.format(java.util.Locale.getDefault(), "₹ %,.0f", amount);
            } else {
                return String.format(java.util.Locale.getDefault(), "₹ %,.2f", amount);
            }
        }
    }
}

