package com.taxicustmr;

import androidx.multidex.MultiDexApplication;

import com.teliver.sdk.core.Teliver;


public class Application extends MultiDexApplication {

    public static final String TRACKING_ID = "tracking_id";


    @Override
    public void onCreate() {
        super.onCreate();
        Teliver.init(this, "teliver_key");
    }
}
