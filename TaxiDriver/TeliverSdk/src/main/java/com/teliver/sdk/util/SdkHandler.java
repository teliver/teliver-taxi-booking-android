package com.teliver.sdk.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.teliver.sdk.core.TLog;
import com.teliver.sdk.core.TripListener;
import com.teliver.sdk.models.NotificationData;
import com.teliver.sdk.models.PushData;
import com.teliver.sdk.models.TConstants;
import com.teliver.sdk.models.TResponse;
import com.teliver.sdk.models.TrackingOptions;
import com.teliver.sdk.models.Trip;
import com.teliver.sdk.models.TripOptions;
import com.teliver.sdk.models.User;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.teliver.sdk.util.TUtils.getGson;

public class SdkHandler {

    private TPreference preference;

    private Context context;

    private GoogleApiClient googleApiClient;

    private TService service;

    private int driverRestriction = 1;

    private int customerRestriction = 1;

    private static SdkHandler handler;

    private final MyServiceConnection serviceConnection = new MyServiceConnection();

    private final class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ServiceBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    }

    public static SdkHandler getInstance(Context context) {
        if (handler == null)
            handler = new SdkHandler(context);
        return handler;
    }

    private SdkHandler(Context context) {
        this.context = context;
        preference = new TPreference(context);
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        FirebaseApp.initializeApp(context);
        TUtils.getToken(context);
    }

    public void initializeClient(String apiKey) {
        preference.storeString(TConstants.API_KEY, apiKey);
        if (!TUtils.isNetConnected(context))
            TLog.log("Internet not Connected");
        else {
            preference.storeString(TConstants.T_PUSH_TOKEN, TUtils.clearNull(TUtils.getToken(context)));
            TRestCall tRestCall = new TRestCall(context);
            tRestCall.setCallBackListener(new InitListener());
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("device_details", TUtils.getDeviceInfo(context));
            builder.add("security", TUtils.getSecureInfo(context));
            tRestCall.requestApi("init", builder.build());
        }
    }

    public void unRegisterUser() {
        if (!TUtils.isNetConnected(context))
            TLog.log("Internet is not Connected");
        else {
            TRestCall tRestCall = new TRestCall(context);
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("user_id", preference.getString(TConstants.USER_ID));
            tRestCall.requestApi("unregister_user", builder.build());
        }
    }


    public void identifyDeviceUser(User user) {
        if (user == null)
            TLog.log("User Object is Null");
        else if (user.getId().isEmpty())
            TLog.log("User ID should not be empty");
        else if (!TUtils.isNetConnected(context))
            TLog.log("Internet is not Connected");
        else {
            preference.storeString(TConstants.USER_ID, user.getId());
            TRestCall tRestCall = new TRestCall(context);
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("user_type", user.getType());
            builder.add("user_id", user.getId());
            builder.add("device_details", TUtils.getDeviceInfo(context));
            String pushKey = TUtils.clearNull(TUtils.getToken(context));
            if (user.isRegisteredForPush())
                builder.add("push_key", pushKey);
            tRestCall.setCallBackListener(new TRestCall.ResponseListener() {
                @Override
                public void onResponse(String result) {
                    //startTService();
                }
            });
            tRestCall.requestApi("identify_user", builder.build());
        }
    }

    public void startTripUpdates(TripOptions options) {
        if (options == null)
            TLog.log("Builder is Null");
        else if (options.getTrackingId().isEmpty())
            TLog.log("Please specify Trip Tracking ID");
        else if (!TUtils.isNetConnected(context))
            TLog.log("Internet is not Connected");
        if (!TUtils.isPermissionGranted(context))
            TLog.log("Location Permission not Enabled. Please enable Permission");
        else if (!TUtils.isGpsEnabled(context))
            TLog.log("Please enable GPS");
        else if (service != null)
            service.prepareUpdates(options, preference.getString(TConstants.USER_ID), driverRestriction);
        else TLog.log("Service is null");
    }

    public void stopTripUpdates(String trackingId) {
        if (TUtils.clearNull(trackingId).isEmpty())
            TLog.log("Please provide Tracking id");
        else if (!TUtils.isPermissionGranted(context))
            TLog.log("Location Permission not Enabled. Please enable Permission");
        else if (!TUtils.isNetConnected(context))
            TLog.log("Internet is not Connected");
        else if (service != null)
            service.stopTripUpdates(trackingId);
    }

    public void addTag(String trackingId, String tag) {
        if (TUtils.clearNull(trackingId).isEmpty())
            TLog.log("Please provide Tracking id");
        else if (TUtils.clearNull(tag).isEmpty())
            TLog.log("Please provide Tag");
        else if (service != null)
            service.tagLocation(trackingId, tag);
    }


    public void setListener(TripListener listener) {
        if (service != null && listener != null)
            service.updateListener(listener);
    }

    public List<Trip> getCurrentTrips() {
        if (service != null)
            return service.getCurrentTrips();
        return new ArrayList<>();
    }

    public void startTracking(TrackingOptions options) {
        if (checkBuilder(options))
            return;
       /* if (options.getMarkerOptions().size() > customerRestriction)
            TLog.log("You can Track only up to " + customerRestriction + " Drivers/Agents");*/
        else if (options.getMapObject() == null)
            context.startActivity(new Intent(context, TeliverMap.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(TConstants.DOTS_OBJ, options));
        else Tracker.getInstance(context).startTracking(options);
    }

    public void stopTracking(List<String> trackingIds) {
        if (trackingIds == null || trackingIds.isEmpty())
            TLog.log("Tracking ID should not be empty or null");
        else Tracker.getInstance(context).stopTracking(trackingIds);
    }


    private class InitListener implements TRestCall.ResponseListener {
        @Override
        public void onResponse(String result) {
            try {
                TLog.log(result);
                if (result.isEmpty())
                    return;
                TResponse response = getGson().fromJson(result, TResponse.class);
                if (response.isSuccess()) {
                    preference.storeString(TConstants.AUTH_TOKEN, response.getToken());
                    driverRestriction = response.getDriverRestriction();
                    customerRestriction = response.getCustomerRestriction();
                    startTService();
                } else TLog.log("On Init:Failure->Reason: " + response.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkBuilder(TrackingOptions options) {
        if (options == null || options.getMarkerOptions().isEmpty())
            TLog.log("Marker Options should not be Empty");
        else if (!TUtils.isNetConnected(context))
            TLog.log("Internet is not Connected");
        else return false;
        return true;
    }

    private void startTService() {
        Intent serviceStartIntent = new Intent(context, TService.class);
        context.startService(serviceStartIntent);
        context.bindService(serviceStartIntent, serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void sendEventPush(String trackingId, PushData pushData) {
        try {
            if (pushData == null)
                TLog.log("Push Data should not be null");
            else if (trackingId.isEmpty())
                TLog.log("Trip ID should not be null");
            else if (!TUtils.isNetConnected(context))
                TLog.log("Internet is not Connected");
            else {
                NotificationData data = new NotificationData();
                data.setCommand(TConstants.CMD_EVENT_PUSH);
                data.setMessage(pushData.getMessage());
                data.setPayload(pushData.getPayload());
                data.setTrackingID(trackingId);
                RequestBody body = new FormBody.Builder()
                        .add(TConstants.PUSH_TITLE, pushData.getMessage())
                        .add(TConstants.PUSH_DATA, getGson().toJson(data))
                        .add("trip_id", trackingId)
                        .add(TConstants.PUSH_IDS, getGson().toJson(pushData.getUsers()))
                        .build();
                TRestCall tRestCall = new TRestCall(context);
                tRestCall.requestApi("send_event_push", body);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

}
