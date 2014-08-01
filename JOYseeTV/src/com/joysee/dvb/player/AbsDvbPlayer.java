/**
 * =====================================================================
 *
 * @file  AbsDvbPlayer.java
 * @Module Name   com.joysee.dvb.player
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月24日
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
 * yueliang         2014年1月24日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.player;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import android.view.SurfaceHolder;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.JDVBStopTimeoutException;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.ProgramCatalog;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.bean.ChannelType;
import com.joysee.dvb.data.ChannelProvider;
import com.joysee.dvb.data.DvbSettings.System;
import com.joysee.dvb.widget.DVBSurfaceViewParent;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class AbsDvbPlayer {
    public interface OnBeginWithChannelListeneer {
        void onBeginWithChannel(DvbService channel);
    }
    
    public interface OnInitCompleteListener {
        public void onInitComplete(AbsDvbPlayer player, int result);
    }

    static final String TAG = JLog.makeTag(AbsDvbPlayer.class);
    
    /**
     * Use with {@link #set3DMode(int)},{@link #get3DMode()}
     */
    public static final int THREED_MODE_OFF = 1;
    /**
     * Use with {@link #set3DMode(int)},{@link #get3DMode()}
     */
    public static final int THREED_MODE_SIDE_BY_SIDE = 2;
    /**
     * Use with {@link #set3DMode(int)},{@link #get3DMode()}
     */
    public static final int THREED_MODE_TOP_AND_BOTTOM = 3;
    /**
     * Use with {@link #set3DMode(int)},{@link #get3DMode()}
     */
    public static final int THREED_MODE_FRAMEPACKING = 4;

    public static JDVBPlayer mDvbPlayer;
    static {
        mDvbPlayer = JDVBPlayer.getInstance();
    }

    private OnInitCompleteListener mOnInitCompleteLis;
    private OnBeginWithChannelListeneer mOnBeginWithChannelLis;
    protected static final int SWITCH_CHANNEL_DELAY = 200;

    protected static boolean mHasInitChannel;
    protected static ChannelCache mChannels;

    public static ArrayList<DvbService> getAllChannel() {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<DvbService> channels = null;
        if (mHasInitChannel && mChannels != null && mChannels.size() > 0) {
            channels = new ArrayList<DvbService>();
            for (Channel c : mChannels) {
                channels.add(c.mChannel);
            }
            JLog.d(TAG, "getAllChannel hit the channels from cache");
        } else {
            channels = mDvbPlayer.getAllChannel();
        }
        JLog.methodEnd(TAG, begin);
        return channels;
    }

    public static int getChannelCount() {
        int count = 0;
        if (mChannels != null) {
            count = mChannels.size();
        } else {
            count = mDvbPlayer.getChannelCount();
        }
        return count;
    }
    
    public void setDongleFileDesc(int desc) {
        mDvbPlayer.setDongleFileDesc(desc);
    }

    public static void initChannel(boolean force) {
        final long begin = JLog.methodBegin(TAG);
        if (force) {
            cleanCacheData();
        }

        if (mChannels == null) {
            mChannels = new ChannelCache();
            Channel c = null;
            DvbService s = null;
            int tIndex = -1;
            while (true) {
                tIndex = mDvbPlayer.getNextChannelIndex(tIndex, DvbService.ALL);
                if (tIndex < 0 || (mChannels.size() > 0 && mChannels.get(0).mIndex == tIndex)) {
                    break;
                }
                s = mDvbPlayer.getChannelByIndex(tIndex);
                JLog.d(TAG, "initChannel tIndex = " + tIndex + " channel = " + s.getLogicChNumber() + "-" + s.getChannelName() + " sid = "
                        + s.getServiceId());
                c = new Channel();
                c.mIndex = tIndex;
                c.mChannelNum = s.getLogicChNumber();
                c.mChannel = s;
                mChannels.add(c);
            }
            mHasInitChannel = true;
        }
        JLog.methodEnd(TAG, begin);
    }

    protected long mCurChannelBeginTime = -1;

    protected boolean mVideoAspectRationEnable = false;

    protected HashMap<Integer, Integer> mCurVideoAspectRations = new HashMap<Integer, Integer>();

    protected Context mContext;
    protected DVBSurfaceViewParent mSurfaceLayout;

