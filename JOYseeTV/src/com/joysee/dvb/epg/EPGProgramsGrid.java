/**
 * =====================================================================
 *
 * @file  EPGProgramsGrid.java
 * @Module Name   com.joysee.dvb.epg
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月10日
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
 * yueliang         2014年2月10日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
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
import com.joysee.dvb.epg.EpgRootView.State;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

class EpgProgramData {
    public ArrayList<ArrayList<Program>> mData;
    public int[][] mDayProgramBeginEndPos;
    public ArrayList<Calendar> mDates;
    public int mCursorXPos;
    public int mCursorYPos;
    public int mTopPos;

    public int getCnt() {
        return mData.size();
    }

    public void setLastPos(int top, int currentX, int currentY) {
        this.mTopPos = top;
        this.mCursorXPos = currentX;
        this.mCursorYPos = currentY;
    }
}

public class EPGProgramsGrid extends LinearLayout implements OnFocusChangeListener, OnClickListener {

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

    public interface OnItemClickListener {
        void onItemClick(EpgProgramItem item);
    }

    private static final String TAG = JLog.makeTag(EPGProgramsGrid.class);

    private EpgProgramData mData;
    private EpgRootView mRootView;
    private EpgDatePicker mDatePicker;
    private View mEmptyView;
    private EpgProgramViewFolderV[] mProgramFolder;

    LinkedList<View> mRemovedViews = new LinkedList<View>();

    private OnItemClickListener mOnItemClickLis;
    LayoutAnimationController mFadeInAnimation;
    LayoutAnimationController mMoveUpAnimation;

    LayoutAnimationController mMoveDownAnimaion;

    private boolean mKeyPressed = false;

    private long mKeyPressInternal = 180;

    private long mCurKeyPressInternal = 180;
    private long mLastKeyDownTime = -1;
    private long mKeyLongPressDownTime = -1;

    public EPGProgramsGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFadeInAnimation = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.category_expand_r2l));
        this.mMoveUpAnimation = new MoveUpAnim(AnimationUtils.loadAnimation(
                context, R.anim.move_up_2_self_1_0));
        this.mMoveDownAnimaion = new MoveDownAnim(AnimationUtils.loadAnimation(
                context, R.anim.move_down_2_self_1_0));
    }

    public void animateClickFeedback(View v, final Runnable r) {
        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater
                .loadAnimator(getContext(), R.anim.view_click_feedback);
        anim.setTarget(v);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                r.run();
            }
        });
        anim.start();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        JLog.d(TAG, "dispatchKeyEvent " + event.toString());
        boolean ret = false;
        ret = super.dispatchKeyEvent(event);
        if (!ret) {
            if (action == KeyEvent.ACTION_UP) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        mKeyPressed = false;
                        mLastKeyDownTime = -1;
                        mKeyLongPressDownTime = -1;
                        ret = true;
                        break;

                    default:
                        break;
                }
            } else if (action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        long current = SystemClock.uptimeMillis();
                        if (mKeyLongPressDownTime == -1) {
                            mKeyLongPressDownTime = current;
                        }
                        mCurKeyPressInternal = mKeyPressInternal - 10 * ((current - mKeyLongPressDownTime) / 400);
                        mCurKeyPressInternal = Math.max(mCurKeyPressInternal, 40);
                        JLog.d(TAG, "dispatchKeyEvent mCurKeyPressInternal = " + mCurKeyPressInternal);
                        if (current - mLastKeyDownTime < mCurKeyPressInternal) {
                            ret = true;
                            break;
                        }
                        View focus = getFocusedChild();
                        if (focus != null && focus instanceof
                                EpgProgramViewFolderV) {
                            focus = ((ViewGroup) focus).getFocusedChild();
                        }
                        if (focus != null && focus instanceof EpgProgramItem) {
                            EpgProgramItem item = (EpgProgramItem) focus;
                            int nextFocusId = -1;
                            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                nextFocusId = item.getNextFocusLeftId();
                            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                nextFocusId = item.getNextFocusRightId();
                            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                                nextFocusId = item.getNextFocusUpId();
                            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                                nextFocusId = item.getNextFocusDownId();
                            }
                            JLog.d(TAG, "find next focus view id = " + nextFocusId);
                            boolean handle = false;
                            if (nextFocusId != -1 && item.getId() != nextFocusId) {
                                View nextFView = findViewById(nextFocusId);
                                if (nextFView != null) {
                                    if (nextFView.requestFocus()) {
                                        handle = true;
                                    }
                                } else {
                                    nextFView = ((ViewGroup) getParent()).findViewById(nextFocusId);
                                    if (nextFView != null) {
                                        if (nextFView.requestFocus()) {
                                            handle = true;
                                        }
                                    }
                                }
                            }
                            if (handle) {
                                playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                            } else {
                                if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM) {
                                    playSoundEffect(5);
                                }
                            }
                        }
                        mKeyPressed = true;
                        mLastKeyDownTime = current;
                        ret = true;
                        break;
                    default:
                        break;
                }
            }
        }
        return ret;
    }

    public void movBottom() {
        final long begin = JLog.methodBegin(TAG);
        View top = getChildAt(0);
        Object localObject1 = top.getTag();
        int i = 0;
        if (localObject1 != null) {
            i = ((Integer) localObject1).intValue();
        }
        EpgProgramViewFolderV addToTop = (EpgProgramViewFolderV) this.mRemovedViews.poll();
        if (addToTop == null) {
            EpgProgramViewFolderV bottom = (EpgProgramViewFolderV) getChildAt(getChildCount() - 1);
            removeView(bottom);
            addToTop = bottom;
        }
        if (i > 0) {
            ((View) addToTop).setVisibility(View.VISIBLE);
        } else {
            ((View) addToTop).setVisibility(View.INVISIBLE);
        }
        ((View) addToTop).setTag(Integer.valueOf(i - 1));
        addToTop.updateWithData(mData, i - 1, false);
        addView(addToTop, 0);
        this.clearDisappearingChildren();

        rebuildFocus();
        setLayoutAnimation(this.mMoveDownAnimaion);
        JLog.methodEnd(TAG, begin);
    }

    public void movUp() {
        final long begin = JLog.methodBegin(TAG);
        EpgProgramViewFolderV upView = (EpgProgramViewFolderV) getChildAt(0);
        EpgProgramViewFolderV bottomView = (EpgProgramViewFolderV) getChildAt(getChildCount() - 1);
        removeView(upView);
        int i = ((Integer) bottomView.getTag()).intValue();
        if (i + 1 < this.mData.getCnt()) {
            upView.setTag(Integer.valueOf(i + 1));
            upView.updateWithData(mData, i + 1, false);
            upView.setVisibility(View.VISIBLE);
            addView(upView);
        } else {
            this.mRemovedViews.add(upView);
        }
        this.clearDisappearingChildren();
        rebuildFocus();
        setLayoutAnimation(this.mMoveUpAnimation);
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onClick(final View v) {
        animateClickFeedback(v, new Runnable() {
            @Override
            public void run() {
                if (mOnItemClickLis != null) {
                    if (v instanceof EpgProgramItem) {
                        mOnItemClickLis.onItemClick((EpgProgramItem) v);
                    }
                }
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        JLog.d(TAG, "onFinishInflate");
        super.onFinishInflate();
        mProgramFolder = new EpgProgramViewFolderV[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            mProgramFolder[i] = (EpgProgramViewFolderV) getChildAt(i);
            for (int j = 0; j < mProgramFolder[i].getChildCount(); j++) {
                mProgramFolder[i].getChildAt(j).setOnFocusChangeListener(this);
                mProgramFolder[i].getChildAt(j).setOnClickListener(this);
            }
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        EpgProgramItem focus = (EpgProgramItem) view;
        EpgProgramViewFolderV folder = (EpgProgramViewFolderV) view.getParent();
        if (hasFocus) {
            if (folder.getBottom() + 300 > getHeight()) {
                movUp();
                clearDisappearingChildren();
            }
            if (folder.getTop() - 300 <= 0) {
                movBottom();
                clearDisappearingChildren();
            }
            int currentX = ((ViewGroup) folder.getParent()).indexOfChild(folder);
            int currentY = 0;
            int topPos = 0;

            View topView = getChildAt(2);
            if (topView != null) {
                Integer tTopPos = (Integer) topView.getTag();
                if (topView.getVisibility() != View.INVISIBLE) {
                    topPos = tTopPos.intValue();
                }
            }
            View currentView = getFocusedChild();
            if (currentView != null) {
                currentY = ((Integer) currentView.getTag()).intValue();
            }
            JLog.d(TAG, "setLastPos topIdx = " + topPos + ", focusIdx = " + currentX + ", focusIdY = " + currentY);
            this.mData.setLastPos(topPos, currentX, currentY);
            if (mDatePicker != null) {
                mDatePicker.updateSelectionByProgramIndex(focus.mProgram);
            }
        } else {
        }
    }

    private void rebuildFocus() {
        final long begin = JLog.methodBegin(TAG);
        final int childCount = getChildCount();
        EpgProgramViewFolderV curFolder = null;
        EpgProgramViewFolderV curUpFolder = null;
        EpgProgramItem curView = null;
        EpgProgramItem curUpView = null;
        EpgProgramItem lastView = null;
        for (int rowIndex = 0; rowIndex < childCount; rowIndex++) {
            curFolder = (EpgProgramViewFolderV) getChildAt(rowIndex);
            for (int colIndex = 0; colIndex < EpgRootView.NUMCOLUMN; colIndex++) {
                if (colIndex < curFolder.mVisibleCount) {
                    curView = (EpgProgramItem) curFolder.getChildAt(colIndex);
                    curView.setNextFocusLeftId(mDatePicker.getId());
                    curView.setNextFocusUpId(curView.getId());
                    if (lastView != null && lastView.getVisibility() != View.INVISIBLE) {
                        lastView.setNextFocusRightId(curView.getId());
                        if (colIndex != 0) {
                            curView.setNextFocusLeftId(lastView.getId());
                        }
                    }
                }
                if (curUpFolder != null && curUpFolder.getVisibility() !=
                        View.INVISIBLE) {
                    curUpView = (EpgProgramItem) curUpFolder.getChildAt(colIndex < curUpFolder.mVisibleCount ? colIndex
                            : (curUpFolder.mVisibleCount - 1));
                    if (colIndex < curFolder.mVisibleCount) {
                        curView.setNextFocusUpId(curUpView.getId());
                    }
                    if (colIndex < curUpFolder.mVisibleCount) {
                        if (curView.getVisibility() != View.INVISIBLE) {
                            curUpView.setNextFocusDownId(curView.getId());
                        }
                    }
                }
                if (curView != null) {
                    lastView = curView;
                    curView.setNextFocusDownId(curView.getId());
                }
            }
            curUpFolder = curFolder;
        }
        JLog.methodEnd(TAG, begin);
    }

    public void requestFocusEx() {
        EpgProgramViewFolderV firstFolder = (EpgProgramViewFolderV) getChildAt(2);
        if (firstFolder != null) {
            firstFolder.getChildAt(0).requestFocus();
        } else {
            if (mEmptyView != null) {
                mEmptyView.requestFocus();
            }
        }
    }

    public void scrollToDay(int day) {
        mData.mTopPos = mData.mDayProgramBeginEndPos[day][0];
        mData.mCursorYPos = mData.mDayProgramBeginEndPos[day][0];
        mData.mCursorXPos = 0;
        setEpgProgramData(mData, false);
    }

    public void setDatePicker(EpgDatePicker picker) {
        mDatePicker = picker;
    }

    public void setEmptyView(View view) {
        mEmptyView = view;
    }

    public void setEpgProgramData(EpgProgramData data, boolean requestFocus) {
        final long begin = JLog.methodBegin(TAG);
        mData = data;
        int columnNum = mData.mData.size();
        if (columnNum == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            if (EpgRootView.mState == State.PROGRAMELIST) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mEmptyView.requestFocus();
                    }
                });
            }
        } else {
            mEmptyView.setVisibility(View.INVISIBLE);
        }

        int needView = columnNum + 2 - this.mData.mTopPos;
        JLog.d(TAG, "setEpgProgramData needView = " + needView + " currentview count = " + getChildCount());
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
                JLog.d(TAG, "setEpgProgramData removeView index = " + (childCount - 1 - r));
                removeView(localView);
            }
        }
        EpgProgramViewFolderV item;
        for (int index = 0; index < getChildCount(); index++) {
            item = (EpgProgramViewFolderV) getChildAt(index);
            int dataIndex = mData.mTopPos - 2 + index;
            item.setTag(dataIndex);
            item.updateWithData(mData, dataIndex, requestFocus);
            JLog.d(TAG, "setEpgProgramData item " + index + " dataIndex = " + dataIndex);
            if (dataIndex < 0) {
                item.setVisibility(View.INVISIBLE);
            } else {
                item.setVisibility(View.VISIBLE);
            }
        }
        rebuildFocus();
        JLog.methodEnd(TAG, begin);
    }

    public void setOnItemClickListener(OnItemClickListener lis) {
        this.mOnItemClickLis = lis;
    }

    public void setRootView(EpgRootView root) {
        mRootView = root;
    }

    public void showInAnimation(boolean anim) {
        this.setVisibility(View.VISIBLE);
        if (anim) {
            setLayoutAnimation(this.mFadeInAnimation);
            requestLayout();
        } else {
        }
    }
}
