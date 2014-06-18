/**
 * =====================================================================
 *
 * @file  TimeShiftControl.java
 * @Module Name   com.joysee.dvb.timeshift
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-15
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
 * benz          2014-1-15           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.vod;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;

public class VodPlayControlbar extends RelativeLayout {

    private static final String TAG = "smmm";
    private VodPlayControl mControl;
    private ProgressBar mProgressBar;
    private RelativeLayout mBubbleCursor;
    private TextView mBubbleCursorTimeTv;
    private TextView mCurrentDurationTv;
    private TextView mTotalDurationTv;
    private RelativeLayout mSeekBarRoot;
    private RelativeLayout mTitleLayout;
    private TextView mTitleTv;
    private TextView mSourceClearLevel;

    private Animator mSeekBarIn;
    private Animator mSeekBarOut;
    private Animator mTitleIn;
    private Animator mTitleOut;

    private int mAnimationDuration = 200;
    private int mDelayToSeekTo = 1000;
    private int mProgressbarWidth;

    private boolean isShowing = false;
    private int mDuration;
    private int mCurrentPosition;
    private int mBubblePosition;

    public VodPlayControlbar(Activity activity) {
        super(activity);
    }

    public VodPlayControlbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VodPlayControlbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mProgressbarWidth = (int) getResources().getDimension(R.dimen.vod_seekbar_progressbar_w);

        mSeekBarRoot = (RelativeLayout) findViewById(R.id.seekbar);
        mProgressBar = (ProgressBar) mSeekBarRoot.findViewById(R.id.progressbar);
        mBubbleCursor = (RelativeLayout) mSeekBarRoot.findViewById(R.id.bubble_cursor);
        mBubbleCursorTimeTv = (TextView) mSeekBarRoot.findViewById(R.id.bubble_cursor_time);
        mCurrentDurationTv = (TextView) mSeekBarRoot.findViewById(R.id.current_duration);
        mTotalDurationTv = (TextView) mSeekBarRoot.findViewById(R.id.total_duration);
        mTitleTv = (TextView) findViewById(R.id.media_name);
        mSourceClearLevel = (TextView) findViewById(R.id.clear_level);
        mTitleLayout = (RelativeLayout) findViewById(R.id.title_layout);

        int titleFromY = -98;
        int titleToY = 32;
        mTitleIn = buildVerticalAnimator(mTitleLayout, titleFromY, titleToY, mAnimationDuration);
        mTitleOut = buildVerticalAnimator(mTitleLayout, titleToY, titleFromY, mAnimationDuration);
        mTitleIn.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mTitleLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        mTitleOut.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mTitleLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

        int seekBarFromY = (int) getResources().getDimension(R.dimen.screen_height);
        int seekBarToY = seekBarFromY - (int) getResources().getDimension(R.dimen.vod_seekbar_h);
        mSeekBarIn = buildVerticalAnimator(mSeekBarRoot, seekBarFromY, seekBarToY, mAnimationDuration);
        mSeekBarOut = buildVerticalAnimator(mSeekBarRoot, seekBarToY, seekBarFromY, mAnimationDuration);
        mSeekBarIn.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mSeekBarRoot.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mControl != null) {
                    mControl.onShow();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

        mSeekBarOut.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (mControl != null) {
                    mControl.onHide();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSeekBarRoot.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        setFocusable(false);
    }

    public void registerTimeShiftControl(VodPlayControl c) {
        mControl = c;
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

    public void setClearLevel(String level) {
        mSourceClearLevel.setText(level);
    }

    public void show() {
        if (!isShowing) {
            isShowing = true;
            int[] initInfo = mControl.syncSeekBar();
            updateProgress(initInfo[0], initInfo[1]);
            mSeekBarIn.start();
            mTitleIn.start();
        }
        updatePausePlay();
    }

    public void hide() {
        mDuration = 0;
        mCurrentPosition = 0;
        mBubblePosition = 0;
        if (isShowing) {
            isShowing = false;
            mSeekBarOut.start();
            mTitleOut.start();
        }
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public int getProgress() {
        return mProgressBar.getProgress();
    }

    public boolean isShowing() {
        return isShowing && !mSeekBarIn.isRunning() && !mSeekBarOut.isRunning();
    }

    private void updatePausePlay() {

    }

    /**
     * 移动气泡游标
     */
    public void seekBubbleCursor(int pos) {
        removeCallbacks(mDelaySeekTo);

        mBubblePosition = pos;
        mBubbleCursor.setVisibility(View.VISIBLE);
        mBubbleCursorTimeTv.setText(getTimeString(mBubblePosition));
        int bubbleCursorPro = mBubblePosition * 100 / mDuration;
        mProgressBar.setSecondaryProgress(bubbleCursorPro);

        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mBubbleCursor.getLayoutParams();
        if (layoutParams != null) {
            int offset = (bubbleCursorPro) * mProgressbarWidth / 100;
            layoutParams.leftMargin = offset;
            mBubbleCursor.setLayoutParams(layoutParams);
            JLog.d(TAG, "move bubble cursor to " + offset);
        }

        postDelayed(mDelaySeekTo, mDelayToSeekTo);
    }

    public void updateProgress(int pos) {
        JLog.d(TAG, "updateProgress  mDuration=" + mDuration + " currentPos=" + pos);
        mCurrentPosition = pos;
        mCurrentDurationTv.setText(getTimeString(mCurrentPosition));
        int pro = mCurrentPosition * 100 / mDuration;
        mProgressBar.setProgress(pro);

        if (mBubbleCursor.getVisibility() != View.VISIBLE) {
            mBubbleCursorTimeTv.setText(getTimeString(mCurrentPosition));
            RelativeLayout.LayoutParams layoutParams = (LayoutParams) mBubbleCursor.getLayoutParams();
            if (layoutParams != null) {
                int offset = (pro) * mProgressbarWidth / 100;
                layoutParams.leftMargin = offset;
                mBubbleCursor.setLayoutParams(layoutParams);
            }
        }
    }

    private void updateProgress(int pro, int duration) {
        mDuration = duration;
        mBubblePosition = pro;
        mTotalDurationTv.setText(getTimeString(mDuration));
        updateProgress(pro);
    }

    private String getTimeString(int ms) {
        int left = ms;
        int hour = left / 3600000;
        left %= 3600000;
        int min = left / 60000;
        left %= 60000;
        int sec = left / 1000;
        return String.format("%1$02d:%2$02d:%3$02d", hour, min, sec);
    }

    private Runnable mDelaySeekTo = new Runnable() {
        @Override
        public void run() {
            JLog.d(TAG, "mDelaySeekTo   bubble postion=" + mBubblePosition + "  pro=" + mCurrentPosition);
            mProgressBar.setProgress(mBubblePosition * 100 / mDuration);
            mBubbleCursor.setVisibility(View.INVISIBLE);
            mCurrentDurationTv.setText(getTimeString(mBubblePosition));
            mControl.onCursorSeekComplete(mBubblePosition);
            mCurrentPosition = mBubblePosition;
        }
    };

    public int getBubbleCursorPostion() {
        return mBubblePosition;
    }

    private AnimatorSet buildVerticalAnimator(View target, int fromY, int toY, int duration) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator yAnim = ObjectAnimator.ofFloat(target, "y", fromY, toY).setDuration(duration);
        set.play(yAnim);
        set.setInterpolator(new DecelerateInterpolator());
        return set;
    }

    public interface VodPlayControl {

        void onHide();

        void onShow();

        /**
         * 预览气泡游标移动完成
         * 
         * @param pos
         */
        void onCursorSeekComplete(int pos);

        boolean isPlaying();

        int[] syncSeekBar();
    }
}
