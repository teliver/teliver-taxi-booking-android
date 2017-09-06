package com.teliver.sdk.util;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Point;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teliver.sdk.BuildConfig;
import com.teliver.sdk.core.TLog;
import com.teliver.sdk.models.TConstants;
import com.teliver.sdk.models.TLocation;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;

public final class TUtils implements TConstants {


    static boolean isNetConnected(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected();
    }

    @NonNull
    public static String clearNull(String stringValue) {
        if (stringValue == null)
            return "";
        else
            return stringValue.trim();
    }

    static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        return !((!TextUtils.isEmpty(provider)) &&
                LocationManager.PASSIVE_PROVIDER.equals(provider));
    }

    static boolean isPermissionGranted(Context context) {
        return (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    static void animateMarker(final GoogleMap map, final Marker marker, final TLocation destination) {
        if (marker != null) {
            final float startRotation = marker.getRotation();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(600);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = animation.getAnimatedFraction();
                    marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    LatLng newPosition = new LatLng(destination.getLatitude(), destination.getLongitude());
                    animateMarkers(map, marker, newPosition);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            valueAnimator.start();
        }
    }


    private static void animateMarkers(final GoogleMap map, final Marker marker, final LatLng toPosition) {
        try {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            Projection projection = map.getProjection();
            Point startPoint = projection.toScreenLocation(marker.getPosition());
            final LatLng startLatLng = projection.fromScreenLocation(startPoint);
            final long duration = 1000;
            final Interpolator interpolator = new LinearInterpolator();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed
                            / duration);
                    double lng = t * toPosition.longitude + (1 - t)
                            * startLatLng.longitude;
                    double lat = t * toPosition.latitude + (1 - t)
                            * startLatLng.latitude;
                    LatLng position = new LatLng(lat, lng);
                    marker.setPosition(position);
                    if (t < 1)
                        handler.postDelayed(this, 16);
                    else
                        marker.setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }
        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    static void maintainCameraPosition(Context context, HashMap<String, Marker> markers, GoogleMap mMap) {
        if (markers.isEmpty())
            return;
        CameraUpdate cu;
        if (markers.size() == 1) {
            cu = CameraUpdateFactory.newLatLngZoom(markers.values().iterator().next().getPosition(), 16);
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers.values()) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int width = context.getResources().getDisplayMetrics().widthPixels;
            int height = context.getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10);
            cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        }
        mMap.animateCamera(cu);
    }

    static String getDeviceInfo(Context context) {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("device_id", getDeviceId(context));
            deviceInfo.put("device_model", Build.MODEL);
            deviceInfo.put("device_brand", Build.BOARD);
            deviceInfo.put("device_country", Locale.getDefault());
            deviceInfo.put("device_os_v", Build.VERSION.RELEASE);
            deviceInfo.put("device_type", "android");
            return deviceInfo.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    static String getSecureInfo(Context context) {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("package_name", context.getPackageName());
            deviceInfo.put("sha1", getCertificateSHA1Fingerprint(context));
            deviceInfo.put("version", BuildConfig.VERSION_CODE);
            return deviceInfo.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @SuppressLint("HardwareIds")
    public static MqttConnectOptions options(Context context) {
        TPreference preference = new TPreference(context);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(getDeviceId(context));
        mqttConnectOptions.setPassword(preference.getString(TConstants.API_KEY).toCharArray());
        return mqttConnectOptions;
    }

    public static Gson getGson() {
        return new GsonBuilder().create();
    }

    static String getToken(Context context) {
        if (!FirebaseApp.getApps(context).isEmpty())
            return FirebaseInstanceId.getInstance().getToken();
        else return null;
    }

    private static String getCertificateSHA1Fingerprint(Context mContext) {
        try {
            PackageManager pm = mContext.getPackageManager();
            String packageName = mContext.getPackageName();
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] signatures = packageInfo.signatures;
            byte[] cert = signatures[0].toByteArray();
            InputStream input = new ByteArrayInputStream(cert);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate c = (X509Certificate) cf.generateCertificate(input);
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getEncoded());
            return byte2HexFormatted(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }

    static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    static void connectClient(Context context, MessageClient client) {
        if (client.isConnected())
            TLog.log("Msg Client already connected");
        else if (!TUtils.isNetConnected(context))
            TLog.log("No internet connection");
        else
            client.connect();
    }

}
