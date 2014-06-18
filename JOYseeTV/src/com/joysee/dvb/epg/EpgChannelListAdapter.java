/**
 * =====================================================================
 *
 * @file  EpgChannelListAdapter.java
 * @Module Name   com.joysee.dvb.epg
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月12日
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
 * yueliang         2014年2月12日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;

import java.util.ArrayList;

public class EpgChannelListAdapter extends BaseAdapter {
    private static final String CHANNEL_NUM_FORMAT = "%03d";
    private Context montext;
    private ArrayList<DvbService> mChannels = null;
    private int mItemHeight = 0;
    private int mItemPaddingLeft = 0;
    private int mTextSize = 0;

    public EpgChannelListAdapter(Context context, ArrayList<DvbService> channels) {
        montext = context;
        mChannels = channels;
        Resources res = context.getResources();
        mItemHeight = res.getDimensionPixelSize(R.dimen.epg_channellist_item_height);
        mItemPaddingLeft = res.getDimensionPixelSize(R.dimen.epg_channellist_item_paddingLeft);
        mTextSize = res.getDimensionPixelSize(R.dimen.epg_channellist_item_textsize);
    }

    @Override
    public int getCount() {
        return mChannels != null ? mChannels.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mChannels != null ? mChannels.get(position) : 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DvbService channel = mChannels.get(position);
        // String channelNum = String.format(CHANNEL_NUM_FORMAT,
        // channel.getLogicChNumber());
        String channelName = channel.getChannelName();
        String text = channelName;
        JTextViewWithTTF item = new JTextViewWithTTF(montext, "fzltzh.TTF");
        item.setPadding(mItemPaddingLeft, 0, 0, 0);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setText(text);
        item.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        item.setMinimumHeight(mItemHeight);
        item.setLines(1);
        item.setTextColor(Color.WHITE);
        item.setSingleLine(true);
        return item;
    }

}
