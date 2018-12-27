package com.app.globaledge_homecontrol_app.Activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.app.globaledge_homecontrol_app.R;
import com.app.globaledge_homecontrol_app.Util.GETextView;

//This activity is responsible for About Screen which displayed version of application
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private GETextView versionText;
    private String versionName;
    private PackageInfo packageInfo;
    private ImageView backButtonAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        init();
        updateVersion();
    }

    //Initialization
    private void init() {
        versionText = (GETextView) findViewById(R.id.textViewVersion);
        backButtonAbout = (ImageView) findViewById(R.id.backButtonAbout);
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        backButtonAbout.setOnClickListener(this);
    }

    //Updating the version of the application to textView
    private void updateVersion() {
        versionText.setText(String.format(getString(R.string.vesrion), versionName));
    }

    /**
     * OnClick of back button of About Activity
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backButtonAbout:
                onBackPressed();
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
