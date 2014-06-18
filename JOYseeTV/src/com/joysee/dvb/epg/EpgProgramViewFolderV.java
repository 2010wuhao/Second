/**
 * =====================================================================
 *
 * @file  EpgProgramViewFolderV.java
 * @Module Name   com.joysee.adtv.ui.epg
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年12月7日
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
 * YueLiang         2013年12月7日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.bean.Program;

import java.util.ArrayList;

public class EpgProgramViewFolderV extends LinearLayout {

    private static final String TAG = JLog.makeTag(EpgProgramViewFolderV.class);
    private ArrayList<Program> mData;

    public static int mChildNum = -1;
    boolean mIsLastColumnOfDay = false;

    EpgProgramItem[] mProgramsViews = null;
    int mVisibleCount = -1;

    public EpgProgramViewFolderV(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EpgProgramViewFolderV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        JLog.d(TAG, "onFinishInflate");
        mChildNum = getChildCount();
        mProgramsViews = new EpgProgramItem[mChildNum];
        for (int i = 0; i < mChildNum; i++) {
            mProgramsViews[i] = (EpgProgramItem) getChildAt(i);
        }
    }

    public void setLastColumnOfDay(boolean bool) {
        if (mIsLastColumnOfDay != bool) {
            mIsLastColumnOfDay = bool;
        }
    }

    public void updateWithData(EpgProgramData data, int position, boolean requestFocus) {
        final long begin = JLog.methodBegin(TAG);
        if (data.mData == null || position < 0) {
            return;
        }

        this.mData = data.mData.get(position);
        int count = getChildCount();
        mVisibleCount = 0;
        for (int i = 0; i < count; i++) {
            if (i < mData.size()) {
                mVisibleCount++;
                getChildAt(i).setVisibility(View.VISIBLE);
                mProgramsViews[i].setProgram(mData.get(i));
                if (requestFocus && position == data.mCursorYPos) {
                    if (i == data.mCursorXPos) {
                        final int focusView = i;
                        post(new Runnable() {
                            @Override
                            public void run() {
                                ((ViewGroup) EpgProgramViewFolderV.this.getParent()).clearDisappearingChildren();
                                boolean bool = EpgProgramViewFolderV.this.getChildAt(focusView).requestFocus();
                                JLog.d(TAG, "child  " + EpgProgramViewFolderV.this.getChildAt(focusView)
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
