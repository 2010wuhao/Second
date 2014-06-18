/**
 * =====================================================================
 *
 * @file  ChannelListGridRow.java
 * @Module Name   com.joysee.dvb.channellist
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月15日
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
 * yueliang         2014年2月15日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.channellist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;

import java.util.ArrayList;

public class ChannelListGridRow extends LinearLayout {
    private static final String TAG = JLog.makeTag(ChannelListGridRow.class);

    private ArrayList<DvbService> mData;

    ChannelItem[] mChannelViews = null;

    int mVisibleCount = -1;

    public ChannelListGridRow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        JLog.d(TAG, "onFinishInflate");
        int count = getChildCount();
        mChannelViews = new ChannelItem[count];
        for (int i = 0; i < count; i++) {
            mChannelViews[i] = (ChannelItem) getChildAt(i);
        }

    }

    public void updateWithData(ChannelListData data, int position, boolean requestFocus) {
        final long begin = JLog.methodBegin(TAG);
        if (data.mChannel == null || position < 0) {
            return;
        }

        this.mData = new ArrayList<DvbService>();
        int maxPosition = Math.min(position * ChannelListGrid.NUMCOLUMN + ChannelListGrid.NUMCOLUMN - 1, data.mChannel.size() - 1);
        for (int i = position * ChannelListGrid.NUMCOLUMN; i <= maxPosition; i++) {
            DvbService channel = data.mChannel.get(i);
            if (channel != null) {
                mData.add(channel);
            }
        }

        int count = getChildCount();
        mVisibleCount = 0;
        for (int i = 0; i < count; i++) {
            if (i < mData.size()) {
                mVisibleCount++;
                getChildAt(i).setVisibility(View.VISIBLE);
                // mChannelViews[i].setCellInfo(i, position);
                mChannelViews[i].setChannel(mData.get(i));
                if (requestFocus && position == data.mCursorYPos) {
                    if (i == data.mCursorXPos) {
                        final int focusView = i;
                        post(new Runnable() {
                            @Override
                            public void run() {
                                ((ViewGroup) ChannelListGridRow.this.getParent()).clearDisappearingChildren();
                                boolean bool = ChannelListGridRow.this.getChildAt(focusView).requestFocus();
                                JLog.d(TAG, "child  " + ChannelListGridRow.this.getChildAt(focusView)
                                        + " request focus res " + bool);
                            }
                        });
                    }
                }
            } else {
                getChildAt(i).setVisibility(View.INVISIBLE);
            }
        }
        JLog.methodEnd(TAG, begin);
    }

}
