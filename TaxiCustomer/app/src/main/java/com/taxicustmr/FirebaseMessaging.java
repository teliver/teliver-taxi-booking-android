package com.taxicustmr;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
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
                final NotificationData data = new GsonBuilder().create().fromJson(pushData.get("description"), NotificationData.class);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("msg", data.getMessage());
                intent.putExtra(Application.TRACKING_ID, data.getTrackingID());
                intent.putExtra("payload", data.getPayload());
                intent.setAction("tripId");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                sendPush(data);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPush(NotificationData data) {

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setContentTitle("Teliver");
        notification.setContentText(data.getMessage());
        notification.setSmallIcon(R.drawable.ic_notification_icon);
        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(data.getMessage()).setBigContentTitle("Teliver"));

        notification.setAutoCancel(true);
        notification.setPriority(Notification.PRIORITY_MAX);
        notification.setDefaults(Notification.DEFAULT_ALL);


        if (!data.getPayload().equals("0")) {
            Intent intent = new Intent();
            intent.setAction("teliverMap");
            intent.putExtra("msg", data.getMessage());
            intent.putExtra(Application.TRACKING_ID, data.getTrackingID());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            notification.setContentIntent(pendingIntent);
            notification.addAction(R.drawable.ic_run, "Start Tracking", pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification.build());
    }
}


