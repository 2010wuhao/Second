/**
 * =====================================================================
 *
 * @file  Cursor.java
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
 * YueLiang          2014年2月17日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import java.util.LinkedList;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.joysee.common.utils.JLog;
import com.joysee.tvbox.settings.R;

public class Cursor extends ImageView implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    static enum AlignType {
        ECenter("ECenter", 0), ETop("ETop", 1), EBottom("EBottom", 2), ELeft("ELeft", 3), ERight(
                "ERight", 4);
        private String text;

        private int value;

        AlignType(String text, int value) {
            this.text = text;
            this.value = value;
        }
    }

    private class AnimationRunner implements Runnable {
        @Override
        public void run() {
            Cursor.this.doAnimation();
        }
    }

    abstract interface CursorStateListener {
        public abstract void CursorMoving();

        public abstract void CursorStopped();
    }

    private class CursorTarget {
        Cursor.AlignType mAlignType;
        boolean mIgnoreTgtSize;
        View mTgtView;

        public CursorTarget(View target, boolean ignore, Cursor.AlignType align) {
            this.mTgtView = target;
            this.mIgnoreTgtSize = true;
            this.mAlignType = align;
        }
    }

    private class CusorStopNotifier implements Runnable {
        private CusorStopNotifier() {
        }

        @Override
        public void run() {
            Cursor.this.doNotifyStop();
        }
    }

    private class RectEvaluator implements TypeEvaluator<Object> {
        private Rect mRect = new Rect();

        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Rect start = (Rect) startValue;
            Rect end = (Rect) endValue;
            int x = end.right - start.right;
            int y = end.bottom - start.bottom;
            this.mRect.set(0, 0, start.right + (int) (fraction * x), start.bottom + (int) (fraction * y));
            return this.mRect;
        }
    }

    private static final String TAG = JLog.makeTag(Cursor.class);
    private static final int KMaxTargets2Trace = 2;
    private static final int KSpeed = 2;
    public static final int Left = 16;
    public static final int Top = 12;
    private AnimationRunner mAnimationRunner = new AnimationRunner();
    private CursorTarget mCurTgt;
    private ValueAnimator mScaleAnimator;
    private int[] mSelfPos = new int[2];
    private CursorStateListener mStateListener;
    private CusorStopNotifier mStopNotifier = new CusorStopNotifier();

    private int[] mTargetPos = new int[2];

    private Rect mTargetRect = new Rect();

    private LinkedList<CursorTarget> mTgtQue;

    private Rect mValidRect = new Rect(Left, Top, 340, 111);

    private ValueAnimator[] movAnimators = new ValueAnimator[2];

    private DecelerateInterpolator movInterpolator = new DecelerateInterpolator();

    public Cursor(Context context, AttributeSet attr) {
        super(context, attr);
        setScaleType(ImageView.ScaleType.FIT_XY);
        setBackgroundResource(R.drawable.main_settings_item_focus);
        this.mTgtQue = new LinkedList<CursorTarget>();

        ObjectAnimator translateXAnim = ObjectAnimator.ofFloat(this, "X", 0.0F);
        translateXAnim.setDuration(200L);
        // translateXAnim.addListener(this);
        ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(this, "Y", 0.0F);
        translateYAnim.setDuration(200L);
        translateYAnim.addListener(this);

        movAnimators[0] = translateXAnim;
        movAnimators[1] = translateYAnim;

        Drawable background = getBackground();
        RectEvaluator bgEvaluator = new RectEvaluator();
        Object[] bgBoundsRect = new Object[1];
        bgBoundsRect[0] = background.getBounds();
        mScaleAnimator = ObjectAnimator.ofObject(background, "Bounds", bgEvaluator, bgBoundsRect);
        mScaleAnimator.setDuration(400L);
        mScaleAnimator.addUpdateListener(this);
    }

    void doAnimation() {
        if (!this.mTgtQue.isEmpty()) {
            if (this.mTgtQue.size() <= KMaxTargets2Trace) {
                mov2Target(this.mTgtQue.pollFirst(), true);
            } else {
                set2Target(this.mTgtQue.pollLast());
            }
        }
    }

    void doNotifyStop() {
        if (this.mStateListener != null) {
            this.mStateListener.CursorStopped();
        }
    }

    private void mov2Target(CursorTarget target, boolean animator) {
        this.mCurTgt = target;
        target.mTgtView.getLocationOnScreen(this.mTargetPos);
        getLocationOnScreen(this.mSelfPos);
        Log.v(TAG, "mov2Target (" + this.mTargetPos[0] + ", " + this.mTargetPos[1] + ") from ("
                + this.mSelfPos[0] + ", " + this.mSelfPos[1] + ")aDoAnimation " + animator
                + " aIgnoreTgtSize " + target.mIgnoreTgtSize);
        Rect bgBoundsRect = getBackground().getBounds();
        long duration = 0L;
        int targetHeight = target.mTgtView.getHeight();
        int targetWidth = target.mTgtView.getWidth();
        if (animator) {
            int selfHeight = bgBoundsRect.height();
            int selfWidth = bgBoundsRect.width();
            int[] selfCenter = new int[2];
            selfCenter[0] = (this.mSelfPos[0] + selfWidth / 2);
            selfCenter[1] = (this.mSelfPos[1] + selfHeight / 2);
            int[] targetCenter = new int[2];
            targetCenter[0] = (this.mTargetPos[0] + targetWidth / 2);
            targetCenter[1] = (this.mTargetPos[1] + targetHeight / 2);

            int centerDist = (int) Math.sqrt((selfCenter[0] - targetCenter[0])
                    * (selfCenter[0] - targetCenter[0]) + (selfCenter[1] - targetCenter[1])
                    * (selfCenter[1] - targetCenter[1]));
            duration = Math.min(centerDist / KSpeed, 50);

            if (target.mAlignType == AlignType.EBottom) {
                this.mTargetPos[1] = (targetHeight + this.mTargetPos[1] - selfHeight + 2 * this.mValidRect.top);
            }
            Log.d(TAG, "selfCenter (" + selfCenter[0] + ", " + selfCenter[1] + ") targetCenter ("
                    + targetCenter[0] + ", " + targetCenter[1] + ") centerDist " + centerDist
                    + " duration " + duration);
            if (Math.abs(getX() - (this.mTargetPos[0] - this.mValidRect.left)) > 1.0F) {
                JLog.d(TAG, "x distance > 1 start animation");
                float[] xValues = new float[1];
                xValues[0] = (this.mTargetPos[0] - this.mValidRect.left);
                this.movAnimators[0].setFloatValues(xValues);
                this.movAnimators[0].setInterpolator(movInterpolator);
                this.movAnimators[0].setDuration(duration);
                this.movAnimators[0].start();
            }
            if (Math.abs(getY() - (this.mTargetPos[1] - this.mValidRect.top)) > 1.0F) {
                JLog.d(TAG, "y distance > 1 start animation");
                float[] yValues = new float[1];
                yValues[0] = (this.mTargetPos[1] - this.mValidRect.top);
                this.movAnimators[1].setFloatValues(yValues);
                this.movAnimators[1].setInterpolator(movInterpolator);
                this.movAnimators[1].setDuration(duration);
                this.movAnimators[1].start();
            }
        } else {
            setX(this.mTargetPos[0] - this.mValidRect.left);
            setY(this.mTargetPos[1] - this.mValidRect.top);
        }
        if (!target.mIgnoreTgtSize) {
            int targetWidthRadioed = targetWidth + 2 * this.mValidRect.left;
            int targetHeightRadioed = targetHeight + 2 * this.mValidRect.top;
            if ((targetWidthRadioed == bgBoundsRect.width())
                    && (targetHeightRadioed == bgBoundsRect.height())) {
                Log.v(TAG, "set2Target ignore the same size scale");
            } else {
                this.mTargetRect.set(0, 0, targetWidthRadioed, targetHeightRadioed);
                JLog.d(TAG, "mov2Target src.rect = " + bgBoundsRect + " target.rect = " + mTargetRect);
                if (animator) {
                    Object[] animationValues = new Object[2];
                    animationValues[0] = bgBoundsRect;
                    animationValues[1] = this.mTargetRect;
                    this.mScaleAnimator.setObjectValues(animationValues);
                    this.mScaleAnimator.setDuration(duration);
                    this.mScaleAnimator.start();
                } else {
                    getBackground().setBounds(this.mTargetRect);
                }
            }
        }
        invalidate();
    }

    public void move2Target(View dest) {
        JLog.d(TAG, "move2Target dest = " + dest);
        move2Target(dest, false, AlignType.ECenter);
    }

    public void move2Target(View dest, boolean ignore, AlignType align) {
        this.movAnimators[0].end();
        this.movAnimators[1].end();
        this.mScaleAnimator.end();
        this.mTgtQue.addLast(new CursorTarget(dest, ignore, align));
        if (this.mTgtQue.size() > 0) {
            Log.v(TAG, "Triger animation now");
            trigerMov();

            Log.v(TAG, "Animation on going just save to target Queue size:" + this.mTgtQue.size());
        }
    }

    public void onAnimationCancel(Animator paramAnimator) {
    }

    public void onAnimationEnd(Animator paramAnimator) {
        if (this.mStateListener != null) {
            Log.v(TAG, "post stop Notifier");
            post(this.mStopNotifier);
        }
    }

    public void onAnimationRepeat(Animator paramAnimator) {
    }

    public void onAnimationStart(Animator paramAnimator) {
        if (this.mStateListener != null) {
            this.mStateListener.CursorMoving();
        }
    }

    public void onAnimationUpdate(ValueAnimator paramValueAnimator) {
        getBackground().invalidateSelf();
    }

    public void set2CurrentTarget() {
        if (this.mCurTgt == null) {
            return;
        }
        mov2Target(this.mCurTgt, false);
    }

    private void set2Target(CursorTarget paramCursorTarget) {
        Log.d(TAG, "set2Target enter");
        this.mTgtQue.clear();
        this.movAnimators[0].end();
        this.movAnimators[1].end();
        this.mScaleAnimator.end();
        mov2Target(paramCursorTarget, false);
        doNotifyStop();
        Log.d(TAG, "set2Target exit");
    }

    public void set2Target(View paramView) {
        set2Target(new CursorTarget(paramView, false, null));
    }

    public void setStateListener(CursorStateListener lis) {
        removeCallbacks(this.mStopNotifier);
        this.mStateListener = lis;
    }

    private boolean trigerMov() {
        removeCallbacks(this.mAnimationRunner);
        post(this.mAnimationRunner);
        return this.mTgtQue.size() > 0;
    }
}
