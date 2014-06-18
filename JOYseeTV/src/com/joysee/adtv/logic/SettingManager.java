/**
 * =====================================================================
 *
 * @file  SettingManager.java
 * @Module Name   com.joysee.adtv.logic
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2012年11月02日
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
 * wuhao           2012年11月02日                         1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic;

import android.graphics.Rect;

class SettingManager {
    private static final String TAG = SettingManager.class.getSimpleName();
    private static SettingManager mSettingsManager = new SettingManager();

    synchronized public static SettingManager getInstance() {
        return mSettingsManager;
    }

    private SettingManager() {
    }

    native int nativeClearLastFrame(int tunerId);

    native int nativeGetAudioLanguage(int tunerId);

    native int nativeGetAudioVolume(int tunerId);

    native int nativeGetSoundTrackMode(int tunerId);

    native String nativeGetTimeFromTs(int tunerId);

    native int nativeGetVideoAspectRatio(int tunerId);

    native int nativeGetVideoBrightness(int tunerId);

    native int nativeGetVideoContrast(int tunerId);

    native int nativeGetVideoSaturation(int tunerId);

    native int nativeGetVideoTvSystem(int tunerId);

    native int nativeGetVideoWindow(int tunerId, Rect rect);

    native int nativeIsShowVideo(int tunerId);

    native boolean nativeIsVideoFull(int tunerId);

    native int nativeSetAudioLanguage(int tunerId, int language);

    native int nativeSetAudioMute(int tunerId, boolean mute);

    native int nativeSetAudioVolume(int tunerId, int volume);

    native int nativeSetSoundTrackMode(int tunerId, int Mode);

    native int nativeSetVideoAspectRatio(int tunerId, int DisplayAspectRatio);

    native int nativeSetVideoBrightness(int tunerId, int bright);

    native int nativeSetVideoContrast(int tunerId, int contrast);

    native void nativeSetVideoFull(int tunerId);

    native int nativeSetVideoSaturation(int tunerId, int sturation);

    native int nativeSetVideoTvSystem(int tunerId, int tvsystem);

    native int nativeSetVideoWindow(int tunerId, int left, int top, int right, int bottom);

    native int nativeShowVideo(int tunerId, boolean show);

}
