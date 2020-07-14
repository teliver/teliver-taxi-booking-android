package com.taxicustmr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.teliver.sdk.core.TLog;
import com.teliver.sdk.core.Teliver;
import com.teliver.sdk.core.TrackingListener;
import com.teliver.sdk.models.MarkerOption;
import com.teliver.sdk.models.TLocation;
import com.teliver.sdk.models.TrackingBuilder;
import com.teliver.sdk.models.UserBuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private String userName = "selvakumar";

    private Application application;

    private GoogleMap googleMap;

    private RelativeLayout relativeLayout;

    private List<String> trackingId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TLog.setVisible(true);
        Teliver.identifyUser(new UserBuilder(userName).setUserType(UserBuilder.USER_TYPE.CONSUMER).registerPush().build());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        application = (Application) getApplication();
        relativeLayout = (RelativeLayout) findViewById(R.id.rooView);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("tripId"));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("payload").equals("0"))
                Teliver.stopTracking(intent.getStringExtra(Application.TRACKING_ID));
            else if (intent.getStringExtra("payload").equals("1")) {
                trackingId.add(intent.getStringExtra(Application.TRACKING_ID));
                startTracking(trackingId);
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    private void startTracking(List<String> trackingId) {
        List<MarkerOption> markerOptionList = new ArrayList<>();
        String[] ids = trackingId.toArray(new String[trackingId.size()]);
        for (String id : ids) {
            MarkerOption option = new MarkerOption(id);
            option.setMarkerTitle("Hi There");
            markerOptionList.add(option);
        }

        Teliver.startTracking(new TrackingBuilder(markerOptionList).withYourMap(googleMap).withListener(new TrackingListener() {
            @Override
            public void onTrackingStarted(String trackingId) {

            }

            @Override
            public void onLocationUpdate(String trackingId, TLocation location) {

            }

            @Override
            public void onTrackingEnded(String trackingId) {

            }

            @Override
            public void onTrackingError(String reason) {

            }
        }).build());
    }

    public void showToast(View view) {
        Snackbar.make(relativeLayout,"This is a Demo Application for LiveTracking",Snackbar.LENGTH_LONG).show();
    }
}
