/**
 * =====================================================================
 *
 * @file  LiveGuideProgramGrid.java
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
import android.content.Intent;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;
import com.joysee.dvb.activity.DvbPlaybackActivity;
import com.joysee.dvb.bean.Program;

import java.util.ArrayList;
import java.util.LinkedList;

class LiveGuideProgramData {
    public ArrayList<Program> mPrograms;
    public int mCursorXPos;
    public int mCursorYPos;
    public int mTopPos;

    public int getRowCnt() {
        return (mPrograms.size() + LiveGuideProgramGrid.NUMCOLUMN - 1) / LiveGuideProgramGrid.NUMCOLUMN;
    }
}

public class LiveGuideProgramGrid extends LinearLayout implements OnFocusChangeListener, OnClickListener {

    private static final String TAG = JLog.makeTag(LiveGuideProgramGrid.class);

    private LiveGuideProgramData mData;
    public static final int NUMCOLUMN = 4;
    public static final int NUMROW = 3;

    private LiveGuideProgramRow[] mChannelListRows;
    LinkedList<View> mRemovedViews = new LinkedList<View>();
    private long mKeyPressInternal = 300;
    private long mLastKeyDownTime = -1;

    LayoutAnimationController mFadeInAnimation;
    LayoutAnimationController mMoveUpAnimation;
    LayoutAnimationController mMoveDownAnimaion;

    public LiveGuideProgramGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        this.mFadeInAnimation = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.category_expand_r2l));
        this.mMoveUpAnimation = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_up_2_self_1_0), 0);
        this.mMoveDownAnimaion = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_down_2_self_1_0), 0);
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
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        mLastKeyDownTime = -1;
                        ret = true;
                        break;

                    default:
                        break;
                }
            } else if (action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        long current = SystemClock.uptimeMillis();
                        if (current - mLastKeyDownTime < mKeyPressInternal) {
                            ret = true;
                            break;
                        }
                        View focus = getFocusedChild();
                        if (focus != null && focus instanceof LiveGuideProgramRow) {
                            focus = ((ViewGroup) focus).getFocusedChild();
                        }
                        if (focus != null && focus instanceof LiveGuideProgramItem) {
                            LiveGuideProgramItem item = (LiveGuideProgramItem) focus;
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
                            if (nextFocusId != -1 && nextFocusId != item.getId()) {
                                View nextFView = findViewById(nextFocusId);
                                if (nextFView != null) {
                                    handle = true;
                                    nextFView.requestFocus();
                                } else {
                                    nextFView = ((ViewGroup) getParent()).findViewById(nextFocusId);
                                    if (nextFView != null) {
                                        handle = true;
                                        nextFView.requestFocus();
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
                        ret = true;
                        mLastKeyDownTime = current;
                        break;
                    default:
                        break;
                }
            }
        }
        return ret;
    }

    public void hide() {
        JLog.d(TAG, "hide", new RuntimeException());
        this.setVisibility(View.INVISIBLE);
    }

    public void movBottom() {
        final long begin = JLog.methodBegin(TAG);
        View top = getChildAt(0);
        Object localObject1 = top.getTag();
        int i = 0;
        if (localObject1 != null) {
            i = ((Integer) localObject1).intValue();
        }
        LiveGuideProgramRow addToTop = (LiveGuideProgramRow) this.mRemovedViews.poll();
        if (addToTop == null) {
            LiveGuideProgramRow bottom = (LiveGuideProgramRow) getChildAt(getChildCount() - 1);
            removeView(bottom);
            addToTop = bottom;
        }

        if (i > 0) {
            ((View) addToTop).setVisibility(View.VISIBLE);
        } else {
            ((View) addToTop).setVisibility(View.INVISIBLE);
        }
        ((View) addToTop).setTag(Integer.valueOf(i - 1));
        addToTop.updateWithData(mData, i - 1);
        addView(addToTop, 0);
        requestLayout();
        this.clearDisappearingChildren();
        setLayoutAnimation(this.mMoveDownAnimaion);
        rebuildFocus();
        JLog.methodEnd(TAG, begin);
    }

    public void movUp() {
        final long begin = JLog.methodBegin(TAG);
        LiveGuideProgramRow upView = (LiveGuideProgramRow) getChildAt(0);
        LiveGuideProgramRow bottomView = (LiveGuideProgramRow) getChildAt(getChildCount() - 1);
        removeView(upView);
        int topDataIndex = ((Integer) bottomView.getTag()).intValue();
        if (topDataIndex + 1 < this.mData.getRowCnt()) {
            upView.setTag(Integer.valueOf(topDataIndex + 1));
            upView.updateWithData(mData, topDataIndex + 1);
            upView.setVisibility(View.VISIBLE);
            addView(upView);
        } else {
            this.mRemovedViews.add(upView);
        }
        requestLayout();
        this.clearDisappearingChildren();
        setLayoutAnimation(this.mMoveUpAnimation);
        rebuildFocus();
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof LiveGuideProgramItem) {
            LiveGuideProgramItem item = (LiveGuideProgramItem) v;
            Intent intent = new Intent(getContext(), DvbPlaybackActivity.class);
            intent.putExtra(DvbPlaybackActivity.INTENT_EXTRA_DEST_CHANNEL_NUM, item.mProgram.logicNumber + "");
            getContext().startActivity(intent);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        JLog.d(TAG, "onFinishInflate");
        mChannelListRows = new LiveGuideProgramRow[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            mChannelListRows[i] = (LiveGuideProgramRow) getChildAt(i);
            for (int j = 0; j < mChannelListRows[i].getChildCount(); j++) {
                mChannelListRows[i].getChildAt(j).setOnFocusChangeListener(this);
                mChannelListRows[i].getChildAt(j).setOnClickListener(this);
            }
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        view = (View) view.getParent();
        if (hasFocus) {
            if (view.getBottom() + 300 > getHeight()) {
                movUp();
                clearDisappearingChildren();
            }
            if (view.getTop() - 300 <= 0) {
                movBottom();
                clearDisappearingChildren();
            }
        } else {
        }
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return false;
    }

    private void rebuildFocus() {
        final long begin = JLog.methodBegin(TAG);
        final int childCount = getChildCount();
        LiveGuideProgramRow curFolder = null;
        LiveGuideProgramRow curUpFolder = null;
        LiveGuideProgramItem curView = null;
        LiveGuideProgramItem curUpView = null;
        LiveGuideProgramItem lastView = null;
        for (int rowIndex = 0; rowIndex < childCount; rowIndex++) {
            curFolder = (LiveGuideProgramRow) getChildAt(rowIndex);
            for (int colIndex = 0; colIndex < NUMCOLUMN; colIndex++) {
                if (colIndex < curFolder.mVisibleCount) {
                    curView = (LiveGuideProgramItem) curFolder.getChildAt(colIndex);
                    // curView.setNextFocusLeftId(curView.getId());
                    curView.setNextFocusUpId(curView.getId());
                    if (lastView != null && lastView.getVisibility() != View.INVISIBLE) {
                        lastView.setNextFocusRightId(curView.getId());
                        if (colIndex != 0) {
                            curView.setNextFocusLeftId(lastView.getId());
                        }
                    }
                }
                if (curUpFolder != null && curUpFolder.getVisibility() != View.INVISIBLE) {
                    curUpView = (LiveGuideProgramItem) curUpFolder.getChildAt(colIndex <
                            curUpFolder.mVisibleCount ? colIndex : (curUpFolder.mVisibleCount - 1));
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

    public void setProgramData(LiveGuideProgramData data, boolean requestFocus) {
        final long begin = JLog.methodBegin(TAG);
        mData = data;
        int columnNum = mData.getRowCnt();

        int needView = columnNum + 2 - this.mData.mTopPos;
        JLog.d(TAG, "setProgramData needView = " + needView + " currentview count = " + getChildCount());
        if (needView > getChildCount()) {
            int maxView = Math.min(7, needView);
            while (getChildCount() < maxView) {
                JLog.d(TAG, "setProgramData add View");
                View view = this.mRemovedViews.poll();
                this.addView(view);
            }
        } else {
            int removeCount = getChildCount() - needView;
            int childCount = getChildCount();
            for (int r = 0; r < removeCount; r++) {
                View localView = getChildAt(childCount - 1 - r);
                this.mRemovedViews.add(localView);
                JLog.d(TAG, "setProgramData removeView index = " + (childCount - 1 - r));
                removeView(localView);
            }
        }
        LiveGuideProgramRow item;
        for (int index = 0; index < getChildCount(); index++) {
            item = (LiveGuideProgramRow) getChildAt(index);
            int dataIndex = mData.mTopPos - 2 + index;
            item.setTag(dataIndex);
            item.updateWithData(mData, dataIndex);
            JLog.d(TAG, "setProgramData item " + index + " dataIndex = " + dataIndex);
            if (dataIndex < 0) {
                item.setVisibility(View.INVISIBLE);
            } else {
                item.setVisibility(View.VISIBLE);
            }
        }
        rebuildFocus();
        JLog.methodEnd(TAG, begin);
    }

    public void show(ArrayList<Program> programs) {
        JLog.d(TAG, "show", new RuntimeException());
        this.setVisibility(View.VISIBLE);
        LiveGuideProgramData data = new LiveGuideProgramData();
        data.mPrograms = programs;
        setProgramData(data, false);
        showInAnimation(true);
    }

    private void showInAnimation(boolean anim) {
        if (anim) {
            setLayoutAnimation(this.mFadeInAnimation);
            requestLayout();
        } else {
        }
    }
}
