package com.example.locationalert.activity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.locationalert.App;
import com.example.locationalert.R;
import com.example.locationalert.location.LocationCallback;
import com.example.locationalert.location.LocationHelper;
import com.example.locationalert.marker.AddMarkerCallback;
import com.example.locationalert.marker.MyMarker;
import com.example.locationalert.utils.MarkerUtils;
import com.example.locationalert.utils.StringUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationCallback, GoogleMap.OnMapClickListener, View.OnClickListener, AddMarkerCallback, ChildEventListener {

    @BindView(R.id.focus_button)
    FloatingActionButton focusButton;

    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private Location myLocation;

    private Handler handler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            removeOldMarkers(markers);
            handler.postDelayed(timerRunnable, 60000);
        }
    };

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final List<MyMarker> markers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        LocationHelper.setLocationCallback(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        focusButton.setOnClickListener(this);
    }

    private void focusToNearestMarker() {
        if (myLocation != null) {
            MyMarker nearest = LocationHelper.findNearestMarker(myLocation, markers);

            CameraUpdate focus = CameraUpdateFactory.newLatLngZoom(new LatLng(nearest.getLatitude(), nearest.getLongitude()), 15);

            googleMap.animateCamera(focus);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        setupMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 50 && grantResults[0] == PERMISSION_GRANTED) {
            setupMap();
        }
    }

    private void setupMap() {
        int fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocation != PERMISSION_GRANTED && coarseLocation != PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

            ActivityCompat.requestPermissions(this, permissions, 50);
        } else {
            LocationHelper.getLocation(this);
            googleMap.setOnMapClickListener(this);
            googleMap.setMyLocationEnabled(true);
            googleMap.setTrafficEnabled(true);
            database.getReference().addChildEventListener(this);
            timerRunnable.run();
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        this.myLocation = location;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        MarkerUtils.showMarkerOptionsDialog(getSupportFragmentManager(), latLng, this);
    }

    @Override
    public void onClick(View view) {
        focusToNearestMarker();
    }

    @Override
    public void onAddClick(LatLng location, int markerType) {
        addMarkerToFirebase(location, markerType);
    }

    private void addMarker(MyMarker myMarker) {
        markers.add(myMarker);

        MarkerOptions options = new MarkerOptions();

        String markerTitle;
        int markerIcon;

        if (myMarker.getMarkerType() == 1) {
            markerTitle = getString(R.string.police_marker_title);
            markerIcon = R.drawable.ic_police_marker;
        } else if (myMarker.getMarkerType() == 2) {
            markerTitle = getString(R.string.accident_marker_title);
            markerIcon = R.drawable.ic_accident_marker;
        } else {
            markerTitle = getString(R.string.road_work_marker_title);
            markerIcon = R.drawable.ic_road_work_marker;
        }

        options.title(markerTitle);
        options.snippet("Postavio: " + myMarker.getUserName() + " - vrijeme: " + MarkerUtils.getMarkerTime(myMarker.getTimestamp()));
        options.position(new LatLng(myMarker.getLatitude(), myMarker.getLongitude()));
        options.icon(BitmapDescriptorFactory.fromResource(markerIcon));
        googleMap.addMarker(options);
    }

    private void addMarkerToFirebase(LatLng location, int markerType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            SharedPreferences preferences = App.getPreferences();

            DatabaseReference newMarker = database.getReference().push();
            MyMarker marker = new MyMarker(newMarker.getKey(), user.getUid(), preferences.getString(getString(R.string.username_key), ""), location.latitude, location.longitude, System.currentTimeMillis(), markerType);

            newMarker.setValue(marker);
        }
    }

    public void removeOldMarkers(List<MyMarker> markers) {
        long currentTime = System.currentTimeMillis();
        long threeHours = 1000 * 3 * 60 * 60;
        long day = 1000 * 24 * 60 * 60;

        for (MyMarker marker : markers) {
            if ((currentTime > marker.getTimestamp() + threeHours && marker.getMarkerType() == 1)
                    || (currentTime > marker.getTimestamp() + day && marker.getMarkerType() == 3)) {
                removeMarker(marker.getId());
            }
        }

        refreshMap();
    }

    private void refreshMap() {
        googleMap.clear();
        database.getReference().removeEventListener(this);
        database.getReference().addChildEventListener(this);
    }

    private void removeMarker(String id) {
        if (id != null) {
            database.getReference().child(id).setValue(null);
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        try {
            MyMarker marker = dataSnapshot.getValue(MyMarker.class);
            addMarker(marker);
            showNotification(marker);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void showNotification(MyMarker marker) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myUserId = user != null ? user.getUid() : "";
        float distance = LocationHelper.getDistance(myLocation, marker);

        if (marker != null && !StringUtils.areEqual(myUserId, marker.getUserId()) && distance < 500) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= 26 && manager != null) {
                NotificationChannel channel = new NotificationChannel("BlickoChannel", "Blicko", NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                manager.createNotificationChannel(channel);
            }

            Notification.Builder notificationBuilder = new Notification.Builder(this)
                    .setGroup("BlickoGroup")
                    .setSmallIcon(R.drawable.common_full_open_on_phone)
                    .setContentTitle("Blicko")
                    .setContentText("Dodana je nova lokacija.")
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setLights(Color.RED, 3000, 3000);

            if (Build.VERSION.SDK_INT >= 26) {
                notificationBuilder.setChannelId("BlickoChannel");
            }

            Intent notificationIntent = new Intent(this, MapActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(contentIntent);

            if (manager != null) {
                manager.notify(0, notificationBuilder.build());
            }
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
}
