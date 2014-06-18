/**
 * =====================================================================
 *
 * @file   ListAnimationProgressAdapter.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 30, 2014
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
 * wumingjun         @Apr 30, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import java.util.ArrayList;
import java.util.HashMap;

import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.ListAnimationAdapter.Holder;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;
import com.joysee.tvbox.settings.network.AccessPoint;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;

public class ListAnimationWirelessAdapter extends BaseAdapter {

    private ArrayList<ListItem> mList;
    private Holder mHolder;
    private LayoutInflater mInflater;
    private Context mContext;
    private boolean mAnimed;
    private TranslateAnimation mTransAnim;
    // 是否可以刷新
    private boolean mNotifyChange = true;
    private String mConnectingSSID;

    public ListAnimationWirelessAdapter(Context context, ArrayList<ListItem> array) {
        super();
        mContext = context;
        mList = array;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getIndex();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            mHolder = new Holder();
            convertView = mInflater.inflate(R.layout.view_settings_wireless_item, null);
            android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_width), mContext
                    .getResources().getDimensionPixelSize(R.dimen.list_settings_item_height));
            convertView.setLayoutParams(params);
            mHolder.jtxtName = (JTextViewWithTTF) convertView.findViewById(R.id.txt_settings_wireless_item_name);
            mHolder.imgLinkType = (ImageView) convertView.findViewById(R.id.img_list_item_wireless_item_type);
            mHolder.imgOptionRight = (ImageView) convertView.findViewById(R.id.img_list_item_option_right);
            mHolder.imgSignal = (ImageView) convertView.findViewById(R.id.img_list_item_signal);

            convertView.setTag(mHolder);
        } else {
            mHolder = (Holder) convertView.getTag();
        }
        ListItem item = mList.get(position);
        final View temp = convertView;
        // Bind data
        if (item != null && item.getName() != null && !item.getName().equals("")) {
            mHolder.jtxtName.setText(item.getName());
            HashMap<Integer, ListItemData> dataMap = item.getDataMap();
            ListItemData data;
            data = dataMap.get(item.getSelectDataIndex());
            if (data != null) {
                mHolder.imgOptionRight.setVisibility(View.INVISIBLE);
                mHolder.imgLinkType.setVisibility(View.VISIBLE);

                AccessPoint ap = (AccessPoint) data.getObj();
                if (ap != null) {
                    int level = ap.getLevel();
                    Boolean isSaved = ap.isSaved;
                    Boolean isConnected = ap.isConnected;
                    int security = ap.security;
                    String ssId = ap.ssid;
                    isConnected = isConnected == null ? false : isConnected;
                    isSaved = isSaved == null ? false : isSaved;

                    if (ssId != null && !ssId.equals("") && mConnectingSSID != null && !mConnectingSSID.equals("") && ssId.equals(mConnectingSSID)) {
                        // 正在链接WIFI
                        String connecting = mContext.getResources().getString(R.string.network_connecting);
                        mHolder.jtxtName.setText(item.getName() + connecting);
                        mHolder.imgSignal.setImageResource(R.anim.anim_wifi_connecting);
                        AnimationDrawable anim = (AnimationDrawable) mHolder.imgSignal.getDrawable();
                        anim.start();
                        // 清空SSID
                        setConnectingSSID(null);
                    } else {

                        mHolder.imgSignal.setImageResource(getWifiSignalRes(level, isConnected));
                    }

                    if (isSaved || isConnected) {
                        mHolder.imgOptionRight.setVisibility(View.VISIBLE);
                    }
                    if (isConnected && mConnectingSSID == null) {
                        mHolder.imgLinkType.setImageResource(R.drawable.radiobutton_selected);
                    } else if (security == AccessPoint.SECURITY_NONE) {
                        mHolder.imgLinkType.setVisibility(View.INVISIBLE);
                    } else {
                        mHolder.imgLinkType.setImageResource(R.drawable.icon_lock_network_setting);
                    }
                }
            }
        }
        if (!mAnimed && position < 6) {
            temp.setVisibility(View.INVISIBLE);
            temp.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mTransAnim = new TranslateAnimation(950, 0, 0, 0);
                    mTransAnim.setDuration(position * 20 + 200);
                    temp.setVisibility(View.VISIBLE);
                    temp.startAnimation(mTransAnim);
                }
            }, 200);
            if (position == mList.size() - 1 || position == ListViewEx.LIST_VISIBLE_ITEMS_COUNT - 1) {

                mAnimed = true;

            }
        }
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        if (mNotifyChange) {
            super.notifyDataSetChanged();
        }
    }

    private int getWifiSignalRes(int level, boolean isConnected) {
        if (!isConnected || (mConnectingSSID != null && !mConnectingSSID.equals(""))) {
            switch (level) {
            case -1:
                return R.drawable.icon_wifi_network_setting_0;
            case 0:
                return R.drawable.icon_wifi_network_setting_0;
            case 1:
                return R.drawable.icon_wifi_network_setting_1;
            case 2:
                return R.drawable.icon_wifi_network_setting_2;
            case 3:
                return R.drawable.icon_wifi_network_setting_3;
            case 4:
                return R.drawable.icon_wifi_network_setting_4;
            default:
                break;
            }
        } else {
            switch (level) {
            case 1:
                return R.drawable.icon_wifi_network_connecting_setting_1;
            case 2:
                return R.drawable.icon_wifi_network_connecting_setting_2;
            case 3:
                return R.drawable.icon_wifi_network_connecting_setting_3;
            case 4:
                return R.drawable.icon_wifi_network_connecting_setting_4;
            default:
                break;
            }
        }
        return 0;
    }

    public boolean isNotifyChange() {
        return mNotifyChange;
    }

    public void setNotifyChange(boolean mNotifyChange) {
        this.mNotifyChange = mNotifyChange;
    }

    public String getConnectingSSID() {
        return mConnectingSSID;
    }

    public void setConnectingSSID(String ssid) {
        this.mConnectingSSID = ssid;
    }

    public class Holder {
        JTextViewWithTTF jtxtName;
        ImageView imgLinkType;
        ImageView imgOptionRight;
        ImageView imgSignal;
    }

}
