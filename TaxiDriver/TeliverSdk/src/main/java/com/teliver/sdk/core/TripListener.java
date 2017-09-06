package com.teliver.sdk.core;

import android.location.Location;

import com.teliver.sdk.models.Trip;

public interface TripListener {

    void onTripStarted(Trip tripDetails);

    void onLocationUpdate(Location location);

    void onTripEnded(String trackingID);

    void onTripError(String reason);
}
