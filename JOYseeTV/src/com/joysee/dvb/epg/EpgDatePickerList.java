/**
 * =====================================================================
 *
 * @file  EpgDatePickerList.java
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
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;

import java.util.LinkedList;

class DatePickerAdapterData {
    String[] days;
    String[] dates;
    int mCurrentPos;
}

class EpgDatePickerList extends LinearLayout {
    public interface OnSelectedChangeListener {
        void onSelectedChange(int index);
    }

    private static final String TAG = JLog.makeTag(EpgDatePickerList.class);

    private EpgRootView mRootView;

    private DatePickerAdapterData mData;
    LinkedList<View> mRemovedViews = new LinkedList<View>();

    LayoutAnimationController mMoveUpAnimation;
    LayoutAnimationController mMoveDownAnimaion;
    AnimationListener mMoveAnimLis;

    OnSelectedChangeListener mOnSelectedChangeLis;

    public EpgDatePickerList(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setFocusable(true);
        setClickable(true);
        this.mMoveUpAnimation = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_up_2_self_1_0), 0.0F);
        this.mMoveDownAnimaion = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_down_2_self_1_0), 0.0F);
        mMoveAnimLis = new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setLayoutAnimationListener(null);
                View child = getChildAt(3);
                if (child != null && child instanceof EpgDatePickerListItem) {
                    ((EpgDatePickerListItem) child).setSelectedEx(true);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                int count = getChildCount();
                EpgDatePickerListItem child;
                for (int i = 0; i < count; i++) {
                    child = (EpgDatePickerListItem) getChildAt(i);
                    child.setSelectedEx(false);
                }
            }
        };
    }

    public void movDown() {
        final long begin = JLog.methodBegin(TAG);
        View top = getChildAt(0);
        Object localObject1 = top.getTag();
        int i = 0;
        if (localObject1 != null) {
            i = ((Integer) localObject1).intValue();
        }
        EpgDatePickerListItem addToTop = (EpgDatePickerListItem) this.mRemovedViews.poll();
        if (addToTop == null) {
            EpgDatePickerListItem bottom = (EpgDatePickerListItem) getChildAt(getChildCount() - 1);
            removeView(bottom);
            addToTop = bottom;
        }
        JLog.methodEnd(TAG, begin);
        if (i > 0) {
            ((View) addToTop).setVisibility(View.VISIBLE);
        } else {
            ((View) addToTop).setVisibility(View.INVISIBLE);
        }
        int dataIndex = i - 1;
        JLog.d(TAG, "movDown topItemDataIndex = " + dataIndex);
        ((View) addToTop).setTag(dataIndex);
        addToTop.updateWithData(mData, i - 1);
        addView(addToTop, 0);
        this.clearDisappearingChildren();
        JLog.methodEnd(TAG, begin);
        setLayoutAnimation(this.mMoveDownAnimaion);
        setLayoutAnimationListener(mMoveAnimLis);
        JLog.methodEnd(TAG, begin);
    }

    public void movUp() {
        final long begin = JLog.methodBegin(TAG);
        EpgDatePickerListItem upView = (EpgDatePickerListItem) getChildAt(0);
        EpgDatePickerListItem bottomView = (EpgDatePickerListItem) getChildAt(getChildCount() - 1);
        removeView(upView);
        int i = ((Integer) bottomView.getTag()).intValue();
        if (i + 1 < this.mData.days.length) {
            upView.setTag(Integer.valueOf(i + 1));
            upView.updateWithData(mData, i + 1);
            upView.setVisibility(View.VISIBLE);
            addView(upView);
        } else {
            this.mRemovedViews.add(upView);
        }
        this.clearDisappearingChildren();
        setLayoutAnimation(this.mMoveUpAnimation);
        setLayoutAnimationListener(mMoveAnimLis);
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        JLog.d(TAG, "onKeyDown key = " + KeyEvent.keyCodeToString(keyCode));
        boolean ret = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                event.startTracking();
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mData.mCurrentPos > 0) {
                    movDown();
                    playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                    mData.mCurrentPos--;
                } else {
                    if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM) {
                        playSoundEffect(5);
                    }
                }
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mData.mCurrentPos < mData.days.length - 1) {
                    movUp();
                    playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                    mData.mCurrentPos++;
                } else {
                    if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM) {
                        playSoundEffect(5);
                    }
                }
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
                break;
            default:
                break;
        }
        return ret ? true : super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        JLog.d(TAG, "onKeyUp key = " + KeyEvent.keyCodeToString(keyCode));
        boolean ret = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                if (mOnSelectedChangeLis != null) {
                    mOnSelectedChangeLis.onSelectedChange(mData.mCurrentPos);
                }
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (event.isTracking()) {
                    mRootView.changeState(EpgRootView.State.CHANNELLIST, true);
                    playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                }
                ret = true;
                break;
            default:
                break;
        }
        return ret ? true : super.onKeyUp(keyCode, event);
    }

    public void setDatePickerData(DatePickerAdapterData data) {
        final long begin = JLog.methodBegin(TAG);
        mData = data;
        int columnNum = mData.days.length;

        int needView = columnNum + 3 - this.mData.mCurrentPos;
        JLog.d(TAG, "setDatePickerData needView = " + needView + " currentview count = "
                + getChildCount());
        if (needView > getChildCount()) {
            int maxView = Math.min(7, needView);
            while (getChildCount() < maxView) {
                JLog.d(TAG, "updateAllData add View");
                View view = this.mRemovedViews.poll();
                this.addView(view);
            }
        } else {
            int removeCount = getChildCount() - needView;
            int childCount = getChildCount();
            for (int r = 0; r < removeCount; r++) {
                View localView = getChildAt(childCount - 1 - r);
                this.mRemovedViews.add(localView);
                JLog.d(TAG, "setDatePickerData removeView index = " + (childCount - 1 - r));
                removeView(localView);
            }
        }
        EpgDatePickerListItem item;
        for (int index = 0; index < getChildCount(); index++) {
            item = (EpgDatePickerListItem) getChildAt(index);
            int dataIndex = mData.mCurrentPos - 3 + index;
            item.setTag(dataIndex);
            item.updateWithData(mData, dataIndex);
            JLog.d(TAG, "setDatePickerData item " + index + " dataIndex = " + dataIndex);
            if (dataIndex < 0) {
                item.setVisibility(View.INVISIBLE);
            } else {
                item.setVisibility(View.VISIBLE);
            }
        }
        mMoveAnimLis.onAnimationEnd(null);
        JLog.methodEnd(TAG, begin);
    }

    public void setOnSelectedChangeListener(OnSelectedChangeListener lis) {
        this.mOnSelectedChangeLis = lis;
    }

    public void setRootView(EpgRootView root) {
        mRootView = root;
    }

}
