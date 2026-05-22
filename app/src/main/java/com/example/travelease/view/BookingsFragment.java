package com.example.travelease.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.travelease.adapter.BookingAdapter;
import com.example.travelease.databinding.FragmentBookingsBinding;
import com.example.travelease.model.Booking;
import com.example.travelease.util.FirebaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class BookingsFragment extends Fragment implements BookingAdapter.OnBookingCancelClickListener {
    private FragmentBookingsBinding binding;
    private final List<Booking> bookings = new ArrayList<>();
    private BookingAdapter bookingAdapter;
    private ListenerRegistration bookingsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        listenToBookings();
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(bookings, this);
        binding.bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.bookingsRecyclerView.setAdapter(bookingAdapter);
    }

    private void listenToBookings() {
        String userId = FirebaseHelper.getInstance().getCurrentUserId();
        if (userId == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            return;
        }

        // Listen for bookings sorted by createdAt
        bookingsListener = FirebaseHelper.getInstance().getBookingsCollection()
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (error != null || value == null) {
                        // Fallback without orderBy in case composite index is building
                        listenToBookingsFallback(userId);
                        return;
                    }

                    bookings.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Booking b = doc.toObject(Booking.class);
                        if (b != null) {
                            bookings.add(b);
                        }
                    }

                    updateUi();
                });
    }

    private void listenToBookingsFallback(String userId) {
        // Fallback without ordering just in case Firestore index requires creation
        bookingsListener = FirebaseHelper.getInstance().getBookingsCollection()
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (error != null || value == null) {
                        return;
                    }

                    bookings.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Booking b = doc.toObject(Booking.class);
                        if (b != null) {
                            bookings.add(b);
                        }
                    }

                    // Sort manually by createdAt in memory
                    bookings.sort((b1, b2) -> Long.compare(b2.getCreatedAt(), b1.getCreatedAt()));

                    updateUi();
                });
    }

    private void updateUi() {
        if (bookings.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.bookingsRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.bookingsRecyclerView.setVisibility(View.VISIBLE);
        }
        bookingAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelClick(Booking booking) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Reservation")
                .setMessage("Are you sure you want to cancel your luxury booking?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelReservation(booking))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelReservation(Booking booking) {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getInstance().getBookingsCollection().document(booking.getId())
                .update("bookingStatus", "Cancelled")
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Trip Cancelled successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to cancel trip.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bookingsListener != null) {
            bookingsListener.remove();
        }
        binding = null;
    }
}
