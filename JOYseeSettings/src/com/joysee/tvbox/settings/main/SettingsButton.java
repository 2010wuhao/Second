/**
 * =====================================================================
 *
 * @file   SettingsButton.java
 * @Module Name   com.joysee.tvbox.settings.main
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 15, 2014
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
 * wumingjun         @Apr 15, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.main;

import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsButton extends LinearLayout {

    private static final String TAG = JLog.makeTag(SettingsButton.class);
    /**
     * Screen Center
     */
    private int mScreenCenterX;
    private int mScreenCenterY;
    /**
     * View Center
     */
    private int mCenterX;
    private int mCenterY;
    /**
     * Center Distance between screen and view
     */
    private int mDistanceX;
    private int mDistanceY;
    /**
     * Translate Distance Fraction
     */
    private float mTranslateX;
    private float mTranslateY;
    /**
     * Animation
     */
    private AnimationSet mAnimationSet;
    private TranslateAnimation mTransAnimation;
    private AlphaAnimation mAlphaAnimation;
    private int mDuration;
    private boolean mAdded;
    /**
     * Location in the screen
     */
    private int[] mLocation = new int[2];
    /**
     * Callback when the animation end
     */
    private AnimationEndListener mAnimEndListener;
    /**
     * Button icon and text
     */
    private ImageView mIcon;
    private JTextViewWithTTF mText;
    private TypedArray mTypedArray;

    public interface AnimationEndListener {
        public void onAnimationEnd();
    }

    public SettingsButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(attrs);
    }

    public SettingsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public SettingsButton(Context context) {
        super(context);
        initView(null);
    }

    private void initView(AttributeSet attrs) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay()
                .getMetrics(metric);
        int width = metric.widthPixels;
        int height = metric.heightPixels;
        mScreenCenterX = width / 2;
        mScreenCenterY = height / 2;
        // Set animation
        mTranslateX = getResources().getInteger(
                R.integer.settings_button_translate_x);
        mTranslateY = getResources().getInteger(
                R.integer.settings_button_translate_y);
        mDuration = getResources().getInteger(
                R.integer.settings_button_anim_duration);
        mAnimationSet = new AnimationSet(true);
        mAnimationSet.setDuration(mDuration);
        mAnimationSet.setInterpolator(new DecelerateInterpolator());
        mAnimationSet.setFillAfter(true);
        mAnimationSet.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mAnimEndListener != null) {
                    mAnimEndListener.onAnimationEnd();
                }
            }
        });
        // Setup image icon
        if (attrs != null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.view_settings_button, null);
            this.addView(view);

            mTypedArray = getContext().obtainStyledAttributes(attrs,
                    R.styleable.settings);
            mIcon = (ImageView) findViewById(R.id.img_settings_icon);
            mText = (JTextViewWithTTF) findViewById(R.id.txt_settings_button);
            int icon = mTypedArray.getResourceId(R.styleable.settings_icon, 0);
            int text = mTypedArray.getResourceId(R.styleable.settings_text, 0);
            if (icon != 0) {
                mIcon.setImageResource(icon);
            }
            if (text != 0) {
                mText.setText(text);
            } else {
                String str = mTypedArray.getString(R.styleable.settings_text);
                if (str != null && !str.equals("")) {
                    mText.setText(str);
                }
            }

            mTypedArray.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getLocationOnScreen(mLocation);
        int xInScreen = mLocation[0];
        int yInScreen = mLocation[1];
        mCenterX = xInScreen + (right - left) / 2;
        mCenterY = yInScreen + (bottom - top) / 2;
        mDistanceX = mScreenCenterX - mCenterX;
        mDistanceY = mScreenCenterY - mCenterY;

        if (!mAdded) {
            if (mDistanceX != 0 && mDistanceY != 0) {

                float x = mDistanceX > 0 ? -1 : 1;
                float y = mDistanceY > 0 ? -1 : 1;
                mTransAnimation = new TranslateAnimation(mTranslateX * x, 0,
                        mTranslateY * y, 0);
                mTransAnimation.setDuration(mDuration);
                mAnimationSet.addAnimation(mTransAnimation);

            } else if (mDistanceY != 0) {
                float y = mDistanceY > 0 ? -1 : 1;
                mTransAnimation = new TranslateAnimation(0, 0, mTranslateY * y,
                        0);
                mTransAnimation.setDuration(mDuration);
                mAnimationSet.addAnimation(mTransAnimation);

            }
            mAlphaAnimation = new AlphaAnimation(0f, 1.0f);
            mAnimationSet.addAnimation(mAlphaAnimation);
            setAnimation(mAnimationSet);
            postDelayed(new Runnable() {

                @Override
                public void run() {
                    mAnimationSet.start();
                }
            }, 200);
            mAdded = true;

        }

    }

    @Override
    protected void onFinishInflate() {

        this.setFocusable(true);
        super.onFinishInflate();

    }

    public void setOnAnimEndListener(AnimationEndListener listener) {
        this.mAnimEndListener = listener;
    }

}
