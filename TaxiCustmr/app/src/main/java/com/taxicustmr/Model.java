package com.taxicustmr;


public class Model {

    private String trackingId;

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public Model(String trackingId) {
        this.trackingId = trackingId;
    }
}
