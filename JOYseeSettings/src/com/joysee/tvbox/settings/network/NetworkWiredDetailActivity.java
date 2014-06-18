/**
 * =====================================================================
 *
 * @file   NetworkDetailActivity.java
 * @Module Name   com.joysee.tvbox.settings.network
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

package com.joysee.tvbox.settings.network;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.net.ethernet.EthernetStateTracker;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.base.SlipButton;
import com.joysee.tvbox.settings.base.SlipButton.OnCheckChangeListener;

public class NetworkWiredDetailActivity extends BaseActivity implements OnCheckChangeListener, OnClickListener {

    public static final String ETH_IP = "ip";
    public static final String ETH_MASK = "mask";
    public static final String ETH_GATEWAY = "gateway";
    public static final String ETH_DNS = "dns";

    private Context mContext;
    private BroadcastReceiver mReceiver;
    private EthernetManager mEthManager;
    private EthernetDevInfo mEthInfo;
    private boolean mAutoConfig;

    private JTextViewWithTTF mSubmit;
    private SlipButton mSwitch;

    private EditText mAddress;
    private EditText mMask;
    private EditText mBcast;
    private EditText mDNS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(EthernetManager.ETH_STATE_CHANGED_ACTION)) {
                    updateEth(context, intent);
                }
            }
        };
        mEthManager = (EthernetManager) mContext.getSystemService(Context.ETH_SERVICE);

    }

    @Override
    protected void setupView() {
        mContext = this;
        setContentView(R.layout.activity_network_wired_detail);
        mSubmit = (JTextViewWithTTF) findViewById(R.id.button_network_detail_submit);
        mSwitch = (SlipButton) findViewById(R.id.switch_network_config_auto);
        mAddress = (EditText) findViewById(R.id.edit_network_detail_address);
        mMask = (EditText) findViewById(R.id.edit_network_detail_mask);
        mBcast = (EditText) findViewById(R.id.edit_network_detail_bcast);
        mDNS = (EditText) findViewById(R.id.edit_network_detail_dns);

        // 点击事件
        mSwitch.setOnCheckChangeListener(this);
        mAddress.setOnClickListener(this);
        mMask.setOnClickListener(this);
        mBcast.setOnClickListener(this);
        mDNS.setOnClickListener(this);
        mSubmit.setOnClickListener(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(mAddress, false);
            setShowSoftInputOnFocus.invoke(mMask, false);
            setShowSoftInputOnFocus.invoke(mBcast, false);
            setShowSoftInputOnFocus.invoke(mDNS, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final void updateEth(Context context, Intent intent) {
        final int event = intent.getIntExtra(EthernetManager.EXTRA_ETH_STATE, EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED);
        switch (event) {
        case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED: {
            // Modify for get ip is zero for first time.
            DhcpInfo dhcpInfo = mEthManager.getDhcpInfo();
            if (dhcpInfo == null || dhcpInfo.ipAddress == 0) {
                // saveDhcpEth();
                return;
            }
            refreshAutoConfigState();
            return;
        }
        case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED:
            refreshAutoConfigState();
            return;
        case EthernetStateTracker.EVENT_HW_CONNECTED:
        case EthernetStateTracker.EVENT_HW_PHYCONNECTED:
            if (mEthManager.getSavedEthConfig().getConnectMode().equals(EthernetDevInfo.ETH_CONN_MODE_MANUAL)) {
                // saveEth(1);
            } else {
                // saveDhcpEth();
            }
            break;
        case EthernetStateTracker.EVENT_DHCP_START:
            // There will be a DISCONNECTED and PHYCONNECTED when doing a
            // DHCP request. Ignore them.
            return;
        case EthernetStateTracker.EVENT_HW_CHANGED:
            if (mEthManager.getEthState() == EthernetManager.ETH_STATE_ENABLED && mEthManager.isEthDeviceAdded()) {
                return;
            }
            return;
        case EthernetStateTracker.EVENT_HW_DISCONNECTED:
            return;
        }
    }

    private void refreshAutoConfigState() {
        if (mEthManager != null && mEthManager.isEthConfigured()) {
            if (mEthManager.getSavedEthConfig().getConnectMode().equals(EthernetDevInfo.ETH_CONN_MODE_MANUAL)) {
                mAutoConfig = false;
            } else {
                mAutoConfig = true;
            }
        }
    }

    public boolean isAutoConfig() {
        return mAutoConfig;
    }

    private EthernetDevInfo getEthernetDevInfo() {
        String[] Devs = mEthManager.getDeviceNameList();
        EthernetDevInfo info;
        info = mEthManager.getSavedEthConfig();
        if (info != null && info.getIpAddress() != null && info.getNetMask() != null && info.getRouteAddr() != null && info.getDnsAddr() != null) {
            return info;
        }
        info = new EthernetDevInfo();
        if (Devs != null) {
            info.setIfName(Devs[0]);
            info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_DHCP);
            if (mEthManager.isEthConfigured()) {
                DhcpInfo dhcpInfo = mEthManager.getDhcpInfo();
                try {
                    SharedPreferences preferences = mContext.getSharedPreferences(Settings.Preferences.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                    info.setIpAddress(preferences.getString(ETH_IP, getAddress(dhcpInfo.ipAddress)));
                    info.setDnsAddr(preferences.getString(ETH_DNS, getAddress(dhcpInfo.dns1)));
                    info.setNetMask(preferences.getString(ETH_MASK, getAddress(dhcpInfo.netmask)));
                    info.setRouteAddr(preferences.getString(ETH_GATEWAY, getAddress(dhcpInfo.gateway)));
                } catch (Exception e) {
                }
            }
        }
        return info;
    }

    private static String getAddress(int addr) {
        return intToInetAddress(addr).getHostAddress();
    }

    // 从Int转换为地址
    private static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte) (0xff & hostAddress), (byte) (0xff & (hostAddress >> 8)), (byte) (0xff & (hostAddress >> 16)), (byte) (0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mSubmit) {
            refreshAutoConfigState();
            if (mEthInfo != null && !isAutoConfig()) {

                String address = mAddress.getText().toString();
                String mask = mMask.getText().toString();
                String bcast = mBcast.getText().toString();
                String dns = mDNS.getText().toString();

                try {

                    mEthInfo.setDnsAddr(dns);
                    mEthInfo.setIpAddress(address);
                    mEthInfo.setNetMask(mask);
                    mEthInfo.setRouteAddr(bcast);

                    mEthManager.updateEthDevInfo(mEthInfo);
                    mEthManager.setEthEnabled(true);
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mEthInfo = getEthernetDevInfo();
        if (mEthInfo != null) {

            mAddress.setText(mEthInfo.getIpAddress());
            mMask.setText(mEthInfo.getNetMask());
            mBcast.setText(mEthInfo.getRouteAddr());
            mDNS.setText(mEthInfo.getDnsAddr());
            mAddress.setSelection(mAddress.getText().length());
            mMask.setSelection(mMask.getText().length());
            mBcast.setSelection(mBcast.getText().length());
            mDNS.setSelection(mDNS.getText().length());
            final boolean flag = EthernetDevInfo.ETH_CONN_MODE_MANUAL.equals(mEthInfo.getConnectMode());
            setViewEnable(flag);
            setViewFocusable(flag);
            mSwitch.post(new Runnable() {

                @Override
                public void run() {
                    mSwitch.setChecked(flag);
                }
            });
        } else {
            finish();
        }

    }

    private void setViewEnable(boolean flag) {
        mAddress.setEnabled(flag);
        mMask.setEnabled(flag);
        mBcast.setEnabled(flag);
        mDNS.setEnabled(flag);
        mSubmit.setEnabled(flag);

    }

    private void setViewFocusable(boolean flag) {
        mAddress.setFocusable(flag);
        mMask.setFocusable(flag);
        mBcast.setFocusable(flag);
        mDNS.setFocusable(flag);
        mSubmit.setFocusable(flag);
    }

    @Override
    public void onCheckChange(boolean check) {
        if (check) {
            mEthInfo.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_MANUAL);
            setViewEnable(true);
            setViewFocusable(true);
            mEthManager.updateEthDevInfo(mEthInfo);
        } else {
            mEthInfo.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_DHCP);
            setViewEnable(false);
            setViewFocusable(false);
            mEthManager.updateEthDevInfo(mEthInfo);
        }
        // mEthManager.setEthEnabled(true);
    }
}
