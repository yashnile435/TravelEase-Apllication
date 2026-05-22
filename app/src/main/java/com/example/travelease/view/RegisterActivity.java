package com.example.travelease.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelease.R;
import com.example.travelease.databinding.ActivityRegisterBinding;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.NetworkUtil;
import com.example.travelease.util.ValidationHelper;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();

        binding.registerBtn.setOnClickListener(v -> handleRegistration());
        binding.btnNavigateLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

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

        binding.emailEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.emailInputLayout.setError(null);
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

        binding.passwordEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.passwordInputLayout.setError(null);
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

    private void handleRegistration() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.err_no_network, Toast.LENGTH_LONG).show();
            return;
        }

        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

        boolean hasError = false;

        if (TextUtils.isEmpty(name)) {
            binding.nameInputLayout.setError("Name cannot be empty");
            hasError = true;
        }

        if (TextUtils.isEmpty(email)) {
            binding.emailInputLayout.setError("Email cannot be empty");
            hasError = true;
        } else if (!ValidationHelper.isValidEmail(email)) {
            binding.emailInputLayout.setError("Enter a valid email address ending with .com");
            hasError = true;
        }

        if (TextUtils.isEmpty(phone)) {
            binding.phoneInputLayout.setError("Phone number cannot be empty");
            hasError = true;
        } else if (!ValidationHelper.isValidMobileNumber(phone)) {
            binding.phoneInputLayout.setError("Enter a valid 10-digit mobile number");
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordInputLayout.setError("Password cannot be empty");
            hasError = true;
        } else if (password.length() < 6) {
            binding.passwordInputLayout.setError("Password must be at least 6 characters");
            hasError = true;
        } else if (!ValidationHelper.isValidPassword(password)) {
            binding.passwordInputLayout.setError("Password must contain uppercase, lowercase, and numeric characters");
            hasError = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordInputLayout.setError("Confirm password cannot be empty");
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            binding.confirmPasswordInputLayout.setError("Passwords do not match");
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(this, "Please correct the errors in the form.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show premium progress overlay
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.loadingText.setText("Creating your elite profile...");
        binding.registerBtn.setEnabled(false);

        firebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseHelper.getAuth().getCurrentUser() != null) {
                        String uid = firebaseHelper.getAuth().getCurrentUser().getUid();
                        
                        // Save user details with phone number to Firestore "users" collection
                        firebaseHelper.registerNewUser(uid, name, email, phone)
                                .addOnCompleteListener(dbTask -> {
                                    binding.registerBtn.setEnabled(true);
                                    binding.loadingOverlay.setVisibility(View.GONE);
                                    
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity(); // Clear backstack entirely
                                    } else {
                                        String errorMsg = dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown Firestore error";
                                        Toast.makeText(RegisterActivity.this, "Firestore Error: " + errorMsg, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    }
                                });
                    } else {
                        binding.registerBtn.setEnabled(true);
                        binding.loadingOverlay.setVisibility(View.GONE);
                        
                        Exception exception = task.getException();
                        String friendlyMsg = "Registration Failed: ";
                        
                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            friendlyMsg += "An account already exists with this email address.";
                        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                            friendlyMsg += "The password is too weak.";
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            friendlyMsg += "Invalid email format.";
                        } else if (exception instanceof FirebaseNetworkException) {
                            friendlyMsg += "Network error. Please check your connection.";
                        } else {
                            friendlyMsg += exception != null ? exception.getMessage() : getString(R.string.err_generic);
                        }
                        
                        Toast.makeText(RegisterActivity.this, friendlyMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
