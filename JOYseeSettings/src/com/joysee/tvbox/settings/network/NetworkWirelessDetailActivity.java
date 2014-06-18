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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.base.ShadowFrameLayout;
import com.joysee.tvbox.settings.base.SlipButton;
import com.joysee.tvbox.settings.base.SlipButton.OnCheckChangeListener;

public class NetworkWirelessDetailActivity extends BaseActivity implements OnCheckChangeListener, OnClickListener {

    public final static String LEFT_BUTTON_CLICK = "dpad_left_button_clik_to_network_detail";
    public final static String NETWORK_ID = "network_id_of_dpad_left_click";
    private final static String PREFERENCE_WIFI_AUTO_CONFIG = "wifi_auto_config";

    private JTextViewWithTTF mSSID;
    private JTextViewWithTTF mSubmit;
    private JTextViewWithTTF mIgnore;
    private SlipButton mSwitch;
    private ShadowFrameLayout mShadowFrame;

    private EditText mAddress;
    private EditText mMask;
    private EditText mBcast;
    private EditText mDNS;

    private WifiManager mWifiManager;
    private WifiConfiguration mConfig;
    private SharedPreferences mPreferences;

    private boolean isDpadLeftClick;
    private int networkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        Intent intent = this.getIntent();
        isDpadLeftClick = intent.getBooleanExtra(LEFT_BUTTON_CLICK, false);
        networkId = intent.getIntExtra(NETWORK_ID, -1);

        mPreferences = Settings.Preferences.getSharedPreferences(this);
        setContentView(R.layout.activity_network_wireless_detail);
        mSSID = (JTextViewWithTTF) findViewById(R.id.txt_network_detail_config_ssid);
        mSubmit = (JTextViewWithTTF) findViewById(R.id.button_network_detail_submit);
        mIgnore = (JTextViewWithTTF) findViewById(R.id.button_network_detail_ignore);
        mSwitch = (SlipButton) findViewById(R.id.switch_network_config_auto);
        mAddress = (EditText) findViewById(R.id.edit_network_detail_address);
        mMask = (EditText) findViewById(R.id.edit_network_detail_mask);
        mBcast = (EditText) findViewById(R.id.edit_network_detail_bcast);
        mDNS = (EditText) findViewById(R.id.edit_network_detail_dns);
        mShadowFrame = (ShadowFrameLayout) findViewById(R.id.view_shadow_layout);
        mShadowFrame.setBackgroundColor(Color.TRANSPARENT);
        mShadowFrame.setShadow(true, true);

        // 点击事件
        mSwitch.setOnCheckChangeListener(this);
        mAddress.setOnClickListener(this);
        mMask.setOnClickListener(this);
        mBcast.setOnClickListener(this);
        mDNS.setOnClickListener(this);
        mSubmit.setOnClickListener(this);
        mIgnore.setOnClickListener(this);

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

