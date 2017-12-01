package com.example.locationalert;


import android.app.Application;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;

public class App extends Application {

    private static SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        preferences = getSharedPreferences("LocationAlertPrefs", MODE_PRIVATE);
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }
}
