package com.example.travelease.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.travelease.R;
import com.example.travelease.adapter.CategoryAdapter;
import com.example.travelease.adapter.VehicleAdapter;
import com.example.travelease.databinding.FragmentExploreBinding;
import com.example.travelease.model.Vehicle;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.NetworkUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExploreFragment extends Fragment implements VehicleAdapter.OnVehicleClickListener {
    private FragmentExploreBinding binding;
    private List<Vehicle> allVehicles = new ArrayList<>();
    private List<Vehicle> filteredVehicles = new ArrayList<>();
    private VehicleAdapter vehicleAdapter;
    private ListenerRegistration vehiclesListener;
    private String selectedCategory = "All";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategoriesRecyclerView();
        setupVehiclesRecyclerView();
        setupSearchListener();
        listenToVehicles();
    }

    private void setupCategoriesRecyclerView() {
        List<String> categories = Arrays.asList("All", "Luxury", "SUV", "Electric", "Sport");
        List<Integer> icons = Arrays.asList(
                R.drawable.ic_explore,
                R.drawable.ic_profile,
                R.drawable.ic_explore,
                R.drawable.ic_fuel,
                R.drawable.ic_transmission
        );

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, icons, (category, position) -> {
            selectedCategory = category;
            applyFilters();
        });

        binding.categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupVehiclesRecyclerView() {
        vehicleAdapter = new VehicleAdapter(filteredVehicles, this);
        binding.vehiclesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.vehiclesRecyclerView.setAdapter(vehicleAdapter);
    }

    private void setupSearchListener() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void listenToVehicles() {
        vehiclesListener = FirebaseHelper.getInstance().getVehiclesCollection()
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    allVehicles.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Vehicle v = doc.toObject(Vehicle.class);
                        if (v != null) {
                            allVehicles.add(v);
                        }
                    }

                    // Hide shimmer, show items list
                    binding.shimmerLayout.stopShimmer();
                    binding.shimmerLayout.setVisibility(View.GONE);
                    binding.vehiclesRecyclerView.setVisibility(View.VISIBLE);

                    applyFilters();
                });
    }

    private void applyFilters() {
        filteredVehicles.clear();
        for (Vehicle v : allVehicles) {
            boolean matchesCategory = selectedCategory.equalsIgnoreCase("All") || 
                                      v.getCategory().equalsIgnoreCase(selectedCategory);
            boolean matchesSearch = v.getBrand().toLowerCase().contains(searchQuery) ||
                                    v.getModel().toLowerCase().contains(searchQuery) ||
                                    v.getCategory().toLowerCase().contains(searchQuery);

            if (matchesCategory && matchesSearch) {
                filteredVehicles.add(v);
            }
        }

        vehicleAdapter.notifyDataSetChanged();

        if (filteredVehicles.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.vehiclesRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.vehiclesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        Intent intent = new Intent(getActivity(), VehicleDetailActivity.class);
        intent.putExtra("vehicle", vehicle);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Vehicle vehicle, boolean isFavoriteNow) {
        // Favorites updated successfully in DB. We can show a simple toast or update states.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (vehiclesListener != null) {
            vehiclesListener.remove();
        }
        binding = null;
    }
}
