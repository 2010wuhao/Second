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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.common.widget.JEditTextWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationWirelessAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.base.StyleDialog;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

public class NetworkWirelessListActivity extends BaseActivity implements OnItemClickListener, OnKeyListener {

    private final static String TAG = NetworkWirelessListActivity.class.getSimpleName();

    private ListViewEx mList;
    private ListView mInnerList;
    private ArrayList<ListItem> mArray;
    private WirelessManager mWirelessManager;
    private List<AccessPoint> mAccessPoints;

    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;
    private ListAnimationWirelessAdapter mAdapter;
    private WifiManager mWifiManager;

    private String mConnectingId;

    private StyleDialog mPasswordDialog;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
    }

    public void onStart() {
        super.onStart();
        mWifiManager.startScan();
        this.registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SupplicantState state = mWifiManager.getConnectionInfo().getSupplicantState();
        if ((state != SupplicantState.ASSOCIATING) && (state != SupplicantState.AUTHENTICATING) && (state != SupplicantState.SCANNING)) {
            loadAccessPoints();
            mAdapter.setConnectingSSID(null);
            mAdapter.setNotifyChange(true);
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void setupView() {
        loadAccessPoints();
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        SupplicantState state = mWifiManager.getConnectionInfo().getSupplicantState();
        if ((state != SupplicantState.ASSOCIATING) && (state != SupplicantState.AUTHENTICATING) && (state != SupplicantState.SCANNING)) {
            mConnectingId = null;
        }

        setContentView(R.layout.activity_network_wireless_list);
        mList = (ListViewEx) findViewById(R.id.list_network_wireless);
        mInnerList = mList.getInnerList();
        mAdapter = ListFactory.initWirelessAccessPointList(mArray, mList, Settings.Type.NETWORK, mConnectingId);
        if (mInnerList != null) {
            mInnerList.setOnItemClickListener(this);
            mInnerList.setOnKeyListener(this);
        }
        mInnerList.setAdapter(mAdapter);

    }

    private void loadAccessPoints() {
        if (mArray == null) {
            mArray = new ArrayList<ListItem>();
        } else {
            mArray.clear();
        }
        if (mWirelessManager == null) {
            mWirelessManager = new WirelessManager(this);
        }
        mAccessPoints = mWirelessManager.getConstructAccessPoints();
        HashMap<Integer, ListItemData> dataMap;
        ListItem item;
        ListItemData data;
        for (int i = 0; i < mAccessPoints.size(); i++) {
            AccessPoint ap = mAccessPoints.get(i);
            dataMap = new HashMap<Integer, ListItemData>();
            item = new ListItem(i, i);
            data = new ListItemData(i, 0);

            // 设置显示Item的数据
            item.setName(ap.ssid);
            item.setDataMap(dataMap);
            item.setSelectDataIndex(0);
            item.setType(Settings.ItemType.SHOW_DIALOG);

            // 设置WIFI数据
            data.setIndex(0);
            data.setValue(ap.getLevel());
            data.setObj(ap);

            dataMap.put(data.getIndex(), data);
            mArray.add(item);
            if (ap.isConnected) {
                mConnectingId = ap.ssid;
            }
        }
    }

    private void handleEvent(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.isConnected()) {
                loadAccessPoints();
                mAdapter.setNotifyChange(true);
                mAdapter.notifyDataSetChanged();
                mList.scrollToTop();
            }
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            SupplicantState state = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        } else {

            loadAccessPoints();
            mAdapter.notifyDataSetChanged();

        }
        Log.d(TAG, "Wireless handleEvent : " + intent.getAction());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        AccessPoint ap = mAccessPoints.get(mList.getSelection());
        if (ap != null) {
            if (ap.isConnected) {

                intent.setClass(view.getContext(), NetworkWirelessDetailActivity.class);
                view.getContext().startActivity(intent);

            } else if (ap.isSaved) {

                mWifiManager.enableNetwork(ap.networkId, true);
                onConnectingWifi(ap.ssid);
            } else if (ap.security != AccessPoint.SECURITY_NONE) {
                showInputPasswordDialog(ap.ssid, ap.security);
            } else {
                connectWifi(ap.ssid, "", ap.security);
                onConnectingWifi(ap.ssid);
            }
        }

    }

    @Override
    public boolean onKey(View view, int code, KeyEvent event) {
        if (event != null && event.getAction() == KeyEvent.ACTION_UP && code == KeyEvent.KEYCODE_DPAD_RIGHT) {
            Intent intent = new Intent();
            AccessPoint ap = mAccessPoints.get(mList.getSelection());
            if (ap != null) {
                if (ap.isSaved) {

                    intent.setClass(view.getContext(), NetworkWirelessDetailActivity.class);
                    intent.putExtra(NetworkWirelessDetailActivity.LEFT_BUTTON_CLICK, true);
                    intent.putExtra(NetworkWirelessDetailActivity.NETWORK_ID, ap.networkId);
                    view.getContext().startActivity(intent);

                }
            }
        }
        return false;
    }

    public WifiConfiguration getConfig(String ssid, String password, int security) {
        WifiConfiguration config = new WifiConfiguration();

        config.SSID = "\"" + ssid + "\"";
        switch (security) {
        case AccessPoint.SECURITY_NONE:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            break;

        case AccessPoint.SECURITY_WEP:
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            if (password.length() != 0) {
                int length = password.length();
                // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                if ((length == 10 || length == 26 || length == 58) && password.matches("[0-9A-Fa-f]*")) {
                    config.wepKeys[0] = password;
                } else {
                    config.wepKeys[0] = '"' + password + '"';
                }
            }
            break;

        case AccessPoint.SECURITY_PSK:
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            if (password.length() != 0) {
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = '"' + password + '"';
                }
            }
            break;

        case AccessPoint.SECURITY_EAP:

            break;
        }

        return config;
    }

    private boolean connectWifi(String ssid, String password, int security) {

        WifiConfiguration config = getConfig(ssid, password, security);
        int networkId = mWifiManager.addNetwork(config);
        boolean ret = mWifiManager.enableNetwork(networkId, true);
        return ret;
    }

    private void showInputPasswordDialog(final String ssid, final int security) {

        StyleDialog.Builder builder = new StyleDialog.Builder(this);
        builder.setTitle(ssid);
        builder.setPositiveButton(R.string.jstyle_dialog_postive_bt);
        builder.setNegativeButton(R.string.jstyle_dialog_negatvie_bt);
        mPasswordDialog = builder.show();
        final JEditTextWithTTF password = (JEditTextWithTTF) mPasswordDialog.findViewById(R.id.content_view_edittext);
        builder.setOnButtonClickListener(new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == DialogInterface.BUTTON_POSITIVE) {

                    String psassword = password.getText().toString();
                    boolean ret = connectWifi(ssid, psassword, security);
                    // if (ret) {
                    onConnectingWifi(ssid);
                    // }
                    mPasswordDialog.dismissByAnimation();
                    password.setText("");
                    password.requestFocus();
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {

                    mPasswordDialog.dismissByAnimation();
                    password.setText("");
                    password.requestFocus();
                }
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {

                    mPasswordDialog.dismissByAnimation();
                    password.setText("");
                    password.requestFocus();
                }
                return false;
            }
        });
        builder.setContentCanFocus(true);
        password.requestFocus();
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String psassword = password.getText().toString();
                    boolean ret = connectWifi(ssid, psassword, security);
                    // if (ret) {
                    onConnectingWifi(ssid);
                    // }
                    mPasswordDialog.dismissByAnimation();
                    password.setText("");
                    password.requestFocus();
                    return true;
                }
                return false;
            }
        });

    }

    public void onConnectingWifi(String ssid) {

        mAdapter.setConnectingSSID(ssid);
        mAdapter.setNotifyChange(true);
        mAdapter.notifyDataSetChanged();
        mAdapter.setNotifyChange(false);

    }

    public static boolean isEthConnect(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();

                if (info != null && info.getType() == ConnectivityManager.TYPE_ETHERNET && info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.v("error", e.toString());
        }
        return false;
    }

}
