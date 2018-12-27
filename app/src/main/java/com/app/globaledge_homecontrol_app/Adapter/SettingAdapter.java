package com.app.globaledge_homecontrol_app.Adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.globaledge_homecontrol_app.Activity.AboutActivity;
import com.app.globaledge_homecontrol_app.R;
import com.app.globaledge_homecontrol_app.Util.Util;

import java.util.ArrayList;

//SettingAdapter provide a binding from an app-specific data set to views that are displayed within
// a recycler view of setting screen
public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.SettingsViewHolder> {
    private TextView settingsItem;
    private ArrayList<String> settingsList;
    private Activity mActivity;
    private Util mUtil;

    public SettingAdapter(ArrayList<String> settingsList, Activity activity) {
        this.settingsList = settingsList;
        this.mActivity = activity;
        mUtil = new Util();
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.settings_row, viewGroup, false);

        return new SettingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder settingsViewHolder, int i) {
        settingsItem.setText(settingsList.get(i));

        settingsViewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                mUtil.goToNextScreen(mActivity, AboutActivity.class);
            }
        });
    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }

    public interface ItemClickListener {
        void onItemClick(int position, View view);
    }

    public class SettingsViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private ItemClickListener clickListner;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            settingsItem = itemView.findViewById(R.id.settingTextView);
            itemView.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener clickListner) {
            this.clickListner = clickListner;
        }

        @Override
        public void onClick(View v) {
            clickListner.onItemClick(getAdapterPosition(), v);

        }
    }
}
