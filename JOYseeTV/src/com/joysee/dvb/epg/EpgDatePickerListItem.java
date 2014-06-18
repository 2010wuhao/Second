/**
 * =====================================================================
 *
 * @file  EpgDatePickerListItem.java
 * @Module Name   com.joysee.dvb.epg
 * @author YueLiang
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
 * YueLiang         2014年2月11日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;

public class EpgDatePickerListItem extends LinearLayout {

    private static final String TAG = JLog.makeTag(EpgDatePickerListItem.class);

    private JTextViewWithTTF mDay;
    private JTextViewWithTTF mDate;

    private DatePickerAdapterData mData;
    private int mDataIndex = -1;
    private boolean mSelected;
    private Paint mPaint;
    
    private int mDayTextSizeNor;
    private int mDayTextSizeSel;
    private int mDateTextSizeNor;
    private int mDateTextSizeSel;

    public EpgDatePickerListItem(Context context, AttributeSet attr) {
        super(context, attr);
        setGravity(Gravity.CENTER);
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setColor(getResources().getColor(R.color.white_alpha_10));
            mPaint.setStyle(Paint.Style.FILL);
        }
        Resources res = context.getResources();
        mDayTextSizeNor = res.getDimensionPixelSize(R.dimen.epg_datepickerlist_item_day_textsize_normal);
        mDayTextSizeSel = res.getDimensionPixelSize(R.dimen.epg_datepickerlist_item_day_textsize_selected);
        mDateTextSizeNor = res.getDimensionPixelSize(R.dimen.epg_datepickerlist_item_date_textsize_normal);
        mDateTextSizeSel = res.getDimensionPixelSize(R.dimen.epg_datepickerlist_item_date_textsize_selected);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mData != null) {
            JLog.d(TAG, "dispatchDraw : " + mData.days[mDataIndex] + " mDataIndex = " + mDataIndex);
            if (mDataIndex > 0) {
                canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, (int) (getHeight() * 0.22), mPaint);
            }
            if (mDataIndex < (mData.days.length - 1)) {
                canvas.drawLine(getWidth() / 2, (int) (getHeight() * 0.78), getWidth() / 2, getHeight(), mPaint);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDay = (JTextViewWithTTF) findViewById(R.id.epg_datepicker_item_day);
        mDate = (JTextViewWithTTF) findViewById(R.id.epg_datepicker_item_date);
    }

    public void setSelectedEx(boolean selected) {
        if (mSelected != selected) {
            mSelected = selected;
            if (selected) {
                mDay.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDayTextSizeSel);
                mDay.setTextColor(Color.WHITE);
                mDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDateTextSizeSel);
                mDate.setTextColor(Color.WHITE);
            } else {
                mDay.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDayTextSizeNor);
                mDay.setTextColor(getResources().getColor(R.color.white_alpha_10));
                mDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDateTextSizeNor);
                mDate.setTextColor(getResources().getColor(R.color.white_alpha_10));
            }
            invalidate();
        }
    }

    public void updateWithData(DatePickerAdapterData data, int dataIndex) {
        this.mData = data;
        if (dataIndex >= 0 && dataIndex < data.days.length) {
            mDataIndex = dataIndex;
            mDay.setText(data.days[dataIndex]);
            mDate.setText(data.dates[dataIndex]);
            invalidate();
        }
    }
}
