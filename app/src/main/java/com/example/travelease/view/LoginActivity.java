package com.example.travelease.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelease.R;
import com.example.travelease.databinding.ActivityLoginBinding;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.NetworkUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseHelper firebaseHelper;

    // Google Sign-In Activity launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                binding.loadingOverlay.setVisibility(View.GONE);
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        String errMsg = "Google Sign-In failed: ";
                        if (e.getStatusCode() == 7) {
                            errMsg += "Network error. Please check your internet connection.";
                        } else if (e.getStatusCode() == 12500) {
                            errMsg += "Configuration error (check SHA-1 signature in Firebase Console).";
                        } else if (e.getStatusCode() == 12501) {
                            errMsg += "Authentication cancelled.";
                        } else {
                            errMsg += e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Error code " + e.getStatusCode();
                        }
                        Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Sign-In cancelled.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();

        // Check if user is already logged in for session persistence
        if (firebaseHelper.getAuth().getCurrentUser() != null) {
            checkRoleAndNavigate(firebaseHelper.getAuth().getCurrentUser().getUid());
            return;
        }

        setupGoogleSignIn();

        binding.loginBtn.setOnClickListener(v -> handleEmailLogin());
        binding.googleSignInBtn.setOnClickListener(v -> launchGoogleSignIn());
        binding.forgotPasswordBtn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
        binding.btnNavigateRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        setupRealTimeValidation();
    }

    private void setupRealTimeValidation() {
        binding.emailEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.emailInputLayout.setError(null);
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
    }

    private void setupGoogleSignIn() {
        // Use the explicit web client ID found in google-services.json for bulletproof execution
        String clientId = "146880609830-i70kk8decvu3raa1mrnsqe0jitr4pjiv.apps.googleusercontent.com";
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void launchGoogleSignIn() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.err_no_network, Toast.LENGTH_LONG).show();
            return;
        }
        
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.loadingText.setText("Connecting to Google...");
        
        // Force account picker to open every time to prevent cached silent login issues
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.loadingText.setText("Verifying with TravelEase...");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseHelper.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && firebaseHelper.getAuth().getCurrentUser() != null) {
                        String uid = firebaseHelper.getAuth().getCurrentUser().getUid();
                        String name = firebaseHelper.getAuth().getCurrentUser().getDisplayName();
                        String email = firebaseHelper.getAuth().getCurrentUser().getEmail();
                        String photoUrl = "";
                        
                        if (firebaseHelper.getAuth().getCurrentUser().getPhotoUrl() != null) {
                            photoUrl = firebaseHelper.getAuth().getCurrentUser().getPhotoUrl().toString();
                        }
                        
                        // Check exist/create/update logic for Google Sign-In user in Firestore
                        firebaseHelper.handleGoogleSignInUser(uid, name != null ? name : "Traveler", email != null ? email : "", photoUrl)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMsg = dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown Firestore error";
                                        Toast.makeText(LoginActivity.this, "Firestore Sync Error: " + errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                    checkRoleAndNavigate(uid);
                                });
                    } else {
                        binding.loadingOverlay.setVisibility(View.GONE);
                        String errMsg = task.getException() != null ? task.getException().getMessage() : "Google authentication failed.";
                        Toast.makeText(LoginActivity.this, errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleEmailLogin() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.err_no_network, Toast.LENGTH_LONG).show();
            return;
        }

        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        boolean hasError = false;

        if (TextUtils.isEmpty(email)) {
            binding.emailInputLayout.setError("Email cannot be empty");
            hasError = true;
        } else if (!com.example.travelease.util.ValidationHelper.isValidEmail(email)) {
            binding.emailInputLayout.setError("Enter a valid email address ending with .com");
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordInputLayout.setError("Password cannot be empty");
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(this, "Please correct the errors in the form.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show premium progress overlay
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.loadingText.setText("Signing you in...");
        binding.loginBtn.setEnabled(false);

        firebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.loginBtn.setEnabled(true);
                    
                    if (task.isSuccessful() && firebaseHelper.getAuth().getCurrentUser() != null) {
                        String uid = firebaseHelper.getAuth().getCurrentUser().getUid();
                        // Update lastLogin / recreate profile if missing
                        firebaseHelper.updateUserLoginTimestamp(uid, email)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMsg = dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown Firestore error";
                                        Toast.makeText(LoginActivity.this, "Firestore Sync Error: " + errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                    checkRoleAndNavigate(uid);
                                });
                    } else {
                        binding.loadingOverlay.setVisibility(View.GONE);
                        Exception exception = task.getException();
                        String friendlyMsg = "Login Failed: ";
                        
                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            friendlyMsg += "No account found with this email address.";
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            friendlyMsg += "Invalid password. Please try again.";
                        } else if (exception instanceof FirebaseNetworkException) {
                            friendlyMsg += "Network error. Please check your connection.";
                        } else {
                            friendlyMsg += exception != null ? exception.getMessage() : getString(R.string.err_generic);
                        }
                        
                        Toast.makeText(LoginActivity.this, friendlyMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkRoleAndNavigate(String uid) {
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.loadingText.setText("Verifying permissions...");

        firebaseHelper.getUserProfile(uid).addOnCompleteListener(task -> {
            binding.loadingOverlay.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                String status = task.getResult().getString("accountStatus");

                if ("Disabled".equalsIgnoreCase(status)) {
                    firebaseHelper.getAuth().signOut();
                    Toast.makeText(LoginActivity.this, "Your account has been disabled by Admin.", Toast.LENGTH_LONG).show();
                } else if ("admin".equalsIgnoreCase(role)) {
                    Toast.makeText(LoginActivity.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, AdminPanelActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                // If profile not found, default to customer MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
