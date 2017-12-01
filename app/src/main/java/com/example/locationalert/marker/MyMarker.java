package com.example.locationalert.marker;

public class MyMarker {

    private String id;
    private String userId;
    private String userName;
    private double latitude;
    private double longitude;
    private long timestamp;
    private int markerType;

    public MyMarker(String id, String userId, String userName, double latitude, double longitude, long timestamp, int markerType) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.markerType = markerType;
    }

    public MyMarker() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMarkerType() {
        return markerType;
    }

    public String getUserName() {
        return userName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMarkerType(int markerType) {
        this.markerType = markerType;
    }
}
