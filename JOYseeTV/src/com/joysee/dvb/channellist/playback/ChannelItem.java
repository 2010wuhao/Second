/**
 * =====================================================================
 *
 * @file  ChannelItem.java
 * @Module Name   com.joysee.dvb.channellist.playback
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月22日
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
 * yueliang         2014年1月22日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.channellist.playback;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.data.EPGProvider;

import java.util.ArrayList;

public class ChannelItem extends FrameLayout {
    private static final String TAG = JLog.makeTag(ChannelItem.class);
    private static final String CHANNEL_NUM_FORMAT = "%03d";
    private TextView mChannelNum;
    private TextView mChannelName;
    private TextView mCurProgram;
    
    private int mChannelNameHeight;
    private int mChannelNameTopMargin;
    private int mChannelNameLeftMargin;

    DvbService mChannel;

    public ChannelItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mChannelNameHeight = getResources().getDimensionPixelSize(R.dimen.playback_channellist_item_channel_name_height);
        mChannelNameTopMargin = getResources().getDimensionPixelSize(R.dimen.playback_channellist_item_channel_name_topmargin);
        mChannelNameLeftMargin = getResources().getDimensionPixelSize(R.dimen.playback_channellist_item_channel_name_leftmargin);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChannelNum = (TextView) findViewById(R.id.playback_channellist_item_channel_num);
        mChannelName = (TextView) findViewById(R.id.playback_channellist_item_channel_name);
        mCurProgram = (TextView) findViewById(R.id.playback_channellist_item_cur_program);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            JLog.d(TAG, "ChannelItem " + this + " gainFocus.");
            setTextColor(Color.WHITE);
        } else {
            JLog.d(TAG, "ChannelItem " + this + " lostFocus.", new RuntimeException());
            setTextColor(Color.BLACK);
        }
    }

    void setChannel(DvbService channel) {
        mChannel = channel;
        mChannelNum.setText(String.format(CHANNEL_NUM_FORMAT, channel.getLogicChNumber()));
        mChannelName.setText(channel.getChannelName());
        ArrayList<Program> ps = EPGProvider.getCurrentSProgramByChannel(getContext(), channel, 1);
        boolean hasProgram = ps != null && ps.size() > 0;
        setProgramVisible(hasProgram);
        if (hasProgram) {
            mCurProgram.setText(ps.get(0).programName);
        }
        JLog.d(TAG, "setChannel " + this);
    }

    void setProgramVisible(boolean visible) {
        if (visible) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, mChannelNameHeight);
            lp.leftMargin = mChannelNameLeftMargin;
            lp.topMargin = mChannelNameTopMargin;
            mChannelName.setLayoutParams(lp);
            mCurProgram.setVisibility(View.VISIBLE);
        } else {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            lp.leftMargin = mChannelNameLeftMargin;
            mChannelName.setLayoutParams(lp);
            mCurProgram.setVisibility(View.INVISIBLE);
        }
    }

    void setTextColor(int color) {
        mChannelNum.setTextColor(color);
        mChannelName.setTextColor(color);
        mCurProgram.setTextColor(color);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mChannelNum.getText());
        sb.append("-");
        sb.append(mChannelName.getText());
        return sb.toString();
    }
}
