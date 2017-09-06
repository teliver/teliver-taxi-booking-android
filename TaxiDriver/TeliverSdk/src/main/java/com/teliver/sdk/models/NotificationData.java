package com.teliver.sdk.models;

import com.google.gson.annotations.SerializedName;

public class NotificationData {

    private String message;

    private String command;

    private String payload;

    @SerializedName("tracking_id")
    private String trackingID;

    public String getMessage() {
        return message;
    }

    public String getCommand() {
        return command;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTrackingID() {
        return trackingID;
    }

    public void setTrackingID(String trackingID) {
        this.trackingID = trackingID;
    }
}
