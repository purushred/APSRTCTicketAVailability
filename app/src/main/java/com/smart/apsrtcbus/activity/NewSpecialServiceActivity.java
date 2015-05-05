package com.smart.apsrtcbus.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.smart.apsrtcbus.R;
import com.smart.apsrtcbus.task.AddNewServiceAsyncTask;
import com.smart.apsrtcbus.vo.SpecialServiceVO;
import com.smart.apsrtcbus.vo.StationVO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewSpecialServiceActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener, TimePickerDialog.OnTimeSetListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    private SimpleDateFormat formatter = new SimpleDateFormat("EEE dd MMM, yyyy");
    private String selectedServiceType = "";
    private Button dateButton;
    private TextView toView;
    private TextView fromView;
    private Spinner typeSpinner;
    private Button timePickerButton;
    private boolean mIntentInProgress;
    /**
     * True if the sign-in button was clicked.  When true, we know to resolve all
     * issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked;
    private MenuItem signOutMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_service);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope("profile"))
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New Special Service");

        dateButton = (Button) findViewById(R.id.dateButtonView);
        timePickerButton = (Button) findViewById(R.id.timePickerButton);

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        setTime(hour, minute);

        String dateStr = formatter.format(new Date());
        dateButton.setText(dateStr);
        fromView = (TextView) findViewById(R.id.fromStationEditView);
        toView = (TextView) findViewById(R.id.toStationEditView);
        typeSpinner = (Spinner) findViewById(R.id.serviceTypeComboView);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedServiceType = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.service_type1, R.layout.custom_stepper_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        fromView.setOnClickListener(this);
        toView.setOnClickListener(this);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }
            mIntentInProgress = false;
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        } else {
            if (resultCode == RESULT_OK) {
                StationVO serviceInfo = (StationVO) intent.getSerializableExtra("ServiceInfo");
                String type = intent.getStringExtra("Type");
                if (type.equals("From")) {
                    fromView.setText(serviceInfo.toString());
                } else if (type.equals("To")) {
                    toView.setText(serviceInfo.toString());
                }
            }
        }
    }

    public void buttonClickHandler(View view) {

        if (view.getId() == dateButton.getId()) {
            Calendar cal = null;
            try {
                Date date = formatter.parse(dateButton.getText().toString());
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

        } else if (view.getId() == timePickerButton.getId()) {

            TimePickerFragment newFragment = new TimePickerFragment();
            newFragment.setOnTimeSetListener(this);
            newFragment.show(getSupportFragmentManager(), "timePicker");

        } else {
            String fromText = fromView.getText().toString();
            String toText = toView.getText().toString();

            if (fromText == null || fromText.length() == 0 ||
                    toText == null || toText.length() == 0) {
                Toast.makeText(this, "From & To stations are mandatory.", Toast.LENGTH_LONG).show();
                return;
            }
            EditText serviceNo = (EditText) findViewById(R.id.serviceNoEditView);
            String servNo = "N/A";
            if (serviceNo.getText() != null && serviceNo.getText().toString().length() > 0) {
                servNo = serviceNo.getText().toString();
            }
            SpecialServiceVO serviceVO = new SpecialServiceVO();
            serviceVO.setServiceNo(servNo);
            serviceVO.setFrom(fromText);
            serviceVO.setTo(toText);
            serviceVO.setType(selectedServiceType);
            serviceVO.setDeparture(timePickerButton.getText().toString());
            serviceVO.setJourneyDate(dateButton.getText().toString());
            new AddNewServiceAsyncTask(this).execute(serviceVO);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
        if (v.getId() == R.id.sign_in_button && !mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
            return;
        } else if (v.getId() == fromView.getId()) {
            intent.putExtra("Type", "From");
        } else if (v.getId() == toView.getId()) {
            intent.putExtra("Type", "To");
        }
        startActivityForResult(intent, 2);
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
        dateButton.setText(formatter.format(cal.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setTime(hourOfDay, minute);
    }

    private void setTime(int hourOfDay, int minute) {
        String amPm = "AM";
        if (hourOfDay >= 12) {
            hourOfDay = hourOfDay - 12;
            if (hourOfDay == 0) {
                hourOfDay = 12;
            }
            amPm = "PM";
        }
        if (hourOfDay == 0) {
            hourOfDay = 12;
        }

        if (minute < 10) {
            timePickerButton.setText(hourOfDay + ":0" + minute + " " + amPm);
        } else {
            timePickerButton.setText(hourOfDay + ":" + minute + " " + amPm);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        findViewById(R.id.linearLayout).setVisibility(View.GONE);
        findViewById(R.id.relativeLayout).setVisibility(View.VISIBLE);
        mSignInClicked = false;
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            if (signOutMenuItem != null) {
                signOutMenuItem.setTitle("Sign Out(" + personName + ")");
                signOutMenuItem.setVisible(true);
                findViewById(R.id.linearLayout1).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            } else {
                findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.relativeLayout).setVisibility(View.GONE);
                if (signOutMenuItem != null) {
                    findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
                    signOutMenuItem.setVisible(false);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_special_service, menu);
        signOutMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.googleSignOut) {

            new AlertDialog.Builder(this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure, You want to Sign Out?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (mGoogleApiClient.isConnected()) {
                                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                                mGoogleApiClient.disconnect();
                                mGoogleApiClient.connect();
                            }
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();


            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}