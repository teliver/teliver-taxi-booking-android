package com.teliver.sdk.core;

import com.teliver.sdk.models.TLocation;

public interface TrackingListener {

    void onTrackingStarted(String trackingId);

    void onLocationUpdate(String trackingId, TLocation location);

    void onTrackingEnded(String trackingId);

    void onTrackingError(String reason);

}
