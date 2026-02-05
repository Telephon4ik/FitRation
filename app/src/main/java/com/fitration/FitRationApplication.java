package com.fitration;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.multidex.MultiDex;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FitRationApplication extends Application {

    private static final String TAG = "FitRationApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate started");

        try {
            // Проверяем доступность Google Play Services
            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                Log.w(TAG, "Google Play Services not available: " + resultCode);
            } else {
                Log.d(TAG, "Google Play Services available");
            }

            // Проверяем, инициализирован ли Firebase
            if (FirebaseApp.getApps(this).isEmpty()) {
                Log.d(TAG, "Initializing Firebase...");
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully");
            } else {
                Log.d(TAG, "Firebase already initialized");
            }

            // Настройка Firestore
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();
            firestore.setFirestoreSettings(settings);
            Log.d(TAG, "Firestore configured successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }
    // В MainActivity или FitRationApplication
    private void checkMemoryUsage() {
        if (BuildConfig.DEBUG) {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double percentUsed = (usedMemory * 100.0) / maxMemory;

            Log.d("Memory", String.format("Memory usage: %.2f MB / %.2f MB (%.1f%%)",
                    usedMemory / (1024.0 * 1024.0),
                    maxMemory / (1024.0 * 1024.0),
                    percentUsed));

            if (percentUsed > 80) {
                Log.w("Memory", "High memory usage! Consider optimizing.");
            }
        }
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}