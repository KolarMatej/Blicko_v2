package com.example.locationalert.location;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.locationalert.marker.MyMarker;

import java.util.List;

public class LocationHelper implements LocationListener {

    private static final LocationHelper instance = new LocationHelper();

    private LocationCallback locationCallback;

    public static void setLocationCallback(LocationCallback locationCallback) {
        instance.locationCallback = locationCallback;
    }

    public static MyMarker findNearestMarker(Location location, List<MyMarker> markers) {
        float distance = Float.MAX_VALUE; // we set it to max to be sure

        if (!markers.isEmpty()) {
            MyMarker nearest = markers.get(0);

            for (MyMarker marker : markers) {
                Location markerLocation = getLocation(marker, location);

                float markerDistance = location.distanceTo(markerLocation);

                if (markerDistance < distance) {
                    nearest = marker;
                    distance = markerDistance;
                }
            }
            return nearest;
        }

        return new MyMarker("", "", "", location.getLatitude(), location.getLongitude(), 0, 0);
    }

    public static float getDistance(Location location, MyMarker marker) {
        if (location != null && marker != null) {
            Location markerLocation = getLocation(marker, location);

            return location.distanceTo(markerLocation);
        }

        return Float.MAX_VALUE;
    }

    private static Location getLocation(MyMarker marker, Location currentLocation) {
        double latitude = marker != null ? marker.getLatitude() : currentLocation.getLatitude();
        double longitude = marker != null ? marker.getLongitude() : currentLocation.getLongitude();

        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;
    }

    @SuppressLint("MissingPermission")
    public static void getLocation(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (manager != null) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, instance);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && locationCallback != null) {
            locationCallback.onLocationUpdated(location);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}
