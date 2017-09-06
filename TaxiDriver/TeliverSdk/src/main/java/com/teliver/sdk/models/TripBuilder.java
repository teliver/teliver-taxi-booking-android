package com.teliver.sdk.models;

import com.google.android.gms.location.LocationRequest;


public class TripBuilder {

    private float distance = 5;

    private long interval = 1000;

    private String trackingId;

    private LocationRequest locationRequest;

    private PushData pushData;

    public TripBuilder(String trackingId) {
        this.trackingId = trackingId;
    }

    public TripBuilder withInterval(long interval) {
        this.interval = interval;
        return this;
    }

    public TripBuilder withDistance(float distance) {
        this.distance = distance;
        return this;
    }


    public TripBuilder withUserPushObject(PushData pushData) {
        this.pushData = pushData;
        return this;
    }

    public TripBuilder withLocationRequest(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
        return this;
    }

    public TripOptions build() {
        TripOptions options = new TripOptions(trackingId);
        options.setInterval(interval);
        options.setDistance(distance);
        options.setPushDataList(pushData);
        options.setLocationRequest(locationRequest);
        return options;
    }
}
