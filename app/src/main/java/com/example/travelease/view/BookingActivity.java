package com.example.travelease.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelease.R;
import com.example.travelease.databinding.ActivityBookingBinding;
import com.example.travelease.model.Booking;
import com.example.travelease.model.Vehicle;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.NetworkUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {
    private ActivityBookingBinding binding;
    private Vehicle vehicle;
    private FirebaseHelper firebaseHelper;

    private Calendar pickupCalendar = Calendar.getInstance();
    private String selectedTimeStr = null;
    private int selectedHour = -1;
    private int selectedMinute = -1;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private Date pickupDate = null;
    private double totalPrice = 0.00;
    private int totalDays = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();

        vehicle = (Vehicle) getIntent().getSerializableExtra("vehicle");
        if (vehicle == null) {
            Toast.makeText(this, "Failed to load booking details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBookingBack.setOnClickListener(v -> finish());
        binding.btnSelectPickupDate.setOnClickListener(v -> showDatePicker());
        binding.btnSelectPickupTime.setOnClickListener(v -> showTimePicker());
        
        binding.btnIncrementDays.setOnClickListener(v -> {
            totalDays++;
            updatePrice();
        });

        binding.btnDecrementDays.setOnClickListener(v -> {
            if (totalDays > 1) {
                totalDays--;
                updatePrice();
            } else {
                Toast.makeText(this, "Minimum rental duration is 1 day.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnConfirmBooking.setOnClickListener(v -> processCheckout());

        // Initialize UI with vehicle details
        binding.textVehicleName.setText(vehicle.getBrand() + " " + vehicle.getModel());
        // Rate display removed as per requirements
        // binding.textVehiclePricePerDay.setText(FirebaseHelper.formatRupees(vehicle.getDailyPrice()) + " / km");
        
        resetCalculations();
    }

    private void showDatePicker() {
        Calendar current = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    pickupCalendar = selected;
                    pickupDate = selected.getTime();
                    binding.textPickupDate.setText(dateFormat.format(pickupDate));
                },
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH));

        // Preclude booking in the past
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar current = Calendar.getInstance();
        int hour = selectedHour != -1 ? selectedHour : current.get(Calendar.HOUR_OF_DAY);
        int minute = selectedMinute != -1 ? selectedMinute : current.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minuteOfHour;

                    // Convert to 12-hour AM/PM format
                    String amPm = "AM";
                    int displayHour = hourOfDay;
                    if (hourOfDay >= 12) {
                        amPm = "PM";
                        if (hourOfDay > 12) {
                            displayHour = hourOfDay - 12;
                        }
                    } else if (hourOfDay == 0) {
                        displayHour = 12;
                    }

                    selectedTimeStr = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minuteOfHour, amPm);
                    binding.textPickupTime.setText(selectedTimeStr);
                },
                hour,
                minute,
                false // 12-hour format inside standard clock picker UI
        );

        timePickerDialog.show();
    }

    private void resetCalculations() {
        pickupDate = null;
        selectedTimeStr = null;
        selectedHour = -1;
        selectedMinute = -1;
        totalDays = 1;
        
        binding.textPickupDate.setText("Select Date");
        binding.textPickupTime.setText("Select Time");
        updatePrice();
    }

    private void updatePrice() {
        binding.textDaysCount.setText(String.valueOf(totalDays));
        totalPrice = 0.0;
        // Total price calculation and display removed
        // binding.textTotalCost.setText(FirebaseHelper.formatRupees(totalPrice));
    }

    private boolean validateDateTime() {
        String pickupLoc = binding.editPickupLocation.getText().toString().trim();
        String dropoffLoc = binding.editDropoffLocation.getText().toString().trim();
        if (pickupLoc.isEmpty()) {
            Toast.makeText(this, "Please enter a pickup location / address.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dropoffLoc.isEmpty()) {
            Toast.makeText(this, "Please enter a dropoff location / address.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pickupDate == null) {
            Toast.makeText(this, "Please select a pickup date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedTimeStr == null) {
            Toast.makeText(this, "Please select a pickup time.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate time if date is today
        Calendar today = Calendar.getInstance();
        Calendar selected = Calendar.getInstance();
        selected.setTime(pickupDate);
        selected.set(Calendar.HOUR_OF_DAY, selectedHour);
        selected.set(Calendar.MINUTE, selectedMinute);
        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);

        if (selected.before(today)) {
            Toast.makeText(this, "Pickup time cannot be in the past.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void processCheckout() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.err_no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateDateTime()) {
            return;
        }

        String userId = firebaseHelper.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnConfirmBooking.setEnabled(false);

        // Format dates as string representation
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String pickupDateStr = dbFormat.format(pickupDate);

        // Fetch user profile details from Firestore
        firebaseHelper.getUserProfile(userId)
                .addOnCompleteListener(userTask -> {
                    String username = "Traveler";
                    String userEmail = "";
                    String userPhone = "";

                    if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().exists()) {
                        com.example.travelease.model.User user = userTask.getResult().toObject(com.example.travelease.model.User.class);
                        if (user != null) {
                            username = user.getUsername();
                            if (username == null || username.isEmpty()) {
                                username = user.getName();
                            }
                            if (username == null || username.isEmpty()) {
                                username = "Traveler";
                            }
                            userEmail = user.getEmail();
                            userPhone = user.getPhoneNumber();
                        }
                    } else {
                        // Fallback using Firebase auth values
                        com.google.firebase.auth.FirebaseUser fbUser = firebaseHelper.getAuth().getCurrentUser();
                        if (fbUser != null) {
                            username = fbUser.getDisplayName();
                            if (username == null || username.isEmpty()) {
                                username = "Traveler";
                            }
                            userEmail = fbUser.getEmail() != null ? fbUser.getEmail() : "";
                            userPhone = fbUser.getPhoneNumber() != null ? fbUser.getPhoneNumber() : "";
                        }
                    }

                    String pickupLoc = binding.editPickupLocation.getText().toString().trim();
                    String dropoffLoc = binding.editDropoffLocation.getText().toString().trim();
                    String note = binding.editSpecialRequest.getText().toString().trim();

                    // Create booking entity
                    Booking booking = new Booking(
                            "", // Auto-generated inside FirebaseHelper
                            userId,
                            username,
                            userEmail,
                            userPhone,
                            vehicle.getId(),
                            vehicle.getName(),
                            pickupDateStr,
                            selectedTimeStr,
                            totalDays,
                            totalPrice,
                            note,
                            "pending",
                            System.currentTimeMillis(),
                            System.currentTimeMillis()
                    );
                    booking.setPickupLocation(pickupLoc);
                    booking.setDropoffLocation(dropoffLoc);

                    firebaseHelper.saveBooking(booking)
                            .addOnCompleteListener(task -> {
                                binding.btnConfirmBooking.setEnabled(true);
                                if (task.isSuccessful()) {
                                    Toast.makeText(BookingActivity.this, "Booking Confirmed Successfully!", Toast.LENGTH_LONG)
                                            .show();

                                    // Navigate directly to dashboard and select bookings tab
                                    Intent intent = new Intent(BookingActivity.this, MainActivity.class);
                                    intent.putExtra("select_tab", "bookings");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(BookingActivity.this, "Persisting booking failed. Try again.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }
}
