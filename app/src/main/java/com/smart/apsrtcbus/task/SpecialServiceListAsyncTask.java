package com.smart.apsrtcbus.task;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.smart.apsrtcbus.adapter.SpecialServiceAdapter;
import com.smart.apsrtcbus.intf.ITaskComplete;
import com.smart.apsrtcbus.restful.SpecialService;
import com.smart.apsrtcbus.utilities.AppUtils;
import com.smart.apsrtcbus.vo.SpecialServiceVO;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class SpecialServiceListAsyncTask extends AsyncTask<Void, Void, List<SpecialServiceVO>> {

    private final SpecialServiceAdapter adapter;
    private ITaskComplete taskComplete;

    public SpecialServiceListAsyncTask(SpecialServiceAdapter adapter, ITaskComplete taskComplete) {
        this.adapter = adapter;
        this.taskComplete = taskComplete;
    }

    protected void onPostExecute(List<SpecialServiceVO> result) {
        Log.e("SpecialServiceList", "Special Services: " + result.size());
        adapter.clear();
        adapter.add(getHeaderDetails());
        if (result != null && result.size() > 0) {
            adapter.addAll(result);
        } else {
            Toast.makeText(adapter.getContext(), "No special services available.", Toast.LENGTH_LONG).show();
        }
        adapter.notifyDataSetChanged();
        taskComplete.onTaskCompleted();
    }

    private SpecialServiceVO getHeaderDetails() {
        SpecialServiceVO serviceVO = new SpecialServiceVO();
        serviceVO.setServiceNo("Service No.");
        serviceVO.setFrom("From");
        serviceVO.setTo("To");
        serviceVO.setDeparture("Departure");
        serviceVO.setJourneyDate("");
        serviceVO.setType("Type");
        return serviceVO;
    }

    @Override
    protected List<SpecialServiceVO> doInBackground(Void... params) {

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(AppUtils.STATION_INFO_URL).build();
        SpecialService service = adapter.create(SpecialService.class);
        List<SpecialServiceVO> specialServiceList = new ArrayList<>();
        try {
            specialServiceList = service.getServiceList();
        } catch (RetrofitError err) {
            Log.e("RETRO ERROR:", err.toString());
        }
        return specialServiceList;
    }
}