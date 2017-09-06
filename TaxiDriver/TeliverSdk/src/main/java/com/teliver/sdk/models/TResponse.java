package com.teliver.sdk.models;

import com.google.gson.annotations.SerializedName;
import com.teliver.sdk.util.TUtils;

public class TResponse {

    private String message;

    private boolean success;

    private String token;

    @SerializedName("driver_restriction")
    private String driverRestriction;

    @SerializedName("customer_restriction")
    private String customerRestriction;

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getToken() {
        return token;
    }

    public int getDriverRestriction() {
        return TUtils.clearNull(driverRestriction).isEmpty() ? 0 : Integer.parseInt(driverRestriction);
    }

    public int getCustomerRestriction() {
        return TUtils.clearNull(customerRestriction).isEmpty() ? 0 : Integer.parseInt(customerRestriction);
    }
}
