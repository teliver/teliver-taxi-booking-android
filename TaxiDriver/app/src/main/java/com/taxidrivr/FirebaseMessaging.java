package com.taxidrivr;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.GsonBuilder;
import com.teliver.sdk.core.Teliver;
import com.teliver.sdk.models.NotificationData;

import java.util.Map;

public class FirebaseMessaging extends FirebaseMessagingService {

    private Application application;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            application = (Application) getApplicationContext();
            if (Teliver.isTeliverPush(remoteMessage)) {
                Map<String, String> pushData = remoteMessage.getData();
                Log.d("TELIVER::", "PUSH MESSAGE == " + remoteMessage.getData());
                final NotificationData data = new GsonBuilder().create().fromJson(pushData.get("description"), NotificationData.class);
                Log.d("TELIVER::", "PUSH MESSAGE == " + data.getTrackingID() + "message == " + data.getMessage() + "command == "
                        + data.getCommand() + data.getPayload());
                Intent intent = new Intent(this, ActivityLauncher.class);
                intent.putExtra("msg", data.getMessage());
                intent.putExtra("tracking_id", data.getTrackingID());
                intent.putExtra("payload", data.getPayload());
                intent.setAction("tripId");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


