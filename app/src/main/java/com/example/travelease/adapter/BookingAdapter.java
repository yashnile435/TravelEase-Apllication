package com.example.travelease.adapter;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelease.R;
import com.example.travelease.databinding.ItemBookingBinding;
import com.example.travelease.model.Booking;
import com.example.travelease.model.Vehicle;
import com.example.travelease.util.FirebaseHelper;

import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private final List<Booking> bookings;
    private final OnBookingCancelClickListener cancelListener;

    public interface OnBookingCancelClickListener {
        void onCancelClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingCancelClickListener cancelListener) {
        this.bookings = bookings;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingBinding binding = ItemBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookingBinding binding;

        ViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Booking booking) {
            binding.datesText.setText(String.format(Locale.getDefault(), "%s at %s • %d %s",
                    booking.getPickupDate(),
                    booking.getPickupTime(),
                    booking.getTotalDays(),
                    booking.getTotalDays() == 1 ? "day" : "days"));
            // Total price display removed as per requirements
            // binding.totalPriceText.setText(FirebaseHelper.formatRupees(booking.getTotalPrice()));
            String status = booking.getBookingStatus();
            if (status == null || status.isEmpty()) {
                status = "pending";
            }
            binding.statusText.setText(status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase());

            if ("pending".equalsIgnoreCase(status)) {
                binding.statusText.setBackgroundResource(R.drawable.badge_confirmed);
                binding.statusText.setTextColor(binding.getRoot().getContext().getColor(R.color.accent_orange));
                binding.cancelBtn.setVisibility(View.VISIBLE);
            } else if ("accepted".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status)) {
                binding.statusText.setBackgroundResource(R.drawable.badge_confirmed);
                binding.statusText.setTextColor(binding.getRoot().getContext().getColor(R.color.success));
                binding.cancelBtn.setVisibility(View.GONE);
            } else if ("rejected".equalsIgnoreCase(status)) {
                binding.statusText.setBackgroundResource(R.drawable.badge_cancelled);
                binding.statusText.setTextColor(binding.getRoot().getContext().getColor(R.color.error));
                binding.cancelBtn.setVisibility(View.GONE);
            } else if ("completed".equalsIgnoreCase(status)) {
                binding.statusText.setBackgroundResource(R.drawable.badge_confirmed);
                binding.statusText.setTextColor(binding.getRoot().getContext().getColor(R.color.primary_blue));
                binding.cancelBtn.setVisibility(View.GONE);
            } else {
                // Cancelled or other
                binding.statusText.setBackgroundResource(R.drawable.badge_cancelled);
                binding.statusText.setTextColor(binding.getRoot().getContext().getColor(R.color.text_medium));
                binding.cancelBtn.setVisibility(View.GONE);
            }

            // Get Vehicle Details in background
            FirebaseHelper.getInstance().getVehiclesCollection().document(booking.getVehicleId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Vehicle vehicle = documentSnapshot.toObject(Vehicle.class);
                        if (vehicle != null) {
                            binding.vehicleName.setText(String.format("%s %s", vehicle.getBrand(), vehicle.getModel()));
                            if (vehicle.getImageBase64() != null && vehicle.getImageBase64().startsWith("data:image")) {
                                try {
                                    String base64Data = vehicle.getImageBase64().substring(vehicle.getImageBase64().indexOf(",") + 1);
                                    byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                                    Glide.with(binding.getRoot().getContext())
                                            .load(decodedString)
                                            .placeholder(R.drawable.placeholder_vehicle)
                                            .into(binding.vehicleImage);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Glide.with(binding.getRoot().getContext())
                                            .load(vehicle.getImageUrl())
                                            .placeholder(R.drawable.placeholder_vehicle)
                                            .into(binding.vehicleImage);
                                }
                            } else {
                                Glide.with(binding.getRoot().getContext())
                                        .load(vehicle.getImageUrl())
                                        .placeholder(R.drawable.placeholder_vehicle)
                                        .into(binding.vehicleImage);
                            }
                        }
                    });

            binding.cancelBtn.setOnClickListener(v -> {
                if (cancelListener != null) {
                    cancelListener.onCancelClick(booking);
                }
            });
        }
    }
}
