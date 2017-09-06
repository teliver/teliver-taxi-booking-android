package com.taxicustmr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.multidex.MultiDexApplication;

import com.bumptech.glide.Glide;
import com.teliver.sdk.core.Teliver;


public class Application extends MultiDexApplication {

    public static final String TRACKING_ID = "tracking_id";


    @Override
    public void onCreate() {
        super.onCreate();
        Teliver.init(this, "teliver_key");
    }

    public static Bitmap getBitmapIcon(Context context) {
        try {
            return Glide.with(context).load(R.drawable.ic_notification_icon).
                    asBitmap().into(144, 144).get();
        } catch (Exception e) {
            return null;
        }
    }
}
