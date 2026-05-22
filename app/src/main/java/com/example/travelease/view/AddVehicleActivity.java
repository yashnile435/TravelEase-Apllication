package com.example.travelease.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.travelease.R;
import com.example.travelease.databinding.ActivityAddVehicleBinding;
import com.example.travelease.model.Vehicle;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.NetworkUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

public class AddVehicleActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1001;
    private ActivityAddVehicleBinding binding;
    private Vehicle existingVehicle;
    private String imageBase64 = "";
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddVehicleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize status spinner with available and unavailable
        String[] statusOptions = new String[]{"available", "unavailable"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statusOptions);
        binding.spinnerStatus.setAdapter(adapter);
        binding.spinnerStatus.setText("available", false); // default to available

        if (getIntent().hasExtra("vehicle")) {
            existingVehicle = (Vehicle) getIntent().getSerializableExtra("vehicle");
            preFillFields();
            binding.toolbarTitle.setText("Update Vehicle");
            binding.btnAddVehicleSave.setText("Update Vehicle Info");
        }

        binding.btnAddVehicleBack.setOnClickListener(v -> finish());
        binding.btnSelectImage.setOnClickListener(v -> selectImageFromGallery());
        binding.btnAddVehicleSave.setOnClickListener(v -> saveVehicleToFirestore());

        // Add a text watcher to automatically preview Image URLs
        binding.inputImageUrl.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (isValidUrl(url)) {
                    selectedImageUri = null; // Clear gallery selection if URL is specified
                    imageBase64 = "";
                    binding.lblImageSelected.setText("Image URL specified");
                    Glide.with(AddVehicleActivity.this)
                            .load(url)
                            .placeholder(R.drawable.placeholder_vehicle)
                            .error(R.drawable.placeholder_vehicle)
                            .into(binding.imagePreview);
                    binding.imagePreview.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url) && android.util.Patterns.WEB_URL.matcher(url).matches();
    }

    private void selectImageFromGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launchImagePicker();
        } else {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1002);
            } else {
                launchImagePicker();
            }
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Vehicle Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to read storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            binding.inputImageUrl.setText(""); // Clear Image URL if device upload is selected
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                Bitmap resizedBitmap = getResizedBitmap(bitmap, 800); // resize to keep under Firestore size limit
                
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                
                imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
                
                binding.imagePreview.setImageBitmap(resizedBitmap);
                binding.imagePreview.setVisibility(View.VISIBLE);
                binding.lblImageSelected.setText("Selected from device gallery");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load and encode image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void preFillFields() {
        if (existingVehicle == null) return;
        binding.inputName.setText(existingVehicle.getName());
        binding.inputDescription.setText(existingVehicle.getDescription());
        binding.inputPassengers.setText(String.valueOf(existingVehicle.getPassengers()));
        binding.inputPricePerKmAC.setText(String.valueOf(existingVehicle.getPricePerKmAC()));
        binding.inputPricePerKmNonAC.setText(String.valueOf(existingVehicle.getPricePerKmNonAC()));
        binding.inputImageUrl.setText(existingVehicle.getImageUrl());
        binding.inputOrder.setText(String.valueOf(existingVehicle.getOrder()));
        binding.spinnerStatus.setText(existingVehicle.getStatus(), false);
        if (!TextUtils.isEmpty(existingVehicle.getTransmission())) {
            binding.inputTransmission.setText(existingVehicle.getTransmission());
        }
        if (!TextUtils.isEmpty(existingVehicle.getFuelType())) {
            binding.inputFuelType.setText(existingVehicle.getFuelType());
        }

        if (!TextUtils.isEmpty(existingVehicle.getImageUrl())) {
            binding.lblImageSelected.setText("Existing Image URL loaded");
            Glide.with(this)
                    .load(existingVehicle.getImageUrl())
                    .placeholder(R.drawable.placeholder_vehicle)
                    .error(R.drawable.placeholder_vehicle)
                    .into(binding.imagePreview);
            binding.imagePreview.setVisibility(View.VISIBLE);
        } else if (!TextUtils.isEmpty(existingVehicle.getImageBase64())) {
            imageBase64 = existingVehicle.getImageBase64();
            try {
                if (imageBase64.contains(",")) {
                    String base64Data = imageBase64.substring(imageBase64.indexOf(",") + 1);
                    byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    binding.imagePreview.setImageBitmap(decodedByte);
                    binding.imagePreview.setVisibility(View.VISIBLE);
                    binding.lblImageSelected.setText("Existing Base64 image loaded");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveVehicleToFirestore() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = binding.inputName.getText().toString().trim();
        String description = binding.inputDescription.getText().toString().trim();
        String passengersStr = binding.inputPassengers.getText().toString().trim();
        String priceAcStr = binding.inputPricePerKmAC.getText().toString().trim();
        String priceNonAcStr = binding.inputPricePerKmNonAC.getText().toString().trim();
        String imageUrl = binding.inputImageUrl.getText().toString().trim();
        String orderStr = binding.inputOrder.getText().toString().trim();
        String status = binding.spinnerStatus.getText().toString().trim();
        String transmission = binding.inputTransmission.getText().toString().trim();
        String fuelType = binding.inputFuelType.getText().toString().trim();

        // REQUIRED FIELDS: Vehicle Name, Price (AC)
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vehicle Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(priceAcStr)) {
            Toast.makeText(this, "Price is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Safe fallback / default values for optional fields
        if (TextUtils.isEmpty(description)) {
            description = "No description available.";
        }
        
        int passengers = 4; // default
        if (!TextUtils.isEmpty(passengersStr)) {
            try {
                passengers = Integer.parseInt(passengersStr);
            } catch (NumberFormatException ignored) {}
        }

        int priceAC = 0;
        try {
            priceAC = Integer.parseInt(priceAcStr);
        } catch (NumberFormatException ignored) {}

        int priceNonAC = priceAC; // fallback to AC price if non-AC is empty
        if (!TextUtils.isEmpty(priceNonAcStr)) {
            try {
                priceNonAcStr = priceNonAcStr.replaceAll("[^0-9]", ""); // sanitize
                if (!priceNonAcStr.isEmpty()) {
                    priceNonAC = Integer.parseInt(priceNonAcStr);
                }
            } catch (NumberFormatException ignored) {}
        }

        double orderValue = 1.0;
        if (!TextUtils.isEmpty(orderStr)) {
            try {
                orderValue = Double.parseDouble(orderStr);
            } catch (NumberFormatException ignored) {}
        }

        if (TextUtils.isEmpty(status)) {
            status = "available";
        }

        binding.btnAddVehicleSave.setEnabled(false);

        String id = existingVehicle != null ? existingVehicle.getId() : FirebaseHelper.getInstance().getVehiclesCollection().document().getId();

        // Prepare Vehicle object (may update imageUrl dynamically after upload completes)
        Vehicle vehicle = new Vehicle(
                id,
                name,
                description,
                imageBase64,
                imageUrl,
                orderValue,
                passengers,
                priceAC,
                priceNonAC,
                status,
                transmission,
                fuelType
        );

        // Handle Image Upload if device gallery image was chosen
        if (selectedImageUri != null) {
            try {
                byte[] imageData = compressImage(selectedImageUri);
                uploadImageAndSaveVehicle(id, imageData, vehicle);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to compress image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.btnAddVehicleSave.setEnabled(true);
            }
        } else {
            // Verify URL is valid if entered
            if (!TextUtils.isEmpty(imageUrl) && !isValidUrl(imageUrl)) {
                Toast.makeText(this, "Invalid Image URL provided.", Toast.LENGTH_SHORT).show();
                binding.btnAddVehicleSave.setEnabled(true);
                return;
            }
            saveToFirestore(vehicle);
        }
    }

    private byte[] compressImage(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 1000);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImageAndSaveVehicle(String vehicleId, byte[] imageData, Vehicle vehicle) {
        binding.uploadProgressBar.setVisibility(View.VISIBLE);
        binding.lblUploadProgress.setVisibility(View.VISIBLE);
        binding.uploadProgressBar.setProgress(0);
        binding.lblUploadProgress.setText("Uploading image: 0%");

        com.google.firebase.storage.StorageReference ref = FirebaseHelper.getInstance().getStorage().getReference()
                .child("vehicle_images/" + vehicleId + ".jpg");

        ref.putBytes(imageData)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    binding.uploadProgressBar.setProgress((int) progress);
                    binding.lblUploadProgress.setText(String.format(Locale.getDefault(), "Uploading image: %.0f%%", progress));
                })
                .addOnFailureListener(e -> {
                    binding.btnAddVehicleSave.setEnabled(true);
                    binding.uploadProgressBar.setVisibility(View.GONE);
                    binding.lblUploadProgress.setVisibility(View.GONE);
                    Toast.makeText(AddVehicleActivity.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                })
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String downloadUrl = downloadUri.toString();
                        vehicle.setImageUrl(downloadUrl);
                        vehicle.setImageBase64(""); // Clear base64 if it has a stored storage URL
                        
                        saveToFirestore(vehicle);
                    }).addOnFailureListener(e -> {
                        binding.btnAddVehicleSave.setEnabled(true);
                        binding.uploadProgressBar.setVisibility(View.GONE);
                        binding.lblUploadProgress.setVisibility(View.GONE);
                        Toast.makeText(AddVehicleActivity.this, "Failed to retrieve image download URL.", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void saveToFirestore(Vehicle vehicle) {
        FirebaseHelper.getInstance().getVehiclesCollection().document(vehicle.getId()).set(vehicle)
                .addOnCompleteListener(task -> {
                    binding.btnAddVehicleSave.setEnabled(true);
                    binding.uploadProgressBar.setVisibility(View.GONE);
                    binding.lblUploadProgress.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String successMsg = existingVehicle != null ? "Vehicle updated successfully!" : "Vehicle added successfully!";
                        Toast.makeText(AddVehicleActivity.this, successMsg, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errMsg = task.getException() != null ? task.getException().getMessage() : "Firestore write failed.";
                        Toast.makeText(AddVehicleActivity.this, "Save Failed: " + errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
