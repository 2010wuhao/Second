/**
 * =====================================================================
 *
 * @file  ChannelListView.java
 * @Module Name   com.joysee.dvb.widget
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
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;
import com.joysee.dvb.bean.ChannelType;

import java.util.ArrayList;

public class ChannelListView extends LinearLayout implements OnFocusChangeListener, OnClickListener {
    public interface OnChannelClickListener {
        void onChannelClick(DvbService channel);
    }

    private static final String TAG = JLog.makeTag(ChannelListView.class);
    private OnChannelClickListener mOnChannelClickLis;
    private ArrayList<ChannelItem> mViews;

    private ChannelListViewData mData;

    private boolean mKeyPressed = false;
    private long mKeyPressInternal = 120;
    private long mCurKeyPressInternal = 120;
    private long mLastKeyDownTime = -1;
    private long mKeyLongPressDownTime = -1;

    public ChannelListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    int checkAndMakeDataIndex(int index) {
        if (index >= mData.mChannels.size()) {
            index = index % mData.mChannels.size();
        } else if (index < 0) {
            do {
                index += mData.mChannels.size();
            } while (index < 0);
        }
        JLog.d(TAG, "checkAndMakeDataIndex index = " + index);
        return index;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();

        boolean ret = false;
        ret = super.dispatchKeyEvent(event);
        if (!ret) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (action == KeyEvent.ACTION_DOWN) {
                        long current = SystemClock.uptimeMillis();
                        if (mKeyLongPressDownTime == -1) {
                            mKeyLongPressDownTime = current;
                        }
                        mCurKeyPressInternal = mKeyPressInternal - 10 * ((current - mKeyLongPressDownTime) / 500);
                        mCurKeyPressInternal = Math.max(mCurKeyPressInternal, 20);
                        if (current - mLastKeyDownTime < mCurKeyPressInternal) {
                            ret = true;
                            break;
                        }
                        JLog.d(TAG, "dispatchKeyEvent mCurKeyPressInternal = " + mCurKeyPressInternal);
                        FocusFinder ff = FocusFinder.getInstance();
                        View next = ff.findNextFocus(this, getFocusedChild(), keyCode == KeyEvent.KEYCODE_DPAD_UP ? View.FOCUS_UP
                                : View.FOCUS_DOWN);
                        JLog.d(TAG, "next focus view = " + next);
                        boolean handle = false;
                        if (next != null && next != getFocusedChild()) {
                            if (next.requestFocus()) {
                                handle = true;
                            }
                        }
                        if (handle) {
                            playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                        } else {
                            if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM) {
                                playSoundEffect(5);
                            }
                        }
                        mKeyPressed = true;
                        mLastKeyDownTime = current;
                    } else {
                        mKeyPressed = false;
                        mLastKeyDownTime = -1;
                        mKeyLongPressDownTime = -1;
                    }
                    ret = true;
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    void moveBottom() {
        ChannelItem top = (ChannelItem) getChildAt(0);
        ChannelItem bottom = (ChannelItem) getChildAt(getChildCount() - 1);
        int dataIndex = (Integer) top.getTag();
        dataIndex--;
        dataIndex = checkAndMakeDataIndex(dataIndex);
        removeView(bottom);
        bottom.setChannel(this.mData.mChannels.get(dataIndex));
        bottom.setTag(dataIndex);
        addView(bottom, 0);
        this.mData.mTopPos--;
    }

    void moveTop() {
        ChannelItem top = (ChannelItem) getChildAt(0);
        ChannelItem bottom = (ChannelItem) getChildAt(getChildCount() - 1);
        int dataIndex = (Integer) bottom.getTag();
        dataIndex++;
        dataIndex = checkAndMakeDataIndex(dataIndex);
        removeView(top);
        top.setChannel(this.mData.mChannels.get(dataIndex));
        top.setTag(dataIndex);
        addView(top);
        this.mData.mTopPos++;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ChannelItem) {
            ChannelItem item = (ChannelItem) v;
            if (mOnChannelClickLis != null) {
                mOnChannelClickLis.onChannelClick(item.mChannel);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViews = new ArrayList<ChannelItem>();
        ChannelItem item;
        for (int i = 0; i < getChildCount(); i++) {
            item = (ChannelItem) getChildAt(i);
            item.setOnFocusChangeListener(this);
            item.setOnClickListener(this);
            mViews.add(item);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v instanceof ChannelItem) {
                if (v.getTop() - 20 <= 0) {
                    moveBottom();
                } else if (v.getBottom() + 20 > getHeight()) {
                    moveTop();
                }
            }
        }
    }

    public void rebuildLayoutByChannel(DvbService channel) {
        if (channel != null) {
            int dataIndex = mData.mChannels.indexOf(channel);
            mData.mCurPos = dataIndex != -1 ? dataIndex : 0;
            mData.mTopPos = mData.mCurPos - 3;
            mData.mFront = true;
            updateWithData(mData);
        }
    }

    public void setOnChannelClickListener(OnChannelClickListener lis) {
        mOnChannelClickLis = lis;
    }

    void updateWithData(ChannelListViewData data) {
        this.mData = data;
        boolean focusIsSet = false;
        if (mData.mChannels != null && mData.mChannels.size() > 0) {
            int dataIndex;
            for (int i = 0; i < getChildCount(); i++) {
                dataIndex = mData.mTopPos - 1 + i;
                dataIndex = checkAndMakeDataIndex(dataIndex);

                final ChannelItem channelItem = (ChannelItem) getChildAt(i);
                channelItem.setTag(dataIndex);
                channelItem.setChannel(data.mChannels.get(dataIndex));
                if (this.mData.mFront) {
                    if (!focusIsSet && dataIndex == this.mData.mCurPos) {
                        focusIsSet = true;
                        JLog.d(TAG, "updateWithData dataIndex = " + dataIndex + " requestFocus");
                        post(new Runnable() {

                            @Override
                            public void run() {
                                channelItem.requestFocus();
                            }
                        });
                    }
                }
            }
        }
    }
}

class ChannelListViewData {
    boolean mFront;
    int mTopPos;
    int mCurPos;
    ChannelType mType;
    ArrayList<DvbService> mChannels;

    public int getCnt() {
        return mChannels.size();
    }

    public void setLastPos(int top, int current) {
        this.mTopPos = top;
        this.mCurPos = current;
    }
}
