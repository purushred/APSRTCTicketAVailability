package com.smart.apsrtcbus.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.smart.apsrtcbus.activity.NewSpecialServiceActivity;
import com.smart.apsrtcbus.restful.SpecialService;
import com.smart.apsrtcbus.utilities.AppUtils;
import com.smart.apsrtcbus.vo.SpecialServiceVO;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddNewServiceAsyncTask extends AsyncTask<SpecialServiceVO, Void, Void> {

    private final NewSpecialServiceActivity specialServiceActivity;
    private ProgressDialog progressDialog;

    public AddNewServiceAsyncTask(NewSpecialServiceActivity newSpecialServiceActivity) {
        this.specialServiceActivity = newSpecialServiceActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(specialServiceActivity);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait while adding special service.");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(SpecialServiceVO... params) {

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(AppUtils.STATION_INFO_URL).build();
        SpecialService stationService = adapter.create(SpecialService.class);
        stationService.addNewService(params[0], new Callback<SpecialServiceVO>() {
            @Override
            public void success(SpecialServiceVO serviceVO, Response response) {
                progressDialog.dismiss();
                if (serviceVO != null) {
                    Toast.makeText(specialServiceActivity, "Special service added successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("SpecialServiceVO", serviceVO);
                    specialServiceActivity.setResult(Activity.RESULT_OK, intent);
                    specialServiceActivity.finish();
                } else {
                    Toast.makeText(specialServiceActivity, "Failed to add special service.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                progressDialog.dismiss();
                Toast.makeText(specialServiceActivity, "Failed to add special service.", Toast.LENGTH_SHORT).show();
            }
        });
        return null;
    }
}