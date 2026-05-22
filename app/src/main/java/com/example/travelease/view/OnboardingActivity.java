package com.example.travelease.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.travelease.R;
import com.example.travelease.databinding.ActivityOnboardingBinding;
import com.example.travelease.databinding.ItemOnboardingSlideBinding;
import com.example.travelease.util.OnboardingManager;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;
    private OnboardingManager onboardingManager;
    private List<OnboardingSlide> slides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        onboardingManager = new OnboardingManager(this);

        setupSlides();
        binding.onboardingViewPager.setAdapter(new OnboardingAdapter(slides));
        setupIndicators();
        setCurrentIndicator(0);

        binding.onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
                if (position == slides.size() - 1) {
                    binding.btnNext.setText(R.string.onboarding_btn_finish);
                    binding.btnSkip.setVisibility(View.GONE);
                } else {
                    binding.btnNext.setText(R.string.onboarding_btn_next);
                    binding.btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            int current = binding.onboardingViewPager.getCurrentItem();
            if (current < slides.size() - 1) {
                binding.onboardingViewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        binding.btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupSlides() {
        slides = new ArrayList<>();
        slides.add(new OnboardingSlide(
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1),
                R.drawable.placeholder_vehicle
        ));
        slides.add(new OnboardingSlide(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                R.drawable.placeholder_vehicle
        ));
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[slides.size()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(getDrawable(R.drawable.chip_category_bg));
            indicators[i].setSelected(false);
            binding.dotsContainer.addView(indicators[i], layoutParams);
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = binding.dotsContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) binding.dotsContainer.getChildAt(i);
            if (i == index) {
                imageView.setSelected(true);
                // Make indicator wider for active tab
                imageView.setLayoutParams(new LinearLayout.LayoutParams(36, 16));
            } else {
                imageView.setSelected(false);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(16, 16));
            }
            // Add margins back
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            lp.setMargins(8, 0, 8, 0);
            imageView.setLayoutParams(lp);
        }
    }

    private void finishOnboarding() {
        onboardingManager.setOnboardingComplete(true);
        startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
        finish();
    }

    // Slide Model
    private static class OnboardingSlide {
        final String title;
        final String desc;
        final int imageRes;

        OnboardingSlide(String title, String desc, int imageRes) {
            this.title = title;
            this.desc = desc;
            this.imageRes = imageRes;
        }
    }

    // Inner ViewPager Adapter
    private static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        private final List<OnboardingSlide> slides;

        OnboardingAdapter(List<OnboardingSlide> slides) {
            this.slides = slides;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemOnboardingSlideBinding b = ItemOnboardingSlideBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OnboardingSlide s = slides.get(position);
            holder.binding.slideTitle.setText(s.title);
            holder.binding.slideDescription.setText(s.desc);
            holder.binding.slideImage.setImageResource(s.imageRes);
        }

        @Override
        public int getItemCount() {
            return slides.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemOnboardingSlideBinding binding;

            ViewHolder(ItemOnboardingSlideBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
