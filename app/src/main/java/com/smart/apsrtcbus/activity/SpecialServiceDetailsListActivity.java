package com.smart.apsrtcbus.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.smart.apsrtcbus.R;
import com.smart.apsrtcbus.service.FetchAddressIntentService;
import com.smart.apsrtcbus.task.GetSpecialServiceAsyncTask;
import com.smart.apsrtcbus.task.UpdateSpecialServiceAsyncTask;
import com.smart.apsrtcbus.utilities.Constants;
import com.smart.apsrtcbus.vo.SpecialServiceVO;

public class SpecialServiceDetailsListActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final int LOCATION_REQUEST_INTERVAL = 1000 * 60; // 1 minute
    private final int LOCATION_REQUEST_FAST_INTERVAL = 1000 * 30; // half minute
    public LinearLayout progressBarLayout;
    public TextView locationStatus = null;
    private SpecialServiceVO serviceVO = null;
    private Button trackButton = null;
    private GoogleApiClient mGoogleApiClient = null;
    private Location mCurrentLocation = null;
    private AddressResultReceiver mResultReceiver;
    private Switch locationUpdateSwitch;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.special_service_details_list);
        getSupportActionBar().setTitle("Special Service Details");
        mResultReceiver = new AddressResultReceiver(new Handler());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        serviceVO = (SpecialServiceVO) getIntent().getSerializableExtra("SpecialServiceVO");

        TextView fromTextView = (TextView) findViewById(R.id.fromTextView);
        TextView toTextView = (TextView) findViewById(R.id.toTextView);
        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        trackButton = (Button) findViewById(R.id.trackButton);
        locationUpdateSwitch = (Switch) findViewById(R.id.locationUpdateSwitch);
        progressBarLayout = (LinearLayout) findViewById(R.id.progressBarLayout);
        locationStatus = (TextView) findViewById(R.id.locationStatus);

        String fromTxt = serviceVO.getFrom();
        fromTxt = fromTxt.split("-").length > 1 ? fromTxt.split("-")[0] : fromTxt;
        fromTextView.setText(fromTxt);

        String toTxt = serviceVO.getTo();
        toTxt = toTxt.split("-").length > 1 ? toTxt.split("-")[0] : toTxt;
        toTextView.setText(toTxt);

        String dateStr = serviceVO.getJourneyDate() + " " + serviceVO.getDeparture();
        dateTextView.setText(dateStr);

        AdView adView = (AdView) this.findViewById(R.id.adMobView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    public void buttonClickHandler(View view) {
        if (view.getId() == trackButton.getId()) {
            progressBarLayout.setVisibility(View.VISIBLE);
            new GetSpecialServiceAsyncTask(this).execute(serviceVO);
        } else if (view.getId() == locationUpdateSwitch.getId()) {
            if (!locationUpdateSwitch.isChecked()) {
                stopLocationUpdates();
                trackButton.setVisibility(View.VISIBLE);
                final TextView locateTextView = (TextView) findViewById(R.id.locateTextView);
                locateTextView.setVisibility(View.GONE);
            } else {

                final TextView locateTextView = (TextView) findViewById(R.id.locateTextView);
                new AlertDialog.Builder(this)
                        .setTitle("Location update")
                        .setMessage("Are you travelling in this service now?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                startLocationUpdates();
                                trackButton.setVisibility(View.GONE);
                                locateTextView.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                locationUpdateSwitch.setChecked(false);
                            }
                        }).show();
            }
        }
    }

    protected void startLocationUpdates() {
        locationStatus.setVisibility(View.VISIBLE);
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FAST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // PRIORITY_BALANCED_POWER_ACCURACY
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        locationStatus.setVisibility(View.GONE);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("Google API connected", "Google API Connected");
        locationUpdateSwitch.setVisibility(View.VISIBLE);
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {
            updateUI();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationUpdateSwitch.setVisibility(View.GONE);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        locationUpdateSwitch.setVisibility(View.GONE);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI();
    }

    private void updateUI() {

        long mLastUpdateTime = System.currentTimeMillis() / 1000L;
        serviceVO.setLatitude(mCurrentLocation.getLatitude());
        serviceVO.setLongitude(mCurrentLocation.getLongitude());
        serviceVO.setLocationTimestamp(mLastUpdateTime);
        new UpdateSpecialServiceAsyncTask(SpecialServiceDetailsListActivity.this).execute(serviceVO);
        // Commented this line to disable the address update.
        startIntentService();
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            long mLastUpdateTime = System.currentTimeMillis() / 1000L;
            serviceVO.setLatitude(mCurrentLocation.getLatitude());
            serviceVO.setLongitude(mCurrentLocation.getLongitude());
            serviceVO.setLocationTimestamp(mLastUpdateTime);
            String address = resultData.getString(Constants.RESULT_DATA_KEY);
            serviceVO.setLocationAddress(address);
//            locationStatus.setText(address);
            new UpdateSpecialServiceAsyncTask(SpecialServiceDetailsListActivity.this).execute(serviceVO);
        }
    }
}
