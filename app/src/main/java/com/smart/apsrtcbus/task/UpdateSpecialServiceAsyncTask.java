package com.smart.apsrtcbus.task;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.smart.apsrtcbus.activity.SpecialServiceDetailsListActivity;
import com.smart.apsrtcbus.restful.SpecialService;
import com.smart.apsrtcbus.utilities.AppUtils;
import com.smart.apsrtcbus.vo.SpecialServiceVO;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UpdateSpecialServiceAsyncTask extends AsyncTask<SpecialServiceVO, Void, Void> {

    private final SpecialServiceDetailsListActivity specialServiceActivity;

    public UpdateSpecialServiceAsyncTask(SpecialServiceDetailsListActivity newSpecialServiceActivity) {
        this.specialServiceActivity = newSpecialServiceActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Toast.makeText(specialServiceActivity,"Please wait while updating special service.",Toast.LENGTH_SHORT).show();
    }

    @Override
	protected Void doInBackground(SpecialServiceVO ... params) {

		RestAdapter adapter = new RestAdapter.Builder().setEndpoint(AppUtils.STATION_INFO_URL).build();
        SpecialService stationService = adapter.create(SpecialService.class);
		stationService.updateService(params[0], new Callback<SpecialServiceVO>() {
            @Override
            public void success(SpecialServiceVO serviceVO, Response response) {

                Log.e("Update Service,", "Special Service Update Status: " + serviceVO);
                if (serviceVO != null) {
                    //Toast.makeText(specialServiceActivity, "Special service updated successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(specialServiceActivity, "Failed to update special service.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(specialServiceActivity, "Failed to update special service.", Toast.LENGTH_SHORT).show();
                Log.e("Update Service,", retrofitError.toString());
            }
        });
        return null;
	}
}