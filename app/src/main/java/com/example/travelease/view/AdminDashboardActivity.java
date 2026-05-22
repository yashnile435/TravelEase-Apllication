package com.example.travelease.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.travelease.adapter.VehicleAdapter;
import com.example.travelease.databinding.ActivityAdminDashboardBinding;
import com.example.travelease.model.Vehicle;
import com.example.travelease.util.FirebaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements VehicleAdapter.OnVehicleClickListener {
    private ActivityAdminDashboardBinding binding;
    private final List<Vehicle> adminVehicles = new ArrayList<>();
    private VehicleAdapter vehicleAdapter;
    private ListenerRegistration vehiclesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        listenToVehicles();

        binding.btnAdminBack.setOnClickListener(v -> finish());
        binding.addVehicleFab.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AddVehicleActivity.class));
        });
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(adminVehicles, this);
        binding.adminVehiclesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.adminVehiclesRecyclerView.setAdapter(vehicleAdapter);
    }

    private void listenToVehicles() {
        vehiclesListener = FirebaseHelper.getInstance().getVehiclesCollection()
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    adminVehicles.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Vehicle v = doc.toObject(Vehicle.class);
                        if (v != null) {
                            adminVehicles.add(v);
                        }
                    }

                    vehicleAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        // Show detailed specifications in details page
        Intent intent = new Intent(this, VehicleDetailActivity.class);
        intent.putExtra("vehicle", vehicle);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Vehicle vehicle, boolean isFavoriteNow) {
        // Toggled from listing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vehiclesListener != null) {
            vehiclesListener.remove();
        }
    }
}
