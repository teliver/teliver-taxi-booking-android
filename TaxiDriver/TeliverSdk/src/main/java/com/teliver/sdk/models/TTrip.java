package com.teliver.sdk.models;

import com.google.gson.annotations.SerializedName;

public class TTrip {

    private String message;

    private boolean success;

    @SerializedName("data")
    private Trip tripData;

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public Trip getTripData() {
        return tripData;
    }


}
