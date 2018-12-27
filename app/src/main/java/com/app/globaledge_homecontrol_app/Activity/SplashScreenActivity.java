package com.app.globaledge_homecontrol_app.Activity;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.app.globaledge_homecontrol_app.R;
import com.app.globaledge_homecontrol_app.Util.Constants;
import com.app.globaledge_homecontrol_app.Util.GELogger;
import com.app.globaledge_homecontrol_app.Util.Util;

//SplashScreenActivity displayed after launching the application
public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "GE_SplashScreenActivity";
    private Util mUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mUtil = new Util();

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GELogger.d(TAG, "Starting BleDeviceListActivity...");
                mUtil.goToNextScreen(SplashScreenActivity.this, BleDeviceListActivity.class);
                finish();
            }
        }, Constants.DELAY_2000);
    }
}
