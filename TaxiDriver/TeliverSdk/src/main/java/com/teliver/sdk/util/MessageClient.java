package com.teliver.sdk.util;

import android.content.Context;

import com.teliver.sdk.core.TLog;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

class MessageClient {

    private MqttAndroidClient client;

    private IMqttActionListener listener;

    private Context context;

    private static MessageClient messageClient;

    private static final String MSG_SERVER_URI = "tcp://mqtt.teliver.xyz:5678";

    static MessageClient getInstance(Context context) {
        if (messageClient == null)
            messageClient = new MessageClient(context);
        return messageClient;
    }

    private MessageClient(Context context) {
        TLog.log("Connecting msg client::");
        this.context = context;
        client = new MqttAndroidClient(context, MSG_SERVER_URI, TUtils.getDeviceId(context),
                MqttAndroidClient.Ack.MANUAL_ACK);
    }

    void setCallbackListener(MqttCallback callbackListener) {
        client.setCallback(callbackListener);
    }

    void setConnectionListener(IMqttActionListener listener) {
        this.listener = listener;
    }

    boolean isConnected() {
        return client.isConnected();
    }

    void connect() {
        try {
            if (isConnected())
                TLog.log("Client already connected");
            else
                client.connect(TUtils.options(context), this, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void disconnect() {
        try {
            TLog.log("Disconnecting client");
            if (isConnected())
                client.disconnect();
            messageClient = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void publishData(String trackingId, MqttMessage mqttMessage, IMqttActionListener listener) {
        try {
            if (!isConnected()) {
                TLog.log("Sending Location error:Client not connected::");
                return;
            }
            client.publish(trackingId, mqttMessage, false, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void subscribeTopic(String trackingId, IMqttActionListener listener) {
        try {
            client.subscribe(trackingId, 1, context, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void unSubscribe(String trackingId) {
        try {
            client.unsubscribe(trackingId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
