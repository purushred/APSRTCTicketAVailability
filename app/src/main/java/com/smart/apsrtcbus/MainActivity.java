package com.smart.apsrtcbus;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smart.apsrtcbus.activity.DatePickerFragment;
import com.smart.apsrtcbus.activity.SearchResultListActivity;
import com.smart.apsrtcbus.activity.SpecialServicesListActivity;
import com.smart.apsrtcbus.activity.StationListActivity;
import com.smart.apsrtcbus.task.StationInfoAsyncTask;
import com.smart.apsrtcbus.utilities.AppRater;
import com.smart.apsrtcbus.utilities.AppUtils;
import com.smart.apsrtcbus.vo.SearchResultVO;
import com.smart.apsrtcbus.vo.StationVO;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener, Callback {

    public static List<StationVO> stationList;
    private final long DAY_IN_MILLI_SEC = 3 * 86400000; //24 * 3600 * 1000;
    private TextView fromTextView;
    private TextView toTextView;
    private Button journeyDateButton;
    private StationVO fromServiceInfo = null;
    private StationVO toServiceInfo = null;
    private ProgressDialog progress = null;
    private short serviceClassId = 0;
    private OkHttpClient httpClient = null;
    private Handler handler = null;
    private String searchURL;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private Spinner typeSpinner;
    private boolean isNetworkUp = false;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        isNetworkUp = AppUtils.isNetworkOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        if (isNetworkUp)
            displayMainScreen();
        else
            buildNetworkErrorUI();
    }

    /**
     * To display error message in case of any network errors.
     */
    private void buildNetworkErrorUI() {

        setContentView(R.layout.activity_network_error);
        Button retryButton = (Button) findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppUtils.isNetworkOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                    displayMainScreen();
                }
            }
        });
    }

    /**
     * To display the main screen
     */
    private void displayMainScreen() {
        setContentView(R.layout.activity_main);
        if (menuItem != null)
            menuItem.setVisible(true);
        journeyDateButton = (Button) findViewById(R.id.journeyDateButton);
        typeSpinner = (Spinner) findViewById(R.id.serviceTypeComboView);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.service_type, R.layout.custom_stepper_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        httpClient = new OkHttpClient();
        handler = new Handler();
        try {
            getStationList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLastSearchParams();

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        AdView adView = (AdView) this.findViewById(R.id.adMobView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        AppRater.app_launched(this);
    }

    private void getStationList() throws IOException {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        String jsonData = pref.getString("STATION_LIST", null);
        long lastSyncTimeStamp = pref.getLong("LAST_SYNC_TIMESTAMP", 0);
        long currentTimeStamp = new Date().getTime();

        if (jsonData == null || (currentTimeStamp - lastSyncTimeStamp >= DAY_IN_MILLI_SEC)) {
            new StationInfoAsyncTask(this).execute();
        } else {
            stationList = AppUtils.getBusStationList(jsonData);
            if (stationList == null || stationList.size() <= 0) {
                new StationInfoAsyncTask(this).execute();
            } else {
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

                progressBar.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void buttonClickHandler(View view) {

        View nextDateButton = findViewById(R.id.nextDateButton);
        View prevDateButton = findViewById(R.id.previousDateButton);
        View searchButton = findViewById(R.id.searchButton);

        if (searchButton == view) {
            searchHandler();
        } else if (journeyDateButton == view) {

            Calendar cal = null;
            try {
                Date date = formatter.parse(journeyDateButton.getText().toString());
                cal = Calendar.getInstance();
                cal.setTime(date);
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
            }

            DatePickerFragment newFragment = new DatePickerFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("Calendar", cal);
            newFragment.setArguments(bundle);
            newFragment.setOnDateSetListener(this);
            newFragment.show(getSupportFragmentManager(), "datePicker");
        } else if (nextDateButton == view) {
            journeyDateButton.setText(AppUtils.getNewDate(1, journeyDateButton.getText().toString()));
        } else if (prevDateButton == view) {
            journeyDateButton.setText(AppUtils.getNewDate(-1, journeyDateButton.getText().toString()));
        }
    }

    /**
     * This method will handle the Main Search button click events.
     * It will search for bus services running with the selected
     * From and To stations and will show the result in a separate activity.
     */
    private void searchHandler() {

        String dateStr = journeyDateButton.getText().toString();

        if (fromServiceInfo == null || fromServiceInfo.getId() == null || fromServiceInfo.getId().length() <= 0) {
            Toast.makeText(this, "Please enter valid 'From' station.", Toast.LENGTH_LONG).show();
            return;
        }
        if (toServiceInfo == null || toServiceInfo.getId() == null || toServiceInfo.getId().length() <= 0) {
            Toast.makeText(this, "Please enter valid 'To' station.", Toast.LENGTH_LONG).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        try {
            Date date = formatter.parse(dateStr);
            cal.setTime(date);
            Calendar today = Calendar.getInstance();
            String str = formatter.format(new Date());
            today.setTime(formatter.parse(str));
            if (cal.compareTo(today) < 0) {
                Toast.makeText(this, R.string.journey_date_validation, Toast.LENGTH_LONG).show();
                return;
            }
        } catch (ParseException e) {
            Log.e("Error", e.getLocalizedMessage());
        }

        String selectedItem = (String) typeSpinner.getSelectedItem();

        if (selectedItem.equals("All"))
            serviceClassId = 0;
        else if (selectedItem.equals("A/C"))
            serviceClassId = 200;
        else
            serviceClassId = 201;

        progress.setMessage(getString(R.string.searching));
        progress.show();

        String url = "serviceClassId=" + serviceClassId + "&concessionId=1347688949874&" +
                "txtJourneyDate=" + dateStr + "&txtReturnJourneyDate=" + dateStr +
                "&searchType=0&startPlaceId=" + fromServiceInfo.getId() +
                "&endPlaceId=" + toServiceInfo.getId();

        storeInLocalStorage(serviceClassId, dateStr, fromServiceInfo, toServiceInfo);

        searchURL = AppUtils.SEARCH_URL + url;
        Request request = new Request.Builder().url(searchURL).build();
        httpClient.newCall(request).enqueue(this);
    }

    /**
     * To get the data from cached local database.
     */
    private void getLastSearchParams() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        String searchData = pref.getString("LAST_SEARCH_DATA", null);
        fromTextView = (TextView) findViewById(R.id.fromAuto);
        toTextView = (TextView) findViewById(R.id.toAuto);

        fromTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
                intent.putExtra("Type", "From");
                startActivityForResult(intent, 2);
            }
        });

        toTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
                intent.putExtra("Type", "To");
                startActivityForResult(intent, 2);
            }
        });

        if (searchData != null) {
            String[] dataArr = searchData.split("#");
            String serviceClassIdStr = dataArr[0];

            if (serviceClassIdStr.equals("0")) {
                typeSpinner.setSelection(0); // all
                serviceClassId = 0;
            } else if (serviceClassIdStr.equals("200")) {
                typeSpinner.setSelection(1); // ac
                serviceClassId = 200;
            } else {
                typeSpinner.setSelection(2); // non ac
                serviceClassId = 201;
            }

            Calendar cal = Calendar.getInstance();
            try {
                Date lastDate = formatter.parse(dataArr[1]);
                cal.setTime(lastDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (cal.compareTo(Calendar.getInstance()) < 0) {
                journeyDateButton.setText(formatter.format(new Date()));
            } else {
                journeyDateButton.setText(dataArr[1]);
            }
            fromServiceInfo = new StationVO(dataArr[2], dataArr[3]);
            toServiceInfo = new StationVO(dataArr[4], dataArr[5]);

            fromTextView.setText(fromServiceInfo.getValue());
            toTextView.setText(toServiceInfo.getValue());
        } else {
            Calendar cal = Calendar.getInstance();
            Date newDate = cal.getTime();
            journeyDateButton.setText(formatter.format(newDate));
        }
    }

    /**
     * To store the last search information in local cache to
     * get display when launched next time.
     *
     * @param serviceClassId
     * @param dateStr
     * @param fromServiceInfo
     * @param toServiceInfo
     */
    private void storeInLocalStorage(short serviceClassId, String dateStr,
                                     StationVO fromServiceInfo, StationVO toServiceInfo) {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        Editor editor = pref.edit();
        String dataStr = serviceClassId + "#" + dateStr + "#" +
                fromServiceInfo.getId()
                + "#" + fromServiceInfo.getValue() + "#"
                + toServiceInfo.getId()
                + "#" + toServiceInfo.getValue();
        editor.putString("LAST_SEARCH_DATA", dataStr);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            StationVO serviceInfo = (StationVO) intent.getSerializableExtra("ServiceInfo");
            String type = intent.getStringExtra("Type");
            if (type.equals("From")) {
                fromServiceInfo = serviceInfo;
                fromTextView.setText(fromServiceInfo.toString());
            } else if (type.equals("To")) {
                toServiceInfo = serviceInfo;
                toTextView.setText(toServiceInfo.toString());
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        if (cal.compareTo(Calendar.getInstance()) < 0) {
            Toast.makeText(this, R.string.journey_date_validation, Toast.LENGTH_LONG).show();
            return;
        }
        journeyDateButton.setText(formatter.format(cal.getTime()));
    }

    @Override
    public void onFailure(Request request, IOException exception) {
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Communication error occured. Try again..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResponse(Response response) throws IOException {

        ArrayList<SearchResultVO> list = AppUtils.parseHTMLUsingJSoup(response.body().string());
        SearchResultVO resultVO = new SearchResultVO();
        resultVO.setServiceName("Service");
        resultVO.setType("Service Type");
        resultVO.setDeparture("Departure");
        resultVO.setArrival("Arrival");
        resultVO.setAvailableSeats("Seats");

        list.add(0, resultVO);
        progress.cancel();
        Intent intent = new Intent().setClass(MainActivity.this, SearchResultListActivity.class);
        intent.putParcelableArrayListExtra("SearchResults", list);
        intent.putExtra("FROM", fromServiceInfo.getValue());
        intent.putExtra("TO", toServiceInfo.getValue());
        intent.putExtra("DATE", journeyDateButton.getText().toString());
        intent.putExtra("SEARCH_URL", searchURL);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuItem = menu.getItem(0);
        if (isNetworkUp)
            menuItem.setVisible(true);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);
        // Fetch and store ShareActionProvider
        setShareIntent((ShareActionProvider) MenuItemCompat.getActionProvider(item));

        return true;
    }

    // Call to update the share intent
    private void setShareIntent(ShareActionProvider mShareActionProvider) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_heading_str));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_str));
        intent.setType("text/plain");
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            Intent intent = new Intent(getApplicationContext(), SpecialServicesListActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
