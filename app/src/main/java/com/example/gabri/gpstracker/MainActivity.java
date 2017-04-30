package com.example.gabri.gpstracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener{

    private GoogleApiClient mGoogleAPIClient;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mLastUpdateTimeTextView;
    private LocationRequest mLocationRequest;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;

    private final int MY_PERMISSION_POSITION = 1;
    private final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES";
    private final String LOCATION_KEY = "LOCATION";
    private final String LAST_UPDATED_TIME_STRING_KEY = "LAST_UPDATED_TIME_STRING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            askPermission();
        }

        if(mGoogleAPIClient == null) {
            mGoogleAPIClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mRequestingLocationUpdates = Boolean.FALSE;

        updateValuesFromBundle(savedInstanceState);
        mLatitudeTextView = (TextView) findViewById(R.id.latitudeTextView);
        mLongitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.lastUpdateTextView);
        Button mButton = (Button) findViewById(R.id.gpsButton);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(mRequestingLocationUpdates.booleanValue() == Boolean.FALSE) {
            mRequestingLocationUpdates = Boolean.TRUE;
            Toast.makeText(this,"GPS ENABLED", Toast.LENGTH_SHORT).show();
        }
        else {
            mRequestingLocationUpdates = Boolean.FALSE;
            Toast.makeText(this,"GPS DISABLED", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        mGoogleAPIClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleAPIClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mRequestingLocationUpdates) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleAPIClient);
                if (mLastLocation != null) {
                    mLatitudeTextView.setText(String.valueOf(mLastLocation.getLatitude()));
                    mLongitudeTextView.setText(String.valueOf(mLastLocation.getLongitude()));
                }
                if (mLocationRequest == null) {
                    createLocationRequest();
                }
                startLocationUpdates();
            }
        }
    }

    private void askPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // TODO brief explanation about location permission
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_POSITION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode) {
            case MY_PERMISSION_POSITION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO get new GPS position
                }
            }
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int a){
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = java.text.DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    public void updateUI(){
        mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(mLastUpdateTime);
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPIClient,this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstaceState) {
        if(savedInstaceState != null) {
            if(savedInstaceState.containsKey(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstaceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
                //setButtonsEnableState();
            }
            if(savedInstaceState.containsKey(LOCATION_KEY)) {
                mCurrentLocation = savedInstaceState.getParcelable(LOCATION_KEY);
            }
            if(savedInstaceState.containsKey(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstaceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        mGoogleAPIClient.disconnect();
        super.onStop();
    }
}