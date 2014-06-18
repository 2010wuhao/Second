/**
 * =====================================================================
 *
 * @file  EpgProgramsLayout.java
 * @Module Name   com.joysee.dvb.epg
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月7日
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
 * yueliang         2014年2月7日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.activity.EPGActivity;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.common.DateUtil;
import com.joysee.dvb.epg.EPGProgramsGrid.OnItemClickListener;
import com.joysee.dvb.epg.EpgDatePickerList.OnSelectedChangeListener;
import com.joysee.dvb.epg.EpgRootView.State;
import com.joysee.dvb.epg.EpgRootView.StateTransitionListener;

import java.util.ArrayList;
import java.util.Calendar;

public class EpgProgramsLayout extends FrameLayout implements StateTransitionListener {

    private static final String TAG = JLog.makeTag(EpgProgramsLayout.class);

    private EPGProgramsGrid mProgramsGrid;
    private EpgDatePicker mDatePicker;
    private JTextViewWithTTF mEmptyView;
    private View mHintMenu;
    private View mRightMask;

    public EpgProgramsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBeginTransition(State from, State to, boolean anim) {
        JLog.d(TAG, "onBeginTransition from = " + from + " to = " + to);
        if (to == EpgRootView.State.PROGRAMELIST) {
            if (anim) {
                mDatePicker.requestFocus();
            }
            mHintMenu.setVisibility(View.VISIBLE);
            mRightMask.setVisibility(View.INVISIBLE);
        } else if (to == EpgRootView.State.CHANNELLIST) {
            mHintMenu.setVisibility(View.INVISIBLE);
            mRightMask.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEndTransition(State from, State to, boolean anim) {
        JLog.d(TAG, "onEndTransition from = " + from + " to = " + to);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHintMenu = findViewById(R.id.epg_program_layout_hint_menu);
        mDatePicker = (EpgDatePicker) findViewById(R.id.epg_programs_layout_datepicker);
        mProgramsGrid = (EPGProgramsGrid) findViewById(R.id.epg_programs_gridview);
        mEmptyView = (JTextViewWithTTF) findViewById(R.id.dvb_epg_programs_layout_emptyview);
        mEmptyView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean ret = false;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        ret = true;
                        break;
                }
                return ret;
            }
        });
        mProgramsGrid.setEmptyView(mEmptyView);
        mProgramsGrid.setDatePicker(mDatePicker);

        mDatePicker.setOnSelectedChangeListener(new OnSelectedChangeListener() {

            @Override
            public void onSelectedChange(int index) {
                JLog.d(TAG, "mDatePicker onSelectedChange index = " + index);
                mProgramsGrid.scrollToDay(index);
                mProgramsGrid.showInAnimation(true);
            }
        });
    }

    public void setOnProgramItemClickListener(OnItemClickListener lis) {
        this.mProgramsGrid.setOnItemClickListener(lis);
    }

    public void setRightMask(View view) {
        this.mRightMask = view;
    }

    public void setRootView(EpgRootView root) {
        this.mDatePicker.setRootView(root);
        this.mProgramsGrid.setRootView(root);
    }

    public void showProgramGrid(boolean show) {
        mProgramsGrid.setVisibility(View.INVISIBLE);
    }

    public void updateDate(ArrayList<Program> programs, boolean requestFocus) {
        final long begin = JLog.methodBegin(TAG);
        int lookbackDaysSupport = EPGActivity.LOOKBACK_DAY_SUPPORT;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);

        EpgProgramData data = new EpgProgramData();
        data.mData = new ArrayList<ArrayList<Program>>();
        data.mDates = new ArrayList<Calendar>();
        data.mDayProgramBeginEndPos = new int[7 + lookbackDaysSupport][2];

        long current = c.getTimeInMillis();
        int currentDay = DateUtil.getCurrentDay();
        c.add(Calendar.DAY_OF_YEAR, -(currentDay - 1) - lookbackDaysSupport);

        ArrayList<Program> tRowPrograms = new ArrayList<Program>();
        boolean curFocusIsSet = false;
        long dayBegin;
        long dayEnd;
        for (int dayIndex = 0; dayIndex < data.mDayProgramBeginEndPos.length; dayIndex++) {
            dayBegin = DateUtil.getDayBeginTime(c.getTimeInMillis());
            dayEnd = DateUtil.getDayEndTime(c.getTimeInMillis());
            data.mDayProgramBeginEndPos[dayIndex][0] = data.mData.size();

            int programCount = 0;
            if (programs != null) {
                for (int pIndex = 0; pIndex < programs.size(); pIndex++) {
                    Program program = programs.get(pIndex);
                    long programBeginTime = program.beginTime;
                    if (programBeginTime >= dayBegin && programBeginTime <= dayEnd) {
                        tRowPrograms.add(program);
                        programCount++;
                        if (dayIndex - lookbackDaysSupport == (currentDay - 1) && !curFocusIsSet) {
                            if (programBeginTime <= current) {
                                data.mTopPos = data.mData.size();
                                data.mCursorXPos = tRowPrograms.indexOf(program);
                                data.mCursorYPos = data.mTopPos;
                            } else {
                                curFocusIsSet = true;
                            }
                        }
                        if (tRowPrograms.size() == 4) {
                            data.mData.add(tRowPrograms);
                            tRowPrograms = new ArrayList<Program>();
                        }
                    }
                    if (pIndex == programs.size() - 1) {
                        if (tRowPrograms.size() > 0) {
                            data.mData.add(tRowPrograms);
                            tRowPrograms = new ArrayList<Program>();
                        }
                    }
                }
            }
            data.mDayProgramBeginEndPos[dayIndex][1] = programCount == 0 ? data.mDayProgramBeginEndPos[dayIndex][0] : Math.max(0,
                    data.mData.size() - 1);
            JLog.d(TAG, "updateDateWithChannel day : " + dayIndex + " has " + programCount + " ps. beginIndex = "
                    + data.mDayProgramBeginEndPos[dayIndex][0] +
                    " endIndex = " + data.mDayProgramBeginEndPos[dayIndex][1]);
            data.mDates.add((Calendar) c.clone());
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        JLog.d(TAG, "updateDateWithChannel mTopPos = " + data.mTopPos + " mCursorXPos = " + data.mCursorXPos + " mCursorYPos = "
                + data.mCursorYPos);
        mDatePicker.setEpgProgramDate(data);
        mProgramsGrid.setEpgProgramData(data, requestFocus);
        mProgramsGrid.showInAnimation(true);
        JLog.methodEnd(TAG, begin);
    }
}
