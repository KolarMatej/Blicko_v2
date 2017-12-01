package com.example.locationalert.marker;


import com.google.android.gms.maps.model.LatLng;

public interface AddMarkerCallback {

    void onAddClick(LatLng location, int markerType);
}
