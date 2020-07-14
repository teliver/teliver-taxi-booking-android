package com.taxidrivr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.teliver.sdk.core.TLog;
import com.teliver.sdk.core.Teliver;
import com.teliver.sdk.core.TripListener;
import com.teliver.sdk.models.PushData;
import com.teliver.sdk.models.Trip;
import com.teliver.sdk.models.TripBuilder;
import com.teliver.sdk.models.UserBuilder;

import java.util.Calendar;

@SuppressWarnings("ALL")
public class ActivityLauncher extends AppCompatActivity implements OnSuccessListener<Location>{

    private static final int GPS_REQ = 124;
    private boolean inCurrentTrip;

    private String driverName = "driver_xolo";

    private GoogleApiClient googleApiClient;

    @Override
    public void onSuccess(Location location) {
        Log.e("onSuccess::",location.getLatitude()+"");
    }

    private enum type {
        trip,
        push
    }

    private Application application;

    private SwitchCompat availabilitySwitch;

    private String trackingId;

    private TextView txtSendPush;

    private EditText edtPushMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Teliver.identifyUser(new UserBuilder(driverName).setUserType(UserBuilder.USER_TYPE.OPERATOR).registerPush().build());
        TLog.setVisible(true);
        Calendar calendar = Calendar.getInstance();
        int month = (calendar.get(Calendar.MONTH) + 1);
        int date = calendar.get(Calendar.DATE);
        trackingId = driverName + date + "/" + month;
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = toolBar.getNavigationIcon();
        drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);

        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        application = (Application) getApplication();
        txtSendPush = (TextView) findViewById(R.id.txtSendPush);
        edtPushMessage = (EditText) findViewById(R.id.edtPushMessage);
        availabilitySwitch = (SwitchCompat) findViewById(R.id.availabilitySwitch);
        availabilitySwitch.setChecked(application.getBooleanInPef("switch_state"));

        availabilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PushData pushData = new PushData("selvakumar");
                    pushData.setMessage("trip started");
                    pushData.setPayload("1");
                    TripBuilder tripBuilder = new TripBuilder(trackingId).
                            withUserPushObject(pushData);
                    Teliver.startTrip(tripBuilder.build());
                    Teliver.setTripListener(new TripListener() {
                        @Override
                        public void onTripStarted(Trip tripDetails) {
                            application.storeBooleanInPref("switch_state", true);
                            Log.d("TELIVER::", "onTripStarted: " + tripDetails.getTrackingId());
                            txtSendPush.setTextColor(ContextCompat.getColor(ActivityLauncher.this, R.color.colorBlack));
                            edtPushMessage.setEnabled(true);
                            edtPushMessage.setHintTextColor(ContextCompat.getColor(ActivityLauncher.this, R.color.colorBackGround));
                        }

                        @Override
                        public void onLocationUpdate(Location location) {
                            Log.d("TELIVER::", "onLocationUpdate: LATLNG VALUES == " + location.getLatitude() + location.getLongitude());
                            toYourServer();
                        }

                        @Override
                        public void onTripEnded(String trackingID) {

                        }

                        @Override
                        public void onTripError(String reason) {

                        }
                    });

                } else if (!isChecked) {
                    application.storeBooleanInPref("switch_state", false);
                    PushData pushData = new PushData("selvakumar");
                    pushData.setMessage("trip_stopped");
                    pushData.setPayload("0");
                    Teliver.sendEventPush(trackingId, pushData, "trip stopped");
                    Teliver.stopTrip(trackingId);
                    Teliver.setTripListener(new TripListener() {
                        @Override
                        public void onTripStarted(Trip tripDetails) {

                        }

                        @Override
                        public void onLocationUpdate(Location location) {

                        }

                        @Override
                        public void onTripEnded(String trackingID) {
                            Log.d(Constants.TAG, "onTripEnded:  ON TRIP ENDED == " + trackingID);
                            txtSendPush.setTextColor(ContextCompat.getColor(ActivityLauncher.this, R.color.colorHint));
                            edtPushMessage.setText("");
                            edtPushMessage.setHintTextColor(ContextCompat.getColor(ActivityLauncher.this, R.color.colorHint));
                            edtPushMessage.setEnabled(false);
                        }

                        @Override
                        public void onTripError(String reason) {

                        }
                    });
                }
            }
        });

        if (Application.checkLPermission(this))
            enableGPS(this);
        
        txtSendPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushData pushData = new PushData("selvakumar");
                pushData.setMessage(edtPushMessage.getText().toString().trim() + ", " + trackingId);
                Teliver.sendEventPush(trackingId, pushData, "taxi");
                edtPushMessage.setText("");
            }
        });
    }

    private void toYourServer() {
        //In the setTripListener callback onLocationUpdate will give you the location values of driver
        // in a certain interval handle  it on your wish..
        // you can get the customer location and show the nearby taxi to your
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == RESULT_OK)
            Toast.makeText(ActivityLauncher.this, "Gps is turned on", Toast.LENGTH_SHORT).show();
        else if (requestCode == 3 && resultCode == RESULT_CANCELED)
            finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==115){
            if (!Application.isPermissionOk(grantResults)){
                Toast.makeText(this,"Location permission denied",Toast.LENGTH_SHORT).show();
                finish();
            }
            else enableGPS(this);
        }
    }

    @Override
    protected void onResume() {
        if (application.getBooleanInPef("switch_state")) {
            edtPushMessage.setHintTextColor(ContextCompat.getColor(this, R.color.colorBackGround));
            edtPushMessage.setEnabled(true);
            txtSendPush.setTextColor(ContextCompat.getColor(this, R.color.colorBlack));
        }
        super.onResume();
    }

    private void fromYourServer() {
        //  Based on the location values show the cabs near to the customer, you can do that by handpicking those trackingId's
// near to the customer and starting the tracking by multiple trackingId's, so that the user can view and track his nearby cabs......
    }


    private void enableGPS(OnSuccessListener<Location> listener) {
        try {
            final FusedLocationProviderClient client = LocationServices
                    .getFusedLocationProviderClient(this);
            final LocationRequest locationRequest = getLocationReq();
            LocationSettingsRequest request = new LocationSettingsRequest
                    .Builder().addLocationRequest(locationRequest)
                    .setAlwaysShow(true).build();
            Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(
                    this).checkLocationSettings(request);
            task.addOnSuccessListener(locationSettingsResponse ->
                    getMyLocation(client, locationRequest, listener));
            task.addOnFailureListener(e -> {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(this, GPS_REQ);
                    } catch (IntentSender.SendIntentException sendEx) {
                        listener.onSuccess(null);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private static void getMyLocation(final FusedLocationProviderClient client
            , LocationRequest locationRequest, final OnSuccessListener<Location> listener) {
        try {
            client.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null)
                        Log.d("TaxiDriver::","result null");
                    else {
                        Location location = locationResult.getLastLocation();
                        client.removeLocationUpdates(this);
                        listener.onSuccess(location);
                    }
                }
            }, Looper.myLooper());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private LocationRequest getLocationReq() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(2000);
        return locationRequest;
    }

}
