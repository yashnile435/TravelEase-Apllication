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
import com.bumptech.glide.Glide;
import com.example.travelease.R;

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
                        if (firebaseHelper.getAuth().getCurrentUser() != null && binding != null) {
                            String name = firebaseHelper.getAuth().getCurrentUser().getDisplayName();
                            String email = firebaseHelper.getAuth().getCurrentUser().getEmail();
                            binding.profileNameText.setText(name != null ? name : "Elite Traveler");
                            binding.profileEmailText.setText(email != null ? email : "");
                            binding.profilePhoneText.setText("Add Mobile Number");

                            // Fallback to Auth Photo Url if available
                            android.net.Uri photoUri = firebaseHelper.getAuth().getCurrentUser().getPhotoUrl();
                            if (photoUri != null && getContext() != null) {
                                binding.avatarImage.setPadding(0, 0, 0, 0);
                                binding.avatarImage.setImageTintList(null);
                                binding.avatarImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);

                                Glide.with(ProfileFragment.this)
                                        .load(photoUri)
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(binding.avatarImage);
                            } else if (getContext() != null) {
                                int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
                                binding.avatarImage.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                                binding.avatarImage.setImageTintList(android.content.res.ColorStateList.valueOf(
                                        androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary_blue)
                                ));
                                binding.avatarImage.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                                binding.avatarImage.setImageResource(R.drawable.ic_profile);
                            }
                        }
                        return;
                    }

                    User user = value.toObject(User.class);
                    if (user != null && binding != null && getContext() != null) {
                        binding.profileNameText.setText(user.getName());
                        binding.profileEmailText.setText(user.getEmail());
                        String phone = user.getPhoneNumber();
                        if (phone == null || phone.trim().isEmpty()) {
                            binding.profilePhoneText.setText("Add Mobile Number");
                        } else {
                            binding.profilePhoneText.setText(phone);
                        }

                        // Load Google/custom profile picture with Glide
                        String photoUrl = user.getProfilePhoto();
                        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                            // Remove image padding and tint for real photo to fill circular layout
                            binding.avatarImage.setPadding(0, 0, 0, 0);
                            binding.avatarImage.setImageTintList(null);
                            binding.avatarImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);

                            Glide.with(ProfileFragment.this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(binding.avatarImage);
                        } else {
                            // Restore default placeholder styling
                            int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
                            binding.avatarImage.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                            binding.avatarImage.setImageTintList(android.content.res.ColorStateList.valueOf(
                                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary_blue)
                            ));
                            binding.avatarImage.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                            binding.avatarImage.setImageResource(R.drawable.ic_profile);
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
