package com.teliver.sdk.core;

import android.content.Context;

import com.google.firebase.messaging.RemoteMessage;
import com.teliver.sdk.models.PushData;
import com.teliver.sdk.models.TrackingOptions;
import com.teliver.sdk.models.Trip;
import com.teliver.sdk.models.TripBuilder;
import com.teliver.sdk.models.TripOptions;
import com.teliver.sdk.models.User;
import com.teliver.sdk.util.SdkHandler;
import com.teliver.sdk.util.TUtils;
import com.teliver.sdk.util.TeliverMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Teliver {

    private static SdkHandler handler;

    private Teliver() {
        // Single ton Private Constructor
    }

    public static void init(Context context, String apiKey) {
        if (context == null)
            TLog.log("Context should not be null");
        else if (TUtils.clearNull(apiKey).isEmpty())
            TLog.log("APP Key is Null or Empty");
        else {
            handler = SdkHandler.getInstance(context);
            handler.initializeClient(apiKey);
        }
    }

    public static void identifyUser(User user) {
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else handler.identifyDeviceUser(user);
    }

    public static void unregisterUser() {
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else handler.unRegisterUser();
    }

    public static void startTrip(String trackingID) {
        startTrip(new TripBuilder(trackingID).build());
    }

    public static void startTrip(TripOptions options){
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else
            handler.startTripUpdates(options);
    }

    public static void stopTrip(String trackingId){
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else
            handler.stopTripUpdates(trackingId);
    }

    public static void startTracking(TrackingOptions options) {
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else
            handler.startTracking(options);
    }

    public static void stopTracking(String trackingId) {
        List<String> ids = new ArrayList<>();
        ids.add(TUtils.clearNull(trackingId));
        stopTracking(ids);
    }


    public static void stopTracking(List<String> trackingId) {
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else
            handler.stopTracking(trackingId);
    }

    public static void setTripListener(TripListener listener) {
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else
            handler.setListener(listener);
    }

    public static List<Trip> getCurrentTrips() {
        if (handler == null) {
            TLog.log("Teliver Client is not initialized. call init()");
            return null;
        } else return handler.getCurrentTrips();

    }

    public static void sendEventPush(String trackingId, PushData pushData, String tag) {
        sendEventPush(trackingId, pushData);
        tagLocation(trackingId, tag);
    }

    public static void sendEventPush(String trackingId, PushData pushData) {
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else
            handler.sendEventPush(trackingId, pushData);
    }

    public static void tagLocation(String trackingId, String tag) {
        if (handler == null)
            TLog.log("Teliver Client is not initialized. call init()");
        else
            handler.addTag(trackingId, tag);
    }

    public static boolean isTeliverPush(RemoteMessage message) {
        if (message == null || message.getData() == null)
            return false;
        Map<String, String> data = message.getData();
        String key = "is_from";
        return data.containsKey(key) &&
                "Teliver".equalsIgnoreCase(TUtils.clearNull(data.get(key)));
    }

    public static SdkHandler getHandler() {
        return handler;
    }

    public static Class getMap() {
        return TeliverMap.class;
    }
}


