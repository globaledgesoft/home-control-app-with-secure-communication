package com.app.globaledge_homecontrol_app.Activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.globaledge_homecontrol_app.R;
import com.app.globaledge_homecontrol_app.Util.Constants;
import com.app.globaledge_homecontrol_app.Util.GELogger;
import com.app.globaledge_homecontrol_app.Util.GETextView;
import com.globlaedge.cloud.blelibrary.blelibrary.Interface.Java.ConnectBle;
import com.globlaedge.cloud.blelibrary.blelibrary.Interface.Service.BLEService;
import com.globlaedge.cloud.blelibrary.blelibrary.Interface.Service.BleServiceCharecteristic;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

//DeviceDetailsActivity responsible for displaying all connected smart devices related
// information after connecting to particular board
public class DeviceDetailsActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "GE_DeviceDetailsActivity";
    private LineGraphSeries<DataPoint> series;
    private boolean isLock = false;
    private boolean isBulbOn = false;
    private LinearLayout bulbLayout, lockLayout;
    private ImageView bulbIcon, lockIcon, backButton;
    private GETextView bulbStatus, lockStatus, titleDeviceName;
    private Toolbar toolbar;
    private ProgressDialog dialog;
    private GraphView graph;
    private BroadcastReceiver notificationReceiver;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    //BroadcastReceiver to receive notification from smoke detector and bulb
    public BroadcastReceiver notification() {
        BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BLEService.ACTION_SMOKE_DATA_AVAILABLE.equals(action)) {
                    String data = intent.getStringExtra(BLEService.SMOKE_DATA);
                    GELogger.d(TAG, "Smoke Notification data : " + data);
                    int smokeVal = Integer.parseInt(data);
                    Date currentTime = Calendar.getInstance().getTime();

                    //  mDbHelper.insertLiveData(smokeVal,String.valueOf(currentTime));
                    setDataToGraph(currentTime, smokeVal);
                } else if (BLEService.ACTION_BULB_DATA_AVAILABLE.equals(action)) {
                    String data = intent.getStringExtra(BLEService.BULB_DATA);
                    GELogger.d(TAG, "Bulb Notification data : " + data);
                    if (data.equals("2")) {
                        GELogger.d(TAG, "Bulb is not connected with board...");
                        isBulbOn = false;
                        bulbIcon.setImageResource(R.drawable.bulb_off_img);
                        bulbStatus.setText(getString(R.string.bulb_not_enable));
                        bulbLayout.setClickable(false);
                    } else if (data.equals("1")) {
                        GELogger.d(TAG, "Bulb is on...");
                        isBulbOn = true;
                        bulbIcon.setImageResource(R.drawable.bulb_on_img);
                        bulbStatus.setText(getString(R.string.on));
                        bulbLayout.setClickable(true);
                    } else {
                        GELogger.d(TAG, "Bulb is off...");
                        isBulbOn = false;
                        bulbIcon.setImageResource(R.drawable.bulb_off_img);
                        bulbStatus.setText(getString(R.string.off));
                        bulbLayout.setClickable(true);
                    }
                } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    String data = intent.getStringExtra(BLEService.DISCONNECTION_STATUS);
                    GELogger.d(TAG, "Disconnection : " + data);
                    Toast.makeText(context, R.string.disconnected, Toast.LENGTH_LONG).show();
                    stopBle();
                }
            }
        };
        return notificationReceiver;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);
        init();
    }

    // Initialization
    private void init() {

        backButton = (ImageView) findViewById(R.id.deviceDetailsBackButton);

        bulbLayout = (LinearLayout) findViewById(R.id.bulbLayout);
        lockLayout = (LinearLayout) findViewById(R.id.lockLayout);

        bulbIcon = (ImageView) findViewById(R.id.bulbImg);
        lockIcon = (ImageView) findViewById(R.id.lockImg);
        toolbar = (Toolbar) findViewById(R.id.toolbarLayout);


        bulbStatus = (GETextView) findViewById(R.id.bulbStatusTxt);
        lockStatus = (GETextView) findViewById(R.id.lockStatusTxt);
        titleDeviceName = (GETextView) toolbar.findViewById(R.id.deviceDetailsToolbarTextView);
        dialog = new ProgressDialog(DeviceDetailsActivity.this);

        //Broadcast Receiver for Notification
        notificationReceiver = notification();
        final IntentFilter notificationIntentFilter = new IntentFilter();
        notificationIntentFilter.addAction(BLEService.ACTION_BULB_DATA_AVAILABLE);
        notificationIntentFilter.addAction(BLEService.ACTION_SMOKE_DATA_AVAILABLE);
        notificationIntentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        registerReceiver(notificationReceiver, notificationIntentFilter);

        //Graph view initialization
        graph = (GraphView) findViewById(R.id.graph);
        GridLabelRenderer gridLabelRenderer = graph.getGridLabelRenderer();
        gridLabelRenderer.setPadding(Constants.GRAPH_PADDING);
        series = new LineGraphSeries<DataPoint>(generateInitialData());
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(Constants.MIN_X);
        graph.getViewport().setMaxX(Constants.MAX_X);
        graph.getGridLabelRenderer().setHumanRounding(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().scrollToEnd();
        graph.getViewport().setScalable(true);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    //return dateFormat.format(new Date((long) value));
                    return dateFormat.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        //Button click handle
        backButton.setOnClickListener(this);
        bulbLayout.setOnClickListener(this);
        lockLayout.setOnClickListener(this);

        //Setting title with device name and mac address as well in single textview
        Intent intent = getIntent();
        titleDeviceName.setText(String.format("%s\n%s", intent.getStringExtra(Constants.DEVICE_NAME),
                intent.getStringExtra(Constants.MAC_ADDRESS)));
    }


    //check for current servo motor status
    private void readServoMotorStatus(final byte[] data, final String msg, final UUID characteristic) {
        createDialog(msg);
        byte[] obj = data;
        BleServiceCharecteristic.getInstance().writeData(obj, Constants.SERVICE, characteristic);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BleServiceCharecteristic.getInstance().readStatus(Constants.SERVICE, characteristic, DeviceDetailsActivity.this);
            }
        }, Constants.DELAY_1000);
        //Fetching data from Servo motor Sensor
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String value = BleServiceCharecteristic.getInstance().readStatus(Constants.SERVICE, Constants.LOCK_UNLOCK_CHARECTERISTIC, DeviceDetailsActivity.this);
                GELogger.d("Read value : ", value);
                if (!value.isEmpty()) {
                    closeDialog();
                    if (value.contains("1")) {
                        GELogger.d(TAG, "Servo motor is Locked...");
                        isLock = true;
                        lockIcon.setImageResource(R.drawable.locked_img);
                        lockStatus.setText(getString(R.string.locked));
                        //bulb should get turnoff when it is locked
                        byte[] obj = {0};
                        write(obj, "Loading...", Constants.BULB_SENSOR_CHARACTERISTIC);

                    } else {
                        GELogger.d(TAG, "Servo motor is Unlocked...");
                        isLock = false;
                        lockIcon.setImageResource(R.drawable.unlocked_img);
                        lockStatus.setText(getString(R.string.unlocked));
                        //bulb should get turn on when it is UnLocked
                        byte[] obj = {1};
                        write(obj, "Loading...", Constants.BULB_SENSOR_CHARACTERISTIC);
                    }
                } else {
                    GELogger.d(TAG, "Could not get data from board!");
                    closeDialog();
                    Toast.makeText(DeviceDetailsActivity.this, getString(R.string.toast_read_failed), Toast.LENGTH_LONG).show();
                }
            }
        }, Constants.DELAY_2000);
    }

    // Enabling smoke detector and bulb notification to get continuous data
    private void enableNotification() {
        GELogger.d(TAG, "Enabling smoke detector and bulb notification to get continuous data...");
        BleServiceCharecteristic.getInstance().notifyData(Constants.SERVICE, Constants.SMOKE_SENSOR_CHARACTERISTIC);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BleServiceCharecteristic.getInstance().notifyData(Constants.SERVICE, Constants.BULB_SENSOR_CHARACTERISTIC);
            }
        }, Constants.DELAY_1000);

    }

    // Generic write method
    public void write(final byte[] data, final String msg, final UUID characteristic) {
        createDialog(msg);
        byte[] obj = data;
        BleServiceCharecteristic.getInstance().writeData(obj, Constants.SERVICE, characteristic);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BleServiceCharecteristic.getInstance().readStatus(Constants.SERVICE, characteristic, DeviceDetailsActivity.this);
                closeDialog();
            }
        }, Constants.DELAY_1000);
    }

    //Create progress dialog
    private void createDialog(String msg) {
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.show();
    }

    //Closing progress dialog
    private void closeDialog() {
        if (dialog != null) {
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }


    // Read servo motor status
    // Initializing bulb notification
    // Initializing smoke detector notification
    private void initialBoardData() {
        try {
            GELogger.d(TAG, "Fetching data from Board!");
            createDialog((getString(R.string.fetch_data)));
            enableNotification();
            //Fetching data from Servo motor Sensor
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    BleServiceCharecteristic.getInstance().readStatus(Constants.SERVICE, Constants.LOCK_UNLOCK_CHARECTERISTIC, DeviceDetailsActivity.this);
                }
            }, Constants.DELAY_2000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String value = BleServiceCharecteristic.getInstance().readStatus(Constants.SERVICE, Constants.LOCK_UNLOCK_CHARECTERISTIC, DeviceDetailsActivity.this);
                    GELogger.d("Read val ", value);
                    if (!value.isEmpty()) {
                        closeDialog();
                        if (value.contains("1")) {
                            GELogger.d(TAG, "Servo motor is locked...");
                            isLock = true;
                            lockIcon.setImageResource(R.drawable.locked_img);
                            lockStatus.setText(getString(R.string.locked));

                        } else {
                            GELogger.d(TAG, "Servo motor is unlocked...");
                            isLock = false;
                            lockIcon.setImageResource(R.drawable.unlocked_img);
                            lockStatus.setText(getString(R.string.unlocked));

                        }
                    } else {
                        GELogger.d(TAG, "Could not get data from board!");
                        closeDialog();
                        Toast.makeText(DeviceDetailsActivity.this, getString(R.string.toast_read_failed), Toast.LENGTH_LONG).show();
                    }
                }
            }, Constants.DELAY_3000);
        } catch (Exception e) {
            e.printStackTrace();
            GELogger.e(TAG, "DeviceDetailsActivity -> initialBoardData() :" + e.getMessage());
        }
    }


    /**
     * Responsible to generate initial data
     *
     * @return DataPoint[]
     */
    private DataPoint[] generateInitialData() {
        DataPoint[] values = new DataPoint[1];

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 0);

        DataPoint v = new DataPoint(cal.getTime(), 0);
        values[0] = v;

        return values;
    }

    //Appending data to the graph
    private void setDataToGraph(final Date date, int liveValue) {
        GELogger.d(TAG, "Appending data to the graph");
        series.appendData(new DataPoint(date.getTime(), liveValue), true, 120);

        graph.invalidate();
        graph.getViewport().scrollToEnd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialBoardData();
    }

    /**
     * Handling onClick over Bulb and Lock layout
     *
     * @param v View
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bulbLayout:
                if (isBulbOn) {
                    byte[] obj = {0};

                    GELogger.d(TAG, "Turning Off bulb...");
                    write(obj, "Turning Off...", Constants.BULB_SENSOR_CHARACTERISTIC);
                    bulbIcon.setImageResource(R.drawable.bulb_off_img);
                    isBulbOn = false;
                    bulbStatus.setText(getString(R.string.off));
                } else {
                    GELogger.d(TAG, "Turning On bulb...");
                    byte[] obj = {1};
                    write(obj, "Turning On...", Constants.BULB_SENSOR_CHARACTERISTIC);
                    bulbIcon.setImageResource(R.drawable.bulb_on_img);
                    isBulbOn = true;
                    bulbStatus.setText(getString(R.string.on));
                }
                break;
            case R.id.lockLayout:
                if (isLock) {
                    GELogger.d(TAG, "Unlocking the Servo motor...");
                    byte[] obj = {0};
                    readServoMotorStatus(obj, "Unlocking...", Constants.LOCK_UNLOCK_CHARECTERISTIC);
                } else {
                    GELogger.d(TAG, "Locking the Servo motor...");
                    byte[] obj = {1};
                    readServoMotorStatus(obj, "Locking...", Constants.LOCK_UNLOCK_CHARECTERISTIC);
                }
                break;
            case R.id.deviceDetailsBackButton:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    /*
     * Making isAutoConnected as false and returning to previous screen
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BleDeviceListActivity.isAutoConnected = false;
        GELogger.i(TAG, "onBackPressed : ");
    }

    // Disconnecting BLE
    private void stopBle() {
        BLEService.bleDisconnection();
        finish();
    }

    //Unregistering Receiver and Unbinding Services
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
        ConnectBle.getInstance(this).unRegisterRecievr();
        ConnectBle.getInstance(this).unBindService();
    }
}
