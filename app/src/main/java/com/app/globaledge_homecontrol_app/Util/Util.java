package com.app.globaledge_homecontrol_app.Util;

import android.content.Context;
import android.content.Intent;

import com.app.globaledge_homecontrol_app.Activity.DeviceDetailsActivity;

//This custom class for go to next screen from particular screen
public class Util {
    private Intent mIntent;

    /**
     * This is generic class to go to next screen
     *
     * @param mContext Context
     * @param cls      Class for next screen
     */
    public void goToNextScreen(Context mContext, Class cls) {
        mIntent = new Intent(mContext, cls);
        mContext.startActivity(mIntent);
    }


    /**
     * To go DeviceDetailsActivity
     *
     * @param mContext      Context
     * @param deviceName    deviceName to append in DeviceDetailsActivity screen
     * @param deviceAddress deviceAddress to append in DeviceDetailsActivity screen
     */
    public void goToDeviceDetailsActivity(Context mContext, String deviceName, String deviceAddress) {
        mIntent = new Intent(mContext, DeviceDetailsActivity.class);
        mIntent.putExtra(Constants.DEVICE_NAME, deviceName);
        mIntent.putExtra(Constants.MAC_ADDRESS, deviceAddress);
        mContext.startActivity(mIntent);
    }
}
