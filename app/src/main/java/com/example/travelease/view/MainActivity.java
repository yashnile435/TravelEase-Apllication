package com.example.travelease.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.travelease.R;
import com.example.travelease.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup default fragment
        if (savedInstanceState == null) {
            String selectTab = getIntent().getStringExtra("select_tab");
            if ("bookings".equals(selectTab)) {
                loadFragment(new BookingsFragment(), "bookings");
                binding.bottomNavigation.setSelectedItemId(R.id.nav_bookings);
            } else {
                loadFragment(new ExploreFragment(), "explore");
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;
            String tag = "";

            if (id == R.id.nav_explore) {
                selectedFragment = new ExploreFragment();
                tag = "explore";
            } else if (id == R.id.nav_bookings) {
                selectedFragment = new BookingsFragment();
                tag = "bookings";
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                tag = "profile";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, tag);
                return true;
            }
            return false;
        });
    }

    public void selectNavigationItem(int menuItemId) {
        binding.bottomNavigation.setSelectedItemId(menuItemId);
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment, tag);
        transaction.commit();
    }
}
