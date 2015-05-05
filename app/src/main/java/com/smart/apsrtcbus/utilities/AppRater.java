package com.smart.apsrtcbus.utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.apsrtcbus.R;

/**
 * Created by Purushotham on 05-05-2015.
 */
public class AppRater {

    private final static String APP_TITLE = "APSRTC Ticket Availability";
    private final static String APP_PNAME = "com.smart.apsrtcbus";

    private final static int DAYS_UNTIL_PROMPT = 0;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 3;//Min number of launches

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }
        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }
        editor.apply();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_TITLE);

        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontalLayout = new LinearLayout(mContext);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(5, 5, 5, 0);

        TextView tv = new TextView(mContext);
        tv.setText("If you enjoy using the app, please take a moment to rate it. Thanks for your support!");
        tv.setWidth(240);
        tv.setPadding(5, 0, 4, 10);
        linearLayout.addView(tv);

        Button b1 = new Button(mContext);
        b1.setText("Rate Now");
        b1.setTextColor(mContext.getResources().getColor(R.color.white_color));
        b1.setBackgroundResource(R.drawable.rectangle_shape);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialog.dismiss();
            }
        });
        horizontalLayout.addView(b1, layoutParams);

        Button b2 = new Button(mContext);
        b2.setTextColor(mContext.getResources().getColor(R.color.white_color));
        b2.setText("Later");
        b2.setBackgroundResource(R.drawable.green_rectangle_shape);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        horizontalLayout.addView(b2, layoutParams);

        Button b3 = new Button(mContext);
        b3.setTextColor(mContext.getResources().getColor(R.color.white_color));
        b3.setText("No, thanks");
        b3.setBackgroundResource(R.drawable.rectangle_shape);
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        horizontalLayout.addView(b3, layoutParams);
        linearLayout.addView(horizontalLayout);
        dialog.setContentView(linearLayout);
        dialog.show();
    }
}