        // mAddress.setCursorVisible(true);
        // mMask.setCursorVisible(true);
        // mBcast.setCursorVisible(true);
        // mDNS.setCursorVisible(true);

        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

    }

    @Override
    public void onClick(View v) {
        if (v == mSubmit) {
            if (mConfig != null) {

                String address = mAddress.getText().toString();
                String mask = mMask.getText().toString();
                String bcast = mBcast.getText().toString();
                String dns = mDNS.getText().toString();

                try {
                    setIpAssignment("STATIC", mConfig);
                    setIpAddress(InetAddress.getByName(address), 24, mConfig);
                    setGateway(InetAddress.getByName(bcast), mConfig);
                    setDNS(InetAddress.getByName(dns), mConfig);
                    mWifiManager.updateNetwork(mConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else if (v == mIgnore) {
            mWifiManager.removeNetwork(mConfig.networkId);
            finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isWifiConnect()) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo.getNetworkId() == networkId) {
                // 需要详细设置
                isDpadLeftClick = false;
            }
        }
        if (isDpadLeftClick) {
            if (networkId == -1) {
                finish();
            }

            mSwitch.setEnabled(false);
            mSwitch.setFocusable(false);

            final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
            if (configs != null) {
                for (WifiConfiguration config : configs) {
                    if (config.networkId == networkId) {
                        mConfig = config;
                        break;
                    }
                }
            }
            if (mConfig != null) {
                ViewGroup group = (ViewGroup) mIgnore.getParent();
                int count = group.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = group.getChildAt(i);
                    if (view instanceof ViewGroup) {

                    }
                    if (view.getId() != R.id.button_network_detail_ignore) {
                        view.setVisibility(View.GONE);
                    } else {
                        group.setPadding(0, 200, 0, 0);
                    }
                }
            }
        } else {
            if (isWifiConnect()) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
                if (configs != null) {
                    for (WifiConfiguration config : configs) {
                        if (config.networkId == wifiInfo.getNetworkId()) {
                            mConfig = config;
                            break;
                        }
                    }
                }
                final boolean isAuto = mPreferences.getBoolean(PREFERENCE_WIFI_AUTO_CONFIG, true);
                mSwitch.post(new Runnable() {

                    @Override
                    public void run() {
                        mSwitch.setChecked(isAuto);
                    }
                });

                mSSID.setText(wifiInfo.getSSID());

                DhcpInfo info = mWifiManager.getDhcpInfo();

                mAddress.setText(getAddress(info.ipAddress));
                mMask.setText(getAddress(info.netmask));
                mBcast.setText(getAddress(info.gateway));
                mDNS.setText(getAddress(info.dns1));
                mAddress.setSelection(mAddress.getText().length());
                mMask.setSelection(mMask.getText().length());
                mBcast.setSelection(mBcast.getText().length());
                mDNS.setSelection(mDNS.getText().length());
            } else {
                finish();
            }
        }

    }

    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    private static String getAddress(int addr) {

        return intToInetAddress(addr).getHostAddress();

    }

    public static int numericToInetAddress(String addrString) throws UnknownHostException {
        return inetAddressToInt(InetAddress.getByName(addrString));
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

    // 从地址转换为int
    public static int inetAddressToInt(InetAddress inetAddr) throws IllegalArgumentException {
        byte[] addr = inetAddr.getAddress();
        if (addr.length != 4) {
            throw new IllegalArgumentException("Not an IPv4 address");
        }
        return ((addr[3] & 0xff) << 24) | ((addr[2] & 0xff) << 16) | ((addr[1] & 0xff) << 8) | (addr[0] & 0xff);
    }

    public static void setIpAssignment(String assign, WifiConfiguration wifiConf) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "ipAssignment");
    }

    public static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        Class laClass = Class.forName("android.net.LinkAddress");
        Constructor laConstructor = laClass.getConstructor(new Class[] { InetAddress.class, int.class });
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    public static void setDNS(InetAddress dns, WifiConfiguration wifiConf) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;

        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
        mDnses.clear(); // or add a new dns address , here I just want to
                        // replace DNS1
        mDnses.add(dns);
    }

    public static Object getField(Object obj, String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    public static Object getDeclaredField(Object obj, String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    public static void setEnumField(Object obj, String value, String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        ArrayList mGateways = (ArrayList) getDeclaredField(linkProperties, "mGateways");
        mGateways.clear();
        mGateways.add(gateway);
    }

    @Override
    public void onCheckChange(boolean check) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREFERENCE_WIFI_AUTO_CONFIG, check);
        editor.apply();
        if (check) {
            mAddress.setEnabled(false);
            mMask.setEnabled(false);
            mBcast.setEnabled(false);
            mDNS.setEnabled(false);
            mSubmit.setEnabled(false);

            mAddress.setFocusable(false);
            mMask.setFocusable(false);
            mBcast.setFocusable(false);
            mDNS.setFocusable(false);
            mSubmit.setFocusable(false);
        } else {
            mAddress.setEnabled(true);
            mMask.setEnabled(true);
            mBcast.setEnabled(true);
            mDNS.setEnabled(true);
            mSubmit.setEnabled(true);

            mAddress.setFocusable(true);
            mMask.setFocusable(true);
            mBcast.setFocusable(true);
            mDNS.setFocusable(true);
            mSubmit.setFocusable(true);
        }
    }

}
