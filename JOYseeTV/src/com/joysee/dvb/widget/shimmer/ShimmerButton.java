/**
 * =====================================================================
 *
 * @file  ShimmerButton.java
 * @Module Name   com.joysee.dvb.widget.shimmer
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-4-24
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
 * benz          2014-4-24           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.widget.shimmer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Button;

public class ShimmerButton extends Button implements ShimmerViewBase {

    private ShimmerViewHelper shimmerViewHelper;

    public ShimmerButton(Context context) {
        super(context);
        shimmerViewHelper = new ShimmerViewHelper(this, getPaint(), null);
        shimmerViewHelper.setPrimaryColor(getCurrentTextColor());
    }

    public ShimmerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        shimmerViewHelper = new ShimmerViewHelper(this, getPaint(), attrs);
        shimmerViewHelper.setPrimaryColor(getCurrentTextColor());
    }

    public ShimmerButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        shimmerViewHelper = new ShimmerViewHelper(this, getPaint(), attrs);
        shimmerViewHelper.setPrimaryColor(getCurrentTextColor());
    }

    @Override
    public float getGradientX() {
        return shimmerViewHelper.getGradientX();
    }

    @Override
    public void setGradientX(float gradientX) {
        shimmerViewHelper.setGradientX(gradientX);
    }

    @Override
    public boolean isShimmering() {
        return shimmerViewHelper.isShimmering();
    }

    @Override
    public void setShimmering(boolean isShimmering) {
        shimmerViewHelper.setShimmering(isShimmering);
    }

    @Override
    public boolean isSetUp() {
        return shimmerViewHelper.isSetUp();
    }

    @Override
    public void setAnimationSetupCallback(ShimmerViewHelper.AnimationSetupCallback callback) {
        shimmerViewHelper.setAnimationSetupCallback(callback);
    }

    @Override
    public int getPrimaryColor() {
        return shimmerViewHelper.getPrimaryColor();
    }

    @Override
    public void setPrimaryColor(int primaryColor) {
        shimmerViewHelper.setPrimaryColor(primaryColor);
    }

    @Override
    public int getReflectionColor() {
        return shimmerViewHelper.getReflectionColor();
    }

    @Override
    public void setReflectionColor(int reflectionColor) {
        shimmerViewHelper.setReflectionColor(reflectionColor);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (shimmerViewHelper != null) {
            shimmerViewHelper.setPrimaryColor(getCurrentTextColor());
        }
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        super.setTextColor(colors);
        if (shimmerViewHelper != null) {
            shimmerViewHelper.setPrimaryColor(getCurrentTextColor());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (shimmerViewHelper != null) {
            shimmerViewHelper.onSizeChanged();
        }
    }

    @SuppressLint("WrongCall")
    @Override
    public void onDraw(Canvas canvas) {
        if (shimmerViewHelper != null) {
            shimmerViewHelper.onDraw();
        }
        super.onDraw(canvas);
    }
}
