/**
 * =====================================================================
 *
 * @file   DownloadProgressListener.java
 * @Module Name   com.joysee.tvbox.settings.upgrade
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  May 22, 2014
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
 * wumingjun         @May 22, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.upgrade;

public interface DownloadProgressListener {

    public final static int ERROR_CODE_UNKNOW = -1;

    public void onProgressChange(int progress);

    public void onStart();

    public void onFinish(boolean flag);

    public void onError(int code);
}
