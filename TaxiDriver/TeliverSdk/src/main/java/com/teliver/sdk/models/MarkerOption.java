package com.teliver.sdk.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.teliver.sdk.R;
import com.teliver.sdk.util.TUtils;

public class MarkerOption implements Parcelable {

    private int iconMarker;

    private String markerTitle;

    private String markerSnippet;

    private String trackingId;

    private Bitmap bitmap;

    public MarkerOption(String trackingId) {
        this.trackingId = trackingId;
    }

    private MarkerOption(Parcel in) {
        iconMarker = in.readInt();
        markerTitle = in.readString();
        markerSnippet = in.readString();
        trackingId = in.readString();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<MarkerOption> CREATOR = new Creator<MarkerOption>() {
        @Override
        public MarkerOption createFromParcel(Parcel in) {
            return new MarkerOption(in);
        }

        @Override
        public MarkerOption[] newArray(int size) {
            return new MarkerOption[size];
        }
    };

    public int getIconMarker() {
        return iconMarker == 0 ? R.drawable.marker_red : iconMarker;
    }

    public void setIconMarker(int iconMarker) {
        this.iconMarker = iconMarker;
    }

    public String getMarkerTitle() {
        return TUtils.clearNull(markerTitle);
    }

    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getMarkerSnippet() {
        return TUtils.clearNull(markerSnippet);
    }

    public void setMarkerSnippet(String markerSnippet) {
        this.markerSnippet = markerSnippet;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setIconMarker(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(iconMarker);
        parcel.writeString(markerTitle);
        parcel.writeString(markerSnippet);
        parcel.writeString(trackingId);
        parcel.writeParcelable(bitmap,0);
    }
}
