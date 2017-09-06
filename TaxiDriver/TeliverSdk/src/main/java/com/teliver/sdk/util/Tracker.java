package com.teliver.sdk.util;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.GsonBuilder;
import com.teliver.sdk.core.TLog;
import com.teliver.sdk.core.TrackingListener;
import com.teliver.sdk.models.MarkerOption;
import com.teliver.sdk.models.TConstants;
import com.teliver.sdk.models.TLocation;
import com.teliver.sdk.models.TMessage;
import com.teliver.sdk.models.TrackingIDStatus;
import com.teliver.sdk.models.TrackingOptions;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.FormBody;

import static com.teliver.sdk.models.TConstants.TYPE_LOCATION;


class Tracker implements MqttCallback, TRestCall.ResponseListener, IMqttActionListener {

    private Context context;

    private GoogleMap map;

    private TrackingListener trackingListener;

    private HashMap<String, Marker> markers;

    private TrackingOptions trackingOptions;

    private static Tracker tracker;

    private MessageClient messageClient;

    static Tracker getInstance(Context context) {
        if (tracker == null)
            tracker = new Tracker(context);
        return tracker;
    }

    private Tracker(Context context) {
        this.context = context;
        messageClient = MessageClient.getInstance(context);
        messageClient.setConnectionListener(this);
        messageClient.setCallbackListener(this);
        TUtils.connectClient(context, messageClient);
    }

    void startTracking(TrackingOptions trackingOptions) {
        this.trackingOptions = trackingOptions;
        this.map = trackingOptions.getMapObject();
        trackingListener = trackingOptions.getTrackingListener();
        markers = new HashMap<>();
        TRestCall restCall = new TRestCall(context);
        restCall.setCallBackListener(this);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add(TConstants.PUSH_IDS, constructIds());
        restCall.requestApi("check_trip", builder.build());
    }

    @Override
    public void onResponse(String result) {
        try {
            if (result.isEmpty()) {
                TLog.log("Could not initiating live tracking. Please try again.");
                return;
            }
            TrackingIDStatus status = TUtils.getGson().fromJson(result, TrackingIDStatus.class);
            if (!status.isSuccess()) {
                TLog.log(status.getMessage());
                sendFailureCallBack(status.getMessage());
                return;
            }
            if (!messageClient.isConnected()) {
                TLog.log("Client not connected after api success::");
                return;
            }
            for (TrackingIDStatus.IDStatus idStatus : status.getStatusList()) {
                String trackingID = idStatus.getTrackingId();
                if (!idStatus.isActive())
                    TLog.log("Tracking ID : " + trackingID + " is not active");
                sendSuccessCallBack(trackingID);
                messageClient.subscribeTopic(trackingID, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        TLog.log("Client Connected:");
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        sendFailureCallBack(exception.getMessage());
    }


    @Override
    public void connectionLost(Throwable cause) {
        tracker = null;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String jsonData = new String(message.getPayload());
        TMessage tMessage = new GsonBuilder().create().fromJson(jsonData, TMessage.class);
        String trackingId = tMessage.getTrackingId();
        if (trackingListener != null) {
            if (TYPE_LOCATION == tMessage.getType())
                trackingListener.onLocationUpdate(trackingId, tMessage.getLocation());
            else {
                List<String> ids = new ArrayList<>();
                ids.add(trackingId);
                stopTracking(ids);
                trackingListener.onTrackingEnded(trackingId);
            }
        }
        if (map != null)
            findAndUpdateMarker(trackingId, tMessage);
    }


    private void findAndUpdateMarker(String trackingId, TMessage tMessage) {
        try {
            if (TYPE_LOCATION == tMessage.getType())
                updateMarker(trackingId, tMessage.getLocation());
            else if (markers.containsKey(trackingId)) {
                markers.get(trackingId).remove();
                markers.remove(trackingId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMarker(String trackingId, TLocation location) {
        if (markers.containsKey(trackingId)) {
            location.setBearing(location.getBearing());
            TUtils.animateMarker(map, markers.get(trackingId), location);
        } else {
            for (MarkerOption markerOption : trackingOptions.getMarkerOptions()) {
                if (!trackingId.equals(markerOption.getTrackingId()))
                    continue;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions options = new MarkerOptions().position(latLng);
                if (markerOption.getBitmap() != null)
                    options.icon(BitmapDescriptorFactory.fromBitmap(markerOption.getBitmap()));
                else
                    options.icon(BitmapDescriptorFactory.fromResource(markerOption.getIconMarker()));
                Marker marker = map.addMarker(options);
                marker.setTitle(markerOption.getMarkerTitle());
                marker.setSnippet(markerOption.getMarkerSnippet());
                markers.put(trackingId, marker);
            }
        }
        TUtils.maintainCameraPosition(context, markers, map);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //Code not Added
    }


    void stopTracking(List<String> ids) {
        if (!messageClient.isConnected())
            return;
        for (String trackingId : ids) {
            trackingId = TUtils.clearNull(trackingId);
            if (!trackingId.isEmpty())
                messageClient.unSubscribe(trackingId);
        }
    }

    void disconnect(TrackingOptions options) {
        if (!messageClient.isConnected())
            return;
        for (MarkerOption option : options.getMarkerOptions())
            messageClient.unSubscribe(option.getTrackingId());
        messageClient.disconnect();
    }


    private String constructIds() {
        String data = "";
        for (MarkerOption option : trackingOptions.getMarkerOptions())
            data += option.getTrackingId() + ",";
        return data;
    }

    private void sendSuccessCallBack(String data) {
        if (trackingListener != null)
            trackingListener.onTrackingStarted(data);
    }

    private void sendFailureCallBack(String data) {
        if (trackingListener != null)
            trackingListener.onTrackingError(data);
    }

}
