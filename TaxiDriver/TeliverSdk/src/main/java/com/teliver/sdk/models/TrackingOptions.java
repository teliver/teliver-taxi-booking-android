package com.teliver.sdk.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.GoogleMap;
import com.teliver.sdk.core.TrackingListener;
import com.teliver.sdk.util.TUtils;

import java.util.ArrayList;
import java.util.List;

public class TrackingOptions implements Parcelable {

    private List<MarkerOption> markerOptions = new ArrayList<>();

    private GoogleMap googleMap;

    private String pageTitle;

    private TrackingListener trackingListener;

    public String getPageTitle() {
        return TUtils.clearNull(pageTitle);
    }

    TrackingOptions(String pageTitle, List<MarkerOption> markerOptions, TrackingListener listener, GoogleMap googleMap) {
        this.markerOptions = markerOptions;
        this.trackingListener = listener;
        this.googleMap = googleMap;
        this.pageTitle = pageTitle;
    }

    private TrackingOptions(Parcel in) {
        markerOptions = in.createTypedArrayList(MarkerOption.CREATOR);
        pageTitle = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(markerOptions);
        dest.writeString(pageTitle);
    }

    public List<MarkerOption> getMarkerOptions() {
        return markerOptions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrackingOptions> CREATOR = new Creator<TrackingOptions>() {
        @Override
        public TrackingOptions createFromParcel(Parcel in) {
            return new TrackingOptions(in);
        }

        @Override
        public TrackingOptions[] newArray(int size) {
            return new TrackingOptions[size];
        }
    };

    public TrackingListener getTrackingListener() {
        return trackingListener;
    }

    public void setTrackingListener(TrackingListener trackingListener) {
        this.trackingListener = trackingListener;
    }

    public GoogleMap getMapObject() {
        return googleMap;
    }
}
