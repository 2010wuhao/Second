/**
 * =====================================================================
 *
 * @file  AudioFocusClien.java
 * @Module Name   com.joysee.common.utils
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年10月29日
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
 * YueLiang         2013年10月29日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.common.utils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

public class JAudioFocusClien {
    private static final String TAG = JLog.makeTag(JAudioFocusClien.class);

    private Context mContext;
    private AudioManager mAudioManager;

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    private Intent mMusicServiceCommandIntent = new Intent(SERVICECMD);
    private OnAudioFocusChangeListener mAudioFocusChangeLis = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

        }
    };

    private boolean isHasAudioFocus = false;

    public JAudioFocusClien(Context context) {
        this.mContext = context;
    }

    public JAudioFocusClien(Context context, OnAudioFocusChangeListener lis) {
        this.mContext = context;
        this.mAudioFocusChangeLis = lis;
    }

    public boolean isHasAudioFocus() {
        return isHasAudioFocus;
    }

    /**
     * Request audio focus. Send a request to obtain the audio focus
     * 
     * @return {@link #AUDIOFOCUS_REQUEST_FAILED} or
     *         {@link #AUDIOFOCUS_REQUEST_GRANTED}
     */
    public int requestAudioFocus(boolean request) {
        final long begin = JLog.methodBegin(TAG);
        int result = -1;
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        if (request && !isHasAudioFocus) {
            result = mAudioManager.requestAudioFocus(mAudioFocusChangeLis,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            mMusicServiceCommandIntent.putExtra(CMDNAME, CMDPAUSE);
            this.mContext.sendBroadcast(mMusicServiceCommandIntent);
        } else if (!request && isHasAudioFocus) {
            result = mAudioManager.abandonAudioFocus(mAudioFocusChangeLis);
        }

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (request) {
                isHasAudioFocus = true;
                JLog.d(TAG, "request AudioFocus success!!");
            } else {
                isHasAudioFocus = false;
                JLog.d(TAG, "abandon AudioFocus success!!");
            }
        } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            if (request) {
                JLog.d(TAG, "request AudioFocus failed!!");
            } else {
                JLog.d(TAG, "abandon AudioFocus failed!!");
            }
        } else {
            JLog.d(TAG, "request = " + request + " current isHasAudioFocus = "
                    + isHasAudioFocus);
        }
        JLog.methodEnd(TAG, begin);
        return result;
    }
}
