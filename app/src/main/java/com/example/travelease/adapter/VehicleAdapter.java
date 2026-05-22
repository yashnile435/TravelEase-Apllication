package com.example.travelease.adapter;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.travelease.R;
import com.example.travelease.databinding.ItemVehicleBinding;
import com.example.travelease.model.Vehicle;
import com.example.travelease.util.FirebaseHelper;

import java.util.List;
import java.util.Locale;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {
    private final List<Vehicle> vehicles;
    private final OnVehicleClickListener listener;
    private final String userId;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
        void onFavoriteClick(Vehicle vehicle, boolean isFavoriteNow);
    }

    public VehicleAdapter(List<Vehicle> vehicles, OnVehicleClickListener listener) {
        this.vehicles = vehicles;
        this.listener = listener;
        this.userId = FirebaseHelper.getInstance().getCurrentUserId();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleBinding binding = ItemVehicleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(vehicles.get(position));
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemVehicleBinding binding;

        ViewHolder(ItemVehicleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Vehicle vehicle) {
            binding.brandText.setText(vehicle.getBrand().toUpperCase());
            binding.modelText.setText(vehicle.getModel());
            // Price display removed as per requirements
            // binding.priceText.setText(FirebaseHelper.formatRupees(vehicle.getDailyPrice()) + "/km");
            binding.ratingText.setText(String.format(Locale.getDefault(), "%.1f", vehicle.getRating()));

            // Specs
            if (vehicle.getSeats() <= 0) {
                binding.seatsContainer.setVisibility(android.view.View.GONE);
            } else {
                binding.seatsContainer.setVisibility(android.view.View.VISIBLE);
                binding.seatsText.setText(String.format(Locale.getDefault(), "%d Seats", vehicle.getSeats()));
            }

            if (android.text.TextUtils.isEmpty(vehicle.getTransmission())) {
                binding.transmissionContainer.setVisibility(android.view.View.GONE);
            } else {
                binding.transmissionContainer.setVisibility(android.view.View.VISIBLE);
                binding.transmissionText.setText(vehicle.getTransmission());
            }

            if (android.text.TextUtils.isEmpty(vehicle.getFuelType())) {
                binding.fuelContainer.setVisibility(android.view.View.GONE);
            } else {
                binding.fuelContainer.setVisibility(android.view.View.VISIBLE);
                binding.fuelText.setText(vehicle.getFuelType());
            }

            // Load Image
            if (vehicle.getImageBase64() != null && vehicle.getImageBase64().startsWith("data:image")) {
                try {
                    String base64Data = vehicle.getImageBase64().substring(vehicle.getImageBase64().indexOf(",") + 1);
                    byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                    Glide.with(binding.getRoot().getContext())
                            .load(decodedString)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .placeholder(R.drawable.placeholder_vehicle)
                            .error(R.drawable.placeholder_vehicle)
                            .into(binding.vehicleImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    Glide.with(binding.getRoot().getContext())
                            .load(vehicle.getImageUrl())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .placeholder(R.drawable.placeholder_vehicle)
                            .error(R.drawable.placeholder_vehicle)
                            .into(binding.vehicleImage);
                }
            } else {
                Glide.with(binding.getRoot().getContext())
                        .load(vehicle.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.placeholder_vehicle)
                        .error(R.drawable.placeholder_vehicle)
                        .into(binding.vehicleImage);
            }

            // Check favorite state in background
            if (userId != null) {
                FirebaseHelper.getInstance().checkIsFavorite(userId, vehicle.getId())
                        .addOnSuccessListener(isFav -> {
                            if (isFav) {
                                binding.favButton.setImageResource(R.drawable.ic_favorite_filled);
                                binding.favButton.setColorFilter(binding.getRoot().getContext().getColor(R.color.error));
                            } else {
                                binding.favButton.setImageResource(R.drawable.ic_favorite_outline);
                                binding.favButton.setColorFilter(binding.getRoot().getContext().getColor(R.color.text_medium));
                            }
                        });
            }

            // Click listeners
            binding.viewDetailsBtn.setOnClickListener(v -> {
                if (listener != null) listener.onVehicleClick(vehicle);
            });

            binding.favButton.setOnClickListener(v -> {
                if (userId == null) return;
                FirebaseHelper.getInstance().toggleFavorite(userId, vehicle.getId())
                        .addOnSuccessListener(aVoid -> {
                            // Check again to update UI
                            FirebaseHelper.getInstance().checkIsFavorite(userId, vehicle.getId())
                                    .addOnSuccessListener(isFav -> {
                                        if (isFav) {
                                            binding.favButton.setImageResource(R.drawable.ic_favorite_filled);
                                            binding.favButton.setColorFilter(binding.getRoot().getContext().getColor(R.color.error));
                                        } else {
                                            binding.favButton.setImageResource(R.drawable.ic_favorite_outline);
                                            binding.favButton.setColorFilter(binding.getRoot().getContext().getColor(R.color.text_medium));
                                        }
                                        if (listener != null) listener.onFavoriteClick(vehicle, isFav);
                                    });
                        });
            });
        }
    }
}
