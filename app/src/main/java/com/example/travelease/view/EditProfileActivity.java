package com.example.travelease.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelease.databinding.ActivityEditProfileBinding;
import com.example.travelease.model.User;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.NetworkUtil;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private FirebaseHelper firebaseHelper;
    private FirebaseUser currentUser;
    private User firestoreUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        currentUser = firebaseHelper.getAuth().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.backBtn.setOnClickListener(v -> finish());
        binding.saveBtn.setOnClickListener(v -> handleSaveChanges());

        loadUserData();
        setupRealTimeValidation();
    }

    private void setupRealTimeValidation() {
        binding.nameEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.nameInputLayout.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.phoneEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.phoneInputLayout.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.oldPasswordEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.oldPasswordInputLayout.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.newPasswordEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.newPasswordInputLayout.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.confirmPasswordEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.confirmPasswordInputLayout.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void loadUserData() {
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.loadingText.setText("Loading user profile...");

        firebaseHelper.getUsersCollection().document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        firestoreUser = task.getResult().toObject(User.class);
                        if (firestoreUser != null) {
                            binding.nameEditText.setText(firestoreUser.getName());
                            binding.phoneEditText.setText(firestoreUser.getPhoneNumber());

                            // Hide password section for Google users
                            if ("Google".equalsIgnoreCase(firestoreUser.getLoginProvider())) {
                                binding.passwordSectionContainer.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        // Fallback to Auth data if Firestore doc is missing
                        binding.nameEditText.setText(currentUser.getDisplayName());
                        binding.phoneEditText.setText("");
                        
                        // Hide password section if Google provider is detected in auth provider list
                        for (com.google.firebase.auth.UserInfo userInfo : currentUser.getProviderData()) {
                            if ("google.com".equalsIgnoreCase(userInfo.getProviderId())) {
                                binding.passwordSectionContainer.setVisibility(View.GONE);
                                break;
                            }
                        }
                    }
                });
    }

    private void handleSaveChanges() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            return;
        }

        String newName = binding.nameEditText.getText().toString().trim();
        String newPhone = binding.phoneEditText.getText().toString().trim();

        boolean hasError = false;

        if (TextUtils.isEmpty(newName)) {
            binding.nameInputLayout.setError("Name cannot be empty");
            hasError = true;
        }

        if (TextUtils.isEmpty(newPhone)) {
            binding.phoneInputLayout.setError("Phone number cannot be empty");
            hasError = true;
        } else if (!com.example.travelease.util.ValidationHelper.isValidMobileNumber(newPhone)) {
            binding.phoneInputLayout.setError("Enter a valid 10-digit mobile number");
            hasError = true;
        }

        boolean changePassword = false;
        String oldPassword = "";
        String newPassword = "";
        String confirmPassword = "";

        if (binding.passwordSectionContainer.getVisibility() == View.VISIBLE) {
            oldPassword = binding.oldPasswordEditText.getText().toString().trim();
            newPassword = binding.newPasswordEditText.getText().toString().trim();
            confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

            if (!TextUtils.isEmpty(oldPassword) || !TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmPassword)) {
                changePassword = true;

                if (TextUtils.isEmpty(oldPassword)) {
                    binding.oldPasswordInputLayout.setError("Please enter your current password");
                    hasError = true;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    binding.newPasswordInputLayout.setError("New password cannot be empty");
                    hasError = true;
                } else if (newPassword.length() < 6) {
                    binding.newPasswordInputLayout.setError("New password must be at least 6 characters");
                    hasError = true;
                } else if (!com.example.travelease.util.ValidationHelper.isValidPassword(newPassword)) {
                    binding.newPasswordInputLayout.setError("Password must contain uppercase, lowercase, and numeric characters");
                    hasError = true;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    binding.confirmPasswordInputLayout.setError("Confirm password cannot be empty");
                    hasError = true;
                } else if (!newPassword.equals(confirmPassword)) {
                    binding.confirmPasswordInputLayout.setError("Passwords do not match");
                    hasError = true;
                }
            }
        }

        if (hasError) {
            Toast.makeText(this, "Please correct the errors in the form.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.loadingText.setText("Saving changes...");

        if (changePassword) {
            final String finalNewName = newName;
            final String finalNewPhone = newPhone;
            final String finalNewPassword = newPassword;

            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser.updatePassword(finalNewPassword)
                                    .addOnCompleteListener(pwTask -> {
                                        if (pwTask.isSuccessful()) {
                                            updateProfileAndFirestore(finalNewName, finalNewPhone);
                                        } else {
                                            binding.loadingOverlay.setVisibility(View.GONE);
                                            String errMsg = pwTask.getException() != null ? pwTask.getException().getMessage() : "Password update failed.";
                                            Toast.makeText(EditProfileActivity.this, errMsg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            binding.loadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(EditProfileActivity.this, "Authentication failed. Incorrect current password.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            updateProfileAndFirestore(newName, newPhone);
        }
    }

    private void updateProfileAndFirestore(String newName, String newPhone) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(profileTask -> {
                    long now = System.currentTimeMillis();
                    
                    firebaseHelper.getUsersCollection().document(currentUser.getUid())
                            .update(
                                    "name", newName,
                                    "username", newName,
                                    "phoneNumber", newPhone,
                                    "updatedAt", now
                            )
                            .addOnCompleteListener(dbTask -> {
                                binding.loadingOverlay.setVisibility(View.GONE);
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    String errorMsg = dbTask.getException() != null ? dbTask.getException().getMessage() : "Sync failed.";
                                    Toast.makeText(EditProfileActivity.this, "Sync error: " + errorMsg, Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }
}
