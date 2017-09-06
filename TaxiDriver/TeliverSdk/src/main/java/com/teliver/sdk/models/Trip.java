package com.teliver.sdk.models;

import com.google.gson.annotations.SerializedName;

public class Trip {

        @SerializedName("tracking_id")
        private String trackingId;

        @SerializedName("start_location")
        private String startLocation;

        @SerializedName("end_location")
        private String endLocation;

        @SerializedName("agent_id")
        private String agentId;

        @SerializedName("tenant_id")
        private String tenantId;

        @SerializedName("_id")
        private String tripId;

        private String ttl;

        public String getTrackingId() {
            return trackingId;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getTripId() {
            return tripId;
        }

        public String getTtl() {
                return ttl;
        }
}
