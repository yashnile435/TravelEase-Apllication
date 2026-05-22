package com.example.travelease.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.travelease.databinding.FragmentProfileBinding;
import com.example.travelease.model.User;
import com.example.travelease.util.FirebaseHelper;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseHelper firebaseHelper;
    private ListenerRegistration userListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseHelper = FirebaseHelper.getInstance();

        loadProfileDetails();
        loadTravelStats();

        binding.logoutBtn.setOnClickListener(v -> handleLogout());
        binding.editProfileRow.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });
    }

    private void loadProfileDetails() {
        String userId = firebaseHelper.getCurrentUserId();
        if (userId == null) return;

        // Use real-time snapshot listener on the user's specific profile
        userListener = firebaseHelper.getUsersCollection().document(userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || !value.exists()) {
                        // In case profile is missing in firestore, fallback to Auth values
                        if (firebaseHelper.getAuth().getCurrentUser() != null) {
                            String name = firebaseHelper.getAuth().getCurrentUser().getDisplayName();
                            String email = firebaseHelper.getAuth().getCurrentUser().getEmail();
                            binding.profileNameText.setText(name != null ? name : "Elite Traveler");
                            binding.profileEmailText.setText(email != null ? email : "");
                            binding.profilePhoneText.setText("Add Mobile Number");
                        }
                        return;
                    }

                    User user = value.toObject(User.class);
                    if (user != null && binding != null) {
                        binding.profileNameText.setText(user.getName());
                        binding.profileEmailText.setText(user.getEmail());
                        String phone = user.getPhoneNumber();
                        if (phone == null || phone.trim().isEmpty()) {
                            binding.profilePhoneText.setText("Add Mobile Number");
                        } else {
                            binding.profilePhoneText.setText(phone);
                        }
                    }
                });
    }

    private void loadTravelStats() {
        String userId = firebaseHelper.getCurrentUserId();
        if (userId == null) return;

        // Query total trips booked
        firebaseHelper.getBookingsCollection().whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding != null) {
                        binding.tripsCountText.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });

        // Query total favorites wishlisted
        firebaseHelper.getFavoritesCollection().whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding != null) {
                        binding.favoritesCountText.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });
    }

    private void handleLogout() {
        firebaseHelper.getAuth().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
        }
        binding = null;
    }
}
