/**
 * =====================================================================
 *
 * @file  LiveGuideProgramRow.java
 * @Module Name   com.joysee.dvb.liveguide
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月17日
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
 * YueLiang         2014年2月17日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.liveguide;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.bean.Program;

import java.util.ArrayList;

public class LiveGuideProgramRow extends LinearLayout {

    private static final String TAG = JLog.makeTag(LiveGuideProgramRow.class);

    private ArrayList<Program> mData;
    LiveGuideProgramItem[] mChannelViews = null;
    int mVisibleCount = -1;

    public LiveGuideProgramRow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        JLog.d(TAG, "onFinishInflate");
        int count = getChildCount();
        mChannelViews = new LiveGuideProgramItem[count];
        for (int i = 0; i < count; i++) {
            mChannelViews[i] = (LiveGuideProgramItem) getChildAt(i);
        }
    }

    public void updateWithData(LiveGuideProgramData data, int position) {
        final long begin = JLog.methodBegin(TAG);
        if (data.mPrograms == null || position < 0) {
            return;
        }

        this.mData = new ArrayList<Program>();
        int maxPosition = Math.min(position * LiveGuideProgramGrid.NUMCOLUMN + LiveGuideProgramGrid.NUMCOLUMN - 1,
                data.mPrograms.size() - 1);
        for (int i = position * LiveGuideProgramGrid.NUMCOLUMN; i <= maxPosition; i++) {
            Program channel = data.mPrograms.get(i);
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
                mChannelViews[i].setProgram(mData.get(i));
            } else {
                getChildAt(i).setVisibility(View.INVISIBLE);
            }
        }
        JLog.methodEnd(TAG, begin);
    }
}
