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

package com.joysee.dvb.liveguide.playback;

import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.bean.ProgramType;

import java.util.ArrayList;
import java.util.LinkedList;

public class ProgramListView extends LinearLayout implements OnFocusChangeListener, OnClickListener {
    public class MoveDownAnim extends LayoutAnimationController {

        public MoveDownAnim(Animation anim) {
            super(anim, 0.0F);
        }

        protected int getTransformedIndex(AnimationParameters param) {
            TranslateAnimation trans = new TranslateAnimation(1, 0.0F, 1, 0.0F, 1, -1.0F, 1, 0.0F);
            trans.setDuration(mCurKeyPressInternal);
            trans.setInterpolator(new LinearInterpolator());
            setAnimation(trans);
            return Math.abs(param.index);
        }
    }

    public class MoveUpAnim extends LayoutAnimationController {

        public MoveUpAnim(Animation anim) {
            super(anim, 0.0F);
        }

        protected int getTransformedIndex(AnimationParameters param) {
            TranslateAnimation trans = new TranslateAnimation(1, 0.0F, 1, 0.0F, 1, 1.0F, 1, 0.0F);
            trans.setDuration(mCurKeyPressInternal);
            trans.setInterpolator(new LinearInterpolator());
            setAnimation(trans);
            return Math.abs(param.index);
        }
    }

    public interface OnProgramClickListener {
        void onProgramClick(Program p);
    }

    private static final String TAG = JLog.makeTag(ProgramListView.class);
    private OnProgramClickListener mOnChannelClickLis;
    private ArrayList<ProgramListItem> mViews;

    private ProgramListViewData mData;
    LayoutAnimationController mMoveUpAnimation;

    LayoutAnimationController mMoveDownAnimaion;

    private boolean mKeyPressed = false;
    private long mKeyPressInternal = 180;
    private long mCurKeyPressInternal = 180;
    private long mLastKeyDownTime = -1;
    private long mKeyLongPressDownTime = -1;

    LinkedList<View> mRemovedViews = new LinkedList<View>();

    public ProgramListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMoveUpAnimation = new MoveUpAnim(AnimationUtils.loadAnimation(
                context, R.anim.move_up_2_self_1_0));
        this.mMoveDownAnimaion = new MoveDownAnim(AnimationUtils.loadAnimation(
                context, R.anim.move_down_2_self_1_0));
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
                        mCurKeyPressInternal = mKeyPressInternal - 10 * ((current - mKeyLongPressDownTime) / 1000);
                        mCurKeyPressInternal = Math.max(mCurKeyPressInternal, 110);
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
                        if (next != null) {
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
        View top = getChildAt(0);
        Object localObject1 = top.getTag();
        int i = 0;
        if (localObject1 != null) {
            i = ((Integer) localObject1).intValue();
        }
        ProgramListItem addToTop = (ProgramListItem) this.mRemovedViews.poll();
        if (addToTop == null) {
            ProgramListItem bottom = (ProgramListItem) getChildAt(getChildCount() - 1);
            removeView(bottom);
            addToTop = bottom;
        }
        if (i > 0) {
            ((View) addToTop).setVisibility(View.VISIBLE);
        } else {
            ((View) addToTop).setVisibility(View.INVISIBLE);
        }
        ((View) addToTop).setTag(Integer.valueOf(i - 1));
        if (i - 1 >= 0) {
            addToTop.setProgram(this.mData.mPrograms.get(i - 1));
        }
        addView(addToTop, 0);
        this.clearDisappearingChildren();

        setLayoutAnimation(this.mMoveDownAnimaion);
    }

    void moveTop() {
        ProgramListItem upView = (ProgramListItem) getChildAt(0);
        ProgramListItem bottomView = (ProgramListItem) getChildAt(getChildCount() - 1);
        removeView(upView);
        int i = ((Integer) bottomView.getTag()).intValue();
        if (i + 1 < this.mData.getCnt()) {
            upView.setTag(Integer.valueOf(i + 1));
            upView.setProgram(this.mData.mPrograms.get(i + 1));
            upView.setVisibility(View.VISIBLE);
            addView(upView);
        } else {
            this.mRemovedViews.add(upView);
        }
        this.clearDisappearingChildren();
        setLayoutAnimation(this.mMoveUpAnimation);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ProgramListItem) {
            ProgramListItem item = (ProgramListItem) v;
            if (mOnChannelClickLis != null) {
                mOnChannelClickLis.onProgramClick(item.mProgram);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViews = new ArrayList<ProgramListItem>();
        ProgramListItem item;
        for (int i = 0; i < getChildCount(); i++) {
            item = (ProgramListItem) getChildAt(i);
            item.setOnFocusChangeListener(this);
            item.setOnClickListener(this);
            mViews.add(item);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v instanceof ProgramListItem) {
                if (v.getTop() - 20 <= 0) {
                    moveBottom();
                } else if (v.getBottom() + 20 > getHeight()) {
                    moveTop();
                }
            }
        }
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        ProgramListItem top = (ProgramListItem) getChildAt(1);
        if (top != null) {
            top.requestFocus();
        }
        return true;
    }

    public void setOnProgramClickListener(OnProgramClickListener lis) {
        mOnChannelClickLis = lis;
    }

    void updateWithData(ProgramListViewData data) {
        this.mData = data;
        int columnNum = mData.getCnt();

        int needView = columnNum + 1 - this.mData.mTopPos;
        JLog.d(TAG, "updateWithData needView = " + needView + " currentview count = " + getChildCount());
        if (needView > getChildCount()) {
            int maxView = Math.min(5, needView);
            while (getChildCount() < maxView) {
                JLog.d(TAG, "updateWithData add View");
                View view = this.mRemovedViews.poll();
                this.addView(view);
            }
        } else {
            int removeCount = getChildCount() - needView;
            int childCount = getChildCount();
            for (int r = 0; r < removeCount; r++) {
                View localView = getChildAt(childCount - 1 - r);
                this.mRemovedViews.add(localView);
                JLog.d(TAG, "updateWithData removeView index = " + (childCount - 1 - r));
                removeView(localView);
            }
        }

        if (mData.getCnt() > 0) {
            int dataIndex;
            for (int i = 0; i < getChildCount(); i++) {
                dataIndex = mData.mTopPos - 1 + i;

                final ProgramListItem channelItem = (ProgramListItem) getChildAt(i);
                channelItem.setTag(dataIndex);
                if (dataIndex >= 0) {
                    channelItem.setVisibility(View.VISIBLE);
                    channelItem.setProgram(data.mPrograms.get(dataIndex));
                } else {
                    channelItem.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}

class ProgramListViewData {
    boolean mFront;
    int mTopPos;
    int mCurPos;
    ProgramType mType;
    ArrayList<Program> mPrograms;

    public int getCnt() {
        int count = 0;
        if (mPrograms != null) {
            count = mPrograms.size();
        }
        return count;
    }
}
