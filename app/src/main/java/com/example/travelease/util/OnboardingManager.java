package com.example.travelease.util;

import android.content.Context;
import android.content.SharedPreferences;

public class OnboardingManager {
    private static final String PREF_NAME = "travelease_prefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";
    private final SharedPreferences sharedPreferences;

    public OnboardingManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setOnboardingComplete(boolean complete) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply();
    }

    public boolean isOnboardingComplete() {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }
}
