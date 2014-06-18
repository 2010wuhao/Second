/**
 * =====================================================================
 *
 * @file  PlaybackController.java
 * @Module Name   com.joysee.dvb.controller
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Jan 9, 2014
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
 * yueliang         Jan 9, 2014            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.controller;

import android.content.Context;
import android.view.SurfaceHolder;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.JDVBStopTimeoutException;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.data.ChannelProvider;
import com.joysee.dvb.data.DvbSettings;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.player.AbsDvbPlayer.OnBeginWithChannelListeneer;
import com.joysee.dvb.player.DvbPlayerFactory;
import com.joysee.dvb.widget.DVBSurfaceViewParent;

import java.util.ArrayList;

public class PlaybackController extends BaseController {
    private static final String TAG = JLog.makeTag(PlaybackController.class);

    private Context mContext;
    public AbsDvbPlayer mDvbPlayer;

    public PlaybackController(Context context) {
        mContext = context;
        mDvbPlayer = DvbPlayerFactory.getPlayer(mContext);
        mDvbPlayer.setVideoAspectRationEnable(true);
        mDvbPlayer.setOnBeginWithChannelLis(new OnBeginWithChannelListeneer() {
            @Override
            public void onBeginWithChannel(DvbService channel) {
                checkDVBErrors();
            }
        });
    }

    public void addOnMonitorListener(JDVBPlayer.OnMonitorListener lis) {
        mDvbPlayer.addOnMonitorListener(lis);
    }

    public void checkDVBErrors() {
        int flag = DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CA | DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CHANNEL
                | DvbMessage.FLAG_REFRESH_DVB_NOTIFY_TUNERSIGNAL;
        checkDVBErrors(flag);
    }

    public void checkDVBErrors(int flag) {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.REFRESH_DVB_NOTIFY;
        msg.arg1 = flag;
        dispatchMessage(msg);
    }

    public void dismissChannelList() {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.DISMISS_CHANNELLIST;
        dispatchMessage(msg);
    }

    public void dismissLiveGuide() {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.DISMISS_LIVEGUIDE;
        dispatchMessage(msg);
    }

    protected void dispatchMessage(DvbMessage msg) {
        final long begin = JLog.methodBegin(TAG);
        JLog.d(TAG, "dispatchMessage msg = " + DvbMessage.getMessageName(msg));
        int count = mViews.size();
        for (int i = 0; i < count; i++) {
            mViews.get(i).processMessage(msg);
        }
        msg.recycle();
        JLog.methodEnd(TAG, begin);
    }

    public int get3DMode() {
        return mDvbPlayer.get3DMode();
    }

    public DvbService getChannelByNum(int num) {
        return mDvbPlayer.getChannelByNum(num);
    }

    public int getChannelCount() {
        return AbsDvbPlayer.getChannelCount();
    }

    public DvbService getCurrentChannel() {
        return mDvbPlayer.getCurrentChannel();
    }

    public DvbService getNextChannel() {
        return mDvbPlayer.getNextChannel();
    }

    public DvbService getPreviousChannel() {
        return mDvbPlayer.getPreviousChannel();
    }

    public int getVideoAspectRation() {
        return mDvbPlayer.getVideoAspectRation(JDVBPlayer.TUNER_0);
    }

    public int init(DVBSurfaceViewParent surfaceLayout) {
        return mDvbPlayer.init(surfaceLayout);
    }

    public void initChannel(boolean force) {
        AbsDvbPlayer.initChannel(force);
    }
    
    public boolean setKeepLastFrameEnable(int tunerId, boolean enable) {
        return mDvbPlayer.setKeepLastFrameEnable(tunerId, enable);
    }

    public void initSurface(SurfaceHolder.Callback callback) {
        mDvbPlayer.initSurface(callback);
    }

    public boolean is3DSupport() {
        return mDvbPlayer.is3DSupport();
    }
    
    public boolean isSoundTrackSupport() {
        return mDvbPlayer.isSoundTrackSupport();
    }

    public void play(int tunerId) {
        mDvbPlayer.prepare(tunerId);
    }

    public void playLastOrSpecial(Context c, int tunerId, int num) {
        ArrayList<DvbService> channels = ChannelProvider.getChannelHistory(c, 1);
        DvbService lastC = null;
        if (channels != null && channels.size() > 0 && num == -1) {
            lastC = channels.get(0);
        } else if (num != -1) {
            lastC = mDvbPlayer.getChannelByNum(num);
        } else {
            lastC = mDvbPlayer.getFirstChannel();
        }
        setChannel(tunerId, lastC);
    }
    
    public boolean isPlaying() {
        return mDvbPlayer.isPlaying();
    }

    public void removeOnMonitorListener(JDVBPlayer.OnMonitorListener lis) {
        mDvbPlayer.removeOnMonitorListener(lis);
    }

    public void set3DMode(int mode) {
        mDvbPlayer.set3DMode(mode);
    }

    public void setChannel(int tunerId, DvbService channel) {
        if (DvbService.isChannelValid(channel)) {
            JLog.d(TAG, "setChannel : " + channel.getChannelName() + "-" + channel.getChannelType());
            if (channel.getChannelType() == DvbService.BC) {
                dispatchMessage(DvbMessage.obtain(DvbMessage.START_PLAY_BC));
            } else {
                dispatchMessage(DvbMessage.obtain(DvbMessage.START_PLAY_TV));
            }
            showMiniEpg(channel);
            mDvbPlayer.setChannel(tunerId, channel);
        } else {
        }
    }

    public void setChannelByNum(int tunerId, int num) {
        DvbService channel = getChannelByNum(num);
        if (channel != null) {
            setChannel(tunerId, channel);
        }
    }
    
    public int getSoundTrack(int tunerId) {
        return mDvbPlayer.getSoundTrack(tunerId);
    }

    public void setSoundTrack(int soundTrack) {
        mDvbPlayer.setSoundTrack(JDVBPlayer.TUNER_0, null, soundTrack);
    }

    public void setVideoAspectRation(int mode) {
        mDvbPlayer.setVideoAspectRation(JDVBPlayer.TUNER_0, mode);
        DvbSettings.System.putInt(mContext.getContentResolver(), DvbSettings.System.VIDEO_ASPECTRATIO_TUNER_0, mode);
    }

    public void showChannelList() {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.SHOW_CHANNELLIST;
        dispatchMessage(msg);
    }

    public void showLiveGuide() {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.SHOW_LIVEGUIDE;
        dispatchMessage(msg);
    }

    public void showMiniEpg() {
        showMiniEpg(null);
    }

    public void showMiniEpg(DvbService channel) {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.SHOW_MINIEPG;
        if (channel != null) {
            msg.obj = channel;
        }
        dispatchMessage(msg);
    }

    public void showNoSpecialChannelError(int num) {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.ERROR_WITHOUT_CHANNEL;
        msg.arg1 = num;
        dispatchMessage(msg);
    }

    public void stop(int tunerId) {
        try {
            mDvbPlayer.stop(tunerId);
        } catch (JDVBStopTimeoutException e) {
            throw e;
        } finally {
            DvbMessage msg = DvbMessage.obtain();
            msg.what = DvbMessage.STOP_PLAY;
            dispatchMessage(msg);
        }
    }
    
    public void onPause() {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.ONPAUSE;
        dispatchMessage(msg);
    }
    
    public void onResume() {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.ONRESUME;
        dispatchMessage(msg);
    }
    
    public void switchToNextChannel(int tunerId) {
        DvbService next = getNextChannel();
        if (next != null) {
            setChannel(tunerId, next);
        }
    }

    public void switchToPreviousChannel(int tunerId) {
        DvbService previous = getPreviousChannel();
        if (previous != null) {
            setChannel(tunerId, previous);
        }
    }

    public void showOSD(String text, int location) {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.SHOW_OSD;
        msg.obj = text;
        msg.arg1 = location;
        dispatchMessage(msg);
    }
    
    public void dismissOSD(int location) {
        DvbMessage msg = DvbMessage.obtain();
        msg.what = DvbMessage.DISMISS_OSD;
        msg.arg1 = location;
        dispatchMessage(msg);
    }
    
    public int unInit() {
        return mDvbPlayer.unInit();
    }
}
