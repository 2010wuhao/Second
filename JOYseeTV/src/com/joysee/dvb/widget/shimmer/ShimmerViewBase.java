/**
 * =====================================================================
 *
 * @file  ShimmerViewBase.java
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

public interface ShimmerViewBase {

    public float getGradientX();

    public void setGradientX(float gradientX);

    public boolean isShimmering();

    public void setShimmering(boolean isShimmering);

    public boolean isSetUp();

    public void setAnimationSetupCallback(ShimmerViewHelper.AnimationSetupCallback callback);

    public int getPrimaryColor();

    public void setPrimaryColor(int primaryColor);

    public int getReflectionColor();

    public void setReflectionColor(int reflectionColor);
}
