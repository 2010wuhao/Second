/**
 * =====================================================================
 *
 * @file   MainActivity.java
 * @Module Name   com.joysee.tvbox.settings.main
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 15, 2014
 * @brief  This file is the http **** implementation.
 * @This file is responsible by ANDROID TEAM.
 * @Comments:
 * =====================================================================
 * Revision History:
 *
 *                   Modification  Tracking
 *
 * Author            Date            OS version        Reason
 * ----------      ------------     -------------     -----------
 * wumingjun         @Apr 15, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.main;

import com.joysee.common.widget.JTextViewWithTTF;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetManager;
import android.net.ethernet.EthernetStateTracker;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.about.AboutActivity;
import com.joysee.tvbox.settings.apps.AppManagerActivity;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.Cursor;
import com.joysee.tvbox.settings.base.SettingsButtonPanel;
import com.joysee.tvbox.settings.common.CommonSettingsActivity;
import com.joysee.tvbox.settings.main.SettingsButton.AnimationEndListener;
import com.joysee.tvbox.settings.network.NetworkActivity;
import com.joysee.tvbox.settings.soundimage.ImageAndSoundActivity;
import com.joysee.tvbox.settings.upgrade.SystemUpgradeActivity;

public class MainActivity extends BaseActivity implements AnimationEndListener, OnClickListener {

    private SettingsButtonPanel mSettingsPanel;
    private Cursor mCursor;
    private SettingsButton mNetworkButton;
    private SettingsButton mImageAndSoundButton;
    private SettingsButton mAppsButton;
    private SettingsButton mCommonButton;
    private SettingsButton mSystemUpgradeButton;
    private SettingsButton mAboutButton;

    private ImageView mImageNetworkState;
    private JTextViewWithTTF mJTextConnectionState;

    private int onResumeNetworkType;

    private WifiManager mWifiManager;
    private IntentFilter mFilter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                // updateWifiState();
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.isConnected()) {
                    mImageNetworkState.setImageResource(R.drawable.main_settings_wifi);
                    mJTextConnectionState.setText(R.string.main_button_network_connected);
                }
            } else if (intent.getAction().equals(EthernetManager.ETH_STATE_CHANGED_ACTION)) {
                final int event = intent.getIntExtra(EthernetManager.EXTRA_ETH_STATE, EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED);
                updateEthState(event);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(EthernetManager.ETH_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void setupView() {
        this.setContentView(R.layout.activity_main);
        mSettingsPanel = (SettingsButtonPanel) findViewById(R.id.settings_panel);
        mCursor = (Cursor) findViewById(R.id.main_cusor);
        mNetworkButton = (SettingsButton) findViewById(R.id.btn_network);
        mImageAndSoundButton = (SettingsButton) findViewById(R.id.btn_images_sound);
        mAppsButton = (SettingsButton) findViewById(R.id.btn_apps);
        mCommonButton = (SettingsButton) findViewById(R.id.btn_common);
        mSystemUpgradeButton = (SettingsButton) findViewById(R.id.btn_system_upgrade);
        mAboutButton = (SettingsButton) findViewById(R.id.btn_about);

        mImageNetworkState = (ImageView) mNetworkButton.findViewById(R.id.img_settings_icon);
        mJTextConnectionState = (JTextViewWithTTF) mNetworkButton.findViewById(R.id.txt_settings_button);
        // Set listener
        mNetworkButton.setOnClickListener(this);
        mNetworkButton.setOnAnimEndListener(this);
        mImageAndSoundButton.setOnClickListener(this);
        mAppsButton.setOnClickListener(this);
        mCommonButton.setOnClickListener(this);
        mSystemUpgradeButton.setOnClickListener(this);
        mAboutButton.setOnClickListener(this);

        mSettingsPanel.setCursor(mCursor);
        mSettingsPanel.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAnimationEnd() {
        if (mCursor != null && mCursor.getVisibility() != View.VISIBLE) {
            mCursor.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSettingsPanel.postDelayed(new Runnable() {

            @Override
            public void run() {
                mSettingsPanel.setVisibility(View.VISIBLE);
            }
        }, 300);
        if (isNetworkAvailable()) {
            if (onResumeNetworkType == ConnectivityManager.TYPE_WIFI) {
                updateWifiState();
            } else if (onResumeNetworkType == ConnectivityManager.TYPE_ETHERNET) {
                updateEthState(EthernetStateTracker.EVENT_HW_CONNECTED);
            }
        } else {
            updateEthState(EthernetStateTracker.EVENT_HW_DISCONNECTED);
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof SettingsButton) {
            Intent intent = new Intent();
            if (v.equals(mAboutButton)) {
                intent.setClass(v.getContext(), AboutActivity.class);
            } else if (v.equals(mCommonButton)) {
                intent.setClass(v.getContext(), CommonSettingsActivity.class);
            } else if (v.equals(mImageAndSoundButton)) {
                intent.setClass(v.getContext(), ImageAndSoundActivity.class);
            } else if (v.equals(mNetworkButton)) {
                intent.setClass(v.getContext(), NetworkActivity.class);
            } else if (v.equals(mAppsButton)) {
                intent.setClass(v.getContext(), AppManagerActivity.class);
            } else if (v.equals(mSystemUpgradeButton)) {
                intent.setClass(v.getContext(), SystemUpgradeActivity.class);
            }
            v.getContext().startActivity(intent);
        }

    }

    private void updateWifiState() {
        int state = mWifiManager.getWifiState();
        switch (state) {
        case WifiManager.WIFI_STATE_DISABLING:
            break;
        case WifiManager.WIFI_STATE_DISABLED:
            mImageNetworkState.setImageResource(R.drawable.main_settings_network_connected);
            mJTextConnectionState.setText(R.string.main_button_network_unconnected);
            break;
        case WifiManager.WIFI_STATE_ENABLING:
            break;
        case WifiManager.WIFI_STATE_ENABLED:
            mImageNetworkState.setImageResource(R.drawable.main_settings_wifi);
            mJTextConnectionState.setText(R.string.main_button_network_connected);
            break;
        case WifiManager.WIFI_STATE_UNKNOWN:
            mImageNetworkState.setImageResource(R.drawable.main_settings_network_connected);
            mJTextConnectionState.setText(R.string.main_button_network_unconnected);
            break;
        default:
            break;

        }
    }

    private void updateEthState(int event) {
        switch (event) {
        case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED:
            mImageNetworkState.setImageResource(R.drawable.main_settings_network_connected);
            mJTextConnectionState.setText(R.string.main_button_network_connected);
            break;
        case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED:
            break;
        case EthernetStateTracker.EVENT_HW_CONNECTED:
            mImageNetworkState.setImageResource(R.drawable.main_settings_network_connected);
            mJTextConnectionState.setText(R.string.main_button_network_connected);
            break;
        case EthernetStateTracker.EVENT_HW_PHYCONNECTED:
            break;
        case EthernetStateTracker.EVENT_DHCP_START:
            break;
        case EthernetStateTracker.EVENT_HW_CHANGED:
            break;
        case EthernetStateTracker.EVENT_HW_DISCONNECTED:
            mImageNetworkState.setImageResource(R.drawable.main_settings_network_connected);
            mJTextConnectionState.setText(R.string.main_button_network_unconnected);
            break;
        default:
            break;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    onResumeNetworkType = info[i].getType();
                    return true;
                }
                onResumeNetworkType = -1;
            }
        }
        return false;
    }

}
