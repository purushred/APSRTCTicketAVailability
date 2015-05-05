package com.smart.apsrtcbus.task;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smart.apsrtcbus.activity.SpecialServiceDetailsListActivity;
import com.smart.apsrtcbus.restful.SpecialService;
import com.smart.apsrtcbus.utilities.AppUtils;
import com.smart.apsrtcbus.vo.SpecialServiceVO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GetSpecialServiceAsyncTask extends AsyncTask<SpecialServiceVO, Void, Void> {

    private final SpecialServiceDetailsListActivity specialServiceActivity;

    public GetSpecialServiceAsyncTask(SpecialServiceDetailsListActivity newSpecialServiceActivity) {
        this.specialServiceActivity = newSpecialServiceActivity;
    }

    @Override
    protected Void doInBackground(SpecialServiceVO... params) {

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(AppUtils.STATION_INFO_URL).build();
        SpecialService stationService = adapter.create(SpecialService.class);
        stationService.getService(params[0].getuId(), new Callback<SpecialServiceVO>() {
            @Override
            public void success(SpecialServiceVO serviceVO, Response response) {

                specialServiceActivity.progressBarLayout.setVisibility(View.GONE);
                Log.e("Get Service,", "Special Service Get Status: " + serviceVO.getLocationAddress());
                if (serviceVO != null) {
                     Date date = new Date(serviceVO.getLocationTimestamp() * 1000L); // *1000 is to convert seconds to milliseconds
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // the format of your date
                    sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating (see comment at the bottom
                    String formattedDate = sdf.format(date);
                    specialServiceActivity.locationStatus.setVisibility(View.VISIBLE);
                    specialServiceActivity.locationStatus.setText("Location updated on: " + formattedDate
                            + "\nAddress: " + serviceVO.getLocationAddress());
                    Log.e("LAT LONG:",serviceVO.getLatitude()+":"+serviceVO.getLongitude());
                    if (serviceVO.getLatitude() == 0 || serviceVO.getLongitude() == 0) {
                        Toast.makeText(specialServiceActivity, "Location not available at the moment.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    /*Uri gmmIntentUri = Uri.parse("geo:" + serviceVO.getLatitude() + "," + serviceVO.getLongitude() + "?z=19");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    specialServiceActivity.startActivity(mapIntent);*/

                } else {
                    Toast.makeText(specialServiceActivity, "Location not available at the moment.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(specialServiceActivity, "Location not available at the moment.", Toast.LENGTH_LONG).show();
                Log.e("Get Service,", retrofitError.toString());
            }
        });
        return null;
    }
}