package com.teliver.sdk.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TGeoLocation {

    @SerializedName("results")
    private List<LocationString> address;

    private class LocationString {

        @SerializedName("formatted_address")
        String address;

        String getAddress() {
            return address;
        }
    }

    public String getAddress() {
        if (address == null || address.isEmpty())
            return "";
        return address.get(0).getAddress();
    }
}
