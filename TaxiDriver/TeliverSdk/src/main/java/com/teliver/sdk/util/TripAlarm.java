package com.teliver.sdk.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.teliver.sdk.core.Teliver;
import com.teliver.sdk.models.TConstants;

public final class TripAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            SdkHandler handler = Teliver.getHandler();
            if (handler == null)
                return;
            String trackingId = TUtils.clearNull(intent.getStringExtra(TConstants.TRIP_DETAILS));
            if (!trackingId.isEmpty())
                handler.stopTripUpdates(trackingId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
