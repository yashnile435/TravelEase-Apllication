package com.example.travelease.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.travelease.R;
import com.example.travelease.databinding.ActivityVehicleDetailBinding;
import com.example.travelease.model.Vehicle;
import com.example.travelease.util.FirebaseHelper;
import java.util.Locale;

public class VehicleDetailActivity extends AppCompatActivity {
    private ActivityVehicleDetailBinding binding;
    private Vehicle vehicle;
    private FirebaseHelper firebaseHelper;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        userId = firebaseHelper.getCurrentUserId();

        // Retrieve vehicle from intent extras
        vehicle = (Vehicle) getIntent().getSerializableExtra("vehicle");
        if (vehicle == null) {
            Toast.makeText(this, "Failed to load vehicle specs.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateSpecs();
        checkFavoriteState();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(VehicleDetailActivity.this, BookingActivity.class);
            intent.putExtra("vehicle", vehicle);
            startActivity(intent);
        });

        binding.btnFavDetail.setOnClickListener(v -> toggleFavoriteState());
    }

    private void populateSpecs() {
        binding.detailBrand.setText(vehicle.getBrand().toUpperCase(Locale.ROOT));
        binding.detailName.setText(String.format("%s %s", vehicle.getBrand(), vehicle.getModel()));
        binding.detailRating.setText(String.format(Locale.getDefault(), "%.1f", vehicle.getRating()));

        if (vehicle.getSeats() <= 0) {
            binding.cardSeats.setVisibility(android.view.View.GONE);
        } else {
            binding.cardSeats.setVisibility(android.view.View.VISIBLE);
            binding.detailSeats.setText(String.format(Locale.getDefault(), "%d Seats", vehicle.getSeats()));
        }

        if (android.text.TextUtils.isEmpty(vehicle.getTransmission())) {
            binding.cardTransmission.setVisibility(android.view.View.GONE);
        } else {
            binding.cardTransmission.setVisibility(android.view.View.VISIBLE);
            binding.detailTransmission.setText(vehicle.getTransmission());
        }

        if (android.text.TextUtils.isEmpty(vehicle.getFuelType())) {
            binding.cardFuel.setVisibility(android.view.View.GONE);
        } else {
            binding.cardFuel.setVisibility(android.view.View.VISIBLE);
            binding.detailFuel.setText(vehicle.getFuelType());
        }

        binding.detailDescription.setText(vehicle.getDescription());
        // Price text removed as per requirements
        // binding.detailPriceText.setText(FirebaseHelper.formatRupees(vehicle.getDailyPrice()) + "/km");

        if (vehicle.getImageBase64() != null && vehicle.getImageBase64().startsWith("data:image")) {
            try {
                String base64Data = vehicle.getImageBase64().substring(vehicle.getImageBase64().indexOf(",") + 1);
                byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.placeholder_vehicle)
                        .error(R.drawable.placeholder_vehicle)
                        .into(binding.vehicleImageDetail);
            } catch (Exception e) {
                e.printStackTrace();
                Glide.with(this)
                        .load(vehicle.getImageUrl())
                        .placeholder(R.drawable.placeholder_vehicle)
                        .error(R.drawable.placeholder_vehicle)
                        .into(binding.vehicleImageDetail);
            }
        } else {
            Glide.with(this)
                    .load(vehicle.getImageUrl())
                    .placeholder(R.drawable.placeholder_vehicle)
                    .error(R.drawable.placeholder_vehicle)
                    .into(binding.vehicleImageDetail);
        }
    }

    private void checkFavoriteState() {
        if (userId == null) return;

        firebaseHelper.checkIsFavorite(userId, vehicle.getId())
                .addOnSuccessListener(isFav -> {
                    if (isFav) {
                        binding.favImageDetail.setImageResource(R.drawable.ic_favorite_filled);
                        binding.favImageDetail.setColorFilter(getColor(R.color.error));
                    } else {
                        binding.favImageDetail.setImageResource(R.drawable.ic_favorite_outline);
                        binding.favImageDetail.setColorFilter(getColor(R.color.text_medium));
                    }
                });
    }

    private void toggleFavoriteState() {
        if (userId == null) {
            Toast.makeText(this, "Please sign in to save favorites.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.toggleFavorite(userId, vehicle.getId())
                .addOnSuccessListener(aVoid -> {
                    // Check again to refresh UI
                    checkFavoriteState();
                });
    }
}
