/**
 * =====================================================================
 *
 * @file  EPGUpdateService.java
 * @Module Name   com.joysee.adtv.service
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年12月14日
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
 * YueLiang         2013年12月14日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.bean.ChannelType;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.bean.ProgramType;
import com.joysee.dvb.bean.SimpleChannel;
import com.joysee.dvb.data.ChannelProvider;
import com.joysee.dvb.data.EPGProvider;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.player.DvbPlayerFactory;

import java.util.ArrayList;
import java.util.List;

public class EPGUpdateService extends Service {
    public class EPGUpdateStatus {
        public boolean running;
        public long nextUpdateBeginTime;
        public int channelSize;
        public int currentChannel;

        @Override
        public String toString() {
            return "EPGUpdateStatus [running=" + running + ", nextUpdateBeginTime=" + nextUpdateBeginTime + "]";
        }

    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public EPGUpdateService getService() {
            return EPGUpdateService.this;
        }
    }

    private static final String TAG = JLog.makeTag(EPGUpdateService.class);
    
    public static final String COMMAND = "epg_update_service_command";
    public static final String COMMAND_START = "epg_update_service_start";
    public static final String COMMAND_STOP = "epg_update_service_stop";
    
    private EPGUpdateStatus mCurrentStatus;
    private boolean mStopped = false;
    private final IBinder mBinder = new LocalBinder();
    public static final long EPG_UPDATE_TIMER_DELAY = 1000 * 20;
    public static final long EPG_UPDATE_INTERVAL = 1000 * 60 * 10;
    private static HandlerThread mUpdateThread = new HandlerThread("EPGUpdateThread", Process.THREAD_PRIORITY_LOWEST);
    static {
        mUpdateThread.start();
    }
    private static final int MSG_UPDATE_PROGRAM = 1;

    private Handler mUpdateHandler = new Handler(mUpdateThread.getLooper()) {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PROGRAM:
                    JLog.d(TAG, "begin MSG_UPDATE_PROGRAM.", new RuntimeException());
                    mCurrentStatus.running = true;
                    mCurrentStatus.nextUpdateBeginTime = 0;
                    mCurrentStatus.currentChannel = 0;
                    DvbPlayerFactory.getPlayer(EPGUpdateService.this).init(null);
                    ArrayList<DvbService> channels = AbsDvbPlayer.getAllChannel();
                    if (channels != null && channels.size() > 0) {
                        syncLocalChannel2DB(channels);
                        mCurrentStatus.channelSize = channels.size();
                        boolean success = EPGUpdateService.this.updateChannelType();
                        if (success) {
                            EPGUpdateService.this.updateTvIdAndTypeForChannels(channels);
                        }
                        if (!TvApplication.FORCE_TS_EPG) {
                            EPGUpdateService.this.updateProgramType();
                            EPGUpdateService.this.updateEPG();
                        }
                    } else {
                        clearData(EPGUpdateService.this);
                    }
                    if (!checkWhetherNeedStop()) {
                        JLog.d(TAG, "EPGUpdateService start MSG_UPDATE_PROGRAM delay " + EPG_UPDATE_INTERVAL + " ms.");
                        mUpdateHandler.removeMessages(MSG_UPDATE_PROGRAM);
                        mUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRAM, EPG_UPDATE_INTERVAL);
                        mCurrentStatus.nextUpdateBeginTime = System.currentTimeMillis() + EPG_UPDATE_INTERVAL;
                    } else {
                        JLog.d(TAG, "EPGUpdateService is exit, cancle MSG_UPDATE_PROGRAM.");
                    }
                    mCurrentStatus.running = false;
                    break;

                default:
                    break;
            }
        };
    };

    public EPGUpdateService() {
    }

    private void clearData(Context context) {
        final long begin = JLog.methodBegin(TAG);
        ChannelProvider.deleteAllChannelType(context);
        ChannelProvider.deleteAllChannel(context);
        EPGProvider.deleteAllProgramType(context);
        EPGProvider.deleteAllProgramOrder(context);
        EPGProvider.deleteAllProgram(context);
        JLog.methodEnd(TAG, begin);
    }

    public EPGUpdateStatus getEPGUpdateStatus() {
        return mCurrentStatus;
    }

    private boolean checkWhetherNeedStop() {
        boolean foreground = false;
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runTasks = am.getRunningTasks(1);
        if (runTasks != null && runTasks.size() > 0) {
            RunningTaskInfo task = runTasks.get(0);
            String pkgName = task.baseActivity.getPackageName();
            if (pkgName.equals(getPackageName())) {
                foreground = true;
            }
        }
        boolean need = !(foreground && !mStopped);
        JLog.d(TAG, "checkWhetherNeedStop ret = " + need + " foreground = " + foreground + " mStopped = " + mStopped);
        return need;
    }

    @Override
    public IBinder onBind(Intent intent) {
        JLog.d(TAG, "EPGUpdateService onBind.");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final long begin = JLog.methodBegin(TAG);
        mCurrentStatus = new EPGUpdateStatus();
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final long begin = JLog.methodBegin(TAG);
        mUpdateHandler.removeMessages(MSG_UPDATE_PROGRAM);
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        JLog.d(TAG, "onStartCommand");
        String command = null;
        if (intent != null) {
            command = intent.getStringExtra(COMMAND);
        }
        JLog.d(TAG, "onStartCommand command = " + command);
        if (COMMAND_START.equals(command)) {
            mStopped = false;
            mUpdateHandler.removeMessages(MSG_UPDATE_PROGRAM);
            mUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRAM, EPG_UPDATE_TIMER_DELAY / 2);
        } else if (COMMAND_STOP.equals(command)) {
            mStopped = true;
            mUpdateHandler.removeMessages(MSG_UPDATE_PROGRAM);
        } else {
            mStopped = false;
            mUpdateHandler.removeMessages(MSG_UPDATE_PROGRAM);
            mUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRAM, EPG_UPDATE_TIMER_DELAY);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    void syncLocalChannel2DB(ArrayList<DvbService> channels) {
        final long begin = JLog.methodBegin(TAG);
        if (channels != null) {
            boolean needUpdate = false;
            int localCount = channels.size();
            int dbCount = ChannelProvider.getChannelCountInDB(EPGUpdateService.this);

            if (localCount != dbCount) {
                needUpdate = true;
            } else {
                ArrayList<DvbService> channelsInDB = ChannelProvider.getChannelByTypeFromDBInternal(EPGUpdateService.this, null);
                boolean match = channels.containsAll(channelsInDB);
                if (!match) {
                    needUpdate = true;
                }
            }
            JLog.d(TAG, "syncLocalChannel2DB localCount = " + localCount + " dbCount = " + dbCount + " needUpdate = " + needUpdate);
            if (needUpdate) {
                clearData(EPGUpdateService.this);
                ChannelProvider.saveChannel(EPGUpdateService.this, channels);
            }
        }
        JLog.methodEnd(TAG, begin);
    }

    boolean updateChannelType() {
        final long begin = JLog.methodBegin(TAG);
        boolean success = false;
        int count = 0;
        EPGUpdateHelper helper = new EPGUpdateHelper();
        ArrayList<ChannelType> types = helper.getAllChannelType();
        if (types != null) {
            ChannelProvider.deleteAllChannelType(EPGUpdateService.this);
            count = ChannelProvider.saveChannelType(EPGUpdateService.this, types);
            success = count > 0;
        } else {
            JLog.d(TAG, "updateChannelType types = null");
        }
        JLog.d(TAG, "updateChannelType " + (success ? "success count = " + count : "fail"));
        JLog.methodEnd(TAG, begin);
        return success;
    }

    void updateEPG() {
        final long begin = JLog.methodBegin(TAG);
        if (checkWhetherNeedStop()) {
            JLog.d(TAG, "EPGUpdateService is exit, break.");
        } else {
            ArrayList<DvbService> channels = AbsDvbPlayer.getAllChannel();
            if (channels != null && channels.size() > 0) {
                JLog.d(TAG, "DEBUG channels.size = " + channels.size());
                for (DvbService channel : channels) {
                    if (!checkWhetherNeedStop()) {
                        updateEpgForChannel(channel);
                        mCurrentStatus.currentChannel++;
                    } else {
                        JLog.d(TAG, "EPGUpdateService is exit, break.");
                        break;
                    }
                }
            } else {
                JLog.d(TAG, "has no channel, exit update");
            }
        }
        JLog.methodEnd(TAG, begin);
    }

    void updateEpgForChannel(final DvbService channel) {
        final long begin = JLog.methodBegin(TAG);
        if (channel == null) {
            return;
        }
        long saveTakes = -1;
        int tvId = ChannelProvider.getChannelTvId(this, channel);
        if (tvId > 0) {
            EPGUpdateHelper helper = new EPGUpdateHelper();
            ArrayList<Program> programs = helper.getProgramBytvId(tvId);
            if (programs != null) {
                for (Program event : programs) {
                    event.serviceId = channel.getServiceId();
                    event.logicNumber = channel.getLogicChNumber();
                }
                EPGProvider.deleteProgramByServiceId(EPGUpdateService.this, channel.getServiceId());
                long saveBegin = SystemClock.uptimeMillis();
                ArrayList<Program> programsToDB = new ArrayList<Program>();
                for (int i = 0; i < programs.size(); i++) {
                    programsToDB.add(programs.get(i));
                    if (programsToDB.size() % 20 == 0 || i == programs.size() - 1) {
                        EPGProvider.savePrograms(EPGUpdateService.this, programsToDB, channel.getServiceId());
                        programsToDB = new ArrayList<Program>();
                    }
                }
                saveTakes = SystemClock.uptimeMillis() - saveBegin;

            }
        }
        JLog.methodEnd(TAG, begin, ", save takes " + saveTakes + " ms.");
    }

    boolean updateProgramType() {
        final long begin = JLog.methodBegin(TAG);
        boolean success = false;
        int count = 0;
        EPGUpdateHelper helper = new EPGUpdateHelper();
        ArrayList<ProgramType> types = helper.getAllProgramType();
        if (types != null) {
            EPGProvider.deleteAllProgramType(EPGUpdateService.this);
            count = EPGProvider.saveProgramType(EPGUpdateService.this, types);
            success = count > 0;
        } else {
            JLog.d(TAG, "updateProgramType types = null");
        }
        JLog.d(TAG, "updateProgramType " + (success ? "success count = " + count : "fail"));
        JLog.methodEnd(TAG, begin);
        return success;
    }

    void updateTvIdAndTypeForChannels(ArrayList<DvbService> channels) {
        final long begin = JLog.methodBegin(TAG);
        if (channels == null || channels.isEmpty()) {
            return;
        }
        EPGUpdateHelper helper = new EPGUpdateHelper();
        ArrayList<SimpleChannel> netChannels = helper.getAllChannel(channels);
        int totalCount = 0;
        if (netChannels != null) {
            for (DvbService c : channels) {
                for (SimpleChannel netC : netChannels) {
                    if (c.getChannelName().equals(netC.mChannelName)) {
                        c.setTvId(netC.mTvId);
                        c.setTypeCode(netC.mTypeId);
                        c.setTypeName(netC.mTypeName);
                        int count = ChannelProvider.updateTvIdAndTypeForChannel(EPGUpdateService.this, c);
                        totalCount += count;
                        break;
                    }
                }
            }
        } else {
            JLog.d(TAG, "updateTypeForChannels netChannels = null");
        }
        JLog.d(TAG, "updateTypeForChannels  count = " + totalCount);
        JLog.methodEnd(TAG, begin);
    }
}
