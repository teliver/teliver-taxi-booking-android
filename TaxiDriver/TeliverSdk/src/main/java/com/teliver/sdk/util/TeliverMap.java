package com.teliver.sdk.util;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.teliver.sdk.R;
import com.teliver.sdk.core.TLog;
import com.teliver.sdk.core.TrackingListener;
import com.teliver.sdk.models.MarkerOption;
import com.teliver.sdk.models.TConstants;
import com.teliver.sdk.models.TLocation;
import com.teliver.sdk.models.TrackingOptions;

import java.util.HashMap;


public class TeliverMap extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, View.OnClickListener, TConstants, TrackingListener {

    private GoogleMap googleMap;

    private GoogleApiClient googleApiClient;

    private HashMap<String, Marker> markers;

    private TrackingOptions trackingOptions;

    private Tracker tracker;

    private String permission = Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        trackingOptions = getIntent().getParcelableExtra(TConstants.DOTS_OBJ);
        String title = trackingOptions.getPageTitle();
        actionBar.setTitle(title.isEmpty() ? getString(R.string.app_name) : title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btn_user_gps).setOnClickListener(this);
        markers = new HashMap<>();
        trackingOptions.setTrackingListener(this);
        tracker = Tracker.getInstance(this);
        tracker.startTracking(trackingOptions);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        if (isPermissionGranted())
            initMapClient();
    }

    private void initMapClient() {
        try {
            googleMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
            if (TUtils.isGpsEnabled(this))
                googleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        enableLocation(locationRequest);
    }

    private void enableLocation(LocationRequest locationRequest) {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    try {
                        Status status = locationSettingsResult.getStatus();
                        if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS)
                            moveToLocation();
                        else if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED)
                            status.startResolutionForResult(TeliverMap.this, PERMISSION_ENABLE_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_user_gps && checkPermission())
            if (!googleApiClient.isConnected() && !googleApiClient.isConnecting())
                googleApiClient.connect();
            else
                moveToLocation();

    }

    private void moveToLocation() {
        final Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            initMapClient();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_ENABLE_CODE)
            moveToLocation();
    }


    @Override
    public void onTrackingStarted(String trackingID) {
        TLog.log("TMap: Tracking Started:::" + trackingID);
    }


    @Override
    public void onLocationUpdate(String trackingId, TLocation location) {
        if (markers.containsKey(trackingId))
            TUtils.animateMarker(googleMap, markers.get(trackingId), location);
        else {
            for (MarkerOption markerOption : trackingOptions.getMarkerOptions()) {
                if (!trackingId.equals(markerOption.getTrackingId()))
                    continue;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions options = new MarkerOptions().position(latLng);
                if (markerOption.getBitmap() != null)
                    options.icon(BitmapDescriptorFactory.fromBitmap(markerOption.getBitmap()));
                else
                    options.icon(BitmapDescriptorFactory.fromResource(markerOption.getIconMarker()));
                Marker marker = googleMap.addMarker(options);
                marker.setTitle(markerOption.getMarkerTitle());
                marker.setSnippet(markerOption.getMarkerSnippet());
                markers.put(trackingId, marker);
            }
        }
        TUtils.maintainCameraPosition(this, markers, googleMap);
    }


    @Override
    public void onTrackingEnded(String trackingId) {
        TLog.log("TMap: On Track ended:" + trackingId);
        if (markers.containsKey(trackingId)) {
            markers.get(trackingId).remove();
            markers.remove(trackingId);
        }
    }

    @Override
    public void onTrackingError(String reason) {
        TLog.log("TMap: Tracking Error:::" + reason);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Code not added
    }

    @Override
    public void onBackPressed() {
        if (tracker != null)
            tracker.disconnect(trackingOptions);
        super.onBackPressed();
    }

    private boolean checkPermission() {
        if (isPermissionGranted())
            return true;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                permission))
            ActivityCompat.requestPermissions(this,
                    new String[]{permission}, TConstants.PERMISSION_REQ_CODE);
        else
            ActivityCompat.requestPermissions(this,
                    new String[]{permission}, TConstants.PERMISSION_REQ_CODE);
        return false;
    }


    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}
