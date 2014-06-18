/**
 * =====================================================================
 *
 * @file   SlipButton.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  May 27, 2014
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
 * wumingjun         @May 27, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.joysee.tvbox.settings.R;

public class SlipButton extends LinearLayout implements OnKeyListener {

    private ImageView mBackground;
    private View mButton;
    private OnCheckChangeListener mListener;

    private boolean isChecked;
    private boolean isFirstShow = true;

    private TranslateAnimation mTranslateLeftAnimation;
    private TranslateAnimation mTranslateRightAnimation;

    public SlipButton(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        initView();
    }

    public SlipButton(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        initView();
    }

    public SlipButton(Context arg0) {
        super(arg0);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_animation_slip_button, null);
        mBackground = (ImageView) view.findViewById(R.id.image_view_slip_background);
        mButton = view.findViewById(R.id.view_slip_button);
        this.addView(view);
        this.setOnKeyListener(this);
    }

    public void setChecked(boolean check) {

        if (isChecked != check) {

            isChecked = check;
            int backgroundWidth = mBackground.getWidth();
            int buttonWidth = mButton.getWidth();
            int distance = backgroundWidth - buttonWidth;
            int duration = 150;
            if (isFirstShow) {
                duration = 0;
                isFirstShow = false;
            }

            if (distance != 0) {
                mTranslateLeftAnimation = new TranslateAnimation(distance, 0, 0, 0);
                mTranslateLeftAnimation.setDuration(duration);
                mTranslateLeftAnimation.setFillAfter(true);
                mTranslateLeftAnimation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation arg0) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {

                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        setBackground(SlipButton.this.isFocused());
                    }
                });

                mTranslateRightAnimation = new TranslateAnimation(0, distance, 0, 0);
                mTranslateRightAnimation.setDuration(duration);
                mTranslateRightAnimation.setFillAfter(true);
                mTranslateRightAnimation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation arg0) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {

                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        setBackground(SlipButton.this.isFocused());
                    }
                });
            }

            if (check) {
                if (mTranslateRightAnimation != null) {
                    mButton.startAnimation(mTranslateRightAnimation);
                }
            } else {
                if (mTranslateLeftAnimation != null) {
                    mButton.startAnimation(mTranslateLeftAnimation);
                }
            }

            if (mListener != null) {
                mListener.onCheckChange(isChecked);
            }

        }

    }

    public void setOnCheckChangeListener(OnCheckChangeListener listener) {
        mListener = listener;
    }

    public interface OnCheckChangeListener {
        public void onCheckChange(boolean check);
    }

    @Override
    protected void onFocusChanged(boolean focused, int arg1, Rect arg2) {
        super.onFocusChanged(focused, arg1, arg2);
        setBackground(focused);
    }

    private void setBackground(boolean focused) {
        if (isChecked) {
            mBackground.setBackgroundResource(focused ? R.drawable.switch_on_focus : R.drawable.switch_on);
        } else {
            mBackground.setBackgroundResource(focused ? R.drawable.switch_off_focus : R.drawable.switch_off);
        }
    }

    @Override
    public boolean onKey(View v, int code, KeyEvent event) {
        if (event != null && event.getAction() == KeyEvent.ACTION_UP) {

            if (code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_DPAD_CENTER) {
                setChecked(!isChecked);
            }
        }
        return false;
    }

}