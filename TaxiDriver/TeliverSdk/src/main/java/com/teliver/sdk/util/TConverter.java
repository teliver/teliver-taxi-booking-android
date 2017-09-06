package com.teliver.sdk.util;

import android.content.Context;
import android.location.Location;

import com.google.gson.GsonBuilder;

import com.teliver.sdk.models.TGeoLocation;

import okhttp3.FormBody;

final class TConverter implements TRestCall.ResponseListener {

    private final Context context;

    private Converter converter;

    TConverter(Context context) {
        this.context = context;
    }

    interface Converter {
        void onLocationString(String location);
    }

    void convertLatLng(Location location, Converter converter) {
        try {
            if (location == null)
                onResponse("");
            String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
            String data = location.getLatitude() + "," + location.getLongitude() + "&sensor=true";
            this.converter = converter;
            TRestCall tRestCall = new TRestCall(context);
            FormBody.Builder builder = new FormBody.Builder();
            tRestCall.setCallBackListener(this);
            tRestCall.requestApi(url, data, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(String result) {
        if (converter == null)
            return;
        try {
            if (result.isEmpty())
                converter.onLocationString("");
            else {
                TGeoLocation location = new GsonBuilder().create().fromJson(result, TGeoLocation.class);
                if (location == null)
                    converter.onLocationString("");
                else
                    converter.onLocationString(location.getAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
            converter.onLocationString("");
        }
    }
}
