package com.teliver.sdk.models;

import com.google.android.gms.location.LocationRequest;
import com.teliver.sdk.util.TUtils;

public class TripOptions {

    private float distance;

    private long interval;

    private String trackingId;

    private PushData pushData;

    private LocationRequest locationRequest;

    TripOptions(String trackingId) {
        this.trackingId = trackingId;
    }

    public float getDistance() {
        return distance;
    }

    public long getInterval() {
        return interval;
    }

    public String getTrackingId() {
        return TUtils.clearNull(trackingId);
    }

    void setDistance(float distance) {
        this.distance = distance;
    }

    void setInterval(long interval) {
        this.interval = interval;
    }

    void setPushDataList(PushData pushData) {
        this.pushData = pushData;
    }

    public PushData getPushData() {
        return pushData;
    }

    public LocationRequest getLocationRequest() {
        return locationRequest;
    }

    void setLocationRequest(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
    }
}
