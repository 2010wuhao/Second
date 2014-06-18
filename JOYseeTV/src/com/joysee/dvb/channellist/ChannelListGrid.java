/**
 * =====================================================================
 *
 * @file  ChannelListGrid.java
 * @Module Name   com.joysee.dvb.channellist
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月15日
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
 * yueliang         2014年2月15日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.channellist;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JStyleDialog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;
import com.joysee.dvb.activity.DvbPlaybackActivity;
import com.joysee.dvb.activity.EPGActivity;
import com.joysee.dvb.widget.StyleDialog;

import java.util.ArrayList;
import java.util.LinkedList;

class ChannelListData {
    public ArrayList<DvbService> mChannel;
    public int mCursorXPos;
    public int mCursorYPos;
    public int mTopPos;

    public int getRowCnt() {
        return mChannel != null ? (mChannel.size() + ChannelListGrid.NUMCOLUMN - 1) / ChannelListGrid.NUMCOLUMN : 0;
    }
}

public class ChannelListGrid extends LinearLayout implements View.OnFocusChangeListener, View.OnClickListener {

    private static final String TAG = JLog.makeTag(ChannelListGrid.class);

    private ChannelListData mData;

    public static final int NUMCOLUMN = 5;
    public static final int NUMROW = 4;
    private ChannelListGridRow[] mChannelListRows;
    LinkedList<View> mRemovedViews = new LinkedList<View>();
    private long mKeyPressInternal = 300;
    private long mLastKeyDownTime = -1;

    LayoutAnimationController mFadeInAnimation;
    LayoutAnimationController mMoveUpAnimation;
    LayoutAnimationController mMoveDownAnimaion;

    private JStyleDialog mSelectDialog;

    public ChannelListGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        this.mFadeInAnimation = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.category_expand_r2l));
        this.mMoveUpAnimation = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_up_2_self_1_0), 0);
        this.mMoveDownAnimaion = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_down_2_self_1_0), 0);
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

    public void clear() {
        if (mSelectDialog != null && mSelectDialog.isShowing()) {
            mSelectDialog.dismiss();
        }
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
                        mLastKeyDownTime = -1;
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
                        if (current - mLastKeyDownTime < mKeyPressInternal) {
                            ret = true;
                            break;
                        }
                        View focus = getFocusedChild();
                        if (focus != null && focus instanceof ChannelListGridRow) {
                            focus = ((ViewGroup) focus).getFocusedChild();
                        }
                        if (focus != null && focus instanceof ChannelItem) {
                            ChannelItem item = (ChannelItem) focus;
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

    public void movBottom() {
        final long begin = JLog.methodBegin(TAG);
        View top = getChildAt(0);
        Object localObject1 = top.getTag();
        int i = 0;
        if (localObject1 != null) {
            i = ((Integer) localObject1).intValue();
        }
        ChannelListGridRow addToTop = (ChannelListGridRow) this.mRemovedViews.poll();
        if (addToTop == null) {
            ChannelListGridRow bottom = (ChannelListGridRow) getChildAt(getChildCount() - 1);
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
        requestLayout();
        this.clearDisappearingChildren();
        setLayoutAnimation(this.mMoveDownAnimaion);
        rebuildFocus();
        JLog.methodEnd(TAG, begin);
    }

    public void movUp() {
        final long begin = JLog.methodBegin(TAG);
        ChannelListGridRow upView = (ChannelListGridRow) getChildAt(0);
        ChannelListGridRow bottomView = (ChannelListGridRow) getChildAt(getChildCount() - 1);
        removeView(upView);
        int topDataIndex = ((Integer) bottomView.getTag()).intValue();
        if (topDataIndex + 1 < this.mData.getRowCnt()) {
            upView.setTag(Integer.valueOf(topDataIndex + 1));
            upView.updateWithData(mData, topDataIndex + 1, false);
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
        if (v instanceof ChannelItem) {
            final ChannelItem item = (ChannelItem) v;
            final String positive = getResources().getString(R.string.channellist_dialog_negative_watching);
            final String negative = getResources().getString(R.string.title_epg);
            final String point = getResources().getString(R.string.channellist_dialog_hint);
            animateClickFeedback(v, new Runnable() {

                @Override
                public void run() {
                    StyleDialog.Builder builder = new StyleDialog.Builder(getContext());
                    builder.setDefaultContentMessage(item.getChannel().getChannelName());
                    builder.setDefaultContentPoint(point);
                    builder.setPositiveButton(positive);
                    builder.setNegativeButton(negative);
                    builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                mSelectDialog.dismiss();
                                Intent intent = new Intent(getContext(), DvbPlaybackActivity.class);
                                intent.putExtra(DvbPlaybackActivity.INTENT_EXTRA_DEST_CHANNEL_NUM,
                                        item.getChannel().getLogicChNumber() + "");
                                getContext().startActivity(intent);
                            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                                mSelectDialog.dismiss();
                                Intent intent = new Intent(getContext(), EPGActivity.class);
                                intent.putExtra(EPGActivity.INTENT_EXTRA_DEST_CHANNEL_NUM, item.getChannel().getLogicChNumber());
                                getContext().startActivity(intent);
                            }
                        }
                    });
                    mSelectDialog = builder.show();
                    /*
                     * mSelectDialog = CustomDialogUtil.get2BtDialog((Activity)
                     * getContext(), null, item.getChannel().getChannelName(),
                     * point, negative, positive, new
                     * DialogInterface.OnClickListener() {
                     * @Override public void onClick(DialogInterface dialog, int
                     * which) { if (which == DialogInterface.BUTTON_POSITIVE) {
                     * mSelectDialog.dismiss(); Intent intent = new
                     * Intent(getContext(), DvbPlaybackActivity.class);
                     * intent.putExtra
                     * (DvbPlaybackActivity.INTENT_EXTRA_DEST_CHANNEL_NUM,
                     * item.getChannel().getLogicChNumber());
                     * getContext().startActivity(intent); } else if (which ==
                     * DialogInterface.BUTTON_NEGATIVE) {
                     * mSelectDialog.dismiss(); Intent intent = new
                     * Intent(getContext(), EPGActivity.class);
                     * intent.putExtra(EPGActivity
                     * .INTENT_EXTRA_DEST_CHANNEL_NUM,
                     * item.getChannel().getLogicChNumber());
                     * getContext().startActivity(intent); } } });
                     * mSelectDialog.show();
                     */

                }
            });
        }
    }

    @Override
    protected void onFinishInflate() {
        JLog.d(TAG, "onFinishInflate");
        super.onFinishInflate();
        mChannelListRows = new ChannelListGridRow[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            mChannelListRows[i] = (ChannelListGridRow) getChildAt(i);
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
            if (view.getBottom() + 100 > getHeight()) {
                movUp();
                clearDisappearingChildren();
            }
            if (view.getTop() - 100 <= 0) {
                movBottom();
                clearDisappearingChildren();
            }
        } else {
        }
    }

    private void rebuildFocus() {
        final long begin = JLog.methodBegin(TAG);
        final int childCount = getChildCount();
        ChannelListGridRow curFolder = null;
        ChannelListGridRow curUpFolder = null;
        ChannelItem curView = null;
        ChannelItem curUpView = null;
        ChannelItem lastView = null;
        for (int rowIndex = 0; rowIndex < childCount; rowIndex++) {
            curFolder = (ChannelListGridRow) getChildAt(rowIndex);
            for (int colIndex = 0; colIndex < NUMCOLUMN; colIndex++) {
                if (colIndex < curFolder.mVisibleCount) {
                    curView = (ChannelItem) curFolder.getChildAt(colIndex);
                    curView.setNextFocusLeftId(curView.getId());
                    curView.setNextFocusUpId(curView.getId());
                    if (lastView != null && lastView.getVisibility() != View.INVISIBLE) {
                        lastView.setNextFocusRightId(curView.getId());
                        if (colIndex != 0 || curUpFolder != null && curUpFolder.getVisibility() != View.INVISIBLE) {
                            curView.setNextFocusLeftId(lastView.getId());
                        }
                    }
                }
                if (curUpFolder != null && curUpFolder.getVisibility() != View.INVISIBLE) {
                    curUpView = (ChannelItem) curUpFolder.getChildAt(colIndex <
                            curUpFolder.mVisibleCount ?
                                    colIndex : (curUpFolder.mVisibleCount - 1));
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

    public void setChannelListData(ChannelListData data, boolean requestFocus) {
        final long begin = JLog.methodBegin(TAG);
        mData = data;
        int columnNum = mData.getRowCnt();

        int needView = columnNum + 1 - this.mData.mTopPos;
        JLog.d(TAG, "setChannelListData needView = " + needView + " currentview count = "
                + getChildCount());
        if (needView > getChildCount()) {
            int maxView = Math.min(6, needView);
            while (getChildCount() < maxView) {
                JLog.d(TAG, "setChannelListData add View");
                View view = this.mRemovedViews.poll();
                this.addView(view);
            }
        } else {
            int removeCount = getChildCount() - needView;
            int childCount = getChildCount();
            for (int r = 0; r < removeCount; r++) {
                View localView = getChildAt(childCount - 1 - r);
                this.mRemovedViews.add(localView);
                JLog.d(TAG, "setChannelListData removeView index = " + (childCount - 1 - r));
                removeView(localView);
            }
        }
        ChannelListGridRow item;
        for (int index = 0; index < getChildCount(); index++) {
            item = (ChannelListGridRow) getChildAt(index);
            int dataIndex = mData.mTopPos - 1 + index;
            item.setTag(dataIndex);
            item.updateWithData(mData, dataIndex, true);
            JLog.d(TAG, "setChannelListData item " + index + " dataIndex = " + dataIndex);
            if (dataIndex < 0) {
                item.setVisibility(View.INVISIBLE);
            } else {
                item.setVisibility(View.VISIBLE);
            }
        }
        rebuildFocus();
        JLog.methodEnd(TAG, begin);
    }

    public void show(ArrayList<DvbService> channels) {
        ChannelListData data = new ChannelListData();
        data.mChannel = channels;
        setChannelListData(data, true);
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
