package com.example.travelease.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelease.R;
import com.example.travelease.databinding.ActivityAdminPanelBinding;
import com.example.travelease.databinding.ItemAdminBookingBinding;
import com.example.travelease.databinding.ItemAdminUserBinding;
import com.example.travelease.adapter.VehicleAdapter;
import com.example.travelease.model.Booking;
import com.example.travelease.model.User;
import com.example.travelease.model.Vehicle;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.NetworkUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity implements VehicleAdapter.OnVehicleClickListener {
    private ActivityAdminPanelBinding binding;
    private FirebaseHelper firebaseHelper;

    // Realtime snapshot listeners
    private ListenerRegistration statsUsersListener;
    private ListenerRegistration statsBookingsListener;
    private ListenerRegistration statsVehiclesListener;

    // Users Tab
    private final List<User> allUsersList = new ArrayList<>();
    private final List<User> filteredUsersList = new ArrayList<>();
    private UsersAdapter usersAdapter;
    private ListenerRegistration usersListener;

    // Destinations Tab
    private final List<Vehicle> destinationsList = new ArrayList<>();
    private VehicleAdapter vehicleAdapter;
    private ListenerRegistration destinationsListener;

    // Bookings Tab
    private final List<Booking> bookingsList = new ArrayList<>();
    private BookingsAdapter bookingsAdapter;
    private ListenerRegistration bookingsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();

        // Safe Admin Role Protection Checks
        verifyAdminAccess();

        // Set up tab buttons navigation
        setupNavigationTabs();

        // Set up action listeners
        setupActionListeners();

        // Start listening to real-time collections count
        startStatsListeners();

        // Initialize lists & adapters
        setupUsersList();
        setupDestinationsList();
        setupBookingsList();
    }

    private void verifyAdminAccess() {
        String currentUid = firebaseHelper.getCurrentUserId();
        if (currentUid == null) {
            kickToLogin("Please log in first.");
            return;
        }

        firebaseHelper.getUserProfile(currentUid).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                String status = task.getResult().getString("accountStatus");

                if ("Disabled".equalsIgnoreCase(status)) {
                    kickToLogin("Your account has been disabled.");
                } else if (!"admin".equalsIgnoreCase(role)) {
                    // Prevent customer from accessing admin panel
                    Toast.makeText(this, "Unauthorized Access! Admin privileges required.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                kickToLogin("Access denied. Admin validation failed.");
            }
        });
    }

    private void kickToLogin(String message) {
        firebaseHelper.getAuth().signOut();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigationTabs() {
        binding.tabOverviewBtn.setOnClickListener(v -> switchTab("overview"));
        binding.tabUsersBtn.setOnClickListener(v -> switchTab("users"));
        binding.tabDestinationsBtn.setOnClickListener(v -> switchTab("destinations"));
        binding.tabBookingsBtn.setOnClickListener(v -> switchTab("bookings"));
    }

    private void switchTab(String tab) {
        // Reset tabs appearance
        int inactiveBg = Color.parseColor("#F0F2F5");
        int activeBg = getResources().getColor(R.color.primary_blue);
        int inactiveText = getResources().getColor(R.color.text_primary_light);
        int activeText = Color.WHITE;

        binding.tabOverviewBtn.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.tabOverviewBtn.setTextColor(inactiveText);
        binding.tabOverviewBtn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));

        binding.tabUsersBtn.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.tabUsersBtn.setTextColor(inactiveText);
        binding.tabUsersBtn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));

        binding.tabDestinationsBtn.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.tabDestinationsBtn.setTextColor(inactiveText);
        binding.tabDestinationsBtn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));

        binding.tabBookingsBtn.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.tabBookingsBtn.setTextColor(inactiveText);
        binding.tabBookingsBtn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));

        // Hide all panels
        binding.panelOverview.setVisibility(View.GONE);
        binding.panelUsers.setVisibility(View.GONE);
        binding.panelDestinations.setVisibility(View.GONE);
        binding.panelBookings.setVisibility(View.GONE);

        // Activate selected tab & panel
        switch (tab) {
            case "overview":
                binding.tabOverviewBtn.setBackgroundTintList(ColorStateList.valueOf(activeBg));
                binding.tabOverviewBtn.setTextColor(activeText);
                binding.panelOverview.setVisibility(View.VISIBLE);
                break;
            case "users":
                binding.tabUsersBtn.setBackgroundTintList(ColorStateList.valueOf(activeBg));
                binding.tabUsersBtn.setTextColor(activeText);
                binding.panelUsers.setVisibility(View.VISIBLE);
                break;
            case "destinations":
                binding.tabDestinationsBtn.setBackgroundTintList(ColorStateList.valueOf(activeBg));
                binding.tabDestinationsBtn.setTextColor(activeText);
                binding.panelDestinations.setVisibility(View.VISIBLE);
                break;
            case "bookings":
                binding.tabBookingsBtn.setBackgroundTintList(ColorStateList.valueOf(activeBg));
                binding.tabBookingsBtn.setTextColor(activeText);
                binding.panelBookings.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupActionListeners() {
        binding.btnAdminLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out from the Admin Panel?")
                    .setPositiveButton("Logout", (dialog, which) -> kickToLogin("Logged out successfully."))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Overview panel quick actions
        binding.actionAddDestination.setOnClickListener(v -> {
            startActivity(new Intent(this, AddVehicleActivity.class));
        });
        binding.actionViewBookings.setOnClickListener(v -> switchTab("bookings"));
        binding.actionViewUsers.setOnClickListener(v -> switchTab("users"));

        // Manage Destinations FAB
        binding.fabAddDestination.setOnClickListener(v -> {
            startActivity(new Intent(this, AddVehicleActivity.class));
        });
    }

    private void startStatsListeners() {
        // Realtime stats - Total Users
        statsUsersListener = firebaseHelper.getUsersCollection().addSnapshotListener((value, error) -> {
            if (value != null) {
                binding.tvStatUsers.setText(String.valueOf(value.size()));
            }
        });

        // Realtime stats - Total Bookings
        statsBookingsListener = firebaseHelper.getBookingsCollection().addSnapshotListener((value, error) -> {
            if (value != null) {
                binding.tvStatBookings.setText(String.valueOf(value.size()));
            }
        });

        // Realtime stats - Total Rides
        statsVehiclesListener = firebaseHelper.getVehiclesCollection().addSnapshotListener((value, error) -> {
            if (value != null) {
                binding.tvStatDestinations.setText(String.valueOf(value.size()));
            }
        });
    }

    private void setupUsersList() {
        usersAdapter = new UsersAdapter(filteredUsersList);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(usersAdapter);

        // Listen for realtime users
        usersListener = firebaseHelper.getUsersCollection().addSnapshotListener((value, error) -> {
            if (value != null) {
                allUsersList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    User u = doc.toObject(User.class);
                    if (u != null) {
                        allUsersList.add(u);
                    }
                }
                filterUsers("");
            }
        });

        // Live user search logic
        binding.searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        filteredUsersList.clear();
        String q = query.trim().toLowerCase();
        for (User u : allUsersList) {
            boolean matchesName = u.getName() != null && u.getName().toLowerCase().contains(q);
            boolean matchesEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(q);
            boolean matchesPhone = u.getPhoneNumber() != null && u.getPhoneNumber().toLowerCase().contains(q);
            
            if (q.isEmpty() || matchesName || matchesEmail || matchesPhone) {
                filteredUsersList.add(u);
            }
        }
        usersAdapter.notifyDataSetChanged();
    }

    private void setupDestinationsList() {
        vehicleAdapter = new VehicleAdapter(destinationsList, this);
        binding.rvDestinations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDestinations.setAdapter(vehicleAdapter);

        destinationsListener = firebaseHelper.getVehiclesCollection().addSnapshotListener((value, error) -> {
            if (value != null) {
                destinationsList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Vehicle v = doc.toObject(Vehicle.class);
                    if (v != null) {
                        destinationsList.add(v);
                    }
                }
                vehicleAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupBookingsList() {
        bookingsAdapter = new BookingsAdapter(bookingsList);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBookings.setAdapter(bookingsAdapter);

        bookingsListener = firebaseHelper.getBookingsCollection().addSnapshotListener((value, error) -> {
            if (value != null) {
                bookingsList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Booking b = doc.toObject(Booking.class);
                    if (b != null) {
                        bookingsList.add(b);
                    }
                }
                bookingsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        // Show choice dialog: Edit or Delete
        new AlertDialog.Builder(this)
                .setTitle("Manage Vehicle")
                .setMessage(vehicle.getBrand())
                .setPositiveButton("Edit", (dialog, which) -> {
                    Intent intent = new Intent(this, AddVehicleActivity.class);
                    intent.putExtra("vehicle", vehicle);
                    startActivity(intent);
                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Confirm Delete")
                            .setMessage("Are you sure you want to delete this ride option?")
                            .setPositiveButton("Delete", (d, w) -> deleteVehicle(vehicle))
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    @Override
    public void onFavoriteClick(Vehicle vehicle, boolean isFavoriteNow) {
        // Unused in admin panel
    }

    private void deleteVehicle(Vehicle vehicle) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.adminLoadingOverlay.setVisibility(View.VISIBLE);
        binding.adminLoadingText.setText("Deleting vehicle...");

        String id = vehicle.getId();
        String imageUrl = vehicle.getImageUrl();

        firebaseHelper.getVehiclesCollection().document(id).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove from adapter data set instantly
                        destinationsList.remove(vehicle);
                        vehicleAdapter.notifyDataSetChanged();

                        // Clean up Firebase Storage if uploaded from device
                        if (!TextUtils.isEmpty(imageUrl) && imageUrl.contains("firebasestorage.googleapis.com")) {
                            try {
                                com.google.firebase.storage.StorageReference storageRef = 
                                        firebaseHelper.getStorage().getReferenceFromUrl(imageUrl);
                                storageRef.delete()
                                        .addOnCompleteListener(storageTask -> {
                                            binding.adminLoadingOverlay.setVisibility(View.GONE);
                                            if (storageTask.isSuccessful()) {
                                                Toast.makeText(AdminPanelActivity.this, "Vehicle and its image deleted successfully!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                String err = storageTask.getException() != null ? storageTask.getException().getMessage() : "Storage delete failed";
                                                Toast.makeText(AdminPanelActivity.this, "Vehicle deleted, but storage cleanup failed: " + err, Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } catch (Exception e) {
                                binding.adminLoadingOverlay.setVisibility(View.GONE);
                                Toast.makeText(AdminPanelActivity.this, "Vehicle deleted. Storage cleanup skipped: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            binding.adminLoadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(AdminPanelActivity.this, "Vehicle deleted successfully!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        binding.adminLoadingOverlay.setVisibility(View.GONE);
                        String err = task.getException() != null ? task.getException().getMessage() : "Firestore delete failed";
                        Toast.makeText(AdminPanelActivity.this, "Delete failed: " + err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statsUsersListener != null) statsUsersListener.remove();
        if (statsBookingsListener != null) statsBookingsListener.remove();
        if (statsVehiclesListener != null) statsVehiclesListener.remove();
        if (usersListener != null) usersListener.remove();
        if (destinationsListener != null) destinationsListener.remove();
        if (bookingsListener != null) bookingsListener.remove();
    }

    // ==========================================
    // USER ADAPTER (HIGH-FIDELITY VIEWS)
    // ==========================================
    private class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
        private final List<User> users;

        public UsersAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAdminUserBinding userBinding = ItemAdminUserBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new UserViewHolder(userBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User u = users.get(position);
            holder.bind(u);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            private final ItemAdminUserBinding b;

            public UserViewHolder(ItemAdminUserBinding b) {
                super(b.getRoot());
                this.b = b;
            }

            public void bind(User u) {
                b.userName.setText(u.getName());
                b.userEmail.setText(u.getEmail());
                
                if (TextUtils.isEmpty(u.getPhoneNumber())) {
                    b.userPhone.setText("No mobile added");
                } else {
                    b.userPhone.setText(u.getPhoneNumber());
                }

                // Role badge
                b.roleBadge.setText("ROLE: " + u.getRole().toUpperCase());
                if ("admin".equalsIgnoreCase(u.getRole())) {
                    b.roleBadge.setTextColor(Color.parseColor("#FEAA00"));
                    b.btnChangeRole.setText("Make Customer");
                } else {
                    b.roleBadge.setTextColor(getResources().getColor(R.color.primary_blue));
                    b.btnChangeRole.setText("Make Admin");
                }

                // Account status badge
                String status = u.getAccountStatus() != null ? u.getAccountStatus() : "Active";
                b.statusBadge.setText(status);

                if ("Disabled".equalsIgnoreCase(status)) {
                    b.statusBadge.setTextColor(getResources().getColor(R.color.error));
                    b.statusBadge.setBackgroundResource(R.drawable.badge_cancelled);
                    b.btnToggleStatus.setText("Enable");
                    b.btnToggleStatus.setTextColor(getResources().getColor(R.color.success));
                } else {
                    b.statusBadge.setTextColor(getResources().getColor(R.color.success));
                    b.statusBadge.setBackgroundResource(R.drawable.badge_confirmed);
                    b.btnToggleStatus.setText("Disable");
                    b.btnToggleStatus.setTextColor(getResources().getColor(R.color.error));
                }

                // Toggle status listener
                b.btnToggleStatus.setOnClickListener(v -> {
                    String newStatus = "Disabled".equalsIgnoreCase(status) ? "Active" : "Disabled";
                    firebaseHelper.getUsersCollection().document(u.getUserId())
                            .update("accountStatus", newStatus)
                            .addOnSuccessListener(aVoid -> Toast.makeText(itemView.getContext(), "Status updated!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(itemView.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                });

                // Change role listener
                b.btnChangeRole.setOnClickListener(v -> {
                    String newRole = "admin".equalsIgnoreCase(u.getRole()) ? "customer" : "admin";
                    firebaseHelper.getUsersCollection().document(u.getUserId())
                            .update("role", newRole)
                            .addOnSuccessListener(aVoid -> Toast.makeText(itemView.getContext(), "Role updated!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(itemView.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                });

                // Click detail modal
                itemView.setOnClickListener(v -> {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("User Details")
                            .setMessage("ID: " + u.getUserId() + "\n" +
                                    "Name: " + u.getName() + "\n" +
                                    "Email: " + u.getEmail() + "\n" +
                                    "Phone: " + u.getPhoneNumber() + "\n" +
                                    "Provider: " + u.getLoginProvider() + "\n" +
                                    "Status: " + status + "\n" +
                                    "Role: " + u.getRole() + "\n" +
                                    "Created At: " + new java.util.Date(u.getCreatedAt()).toString())
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        }
    }

    // ==========================================
    // BOOKINGS ADAPTER (REALTIME VIEWS)
    // ==========================================
    private class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {
        private final List<Booking> bookings;

        public BookingsAdapter(List<Booking> bookings) {
            this.bookings = bookings;
        }

        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAdminBookingBinding bookingBinding = ItemAdminBookingBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new BookingViewHolder(bookingBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            Booking b = bookings.get(position);
            holder.bind(b);
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }

        class BookingViewHolder extends RecyclerView.ViewHolder {
            private final ItemAdminBookingBinding b;

            public BookingViewHolder(ItemAdminBookingBinding b) {
                super(b.getRoot());
                this.b = b;
            }

            public void bind(Booking booking) {
                b.tvBookingId.setText("Booking ID: #" + (booking.getBookingId() != null ? booking.getBookingId() : ""));
                
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
                b.tvBookingDate.setText("Booked On: " + format.format(new java.util.Date(booking.getCreatedAt())));

                // Set status text & color badge
                String status = booking.getBookingStatus() != null ? booking.getBookingStatus() : "pending";
                b.bookingStatusBadge.setText(status.toLowerCase());

                if ("accepted".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status)) {
                    b.bookingStatusBadge.setTextColor(getResources().getColor(R.color.success));
                    b.bookingStatusBadge.setBackgroundResource(R.drawable.badge_confirmed);
                    b.btnConfirmBooking.setVisibility(View.GONE);
                    b.btnCancelBooking.setVisibility(View.GONE);
                } else if ("rejected".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
                    b.bookingStatusBadge.setTextColor(getResources().getColor(R.color.error));
                    b.bookingStatusBadge.setBackgroundResource(R.drawable.badge_cancelled);
                    b.btnConfirmBooking.setVisibility(View.GONE);
                    b.btnCancelBooking.setVisibility(View.GONE);
                } else if ("completed".equalsIgnoreCase(status)) {
                    b.bookingStatusBadge.setTextColor(getResources().getColor(R.color.primary_blue));
                    b.bookingStatusBadge.setBackgroundResource(R.drawable.badge_confirmed);
                    b.btnConfirmBooking.setVisibility(View.GONE);
                    b.btnCancelBooking.setVisibility(View.GONE);
                } else {
                    // Pending status
                    b.bookingStatusBadge.setTextColor(getResources().getColor(R.color.accent_orange));
                    b.bookingStatusBadge.setBackgroundResource(R.drawable.badge_confirmed);
                    b.btnConfirmBooking.setVisibility(View.VISIBLE);
                    b.btnCancelBooking.setVisibility(View.VISIBLE);
                }

                // Bind initial available values from Booking object
                b.bookingUser.setText(booking.getUsername() != null && !booking.getUsername().isEmpty() ? booking.getUsername() : "Traveler");
                b.tvCustomerPhone.setText(booking.getUserPhone() != null && !booking.getUserPhone().isEmpty() ? booking.getUserPhone() : "Not Provided");
                b.tvCustomerEmail.setText(booking.getUserEmail() != null && !booking.getUserEmail().isEmpty() ? booking.getUserEmail() : "Not Provided");
                b.bookingDestName.setText(booking.getVehicleName() != null && !booking.getVehicleName().isEmpty() ? booking.getVehicleName() : "Loading Vehicle...");
                b.tvPickupDate.setText("Pickup: " + booking.getPickupDate());
                b.tvPickupTime.setText("Time: " + booking.getPickupTime());
                b.tvTotalDays.setText("Duration: " + booking.getTotalDays() + (booking.getTotalDays() == 1 ? " Day" : " Days"));
                b.tvPickupLocation.setText("Main Hub Office");
                b.tvAdminPickupLocation.setText("Pickup: " + (booking.getPickupLocation() != null ? booking.getPickupLocation() : "Not Provided"));
                b.tvAdminDropoffLocation.setText("Dropoff: " + (booking.getDropoffLocation() != null ? booking.getDropoffLocation() : "Not Provided"));

                if (booking.getBookingNote() == null || booking.getBookingNote().trim().isEmpty()) {
                    b.layoutBookingNote.setVisibility(View.GONE);
                } else {
                    b.layoutBookingNote.setVisibility(View.VISIBLE);
                    b.tvBookingNote.setText("Special Request: " + booking.getBookingNote());
                }


                // Resolve customer details in realtime from database for high fidelity
                firebaseHelper.getUsersCollection().document(booking.getUserId()).get()
                        .addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {
                                String uName = snapshot.getString("name");
                                if (uName == null || uName.isEmpty()) {
                                    uName = snapshot.getString("username");
                                }
                                String uPhone = snapshot.getString("phoneNumber");
                                String uEmail = snapshot.getString("email");
                                String photo = snapshot.getString("profilePhoto");

                                if (uName != null && !uName.isEmpty()) {
                                    b.bookingUser.setText(uName);
                                }
                                if (uPhone != null && !uPhone.isEmpty()) {
                                    b.tvCustomerPhone.setText(uPhone);
                                }
                                if (uEmail != null && !uEmail.isEmpty()) {
                                    b.tvCustomerEmail.setText(uEmail);
                                }

                                if (photo != null && !photo.isEmpty()) {
                                    Glide.with(itemView.getContext())
                                            .load(photo)
                                            .placeholder(R.drawable.ic_profile)
                                            .circleCrop()
                                            .into(b.ivUserProfile);
                                } else {
                                    b.ivUserProfile.setImageResource(R.drawable.ic_profile);
                                }
                            } else {
                                b.ivUserProfile.setImageResource(R.drawable.ic_profile);
                            }
                        })
                        .addOnFailureListener(e -> {
                            b.ivUserProfile.setImageResource(R.drawable.ic_profile);
                        });

                // Resolve vehicle name in realtime
                firebaseHelper.getVehiclesCollection().document(booking.getVehicleId()).get()
                        .addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {
                                String name = snapshot.getString("name");
                                if (name != null && !name.isEmpty()) {
                                    b.bookingDestName.setText(name);
                                }
                            }
                        });

                // Reject booking click listener
                b.btnCancelBooking.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(AdminPanelActivity.this)
                            .setTitle("Reject Booking")
                            .setMessage("Are you sure you want to reject this booking?")
                            .setPositiveButton("Yes, Reject", (dialog, which) -> {
                                updateBookingStatus(booking.getBookingId(), "rejected");
                            })
                            .setNegativeButton("No", null)
                            .show();
                });

                // Accept booking click listener
                b.btnConfirmBooking.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(AdminPanelActivity.this)
                            .setTitle("Accept Booking")
                            .setMessage("Are you sure you want to accept this booking?")
                            .setPositiveButton("Yes, Accept", (dialog, which) -> {
                                updateBookingStatus(booking.getBookingId(), "accepted");
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            private void updateBookingStatus(String bookingId, String newStatus) {
                firebaseHelper.getBookingsCollection().document(bookingId)
                        .update("bookingStatus", newStatus, "updatedAt", System.currentTimeMillis())
                        .addOnSuccessListener(aVoid -> Toast.makeText(itemView.getContext(), "Booking status updated to " + newStatus, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(itemView.getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }
    }
}
