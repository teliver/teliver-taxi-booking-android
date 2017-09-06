package com.teliver.sdk.models;

import com.google.gson.annotations.SerializedName;
import com.teliver.sdk.util.TUtils;

import java.util.List;

public class TrackingIDStatus extends TResponse {


    @SerializedName("data")
    private List<IDStatus> statusList;

    public List<IDStatus> getStatusList() {
        return statusList;
    }

    public class IDStatus {

        @SerializedName("_id")
        private String trackingId;

        private String status;

        public String getTrackingId() {
            return trackingId;
        }

        public boolean isActive() {
            return TUtils.clearNull(status).equals("1");
        }
    }

}
