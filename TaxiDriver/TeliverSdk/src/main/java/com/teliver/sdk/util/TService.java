package com.teliver.sdk.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.teliver.sdk.R;
import com.teliver.sdk.core.TLog;
import com.teliver.sdk.core.Teliver;
import com.teliver.sdk.core.TripListener;
import com.teliver.sdk.models.NotificationData;
import com.teliver.sdk.models.PushData;
import com.teliver.sdk.models.TConstants;
import com.teliver.sdk.models.TLocation;
import com.teliver.sdk.models.TMessage;
import com.teliver.sdk.models.TTrip;
import com.teliver.sdk.models.Trip;
import com.teliver.sdk.models.TripHistory;
import com.teliver.sdk.models.TripOptions;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.teliver.sdk.models.TConstants.TYPE_CMD;
import static com.teliver.sdk.models.TConstants.TYPE_LOCATION;
import static com.teliver.sdk.util.TUtils.getGson;


public class TService extends Service implements LocationListener, IMqttActionListener {

    private GoogleApiClient googleApiClient;

    private ServiceBinder binder;

    private TripListener tripListener;

    private int driverRestriction = 1;

    private String agentId;

    private Location lastKnown;

    private MessageClient messageClient;

    private Map<String, Trip> currentTrips = new HashMap<>();

    private Location lastUsed = null, lastSent = null;

    private float lastDifference = 0.0f, lastBearing = 0.0f;

