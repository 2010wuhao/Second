/**
 * =====================================================================
 *
 * @file  JDVBPlayerDTVImpl.java
 * @Module Name   com.joysee.adtv.logic
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月28日
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
 * YueLiang          2014年3月28日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic;

import android.util.Log;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.EmailContent;
import com.joysee.adtv.logic.bean.EmailHead;
import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.adtv.logic.bean.LicenseInfo;
import com.joysee.adtv.logic.bean.ProgramCatalog;
import com.joysee.adtv.logic.bean.TaskInfo;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.logic.bean.TunerSignal;
import com.joysee.adtv.logic.bean.WatchTime;

import java.util.ArrayList;
import java.util.Vector;

class JDVBPlayerDTVImpl extends JDVBPlayer {
    static {
        System.loadLibrary("dtvplayer_jni");
    }

    private static final DVBPlayManager mDvbPlayManager = DVBPlayManager.getInstance();
    private static final ChannelManager mChannelManager = ChannelManager.getInstance();
    private static final SettingManager mSettingManager = SettingManager.getInstance();
    private static final EPGManager mEpgManager = EPGManager.getInstance();
    private static final CaManager mCaManager = CaManager.getInstance();
    private static final TaskManager mTaskManager = TaskManager.getInstance();
    private static ArrayList<JDVBPlayer.OnMonitorListener> onMonitorListeners = new ArrayList<JDVBPlayer.OnMonitorListener>();

    private static final int NO_ERROR = 0;

    private int mCurrentState = DVBPLAYER_IDLE;

    JDVBPlayerDTVImpl() {
        Log.i(TAG, "JDVBPlayerDTVImpl constructor is call.");
        DVBPlayManager.onMonitorListener = new DVBPlayManager.OnMonitorListener() {
            @Override
            public void onMonitor(int monitorType, Object message) {
                Log.d(TAG, "onMonitor type = " + JDVBPlayer.getMonitorCallbackName(monitorType) + " message = " + message);
                if (monitorType >= CALLBACK_FOR_SEARCH_FIRST && monitorType <= CALLBACK_FOR_SEARCH_LAST) {
                    switch (monitorType) {
                        case CALLBACK_RECEIVE_CHANNELS:
                            if (mOnSearchInfoListener != null) {
                                mOnSearchInfoListener.onReceiveChannel((ArrayList<DvbService>) message);
                            }
                            break;
                        case CALLBACK_PROGRESS_CHANGE:
                            if (mOnSearchInfoListener != null) {
                                mOnSearchInfoListener.onProgressChange((Integer) message);
                            }
                            break;
                        case CALLBACK_TRANSPONDER_INFO:
                            if (mOnSearchInfoListener != null) {
                                mOnSearchInfoListener.onTransponderInfo((Transponder) message);
                            }
                            break;
                        case CALLBACK_SIGNAL_INFO:
                            if (mOnSearchInfoListener != null) {
                                mOnSearchInfoListener.onSignalInfo((TunerSignal) message);
                            }
                            break;
                        case CALLBACK_SEARCH_END:
                            if (mOnSearchInfoListener != null) {
                                mOnSearchInfoListener.onSearchEnd((ArrayList<DvbService>) message);
                            }
                            break;
                        default:
                            break;
                    }
                } else if (monitorType >= CALLBACK_LOCALDATA_UPDATE_PROGRESS && monitorType <= CALLBACK_LOCALDATA_UPDATE_VERSION_FOUND) {
                    if (mOnTaskInfoListener != null) {
                        TaskInfo task = (TaskInfo) message;
                        task.mCBType = monitorType;
                        mOnTaskInfoListener.onTaskInfo(task);
                    }
                } else if (monitorType == CALLBACK_SERVICE_DIED) {
                    mCurrentState = DVBPLAYER_ERROR;
                    init();
                    if (mOnDTVDeathNotifier != null) {
                        mOnDTVDeathNotifier.onServiceDied();
                    }
                }  else {
                    for (OnMonitorListener lis : onMonitorListeners) {
                        lis.onMonitor(monitorType, message);
                    }
                }
            }
        };
    }

    /**
     * 初始化播放器
     * 
     * @return true:成功, false:失败
     */
    @Override
    public int init() {
        int ret = 0;
        if (mCurrentState == DVBPLAYER_IDLE || mCurrentState == DVBPLAYER_ERROR) {
            mCurrentState = DVBPLAYER_INITIALIZING;
            if ((ret = mDvbPlayManager.nativeInit()) == NO_ERROR) {
                mCurrentState = DVBPLAYER_INITIALIZED;
            } else {
                Log.e(TAG, "init fail ret = " + ret, new RuntimeException());
                mCurrentState = DVBPLAYER_ERROR;
            }
            Log.d(TAG, "init ret = " + ret);
        }
        return ret;
    }

    @Override
    public boolean prepare(int tunerId) throws IllegalStateException {
        boolean success = false;
        if ((mCurrentState & (DVBPLAYER_INITIALIZED | DVBPLAYER_STOPPED)) != 0) {
            if (mDvbPlayManager.nativePlay(tunerId) == NO_ERROR) {
                mCurrentState = DVBPLAYER_PREPARED;
                success = true;
            } else {
                Log.e(TAG, "prepare fail", new RuntimeException());
            }
        } else {
            Log.e(TAG, "prepare call in state " + dvbPlayerState2String(mCurrentState));
            if (mStrictMode) {
                throw new IllegalStateException();
            }
        }
        return success;
    }

    @Override
    public void setChannel(int tunerId, DvbService channel) throws IllegalStateException {
        Log.i(TAG, "setChannel channel = " + channel.getChannelName());
        if ((mCurrentState & (DVBPLAYER_PREPARED | DVBPLAYER_STARTED)) != 0) {
            mDvbPlayManager.nativeChangeService(tunerId, channel);
            mCurrentState = DVBPLAYER_STARTED;
        } else {
            Log.e(TAG, "setChannel call in state " + dvbPlayerState2String(mCurrentState));
            if (mStrictMode) {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public void stop(int tunerId) throws IllegalStateException {
        Log.i(TAG, "stop tunerId = " + tunerId);
        if ((mCurrentState & (DVBPLAYER_PREPARED | DVBPLAYER_STARTED)) != 0) {
            mDvbPlayManager.nativeStop(tunerId);
            mCurrentState = DVBPLAYER_STOPPED;
        } else {
            Log.e(TAG, "stop call in state " + dvbPlayerState2String(mCurrentState));
            if (mStrictMode) {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public boolean isPlaying() {
        boolean playing = false;
        if ((mCurrentState & (DVBPLAYER_PREPARED | DVBPLAYER_STARTED)) != 0) {
            playing = true;
        }
        return playing;
    }

    @Override
    public void addOnMonitorListener(OnMonitorListener lis) {
        if (lis != null) {
            if (!onMonitorListeners.contains(lis)) {
                onMonitorListeners.add(lis);
            }
        }
    }

    @Override
    public void removeOnMonitorListener(OnMonitorListener lis) {
        if (lis != null) {
            if (onMonitorListeners.contains(lis)) {
                onMonitorListeners.remove(lis);
            }
        }
    }

    @Override
    public ArrayList<EpgEvent> getEpgDataByDuration(int tunerId, int serviceId, long startTime, long endTime) {
        ArrayList<EpgEvent> events = new ArrayList<EpgEvent>();
        int result = mDvbPlayManager.nativeGetEpgDataByDuration(tunerId, serviceId, events, startTime, endTime);
        if (result != NO_ERROR || events.size() == 0) {
            events = null;
        }
        return events;
    }

    @Override
    public int updateChannel(int tvIndex, DvbService channel) {
        return mDvbPlayManager.nativeSyncServiceToProgram(tvIndex, channel);
    }

    @Override
    public int getTunerSignalStatus(int tunerId) {
        int ret = mDvbPlayManager.nativeGetTunerSignalStatus(tunerId);
        return ret;
    }

    @Override
    public boolean setKeepLastFrameEnable(int tunerId, boolean enable) {
        boolean ret = false;
        int result = -1;
        if ((result = mDvbPlayManager.nativeDisableKeepLastFrame(tunerId, enable)) == NO_ERROR) {
            ret = true;
        } else {
            Log.e(TAG, "setKeepLastFrameEnable result = " + result);
        }
        return ret;
    }

    @Override
    public ArrayList<DvbService> getAllChannel() {
        ArrayList<DvbService> channels;
        channels = new ArrayList<DvbService>();
        if (mChannelManager.nativeGetAllService(channels) == NO_ERROR) {
        } else {
            channels = null;
        }
        if (channels != null && channels.isEmpty()) {
            channels = null;
        }
        Log.d(TAG, "getAllChannel " + ((channels != null) ? " success, count = " + channels.size() : "fail"));
        return channels;
    }

    @Override
    public boolean deleteAllChannel() {
        return mChannelManager.nativedelAllService() == NO_ERROR;
    }

    @Override
    public int getChannelCount() {
        final int count = mChannelManager.nativeGetServiceCount();
        Log.d(TAG, "getChannelCount count = " + count);
        return count;
    }

    @Override
    public ArrayList<ProgramCatalog> getAllChannelType() {
        ArrayList<ProgramCatalog> catalogs = new ArrayList<ProgramCatalog>();
        if (mChannelManager.nativeGetProgramCatalogs(catalogs) == NO_ERROR) {
            Log.d(TAG, "getAllChannelType success, count = " + catalogs.size());
        } else {
            catalogs = null;
        }
        return catalogs;
    }

    @Override
    public ArrayList<DvbService> getChannelByChannelType(ProgramCatalog cata) {
        ArrayList<DvbService> channels = new ArrayList<DvbService>();
        Log.d(TAG, "getChannelByProgramCatalog catelog = " + cata);
        if (cata != null) {
            int tIndex = -1;
            DvbService s = null;
            int firstIndex = -1;
            boolean first = true;
            while (true) {
                tIndex = getNextChannelIndex(tIndex, cata.getFilter());
                if (first) {
                    firstIndex = tIndex;
                    first = false;
                }
                if (tIndex < 0 || (channels.size() > 0 && firstIndex == tIndex)) {
                    break;
                }
                s = getChannelByIndex(tIndex);
                Log.d(TAG, "getChannelByChannelType tIndex = " + tIndex + " channel = " + s.getLogicChNumber() +
                        "-" + s.getChannelName() + " sid = " + s.getServiceId());
                channels.add(s);
            }
        }
        if (channels.size() == 0) {
            channels = null;
        }

        return channels;
    }

    @Override
    public DvbService getChannelByIndex(int channelIndex) {
        DvbService channel = new DvbService();
        int result = mChannelManager.nativeGetServiceByIndex(channelIndex, channel);
        if (result < DVBPlayManager.NO_ERROR) {
            channel = null;
        }
        return channel;
    }

    @Override
    public DvbService getChannelByNum(int number) {
        return getChannelByNum(number, DvbService.ALL);
    }

    @Override
    public DvbService getChannelByNum(int number, int type) {
        DvbService channel = new DvbService();
        int result = mChannelManager.nativeGetService(number, channel, type);
        Log.d(TAG, "getChannelByNum number = " + number + " type = " + type + " result = " + result);
        if (result < DVBPlayManager.NO_ERROR) {
            channel = null;
        }
        return channel;
    }

    @Override
    public DvbService getCurrentChannel() {
        DvbService channel = new DvbService();
        int result = mChannelManager.nativeGetCurrentService(channel);
        if (result < NO_ERROR) {
            channel = null;
        }
        return channel;
    }

    @Override
    public int getIndexByChannelNum(int number) {
        DvbService channel = new DvbService();
        return mChannelManager.nativeGetService(number, channel, DvbService.ALL);
    }

    @Override
    public int getNextChannelIndex(int index, int filter) {
        return mChannelManager.nativeGetNextDVBService(index, filter);
    }

    @Override
    public int setOperatorCode(String operatorCode) {
        return mChannelManager.nativeSetSearchAreaInfo(operatorCode);
    }

    @Override
    public int startSearchChannel(int searchMode, Transponder transponder) {
        Log.d(TAG, "startSearchChannel mode = " + searchMode + " transponder = " + transponder);
        return mChannelManager.nativeStartSearchTV(searchMode, transponder);
    }

    @Override
    public int stopSearchChannel(boolean isSave) {
        Log.d(TAG, "stopSearchChannel isSave = " + isSave);
        return mChannelManager.nativeCancelSearchTV(isSave);
    }

    @Override
    public int startSearchEPG(int tunerId, Transponder param) {
        int result = mEpgManager.nativeStartEPGSearch(tunerId, param, 0);
        Log.d(TAG, "startSearchEPG result = " + epgSearchResule2String(result));
        return result;
    }

    @Override
    public int stopSearchEPG() {
        return mEpgManager.nativeCancelEPGSearch();
    }

    @Override
    public int setEPGSourceMode(boolean mode) {
        int result = mEpgManager.nativeSetEPGSourceMode(mode);
        return result;
    }

    @Override
    public int getSoundTrack(int tunerId) {
        return mSettingManager.nativeGetSoundTrackMode(tunerId);
    }

    @Override
    public int setSoundTrack(int tunerId, int Mode) {
        return mSettingManager.nativeSetSoundTrackMode(tunerId, Mode);
    }

    @Override
    public int setVideoAspectRatio(int tunerId, int ratio) {
        return mSettingManager.nativeSetVideoAspectRatio(tunerId, ratio);
    }

    @Override
    public int setVideoWindow(int tunerId, int left, int top, int right, int bottom) {
        return mSettingManager.nativeSetVideoWindow(tunerId, left, top, right, bottom);
    }

    @Override
    public void changeCAcardPassword(String oldPwd, String newPwd) throws CAControlException {
        int ret = mCaManager.nativeChangePinCode(oldPwd, newPwd);
        if (ret != CDCA_RC_OK) {
            throw new CAControlException(ret);
        }
    }

    @Override
    public boolean deleteEmailById(int id) {
        return mCaManager.nativeDelEmail(id) == NO_ERROR;
    }

    @Override
    public Vector<LicenseInfo> getAuthorization(int operID) throws CAControlException {
        Vector<LicenseInfo> vec = new Vector<LicenseInfo>();
        int ret = mCaManager.nativeGetAuthorization(operID, vec);
        if (ret != CDCA_RC_OK) {
            vec = null;
            throw new CAControlException(ret);
        }
        return vec;
    }

    @Override
    public String getCacardSN() {
        return mCaManager.nativeGetCardSN();
    }

    @Override
    public EmailContent getEmailContentById(int emailId) {
        EmailContent content = new EmailContent();
        if (mCaManager.nativeGetEmailContent(emailId, content) < 0) {
            content = null;
        }
        return content;
    }

    @Override
    public ArrayList<EmailHead> getAllEmailHead() {
        ArrayList<EmailHead> heads = new ArrayList<EmailHead>();
        int ret = 0;
        if ((ret = mCaManager.nativeGetEmailHeads(heads)) < 0) {
            Log.d(TAG, "getAllEmailHead ret state = " + ret);
            heads = null;
        }
        return heads;
    }

    @Override
    public Vector<Integer> getOperatorID() throws CAControlException {
        Vector<Integer> vec = new Vector<Integer>();
        int ret = mCaManager.nativeGetOperatorID(vec);
        if (ret != CDCA_RC_OK) {
            vec = null;
            throw new CAControlException(ret);
        }
        return vec;
    }

    @Override
    public int getEmailCount() {
        return mCaManager.nativeGetEmailUsedSpace();
    }

    @Override
    public int getWatchLevel() {
        return mCaManager.nativeGetWatchLevel();
    }

    @Override
    public void setWatchLeve(String pwd, int level) throws CAControlException {
        int ret = mCaManager.nativeSetWatchLevel(pwd, level);
        if (ret != CDCA_RC_OK) {
            throw new CAControlException(ret);
        }
    }

    @Override
    public WatchTime getValidWatchingTime() throws CAControlException {
        WatchTime watchTime = new WatchTime();
        int ret = mCaManager.nativeGetWatchTime(watchTime);
        if (ret != CDCA_RC_OK) {
            watchTime = null;
            throw new CAControlException(ret);
        }
        return watchTime;
    }

    @Override
    public void setValidWatchingTime(String pwd, WatchTime watchTime) throws CAControlException {
        int ret = mCaManager.nativeSetWatchTime(pwd, watchTime);
        if (ret != CDCA_RC_OK) {
            throw new CAControlException(ret);
        }
    }

    @Override
    public int getStateByType(int tunerId, int type) {
        return mCaManager.nativeQueryMsgType(tunerId, type);
    }

    @Override
    public void setDongleFileDesc(int desc) {
        mDvbPlayManager.mUSBDongleFd = desc;
    }

    @Override
    public int uninit() {
        int ret = 0;
        if ((ret = mDvbPlayManager.nativeUninit()) == NO_ERROR) {
            mCurrentState = DVBPLAYER_IDLE;
        } else {
            Log.e(TAG, "uninit fail ret = " + ret, new RuntimeException());
        }
        return ret;
    }

    @Override
    public int startTask(TaskInfo task) {
        if (task == null || task.mUrl == null || task.mUrl.isEmpty()) {
            throw new IllegalArgumentException("params error");
        }
        return mTaskManager.nativeStartTask(task.mUrl);
    }

    @Override
    public int cancleTask(TaskInfo task) {
        if (task == null || task.mUrl == null || task.mUrl.isEmpty()) {
            throw new IllegalArgumentException("params error");
        }
        return mTaskManager.nativeCancleTask(task.mUrl);
    }

    @Override
    public int getTaskStatus(TaskInfo task) {
        if (task == null || task.mUrl == null || task.mUrl.isEmpty()) {
            throw new IllegalArgumentException("params error");
        }
        return mTaskManager.nativeGetTaskStatus(task.mUrl);
    }

    @Override
    public int getTaskProgress(TaskInfo task) {
        if (task == null || task.mUrl == null || task.mUrl.isEmpty()) {
            throw new IllegalArgumentException("params error");
        }
        return mTaskManager.nativeGetTaskProgress(task.mUrl);
    }

    @Override
    public String getDomainCode() {
        return mTaskManager.nativeGetDomainCode();
    }

    @Override
    public int getLocalPackVersionCode(String url) {
        return mTaskManager.nativeGetIntLocalPacketVer(url);
    }

    @Override
    public String getLocalPackVersionName(String url) {
        return mTaskManager.nativeGetLocalPacketVer(url);
    }

    @Override
    public int getCurrentState() {
        return mCurrentState;
    }
}
