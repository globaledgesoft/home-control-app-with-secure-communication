package com.app.globaledge_homecontrol_app.Adapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.globaledge_homecontrol_app.R;
import com.app.globaledge_homecontrol_app.Util.Constants;
import com.app.globaledge_homecontrol_app.Util.GELogger;
import com.app.globaledge_homecontrol_app.Util.Util;
import com.globlaedge.cloud.blelibrary.blelibrary.Interface.Interface.IServiceDiscovered;
import com.globlaedge.cloud.blelibrary.blelibrary.Interface.Java.ConnectBle;

import java.util.ArrayList;
import java.util.List;

//BleDeviceListAdapter provide a binding from an app-specific data set to views that are displayed within
// a recycler view of BLEDeviceList screen
public class BleDeviceListAdapter extends RecyclerView.Adapter<BleDeviceListAdapter.DeviceViewHolder> {

    private static final String TAG = "BleDeviceListAdapter";
    private static final int REQUEST_ENABLE_BT = 1;
    private static List<BluetoothGattService> serviceList;
    private ArrayList<BluetoothDevice> deviceList;
    private Activity mActivity;
    private ConnectBle connectBle;
    private BluetoothAdapter mBluetoothAdapter;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Util mUtil;

    public BleDeviceListAdapter(ArrayList<BluetoothDevice> deviceList, Activity activity,
                                BluetoothAdapter mBluetoothAdapter) {
        this.deviceList = deviceList;
        this.mActivity = activity;
        this.mBluetoothAdapter = mBluetoothAdapter;
        connectBle = ConnectBle.getInstance(mActivity);
        mSharedPreferences = activity.getSharedPreferences(activity.getString(R.string.last_paired_preference), Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mUtil = new Util();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.device_row, viewGroup, false);

        return new DeviceViewHolder(itemView);
    }

    //Clearing the device list
    public void clear() {
        deviceList.clear();
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder deviceViewHolder, int i) {
        final BluetoothDevice device = deviceList.get(i);

        if (device.getName() == null || device.getName().isEmpty()) {
            deviceViewHolder.deviceName.setText(mActivity.getString(R.string.unknown));
        } else {
            deviceViewHolder.deviceName.setText(device.getName());
        }
        deviceViewHolder.deviceMacAddr.setText(device.getAddress());

        deviceViewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                    connectBle.connect(mActivity, device, new IServiceDiscovered() {
                        @Override
                        public void onServiceDiscovered(List<BluetoothGattService> service) {
                            GELogger.i(TAG, "ListOfService :" + service);

                            if (service != null) {
                                serviceList = service;
                                for (int i = 0; i < service.size(); i++) {
                                    GELogger.i(TAG, "Service :" + service.get(i));
                                    if (service.get(i).getUuid().equals(Constants.SERVICE)) {
                                        mUtil.goToDeviceDetailsActivity(mActivity, device.getName(),
                                                device.getAddress());
                                        mEditor.putString(Constants.MAC_ADDRESS, device.getAddress());
                                        mEditor.commit();
                                    }
                                }
                            } else {
                                Toast.makeText(mActivity, mActivity.getString(R.string.service_not_found),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    GELogger.d(TAG, "Enable Bluetooth");
                    Toast.makeText(mActivity, mActivity.getString(R.string.enable_bluetooth),
                            Toast.LENGTH_SHORT).show();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (deviceList != null) {
            return deviceList.size();
        }
        return 0;
    }

    public interface ItemClickListener {
        void onItemClick(int position, View view);
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView deviceName, deviceMacAddr;
        private ItemClickListener clickListener;

        private DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.textViewDeviceName);
            deviceMacAddr = itemView.findViewById(R.id.textViewMacAddress);
            itemView.setOnClickListener(this);
        }

        private void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }
}
