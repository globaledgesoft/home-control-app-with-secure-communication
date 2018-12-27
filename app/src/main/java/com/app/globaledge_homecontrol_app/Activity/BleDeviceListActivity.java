package com.app.globaledge_homecontrol_app.Activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.globaledge_homecontrol_app.Adapter.BleDeviceListAdapter;
import com.app.globaledge_homecontrol_app.Model.BLEScanningModel;
import com.app.globaledge_homecontrol_app.Presenter.BleScanningPresenter;
import com.app.globaledge_homecontrol_app.R;
import com.app.globaledge_homecontrol_app.Util.Constants;
import com.app.globaledge_homecontrol_app.Util.GELogger;
import com.app.globaledge_homecontrol_app.Util.RuntimePermission;
import com.app.globaledge_homecontrol_app.Util.Util;
import com.app.globaledge_homecontrol_app.View.BleScanningView;
import com.globlaedge.cloud.blelibrary.blelibrary.Interface.Interface.IServiceDiscovered;
import com.globlaedge.cloud.blelibrary.blelibrary.Interface.Java.ConnectBle;
import com.globlaedge.cloud.blelibrary.blelibrary.Interface.Service.BLEService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.app.globaledge_homecontrol_app.Util.Constants.REQUEST_ENABLE_BT;

