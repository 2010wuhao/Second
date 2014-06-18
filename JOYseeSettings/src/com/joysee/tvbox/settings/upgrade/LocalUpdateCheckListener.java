/**
 * =====================================================================
 *
 * @file   LocalUpdateCheckListener.java
 * @Module Name   com.joysee.tvbox.settings.upgrade
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  May 26, 2014
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
 * wumingjun         @May 26, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.upgrade;

public interface LocalUpdateCheckListener {

    public final static int VERSION_NEW = 1;
    public final static int VERSION_LATEST = 2;

    public void onCheckFinish(int result);

    public void onError();
}
