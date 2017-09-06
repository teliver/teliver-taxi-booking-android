package com.teliver.sdk.models;

 public class TripHistory {

    private double latitude;

    private double longitude;

    private String eventId;

    public TripHistory(double latitude, double longitude, String eventId){
        this.latitude=latitude;
        this.longitude=longitude;
        this.eventId=eventId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

}
