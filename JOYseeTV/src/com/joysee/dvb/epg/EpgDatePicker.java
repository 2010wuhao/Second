/**
 * =====================================================================
 *
 * @file  EpgDatePicker.java
 * @Module Name   com.joysee.dvb.epg
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月11日
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
 * yueliang         2014年2月11日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.common.DateUtil;
import com.joysee.dvb.epg.EpgDatePickerList.OnSelectedChangeListener;

import java.util.Calendar;

public class EpgDatePicker extends FrameLayout {

    private static final String TAG = JLog.makeTag(EpgDatePicker.class);
    public String[] days = new String[14];
    public String mCurrentDayStr;

    ObjectAnimator mFadeOutAnim = null;
    ObjectAnimator mFadeInAnim = null;
    private View mWheelViewFocusBg;

    private DatePickerAdapterData mDatePickData;
    private EpgProgramData mProgramData;
    private EpgDatePickerList mList;

    public EpgDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        days[0] = getResources().getString(R.string.epg_datepicker_last_monday);
        days[1] = getResources().getString(R.string.epg_datepicker_last_tuesday);
        days[2] = getResources().getString(R.string.epg_datepicker_last_wednesday);
        days[3] = getResources().getString(R.string.epg_datepicker_last_thursday);
        days[4] = getResources().getString(R.string.epg_datepicker_last_friday);
        days[5] = getResources().getString(R.string.epg_datepicker_last_saturday);
        days[6] = getResources().getString(R.string.epg_datepicker_last_sunday);
        days[7] = getResources().getString(R.string.epg_datepicker_monday);
        days[8] = getResources().getString(R.string.epg_datepicker_tuesday);
        days[9] = getResources().getString(R.string.epg_datepicker_wednesday);
        days[10] = getResources().getString(R.string.epg_datepicker_thursday);
        days[11] = getResources().getString(R.string.epg_datepicker_friday);
        days[12] = getResources().getString(R.string.epg_datepicker_saturday);
        days[13] = getResources().getString(R.string.epg_datepicker_sunday);

        mCurrentDayStr = getResources().getString(R.string.epg_datepicker_current_day);

        mFadeInAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.anim.fade_in);
        mFadeOutAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.anim.fade_out);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        JLog.d(TAG, "dispatchKeyEvent  " + event.toString());
        boolean ret = false;
        if (action == KeyEvent.ACTION_UP) {
        } else if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                default:
                    break;
            }
        }
        return ret ? true : super.dispatchKeyEvent(event);
    }

    @Override
    public int getId() {
        return mList.getId();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWheelViewFocusBg = findViewById(R.id.epg_programs_datepicker_focused);
        mList = (EpgDatePickerList) findViewById(R.id.epg_programs_datepicker_list);
        mList.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mFadeOutAnim.isStarted()) {
                    mFadeOutAnim.end();
                }
                if (mFadeInAnim.isStarted()) {
                    mFadeInAnim.end();
                }
                if (hasFocus) {
                    mFadeInAnim.setTarget(mWheelViewFocusBg);
                    mFadeInAnim.start();
                } else {
                    mFadeOutAnim.setTarget(mWheelViewFocusBg);
                    mFadeOutAnim.start();
                }
            }
        });
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return mList.requestFocus(direction, previouslyFocusedRect);
    }

    public void setEpgProgramDate(EpgProgramData data) {
        mProgramData = data;

        mDatePickData = new DatePickerAdapterData();
        int curDayOfWeek = DateUtil.getCurrentDay();

        int dayNum = data.mDates.size();
        mDatePickData.dates = new String[dayNum];
        for (int i = 0; i < dayNum; i++) {
            Calendar c = mProgramData.mDates.get(i);
            int month = c.get(Calendar.MONTH) + 1;
            int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            mDatePickData.dates[i] = month + "/" + dayOfMonth;
        }

        String[] visibleDays = new String[dayNum];
        System.arraycopy(days, days.length - dayNum, visibleDays, 0, dayNum);
        JLog.d(TAG, "setEpgProgramData dayNum " + dayNum + " curDayOfWeek = " + curDayOfWeek);
        for (int i = visibleDays.length - 7; i < visibleDays.length && i >= 0; i++) {
            if (i == curDayOfWeek - 1 + (visibleDays.length - 7)) {
                JLog.d(TAG, "setEpgProgramData current index = " + i);
                visibleDays[i] = mCurrentDayStr;
                mDatePickData.mCurrentPos = i;
                break;
            }
        }
        mDatePickData.days = visibleDays;

        mList.setDatePickerData(mDatePickData);
    }

    public void setOnSelectedChangeListener(OnSelectedChangeListener lis) {
        mList.mOnSelectedChangeLis = lis;
    }

    public void setRootView(EpgRootView root) {
        mList.setRootView(root);
    }

    public void updateSelectionByProgramIndex(Program program) {
        int nextCurPos = -1;
        for (int i = 0; i < mProgramData.mDates.size(); i++) {
            Calendar c = mProgramData.mDates.get(i);
            long dayBegin = DateUtil.getDayBeginTime(c.getTimeInMillis());
            long dayEnd = DateUtil.getDayEndTime(c.getTimeInMillis());
            if (program.beginTime >= dayBegin && program.beginTime < dayEnd) {
                nextCurPos = i;
            }
        }
        if (nextCurPos != mDatePickData.mCurrentPos) {
            if (nextCurPos - mDatePickData.mCurrentPos == 1) {
                mList.movUp();
                mDatePickData.mCurrentPos++;
            } else if (nextCurPos - mDatePickData.mCurrentPos == -1) {
                mList.movDown();
                mDatePickData.mCurrentPos--;
            } else {
                mDatePickData.mCurrentPos = nextCurPos;
                mList.setDatePickerData(mDatePickData);
            }
            JLog.d(TAG, "updateSelectionByProgramIndex program = " + program + " pos = " + mDatePickData.mCurrentPos);
        }
    }
}
