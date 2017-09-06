package com.teliver.sdk.models;


import com.google.android.gms.maps.GoogleMap;
import com.teliver.sdk.core.TrackingListener;

import java.util.ArrayList;
import java.util.List;


public class TrackingBuilder {

    private List<MarkerOption> markerOptions = new ArrayList<>();

    private MarkerOption markerOption;

    private TrackingListener trackingListener;

    private GoogleMap googleMap;

    private String title;

    public TrackingBuilder(MarkerOption markerOption) {
        this.markerOption = markerOption;
    }

    public TrackingBuilder(List<MarkerOption> markerOptions) {
        this.markerOptions = markerOptions;
    }

    public TrackingBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public TrackingBuilder withListener(TrackingListener listener) {
        trackingListener = listener;
        return this;
    }

    public TrackingBuilder withYourMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        return this;
    }

    public TrackingOptions build() {
        if (markerOption != null) {
            markerOptions = new ArrayList<>();
            markerOptions.add(markerOption);
        }
        return new TrackingOptions(title, markerOptions, trackingListener, googleMap);
    }

}
