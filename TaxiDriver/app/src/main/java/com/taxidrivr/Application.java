package com.taxidrivr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDexApplication;

import com.teliver.sdk.core.Teliver;

public class Application extends MultiDexApplication {

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    @SuppressLint("InlinedApi")
    private static final String FINE_LOC = Manifest.permission.ACCESS_FINE_LOCATION,
            BG_LOC = Manifest.permission.ACCESS_BACKGROUND_LOCATION;

    private static final int PERMISSION_REQ_CODE = 115, GPS_REQ = 124,
            TAKE_PHOTO = 111, FROM_GALLERY = 116;


    @Override
    public void onCreate() {
        super.onCreate();
        Teliver.init(this, "teliver_key");
        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public void storeBooleanInPref(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }


    public boolean getBooleanInPef(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public static boolean checkLPermission(Activity context) {
        try {
            if (isPermissionGranted(context))
                return true;
            String[] permissions = isAndQ() ? new String[]{FINE_LOC, BG_LOC}
                    : new String[]{FINE_LOC};
            if (isShouldShow(context, FINE_LOC))
                showPermission(context, permissions);
            else
                showPermission(context, permissions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void showPermission(Activity context, String... permissions) {
        ActivityCompat.requestPermissions(context, permissions, PERMISSION_REQ_CODE);
    }

    public static boolean isAndQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean isPermissionGranted(Context context) {
        if (isPermissionOk(context, FINE_LOC))
            return !isAndQ() || isPermissionOk(context, BG_LOC);
        else
            return false;
    }

    private static boolean isShouldShow(Activity context, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permission);
    }

    private static boolean isPermissionOk(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isPermissionOk(int... results) {
        boolean isAllGranted = true;
        for (int result : results) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                isAllGranted = false;
                break;
            }
        }
        return isAllGranted;
    }


}
