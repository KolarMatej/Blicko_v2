package com.example.locationalert.utils;


import android.support.v4.app.FragmentManager;

import com.example.locationalert.location.AddMarkerFragment;
import com.example.locationalert.marker.AddMarkerCallback;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MarkerUtils {

    public static void showMarkerOptionsDialog(FragmentManager manager, LatLng location, AddMarkerCallback callback) {
        AddMarkerFragment fragment = new AddMarkerFragment();
        fragment.setCallback(callback);
        fragment.setLocation(location);

        fragment.show(manager, null);
    }

    public static String getMarkerTime(long timestamp) {
        String format = "HH:mm - dd.MM.";
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());

        return formatter.format(timestamp);
    }
}
