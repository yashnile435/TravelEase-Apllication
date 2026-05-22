package com.example.travelease.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelease.R;
import com.example.travelease.databinding.ActivityForgotPasswordBinding;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.NetworkUtil;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();

        binding.resetBtn.setOnClickListener(v -> handlePasswordReset());
        binding.backToLoginBtn.setOnClickListener(v -> finish());
    }

    private void handlePasswordReset() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.err_no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        String email = binding.emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.err_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.resetBtn.setEnabled(false);

        firebaseHelper.getAuth().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    binding.resetBtn.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Recovery reset email sent successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errMsg = task.getException() != null ? task.getException().getMessage() : getString(R.string.err_generic);
                        Toast.makeText(ForgotPasswordActivity.this, "Reset Failed: " + errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