//    static JDVBPlayer.OnMonitorListener mMonitor = new JDVBPlayer.OnMonitorListener() {
//        @Override
//        public void onMonitor(int monitorType, Object message) {
//            if (monitorType == JDVBPlayer.CALLBACK_UPDATE_SERVICE
//                    || monitorType == JDVBPlayer.CALLBACK_UPDATE_PROGRAM) {
//                JLog.d(TAG, "DvbPlayer onMonitor type = " + JDVBPlayer.getMonitorCallbackName(monitorType));
//                cleanCacheData();
//            } else if (monitorType == JDVBPlayer.CALLBACK_SERVICE_DIED) {
//                mHasInitChannel = false;
//            }
//        }
//    };
    protected Handler mHandler = new Handler();
    protected static HandlerThread mChannelThread = new HandlerThread("DVB-Channel");
    static {
        mChannelThread.start();
//        DVBPlayManager.addOnMonitorListener(mMonitor);
    }
    protected static boolean isChannelHandlerWorking = false;
    protected static MessageQueue mDVBMessageQueue;
    protected static final int MSG_INIT = 0;
    protected static final int MSG_PREPARE = 1;
    protected static final int MSG_SWITCH_CHANNEL = 2;
    protected static final int MSG_STOP = 3;
    
    private static final String getChannelHandleMsgName(int msg) {
        String ret = "";
        switch (msg) {
            case MSG_INIT:
                ret = MSG_INIT + " - MSG_INIT";
                break;
            case MSG_PREPARE:
                ret = MSG_PREPARE + " - MSG_PREPARE";
                break;
            case MSG_SWITCH_CHANNEL:
                ret = MSG_SWITCH_CHANNEL + " - MSG_SWITCH_CHANNEL";
                break;
            case MSG_STOP:
                ret = MSG_STOP + " - MSG_STOP";
                break;
            default:
                break;
        }
        return ret;
    }

    public static void cleanCacheData() {
        final long begin = JLog.methodBegin(TAG);
        JLog.d(TAG, "cleanCacheData");
        if (mChannels != null) {
            mChannels.clear();
        }
        mHasInitChannel = false;
        mChannels = null;
        JLog.methodEnd(TAG, begin);
    }

    protected Handler mChannelHandler = new Handler(mChannelThread.getLooper()) {
        public void dispatchMessage(Message msg) {
            if (mDVBMessageQueue == null) {
                mDVBMessageQueue = Looper.myQueue();
                JLog.d(TAG, "mDVBMessageQueue = " + mDVBMessageQueue);
            }
            mDVBMessageQueue.addIdleHandler(new IdleHandler() {
                @Override
                public boolean queueIdle() {
                    if (isChannelHandlerWorking) {
                        isChannelHandlerWorking = false;
                        JLog.d(TAG, "mChannelHandler idle, isChannelHandlerWorking = false;");
                    }
                    return false;
                }
            });
            super.dispatchMessage(msg);
        };

        public void handleMessage(android.os.Message msg) {
            final long begin = SystemClock.uptimeMillis();
            long interval = 0;
            String msgName = getChannelHandleMsgName(msg.what);
            JLog.d(TAG, "mChannelHandler " + msgName + " begin tunerId = " + msg.arg1);
            switch (msg.what) {
                case MSG_INIT:
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final int result = init();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnInitCompleteLis != null) {
                                mOnInitCompleteLis.onInitComplete(AbsDvbPlayer.this, result);
                            }
                        }
                    });
                    break;
                case MSG_PREPARE:
                    mDvbPlayer.prepare(msg.arg1);
                    break;
                case MSG_SWITCH_CHANNEL:
                    mDvbPlayer.setChannel(msg.arg1, (DvbService) msg.obj);
                    onBeginWithChannel((DvbService) msg.obj, msg.arg1);
                    break;
                case MSG_STOP:
                    mDvbPlayer.stop(msg.arg1);
                    break;
                default:
                    break;
            }
            interval = SystemClock.uptimeMillis() - begin;
            JLog.d(TAG, "mChannelHandler " + msgName + " tunerId = " + msg.arg1 + " end, takes " + interval + " ms");
        };

        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            isChannelHandlerWorking = true;
            JLog.d(TAG, "ChannelHandler sendMessageAtTime msg = " + msg.what);
            return super.sendMessageAtTime(msg, uptimeMillis);
        };

    };

    public void addOnMonitorListener(JDVBPlayer.OnMonitorListener lis) {
        mDvbPlayer.addOnMonitorListener(lis);
    }

    private void beginWithChannel(DvbService channel) {
        final long begin = JLog.methodBegin(TAG);
        if (channel != null && mChannels.mCurChannel.mChannel.equals(channel)) {
            mCurChannelBeginTime = SystemClock.uptimeMillis();
            JLog.d(TAG, "beginWithChannel : " + channel.getLogicChNumber() + "-"
                    + channel.getChannelName());
            ChannelProvider.updatePlayHistory(mContext, channel);
            ChannelProvider.increaseWatchingTime(mContext, channel, 1L);
        }
        JLog.methodEnd(TAG, begin);
    }
    
    void clearSurfaceViewParent() {
        if (mSurfaceLayout != null) {
            mSurfaceLayout.removeAllViews();
        }
    }

    private void endWithChannel(DvbService channel) {
        final long begin = JLog.methodBegin(TAG);
        if (channel != null && mCurChannelBeginTime != -1) {
            long current = SystemClock.uptimeMillis();
            long interval = current - mCurChannelBeginTime;
            mCurChannelBeginTime = -1;
            JLog.d(TAG, "endWithChannel : " + channel.getLogicChNumber() + "-"
                    + channel.getChannelName() + " increase " + interval / 1000 + " s.");
            ChannelProvider.increaseWatchingTime(mContext, channel, interval);
        }
        JLog.methodEnd(TAG, begin);
    }
    
    public int getCurrentState() {
        return mDvbPlayer.getCurrentState();
    }

    public abstract int get3DMode();

    public DvbService getChannelByNum(int number) {
        DvbService c = null;
        if (mChannels != null) {
            Channel next = mChannels.getChannelByNum(number);
            if (next != null) {
                c = next.mChannel;
            } else {
                c = mDvbPlayer.getChannelByNum(number);
            }
        }
        return c;
    }
    
    public void setOnInitCompleteLis(OnInitCompleteListener lis) {
        this.mOnInitCompleteLis = lis;
    }

    public void setOnBeginWithChannelLis(OnBeginWithChannelListeneer lis) {
        this.mOnBeginWithChannelLis = lis;
    }

    public DvbService getChannelBySid(int sid) {
        DvbService c = null;
        if (mChannels != null) {
            Channel next = mChannels.getChannelBySid(sid);
            if (next != null) {
                c = next.mChannel;
            }
        }
        return c;
    }

    public ArrayList<DvbService> getChannelByType(ChannelType type) {
        ArrayList<DvbService> ret = null;
        ProgramCatalog cata = ChannelType.convert2Programcatalog(type);
        ret = mDvbPlayer.getChannelByChannelType(cata);
        return ret;
    }

    public DvbService getCurrentChannel() {
        DvbService channel = null;
        if (mChannels != null) {
            if (mChannels.mCurChannel != null) {
                channel = mChannels.mCurChannel.mChannel;
                JLog.d(TAG, "getCurrentChannel fit from cache.");
            }
        }
        if (channel == null) {
            channel = mDvbPlayer.getCurrentChannel();
        }
        return channel;
    }

    public DvbService getFirstChannel() {
        DvbService firstChannel = null;
        if (mChannels != null) {
            Channel first = mChannels.getFirstChannel();
            if (first != null) {
                firstChannel = first.mChannel;
            }
        }
        return firstChannel;
    }

    public DvbService getNextChannel() {
        DvbService nextChannel = null;
        if (mChannels != null) {
            Channel next = mChannels.getNext();
            if (next != null) {
                nextChannel = next.mChannel;
            }
        }
        return nextChannel;
    }

    public DvbService getPreviousChannel() {
        DvbService previousChannel = null;
        if (mChannels != null) {
            Channel next = mChannels.getPrevious();
            if (next != null) {
                previousChannel = next.mChannel;
            }
        }
        return previousChannel;
    }

    public int getVideoAspectRation(int tunerId) {
        int videoAspectRation = JDVBPlayer.VIDEOASPECTRATION_16TO9;
        if (mVideoAspectRationEnable) {
            String key = null;
            switch (tunerId) {
                case JDVBPlayer.TUNER_0:
                    key = System.VIDEO_ASPECTRATIO_TUNER_0;
                    break;
                case JDVBPlayer.TUNER_1:
                    key = System.VIDEO_ASPECTRATIO_TUNER_1;
                    break;
                case JDVBPlayer.TUNER_2:
                    key = System.VIDEO_ASPECTRATIO_TUNER_2;
                    break;
                default:
                    throw new RuntimeException("TunerId is invalid " + tunerId);// will
                    // never
                    // happen//
            }
            videoAspectRation = System.getInt(mContext.getContentResolver(), key, JDVBPlayer.VIDEOASPECTRATION_16TO9);
        } else {
            throw new RuntimeException("VideoAspectRation is not enable");
        }
        return videoAspectRation;
    }

    public int init() {
        final long begin = JLog.methodBegin(TAG);
        JLog.e(TAG, "init", new RuntimeException());
        int ret = mDvbPlayer.init();
        if (mVideoAspectRationEnable) {
            mCurVideoAspectRations.put(JDVBPlayer.TUNER_0,
                    System.getInt(mContext.getContentResolver(), System.VIDEO_ASPECTRATIO_TUNER_0,
                            JDVBPlayer.VIDEOASPECTRATION_16TO9));
            mCurVideoAspectRations.put(JDVBPlayer.TUNER_1,
                    System.getInt(mContext.getContentResolver(), System.VIDEO_ASPECTRATIO_TUNER_0,
                            JDVBPlayer.VIDEOASPECTRATION_16TO9));
            mCurVideoAspectRations.put(JDVBPlayer.TUNER_2,
                    System.getInt(mContext.getContentResolver(), System.VIDEO_ASPECTRATIO_TUNER_0,
                            JDVBPlayer.VIDEOASPECTRATION_16TO9));
        }
        JLog.methodEnd(TAG, begin);
        return ret;
    }
    
    public void setSurfaceParent(DVBSurfaceViewParent surfaceLayout) {
        JLog.d(TAG, "setSurfaceParent parent = " + surfaceLayout);
        mSurfaceLayout = surfaceLayout;
    }
    
    public void initAsyn() {
        JLog.e(TAG, "initAsyn", new RuntimeException());
        Message msg = Message.obtain(mChannelHandler, MSG_INIT);
        msg.sendToTarget();
    }

    public abstract void initSurface(SurfaceHolder.Callback callback);

    public abstract boolean is3DSupport();
    public abstract boolean isSoundTrackSupport();

    public void onBeginWithChannel(DvbService channel, int tunerId) {
        if (mOnBeginWithChannelLis != null) {
            mOnBeginWithChannelLis.onBeginWithChannel(channel);
        }
    }

    public void onPlayBegin(int tunerId) {
    }

    public void onPlayEnd(int tunerId) {
    }

    public void prepare(int tunerId) {
        JLog.e(TAG, "prepare", new RuntimeException());
        onPlayBegin(tunerId);
        Message msg = Message.obtain();
        msg.what = MSG_PREPARE;
        msg.arg1 = tunerId;
        mChannelHandler.sendMessage(msg);
    }
    
    public boolean isPlaying() {
        return mDvbPlayer.isPlaying();
    }

    public void removeOnMonitorListener(JDVBPlayer.OnMonitorListener lis) {
//        DVBPlayManager.removeOnMonitorListener(lis);
        mDvbPlayer.removeOnMonitorListener(lis);
    }

    /**
     * @param mode
     * @see #THREED_MODE_OFF
     * @see #THREED_MODE_SIDE_BY_SIDE
     * @see #THREED_MODE_TOP_AND_BOTTOM
     * @see #THREED_MODE_FRAMEPACKING
     */
    public abstract void set3DMode(int mode);

    public void setChannel(int tunerId, DvbService channel) {
        JLog.d(TAG, "setChannel channel = " + channel.getLogicChNumber() + "-" + channel.getChannelName(), new RuntimeException());
        setCurrentChannel(channel);
        mChannelHandler.removeMessages(MSG_SWITCH_CHANNEL);
        Message msg = Message.obtain();
        msg.what = MSG_SWITCH_CHANNEL;
        msg.obj = channel;
        msg.arg1 = tunerId;
        mChannelHandler.sendMessageDelayed(msg, SWITCH_CHANNEL_DELAY);
    }

    private void setCurrentChannel(DvbService channel) {
        if (mChannels != null) {
            for (Channel c : mChannels) {
                if (c.mChannel.equals(channel)) {
                    if (mChannels.mCurChannel != null) {
                        endWithChannel(mChannels.mCurChannel.mChannel);
                    }
                    mChannels.setCurrent(c);
                    break;
                }
            }
        }
        beginWithChannel(channel);
    }

    public int getSoundTrack(int tunerId) {
        if (!isSoundTrackSupport()) {
            throw new RuntimeException("SoundTrack is not support.");
        }
        return 0;
    }
    public void setSoundTrack(int tunerId, DvbService channel, int soundTrack) {
        if (!isSoundTrackSupport()) {
            throw new RuntimeException("SoundTrack is not support.");
        }
    }

    public void setVideoAspectRation(int tunerId, int mode) {
        JLog.d(TAG, "setVideoAspectRation mode = " + mode);
        String key = null;
        switch (tunerId) {
            case JDVBPlayer.TUNER_0:
                key = System.VIDEO_ASPECTRATIO_TUNER_0;
                break;
            case JDVBPlayer.TUNER_1:
                key = System.VIDEO_ASPECTRATIO_TUNER_1;
                break;
            case JDVBPlayer.TUNER_2:
                key = System.VIDEO_ASPECTRATIO_TUNER_2;
                break;
            default:
                throw new RuntimeException("TunerId is invalid " + tunerId);// will
                // never
                // happen//
        }
        System.putInt(mContext.getContentResolver(), key, mode);
    }

    public void setVideoAspectRationEnable(boolean enable) {
        mVideoAspectRationEnable = enable;
    }

    public void stop(int tunerId) {
        final long begin = JLog.methodBegin(TAG);
        JLog.e(TAG, "stop", new RuntimeException());
        if (mChannels.mCurChannel != null) {
            endWithChannel(mChannels.mCurChannel.mChannel);
            mChannels.setCurrent(null);
        }
        mChannelHandler.removeMessages(MSG_SWITCH_CHANNEL);
        mChannelHandler.removeMessages(MSG_STOP);
        Message msg = Message.obtain();
        msg.what = MSG_STOP;
        msg.arg1 = tunerId;
        mChannelHandler.sendMessage(msg);
        onPlayEnd(tunerId);
        int num = 0;
        boolean timeOut = false;
        long tElapsedTime = 0;
        while (isChannelHandlerWorking && !timeOut) {
            tElapsedTime = java.lang.System.currentTimeMillis() - begin;
            if (tElapsedTime <= 350) {
                try {
                    Thread.sleep(20);
                    if (num % 5 == 0) {
                        JLog.d(TAG, "ChannelHandler is Working, wait for idle. " + num);
                    }
                    num++;
                } catch (InterruptedException e) {
                    JLog.d(TAG, "stop catch Exception", e);
                }
            } else {
                timeOut = true;
                JLog.d(TAG, "ChannelHandler is Working, stop timeout. ");
            }
        }
        JLog.methodEnd(TAG, begin);
        if (timeOut) {
            throw new JDVBStopTimeoutException(tElapsedTime);
        }
    }
    
    public boolean setKeepLastFrameEnable(int tunerId, boolean enable) {
        return mDvbPlayer.setKeepLastFrameEnable(tunerId, enable);
    }

    public int unInit() {
        return mDvbPlayer.uninit();
    }
}
