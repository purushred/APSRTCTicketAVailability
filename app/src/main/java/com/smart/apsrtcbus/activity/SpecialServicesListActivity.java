package com.smart.apsrtcbus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smart.apsrtcbus.R;
import com.smart.apsrtcbus.adapter.SpecialServiceAdapter;
import com.smart.apsrtcbus.intf.ITaskComplete;
import com.smart.apsrtcbus.task.SpecialServiceListAsyncTask;
import com.smart.apsrtcbus.vo.SpecialServiceVO;

import java.util.ArrayList;

public class SpecialServicesListActivity extends ActionBarActivity implements ITaskComplete {

    private static final int NEW_SPECIAL_SERVICE = 1;
    private AdView adView;
    private ArrayList<SpecialServiceVO> list = new ArrayList<>();
    private SpecialServiceAdapter adapter = null;
    private ListView listView;
    private LinearLayout progressBarLayout;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_services_list);
        getSupportActionBar().setTitle("Special Services");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.service_list);
        progressBarLayout = (LinearLayout) findViewById(R.id.progressBarLayout);
        adapter = new SpecialServiceAdapter(this, list);
        listView.setAdapter(adapter);
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Intent intent = new Intent(SpecialServicesListActivity.this, SpecialServiceDetailsListActivity.class);
                    intent.putExtra("SpecialServiceVO", adapter.getItem(position));
                    startActivity(intent);
                }
            }
        });*/

        new SpecialServiceListAsyncTask(adapter, this).execute();

        adView = (AdView) this.findViewById(R.id.adMobView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_special_service, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new) {
            Intent intent = new Intent(getApplicationContext(), NewSpecialServiceActivity.class);
            startActivityForResult(intent, NEW_SPECIAL_SERVICE);
            return true;
        } else if (id == R.id.about) {
            Intent intent = new Intent(getApplicationContext(), AboutSpecialServiceActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_SPECIAL_SERVICE) {
            if (resultCode == RESULT_OK) {
                SpecialServiceVO serviceVO = (SpecialServiceVO) data.getSerializableExtra("SpecialServiceVO");
                if (adapter.isEmpty()) {
                    adapter.add(serviceVO);
                } else {
                    adapter.insert(serviceVO, 1);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onTaskCompleted() {
        progressBarLayout.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }
}
