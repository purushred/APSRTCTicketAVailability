package com.smart.apsrtcbus;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by Purushotham on 05-05-2015.
 */
public class APSRTCApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "WeQI0JKfOSY5rXZPxJNrJOXOY3XQdQZyJVpSvILW", "BpXkKgNb3XnP0UBpSExBmE1TQ1JYHMOkywy8J9db");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