    @Override
    public void onCreate() {
        super.onCreate();
        SdkHandler handler = Teliver.getHandler();
        if (handler == null) {
            TLog.log("Teliver Client is not initialized. call init()");
            stopSelf();
        } else {
            binder = new ServiceBinder(this);
            googleApiClient = handler.getGoogleApiClient();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        messageClient = MessageClient.getInstance(this);
        messageClient.setConnectionListener(this);
        TUtils.connectClient(this, messageClient);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void prepareUpdates(final TripOptions options, final String agentId, int driverRestriction) {
        try {
            this.agentId = agentId;
            this.driverRestriction = driverRestriction;
            final Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location != null) {
                TConverter converter = new TConverter(this);
                converter.convertLatLng(location, new TConverter.Converter() {
                    @Override
                    public void onLocationString(String address) {
                        String startsAt = location.getLatitude() + "," + location.getLongitude();
                        callStartTrip(options, startsAt, address);
                    }
                });
            } else
                callStartTrip(options, "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callStartTrip(TripOptions options, String startsAt, String address) {
        TRestCall tRestCall = new TRestCall(this);
        tRestCall.setCallBackListener(new TripStartListener(options));
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("agent_id", TUtils.clearNull(agentId));
        builder.add("start_location", address);
        builder.add("starts_at", startsAt);
        builder.add("tracking_id", options.getTrackingId());
        tRestCall.requestApi("trip", builder.build());
    }

    public void stopTripUpdates(final String trackingId) {
        try {
            if (googleApiClient == null) {
                return;
            }
            sendMessage(createPayload(trackingId), trackingId);
            stopForeground(true);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            final Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location != null) {
                TConverter converter = new TConverter(this);
                converter.convertLatLng(location, new TConverter.Converter() {
                    @Override
                    public void onLocationString(String address) {
                        String endsAt = location.getLatitude() + "," + location.getLongitude();
                        stopUpdates(trackingId, endsAt, address);
                    }
                });
            } else stopUpdates(trackingId, "", "");
            stopAlarm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopUpdates(String id, String endsAt, String address) {
        TRestCall tRestCall = new TRestCall(this);
        tRestCall.setHttpType(TRestCall.HTTP_TYPE.TYPE_PATCH);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("end_location", address);
        builder.add("ends_at", endsAt);
        builder.add("tracking_id", id);
        builder.add("agent_id", TUtils.clearNull(agentId));
        tRestCall.setCallBackListener(new TripStopListener(id));
        tRestCall.requestApi("trip", builder.build());
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        TLog.log("Client Success:");
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        TLog.log("Client Failure:" + exception.getMessage());
    }

    private class TripStartListener implements TRestCall.ResponseListener {

        private TripOptions options;

        private Gson gson;

        TripStartListener(TripOptions options) {
            this.options = options;
            gson = getGson();
        }

        @Override
        public void onResponse(String result) {
            try {
                if (result.isEmpty() && tripListener != null) {
                    tripListener.onTripError("Server Error");
                    return;
                }
                TLog.log(result);
                TTrip response = gson.fromJson(result, TTrip.class);
                if (response.isSuccess()) {
                    currentTrips.put(response.getTripData().getTrackingId(), response.getTripData());
                    if (tripListener != null)
                        tripListener.onTripStarted(response.getTripData());
                    sendTripPush();
                    showForeGroundInfo(options);
                    setAlarmToStopTrip(response.getTripData().getTrackingId());
                } else if (tripListener != null)
                    tripListener.onTripError(response.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendTripPush() {
            try {
                PushData pushData = options.getPushData();
                if (pushData == null || pushData.getUsers().length == 0)
                    return;
                NotificationData data = new NotificationData();
                data.setCommand(TConstants.CMD_TRIP_START);
                data.setMessage(pushData.getMessage());
                data.setPayload(pushData.getPayload());
                data.setTrackingID(options.getTrackingId());
                TLog.log(gson.toJson(data));
                RequestBody body = new FormBody.Builder()
                        .add(TConstants.PUSH_TITLE, pushData.getMessage())
                        .add(TConstants.PUSH_DATA, gson.toJson(data))
                        .add(TConstants.PUSH_IDS, gson.toJson(pushData.getUsers()))
                        .build();
                TRestCall tRestCall = new TRestCall(TService.this);
                tRestCall.setCallBackListener(new TRestCall.ResponseListener() {
                    @Override
                    public void onResponse(String result) {
                        TLog.log("PUSH RESULT::>>>" + result);
                    }
                });
                tRestCall.requestApi("send_push", body);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setAlarmToStopTrip(String trackingId) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, TripAlarm.class);
            intent.putExtra(TConstants.TRIP_DETAILS, trackingId);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            long timeInMills = System.currentTimeMillis() + driverRestriction * 60 * 60 * 1000;
            alarmManager.set(AlarmManager.RTC, timeInMills, alarmIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class TripStopListener implements TRestCall.ResponseListener {

        String trackingId;

        TripStopListener(String trackingId) {
            this.trackingId = trackingId;
        }

        @Override
        public void onResponse(String result) {
            try {
                currentTrips.remove(trackingId);
                if (tripListener != null)
                    tripListener.onTripEnded(trackingId);
                if (currentTrips.size() == 0)
                    stopSelf();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void showForeGroundInfo(TripOptions tripOptions) {
        try {
            if (googleApiClient == null || !googleApiClient.isConnected()) {
                TLog.log("Google client is not Connected");
                return;
            }
            LocationRequest locationRequest = tripOptions.getLocationRequest();
            if (locationRequest == null) {
                locationRequest = LocationRequest.create();
                locationRequest.setInterval(tripOptions.getInterval());
                locationRequest.setSmallestDisplacement(tripOptions.getDistance());
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, TService.this);
            NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(this);
            notBuilder.setContentTitle(getString(R.string.app_name));
            notBuilder.setContentText("Your Trip is Active");
            notBuilder.setSmallIcon(getApplicationContext().getResources().
                    getIdentifier("ic_launcher", "mipmap", getApplicationContext().getPackageName()));
            notBuilder.setAutoCancel(false);
            notBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notBuilder.setContentIntent(pendingIntent);
            startForeground(909, notBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null)
            return;
        if (location.getAccuracy() >= 2 && location.getAccuracy() <= 30) {
            processLocation(location);
            if (tripListener != null)
                tripListener.onLocationUpdate(location);
        }
    }

    public void updateListener(TripListener listener) {
        this.tripListener = listener;
    }

    public List<Trip> getCurrentTrips() {
        return new ArrayList<>(currentTrips.values());
    }

    private String createLocationPayload(Location location, Trip trip, boolean shouldSave,
                                         Location saveLocation, String tag) {
        TMessage tMessage = new TMessage();
        tMessage.setType(TYPE_LOCATION);
        tMessage.setTrackingId(trip.getTrackingId());
        TLocation tLocation = new TLocation();
        tLocation.setLatitude(location.getLatitude());
        tLocation.setLongitude(location.getLongitude());
        if (lastKnown != null)
            tLocation.setBearing(lastKnown.bearingTo(location));
        tMessage.setLocation(tLocation);
        tMessage.setShouldSave(shouldSave);
        tMessage.setTripId(trip.getTripId());
        tMessage.setTtl(trip.getTtl());
        if (shouldSave) {
            tMessage.setTripHistory(new TripHistory(saveLocation.getLatitude(), saveLocation.getLongitude(), tag));
        }
        String payload = getGson().toJson(tMessage);
        TLog.log(payload);
        return getGson().toJson(tMessage);
    }

    private String createPayload(String trackingId) throws JSONException {
        TMessage message = new TMessage();
        message.setType(TYPE_CMD);
        message.setTrackingId(trackingId);
        return getGson().toJson(message);
    }

    private void sendMessage(String payload, String trackingId) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setPayload(payload.getBytes());
        messageClient.publishData(trackingId, mqttMessage, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                TLog.log("onSuccess");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                try {
                    Gson gson = getGson();
                    TMessage message = gson.fromJson(new String(((IMqttDeliveryToken) asyncActionToken).getMessage().getPayload(), "UTF-8"), TMessage.class);
                    stopTripUpdates(message.getTrackingId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void stopAlarm() {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, TripAlarm.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            alarmManager.cancel(alarmIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tagLocation(String trackingId, String tag) {
        Location tagLoc = lastKnown;
        if (tagLoc == null)
            tagLoc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if ((currentTrips.get(trackingId) != null || (currentTrips.get(trackingId) == null
                && currentTrips.containsKey(trackingId))) && tagLoc != null)
            sendMessage(createLocationPayload(tagLoc, currentTrips.get(trackingId), true, tagLoc, tag), trackingId);
        else TLog.log("Couldn't get Current Location or Trip with the given id");
    }

    private void processLocation(Location currentLocation) {
        float ms = 0.0f;
        if (lastKnown != null) {
            long diffInMs = currentLocation.getTime() - lastKnown.getTime();
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
            float distance = lastKnown.distanceTo(currentLocation);
            ms = distance / diffInSec;
        }
        if (ms < 100.0f) {
            boolean shouldSave = false;
            if (lastSent == null) {
                lastSent = currentLocation;
                shouldSave = true;
            } else {
                float currentBearing = lastSent.bearingTo(currentLocation);
                if (Float.compare(0.0f, lastBearing) != 0) {
                    float currentDifference = Math.abs(lastBearing - currentBearing);
                    if (Float.compare(0.0f, lastDifference) != 0) {
                        float difference = Math.abs(lastDifference - currentDifference);
                        int compare = Float.compare(difference, 5.0f);
                        if (compare > 0 && lastUsed != null) {
                            lastSent.set(lastUsed);
                            lastDifference = 0.0f;
                            shouldSave = true;
                        } else
                            lastUsed = currentLocation;
                    } else
                        lastDifference = currentDifference;
                } else
                    lastBearing = currentBearing;
            }
            Set<String> trackingIds = currentTrips.keySet();
            for (String trackingId : trackingIds) {
                sendMessage(createLocationPayload(currentLocation, currentTrips.get(trackingId), shouldSave, lastSent, ""), trackingId);
            }
        }
        lastKnown = currentLocation;
    }
}