// This Activity is responsible for displaying scanning device ,scan again and ref to About screen
public class BleDeviceListActivity extends AppCompatActivity
        implements BleScanningView, View.OnClickListener {

    private static final String TAG = "BleDeviceListActivity";
    public static List<BluetoothGattService> serviceList;
    public static boolean isAutoConnected;
    private BluetoothAdapter mBluetoothAdapter;
    private RecyclerView recyclerView;
    private BleDeviceListAdapter mAdapter;
    private BluetoothManager bluetoothManager;
    private RecyclerView.LayoutManager mLayoutManager;
    private BleScanningPresenter mBleScanningPresenter;
    private ImageView aboutImage, scanImage;
    private RuntimePermission runtimePermission;
    private ArrayList<BluetoothDevice> bleDeviceList;
    private SharedPreferences mSharedPreferences;
    private BluetoothDevice pairedDevice;
    private String lastConnectedDevice;
    private Set<BluetoothDevice> pairedDevices;
    private boolean status;
    private ConnectBle connectBle;
    private Util mUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bledevice_list);
        GELogger.i(TAG, "onCreate()");
        init();
        checkForAutoConnect();

    }

    // Initialization
    private void init() {
        runtimePermission = RuntimePermission.getInstance(this);
        aboutImage = (ImageView) findViewById(R.id.aboutImageViewClick);
        scanImage = (ImageView) findViewById(R.id.scanBLEImageView);

        aboutImage.setOnClickListener(this);
        scanImage.setOnClickListener(this);
        bleDeviceList = new ArrayList<BluetoothDevice>();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        connectBle = ConnectBle.getInstance(this);
        mBleScanningPresenter = new BLEScanningModel(BleDeviceListActivity.this, this);

        mUtil = new Util();

    }

    /*
     * This method will check for auto connect is possible or not depends on last
     * connected and paired device list
     *
     */
    private void checkForAutoConnect() {
        isAutoConnected = true;
        mSharedPreferences = getSharedPreferences(getString(R.string.last_paired_preference),
                Context.MODE_PRIVATE);
        lastConnectedDevice = mSharedPreferences.getString(Constants.MAC_ADDRESS, null);
        GELogger.i(TAG, "lastConnectedDevice -----------" + lastConnectedDevice);
        if (checkLastConnectDevice(lastConnectedDevice)) {
            GELogger.i(TAG, " auto connect...");
            tryAutoConnect(pairedDevice);
        } else {
            isAutoConnected = false;
        }
    }


    /**
     * It will check the last connected device is already paired or not
     *
     * @param lastConnectedDevice last connected device from shared preference
     * @return boolean
     */
    private boolean checkLastConnectDevice(String lastConnectedDevice) {
        if (lastConnectedDevice != null) {
            GELogger.i(TAG, lastConnectedDevice);

            pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(lastConnectedDevice)) {
                    pairedDevice = device;
                    status = true;
                    break;
                } else {
                    status = false;
                }
            }
        } else {
            GELogger.i(TAG, "lastConnectedDevice device is null");
        }
        return status;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isAutoConnected) {
            if (bleDeviceList.size() == 0) {
                GELogger.i(TAG, "Scanning ...");
                scanBle();
            }
        }
    }

    /**
     * This method will try to auto connect after launching the app if last connected device is
     * already in paired list
     *
     * @param pairedDevice last connected device which is already paired
     */
    private void tryAutoConnect(final BluetoothDevice pairedDevice) {
        GELogger.i(TAG, "Scan not required");
        Toast.makeText(this, getString(R.string.auto_connecting) + pairedDevice.getName(), Toast.LENGTH_SHORT).show();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            connectBle.connect(this, pairedDevice,
                    new IServiceDiscovered() {
                        @Override
                        public void onServiceDiscovered(List<BluetoothGattService> service) {
                            GELogger.i(TAG, "ListOfService :" + service);
                            if (service != null) {
                                serviceList = service;
                                for (int i = 0; i < service.size(); i++) {
                                    GELogger.i(TAG, "Service :" + service.get(i));
                                    if (service.get(i).getUuid().equals(Constants.SERVICE)) {
                                        mUtil.goToDeviceDetailsActivity(BleDeviceListActivity.this,
                                                pairedDevice.getName(), pairedDevice.getAddress());
                                    }
                                }
                            } else {
                                Toast.makeText(BleDeviceListActivity.this, getString(R.string.service_not_found), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            GELogger.d(TAG, "Enable Bluetooth");
            Toast.makeText(this, getString(R.string.enable_bluetooth), Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    /**
     * We will get the available BLE device and add to the adapter
     *
     * @param bluetoothDevice ArrayList of BluetoothDevice
     */
    @SuppressLint("LongLogTag")
    @Override
    public void ScannedDevice(ArrayList<BluetoothDevice> bluetoothDevice) {

        if (bluetoothDevice.size() == 0) {
            Toast.makeText(this, getString(R.string.device_not_found), Toast.LENGTH_LONG).show();
        } else {
            mAdapter = new BleDeviceListAdapter(bluetoothDevice, this, mBluetoothAdapter);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            bleDeviceList.addAll(bluetoothDevice);


        }

        GELogger.i(TAG, "BluetoothDevice : " + bluetoothDevice);

    }

    /**
     * OnClick listener on device list activity
     *
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aboutImageViewClick:
                mUtil.goToNextScreen(this, SettingsScreenActivity.class);
                break;
            case R.id.scanBLEImageView:
                BLEService.bleDisconnection();
                if (mAdapter != null) {
                    mAdapter.clear();
                    mAdapter.notifyDataSetChanged();
                }
                scanBle();
                break;
            default:
                break;
        }
    }

    //Scan for BLE devices
    private void scanBle() {
        if (!runtimePermission.hasAllPermissionsGranted()) {
            runtimePermission.requestLocPermissions();

        } else {
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBleScanningPresenter = new BLEScanningModel(BleDeviceListActivity.this, this);
                mBleScanningPresenter.scanLeDevice(mBluetoothAdapter);
            }

        }
    }

    /**
     * @param requestCode  The request code passed in requestPermissions
     * @param permissions  The requested permissions
     * @param grantResults The grant results for the corresponding permissions which is either
     *                     PERMISSION_GRANTED or PERMISSION_DENIED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_ID_MULTIPLE_PERMISSIONS) {
            if (permissions.length > 0) {
                if ((ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED)) {
                } else if (runtimePermission.hasAllPermissionsGranted()) {
                    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                        mBleScanningPresenter = new BLEScanningModel(BleDeviceListActivity.this, this);
                        mBleScanningPresenter.scanLeDevice(mBluetoothAdapter);
                    }

                } else runtimePermission.requestLocPermissions();

            }
        }
    }

    // In onStop we are clearing the adapter value
    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.clear();
            bleDeviceList.clear();
            mAdapter.notifyDataSetChanged();
            isAutoConnected = false;
            GELogger.i(TAG, "bleList     ===== " + bleDeviceList);

        } else {
            GELogger.i(TAG, "bleList     ===== " + bleDeviceList);

        }
    }

    /**
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            finish();
            Toast.makeText(this, getString(R.string.bluetooth_permission_required),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
