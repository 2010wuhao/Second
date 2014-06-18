/**
 * =====================================================================
 *
 * @file  EpgChannelList.java
 * @Module Name   com.joysee.dvb.epg
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月12日
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
 * yueliang         2014年2月12日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.widget.ListView;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;

public class EpgChannelList extends ListView {

    private static final String TAG = JLog.makeTag(EpgChannelsLayout.class);

    private EpgRootView mRootView;

    public EpgChannelList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean ret = false;
        boolean handle = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                event.startTracking();
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                handle = super.onKeyDown(keyCode, event);
                if (!handle) {
                    if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM) {
                        playSoundEffect(5);
                    }
                }
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                handle = super.onKeyDown(keyCode, event);
                if (!handle) {
                    if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM) {
                        playSoundEffect(5);
                    }
                }
                ret = true;
                break;
            default:
                break;
        }
        return ret ? true : super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean ret = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (event.isTracking()) {
                    mRootView.changeState(EpgRootView.State.PROGRAMELIST, true);
                    playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                }
                ret = true;
                break;
            default:
                break;
        }
        return ret ? true : super.onKeyUp(keyCode, event);
    }

    public void setRootView(EpgRootView root) {
        mRootView = root;
    }
}
