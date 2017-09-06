package com.teliver.sdk.models;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
public class TMessage {

    private int type;

    private TLocation location;

    @SerializedName("tracking_id")
    private String trackingId;

    private TripHistory tripHistory;

    private boolean shouldSave;

    private String tripId;

    private String ttl;

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLocation(TLocation location) {
        this.location = location;
    }

    public int getType() {
        return type;
    }

    public TLocation getLocation() {
        return location;
    }

    public void setTripHistory(TripHistory tripHistory) {
        this.tripHistory = tripHistory;
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

}