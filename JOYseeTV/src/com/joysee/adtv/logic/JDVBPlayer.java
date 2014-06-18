/**
 * =====================================================================
 *
 * @file  JDVBPlayer.java
 * @Module Name   com.joysee.adtv.logic
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月29日
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
 * YueLiang         2014年3月29日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

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

/**
 * JDVBPlayer可以用来调用DVB的播放，EPG，搜索频道，CA卡设置等功能。
 * @author YueLiang
 *
 * 
 * <P>回调</P>
 * 播放,搜索频道过程中可以注册一些接口来监测DVB的状态.</Br>
 * {@link #addOnMonitorListener(OnMonitorListener)}</Br>
 * {@link #setOnSearchInfoListener(OnSearchInfoListener)}
 */
public abstract class JDVBPlayer {

    public static final int TASK_STATUS_START_DOWNLOAD = 1;
    public static final int TASK_STATUS_DOWNLOAD_SUCCESS = 2;
    public static final int TASK_STATUS_SERVICE_TASK_NAME_ERROR = -1;
    public static final int TASK_STATUS_REQUEST_HTTP_ERROR = -2;
    public static final int TASK_STATUS_NO_UPDATE_VERSION = -3;
    public static final int TASK_STATUS_UPDATE_URL_NULL = -4;
    public static final int TASK_STATUS_MKDIR_LOCALPACK_ERROR = -5;
    public static final int TASK_STATUS_DOWNLOAD_FAILED = -6;
    public static final int TASK_STATUS_ZIP_FILE_FAILED = -7;

    public static String status2String(int status) {
        StringBuilder sb = new StringBuilder();
        switch (status) {
            case TASK_STATUS_START_DOWNLOAD:
                sb.append(status + " - START_DOWNLOAD");
                break;
            case TASK_STATUS_DOWNLOAD_SUCCESS:
                sb.append(status + " - DOWNLOAD_SUCCESS");
                break;
            case TASK_STATUS_ZIP_FILE_FAILED:
                sb.append(status + " - ZIP_FILE_FAILED");
                break;
            case TASK_STATUS_SERVICE_TASK_NAME_ERROR:
                sb.append(status + " - SERVICE_TASK_NAME_ERROR");
                break;
            case TASK_STATUS_REQUEST_HTTP_ERROR:
                sb.append(status + " - REQUEST_HTTP_ERROR");
                break;
            case TASK_STATUS_NO_UPDATE_VERSION:
                sb.append(status + " - NO_UPDATE_VERSION");
                break;
            case TASK_STATUS_UPDATE_URL_NULL:
                sb.append(status + " - UPDATE_URL_NULL");
                break;
            case TASK_STATUS_DOWNLOAD_FAILED:
                sb.append(status + " - DOWNLOAD_FAILED");
                break;
            case TASK_STATUS_MKDIR_LOCALPACK_ERROR:
                sb.append(status + " - MKDIR_LOCALPACK_ERROR");
                break;
        }
        return sb.toString();
    }
    
    public interface OnSearchInfoListener {
        void onReceiveChannel(ArrayList<DvbService> channels);

        void onProgressChange(int progress);

        void onTransponderInfo(Transponder tran);

        void onSignalInfo(TunerSignal signal);

        void onSearchEnd(ArrayList<DvbService> channels);
    }
    
    public interface OnTaskInfoListener {
        void onTaskInfo(TaskInfo task);
    }

    /**
     * 播放器类型 ： </Br> 1. {@link PlayerType#DTV} :适配DTVService结构的播放器</Br> 2.
     * {@link PlayerType#COMMON}:跨平台通用播放器
     * 
     * @author YueLiang
     */
    public enum PlayerType {
        DTV, COMMON
    }

    public interface OnMonitorListener {
        void onMonitor(int monitorType, Object message);
    }

    static final String TAG = JDVBPlayer.class.getSimpleName();
    static PlayerType mPlayerType = null;
    static JDVBPlayer mDvbPlayer;
    OnSearchInfoListener mOnSearchInfoListener;
    OnTaskInfoListener mOnTaskInfoListener;
    
    public static boolean mStrictMode = false;

    public static final int DVBPLAYER_ERROR = 0;
    public static final int DVBPLAYER_IDLE = 1 << 0;
    public static final int DVBPLAYER_INITIALIZED = 1 << 1;
    public static final int DVBPLAYER_PREPARED = 1 << 2;
    public static final int DVBPLAYER_STARTED = 1 << 3;
    public static final int DVBPLAYER_STOPPED = 1 << 4;

    /**
     * Tuner Index 0
     */
    public static final int TUNER_0 = 0;
    /**
     * Tuner Index 1
     */
    public static final int TUNER_1 = 1;
    /**
     * Tuner Index 2
     */
    public static final int TUNER_2 = 2;

    /**
     * 有信号.<br/>
     * Use with {@link CALLBACK_TUNER_SIGNAL}
     */
    public static final int TUNER_SIGNAL_ON = 0;
    /**
     * 无信号.<br/>
     * Use with {@link CALLBACK_TUNER_SIGNAL}
     */
    public static final int TUNER_SIGNAL_OFF = 1;
    /**
     * TVNOTIFY_MINEPG.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_MINEPG = 100;
    /**
     * TVNOTIFY_EPGCOMPLETE.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_EPGCOMPLETE = 101;
    /**
     * 信号状态. TVNOTIFY_TUNER_SIGNAL.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     * 
     * @see #TUNER_SIGNAL_ON
     * @see #TUNER_SIGNAL_OFF
     */
    public static final int CALLBACK_TUNER_SIGNAL = 102;
    /**
     * 通知更新DVBService(name,PMT and A/V pid).TVNOTIFY_UPDATE_SERVICE.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_UPDATE_SERVICE = 103;
    /**
     * 通知更新DVBService(name,PMT and A/V pid).TVNOTIFY_UPDATE_PROGRAM.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_UPDATE_PROGRAM = 104;
    /**
     * .<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_INIT = 105;
    /**
     * 不能正常收看节目的提示. TVNOTIFY_BUYMSG.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_BUYMSG = 200;
    /**
     * 显示/隐藏OSD信息. TVNOTIFY_OSD.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_OSD = 201;
    /**
     * 指纹显示. TVNOTIFY_SHOW_FINGERPRINT.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_SHOW_FINGERPRINT = 202;
    /**
     * 进度显示. TVNOTIFY_SHOW_PROGRESSSTRIP.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_SHOW_PROGRESSSTRIP = 203;
    /**
     * 新邮件通知消息. TVNOTIFY_MAIL_NOTIFY.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_MAIL_NOTIFY = 204;
    /**
     * 实时购买IPP. TVNOTIFY_BUYIPP.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_BUYIPP = 205;
    /**
     * 应急广播. TVNOTIFY_GENCYBROADCAST.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_GENCYBROADCAST = 206;
    /**
     * 子母卡配对消息. TVNOTIFY_MOTHER_CARDPAIR.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_MOTHER_CARDPAIR = 207;
    /**
     * 单频点区域锁定. TVNOTIFY_AREAlOCK.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_AREAlOCK = 208;
    /**
     * 通知区域信息更新,重新搜台(数码). TVNOTIFY_UPDATE_AREA.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_UPDATE_AREA = 209;
    /**
     * 授权改变. TVNOTIFY_ENTITLE_CHANGE.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_ENTITLE_CHANGE = 210;
    /**
     * 反授权通知消息. TVNOTIFY_DETITLE.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_DETITLE = 211;
    /**
     * DTVService崩溃. TVNOTIFY_SERVICE_DIED.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_SERVICE_DIED = 302;
    /**
     * Search: new Channels. TVNOTIFY_DVBSERVICE.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_RECEIVE_CHANNELS = 400;
    /**
     * Search: progress change. TVNOTIFY_PROGRESS.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_PROGRESS_CHANGE = 401;
    /**
     * Search: current transponder info. TVNOTIFY_TUNERINFO.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_TRANSPONDER_INFO = 402;
    /**
     * Search: current signal info. TVNOTIFY_SIGNALINFO.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_SIGNAL_INFO = 403;
    /**
     * Search: search end. TVNOTIFY_DVBALLSERVICE.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_SEARCH_END = 404;
    public static final int CALLBACK_FOR_SEARCH_FIRST = CALLBACK_RECEIVE_CHANNELS;
    public static final int CALLBACK_FOR_SEARCH_LAST = CALLBACK_SEARCH_END;
    /**
     * TVNOTIFY_ServiceType.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_ServiceType = 405;
    /**
     * TVNOTIFY_EPGCOMPLETE_SH.<br/>
     * Use with {@link JDVBPlayer.OnMonitorListener}.
     */
    public static final int CALLBACK_EPGCOMPLETE_SH = 406;
    /**
     * @param type
     */
    public static final int CALLBACK_EPGINFO_NET = 407;
    /**
     * update local data callback
     */
    public static final int CALLBACK_LOCALDATA_UPDATE_PROGRESS = 620;
    public static final int CALLBACK_LOCALDATA_UPDATE_STATUS = 621;
    public static final int CALLBACK_LOCALDATA_UPDATE_VERSION_FOUND = 622;

    /**
     * 立体声<br/>
     * Use with {@link #setSoundTrack(int, int)}
     */
    public static final int SOUNDTRACK_STEREO = 0;
    /**
     * 左声道<br/>
     * Use with {@link #setSoundTrack(int, int)}
     */
    public static final int SOUNDTRACK_LEFT = 1;
    /**
     * 右声道<br/>
     * Use with {@link #setSoundTrack(int, int)}
     */
    public static final int SOUNDTRACK_RIGHT = 2;
    /**
     * 单声道<br/>
     * Use with {@link #setSoundTrack(int, int)}
     */
    public static final int SOUNDTRACK_MONO = 3;
    
    /**
     * STVMODE_MANUAL, 手动搜索模式.<br/>
     * Use with {@link #startSearchChannel}
     */
    public static final int SEARCHMODE_MANUAL = 0;
    /**
     * STVMODE_FULL, 全频搜索模式.<br/>
     * Use with {@link #startSearchChannel}
     */
    public static final int SEARCHMODE_FULL = 1;
    /**
     * STVMODE_NIT , NIT搜索模式(NIT+PAT+PMT+SDT+CAT).<br/>
     * Use with {@link #startSearchChannel}
     */
    public static final int SEARCHMODE_FAST = 2;
    /**
     * STVMODE_NIT_S, NIT搜索模式简化版(NIT+SDT).<br/>
     * Use with {@link #startSearchChannel}
     */
    public static final int SEARCHMODE_FAST_S = 3;
    /**
     * STVMODE_MONITOR_NET, 网络方式.<br/>
     * Use with {@link #startSearchChannel}
     */
    public static final int SEARCHMODE_NET = 4;
    /**
     * STVMODE_MONITOR_PMT, 手动方式(按SID取VPID+APID)并监视NIT表的版本.<br/>
     * Use with {@link #startSearchChannel}
     */
    public static final int SEARCHMODE_MONITOR_PMT = 5;

    /**
     * 返回值：开始下载</Br> Use with {@link #startSearchEPG}
     */
    public static final int EPG_SEARCH_BEGIN = 0;
    /**
     * 返回值：主频点参数错误</Br> Use with {@link #startSearchEPG}
     */
    public static final int EPG_SEARCH_MAIN_FREQ_ERROR = -1;
    /**
     * 返回值：主频点锁频失败</Br> Use with {@link #startSearchEPG}
     */
    public static final int EPG_SEARCH_TUNER_ERROR = -2;
    /**
     * 返回值：未发现新版本</Br> Use with {@link #startSearchEPG}
     */
    public static final int EPG_SEARCH_ALLREADY_EXIST = -3;
    /**
     * 返回值：下载中</Br> Use with {@link #startSearchEPG}
     */
    public static final int EPG_SEARCH_IS_SEARCHING = -4;
    /**
     * 返回值：未知错误</Br> Use with {@link #startSearchEPG}
     */
    public static final int EPG_SEARCH_ERROR = -5;

    /**
     * 画面比例: 自适应</Br> Use with {@link #setVideoAspectRatio(int, int)}
     */
    public static final int VIDEOASPECTRATION_NORMAL = 0;
    /**
     * 画面比例: 4:3</Br> Use with {@link #setVideoAspectRatio(int, int)}
     */
    public static final int VIDEOASPECTRATION_4TO3 = 1;
    /**
     * 画面比例: 16:9</Br> Use with {@link #setVideoAspectRatio(int, int)}
     */
    public static final int VIDEOASPECTRATION_16TO9 = 2;

    /*---------- CAS 提示信息---------*/
    /** 取消当前的显示 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE = 0x00;
    /** 无法识别卡 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_BADCARD_TYPE = 0x01;
    /** 智能卡过期,请更换新卡 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_EXPICARD_TYPE = 0x02;
    /** 加扰节目,请插入智能卡 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_INSERTCARD_TYPE = 0x03;
    /** 卡中不存在节目运营商 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_NOOPER_TYPE = 0x04;
    /** 条件禁播 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_BLACKOUT_TYPE = 0x05;
    /** 当前时段被设定为不能观看 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_OUTWORKTIME_TYPE = 0x06;
    /** 节目级别高于设定的观看级别 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_WATCHLEVEL_TYPE = 0x07;
    /** 智能卡与本机顶盒不对应 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_PAIRING_TYPE = 0x08;
    /** 没有授权 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_NOENTITLE_TYPE = 0x09;
    /** 节目解密失败 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_DECRYPTFAIL_TYPE = 0x0A;
    /** 卡内金额不足 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_NOMONEY_TYPE = 0x0B;
    /** 区域不正确 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_ERRREGION_TYPE = 0x0C;
    /** 子卡需要和母卡对应,请插入母卡 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_NEEDFEED_TYPE = 0x0D;
    /** 智能卡校验失败,请联系运营商 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_ERRCARD_TYPE = 0x0E;
    /** 智能卡升级中,请不要拔卡或者关机 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_UPDATE_TYPE = 0x0F;
    /** 请升级智能卡 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_LOWCARDVER_TYPE = 0x10;
    /** 请勿频繁切换频道 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_VIEWLOCK_TYPE = 0x11;
    /** 智能卡暂时休眠请分钟后重新开机 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_MAXRESTART_TYPE = 0x12;
    /** 智能卡已冻结,请联系运营商 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_FREEZE_TYPE = 0x13;
    /** 智能卡已暂停请回传收视记录给运营商 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_CALLBACK_TYPE = 0x14;
    /** 请重启机顶盒 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_STBLOCKED_TYPE = 0x20;
    /** 机顶盒被冻结 */
    public static final int NOTIFICATION_ACTION_CA_MESSAGE_STBFREEZE_TYPE = 0x21;

    /**
     * CA相关操作，状态码 </Br> 成功
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_OK = 0x00;
    /**
     * CA相关操作，状态码 </Br> 未知错误
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_UNKNOWN = 0x01;
    /**
     * CA相关操作，状态码 </Br> 指针无效
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_POINTER_INVALID = 0x02;
    /**
     * CA相关操作，状态码 </Br> 智能卡无效
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_CARD_INVALID = 0x03;
    /**
     * CA相关操作，状态码 </Br> PIN码无效
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_PIN_INVALID = 0x04;
    /**
     * CA相关操作，状态码 </Br> 所给的空间不足
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_DATASPACE_SMALL = 0x06;
    /**
     * CA相关操作，状态码 </Br> 智能卡已经对应别的机顶盒
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_CARD_PAIROTHER = 0x07;
    /**
     * CA相关操作，状态码 </Br> 没有找到所要的数据
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_DATA_NOT_FIND = 0x08;
    /**
     * CA相关操作，状态码 </Br> 要购买的节目状态无效
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_PROG_STATUS_INVALID = 0x09;
    /**
     * CA相关操作，状态码 </Br> 智能卡没有空间存放购买的节目
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_CARD_NO_ROOM = 0x0A;
    /**
     * CA相关操作，状态码 </Br> 设定的工作时段无效
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_WORKTIME_INVALID = 0x0B;
    /**
     * CA相关操作，状态码 </Br> IPPV节目不能被删除
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_IPPV_CANNTDEL = 0x0C;
    /**
     * CA相关操作，状态码 </Br> 智能卡没有对应任何的机顶盒
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_CARD_NOPAIR = 0x0D;
    /**
     * CA相关操作，状态码 </Br> 设定的观看级别无效
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_WATCHRATING_INVALID = 0x0E;
    /**
     * CA相关操作，状态码 </Br> 当前智能卡不支持此功能
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_CARD_NOTSUPPORT = 0x0F;
    /**
     * CA相关操作，状态码 </Br> 数据错误，智能卡拒绝
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_DATA_ERROR = 0x10;
    /**
     * CA相关操作，状态码 </Br> 喂养时间未到，子卡不能被喂养
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_FEEDTIME_NOT_ARRIVE = 0x11;
    /**
     * CA相关操作，状态码 </Br> 子母卡喂养失败，插入智能卡类型错误
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_CARD_TYPEERROR = 0x12;
    /**
     * CA相关操作，状态码 </Br> 发卡cas指令执行失败
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_CAS_FAILED = 0x20;
    /**
     * CA相关操作，状态码 </Br> 发卡运营商指令执行失败
     * 
     * @see CAControlException
     * @see CAControlException#getErrorCode()
     */
    public static final int CDCA_RC_OPER_FAILED = 0x21;

    JDVBPlayer() {
    }

    /**
     * 设置播放器类型 </Br> 1. {@link PlayerType#DTV} 集成DTVService 2.
     * {@link PlayerType#COMMON} 适配通用播放器
     * 
     * @param type one of {@link PlayerType#DTV},{@link PlayerType#COMMON}
     */
    public static void setPlayerType(PlayerType type) {
        if (mPlayerType != type) {
            mPlayerType = type;
            if (PlayerType.DTV == type) {
                if (mDvbPlayer == null && !(mDvbPlayer instanceof JDVBPlayerDTVImpl)) {
                    mDvbPlayer = new JDVBPlayerDTVImpl();
                }
            } else if (PlayerType.COMMON == type) {
                if (mDvbPlayer == null && !(mDvbPlayer instanceof JDVBPlayerCOMMONImpl)) {
                    mDvbPlayer = new JDVBPlayerCOMMONImpl();
                }
            }
        }
    }
    
    public PlayerType getPlayerType() {
        return mPlayerType;
    }

    /**
     * 获取播放器实例，在{@link #setPlayerType(PlayerType)} 之后调用。
     * 
     * @return
     * @throws IllegalStateException 如果没有设置过{@link #setPlayerType(PlayerType)}
     *             则抛出
     */
    public static final JDVBPlayer getInstance() throws IllegalStateException {
        if (mPlayerType == null) {
            Log.e(TAG, "setPlayerType must call before getInstance ");
            throw new IllegalStateException();
        }
        return mDvbPlayer;
    }

    /**
     * 注册一个回调，搜索频道时会被调用。
     * 
     * @param lis
     * @see OnSearchInfoListener
     */
    public void setOnSearchInfoListener(OnSearchInfoListener lis) {
        this.mOnSearchInfoListener = lis;
    }
    
    public void setOnTaskInfoListener(OnTaskInfoListener lis) {
        this.mOnTaskInfoListener = lis;
    }

    String dvbPlayerState2String(int state) {
        String ret = null;
        switch (state) {
            case DVBPLAYER_ERROR:
                ret = "DVBPLAYER_ERROR";
                break;
            case DVBPLAYER_IDLE:
                ret = "DVBPLAYER_IDLE";
                break;
            case DVBPLAYER_INITIALIZED:
                ret = "DVBPLAYER_INITIALIZED";
                break;
            case DVBPLAYER_PREPARED:
                ret = "DVBPLAYER_PREPARED";
                break;
            case DVBPLAYER_STARTED:
                ret = "DVBPLAYER_STARTED";
                break;
            case DVBPLAYER_STOPPED:
                ret = "DVBPLAYER_STOPPED";
                break;
            default:// will never happen
                break;
        }
        return ret;
    }

    String epgSearchResule2String(int result) {
        String ret = result + "-";
        switch (result) {
            case JDVBPlayer.EPG_SEARCH_BEGIN:
                ret += "EPG_SEARCH_BEGIN";
                break;
            case JDVBPlayer.EPG_SEARCH_MAIN_FREQ_ERROR:
                ret += "EPG_SEARCH_MAIN_FREQ_ERROR";
                break;
            case JDVBPlayer.EPG_SEARCH_TUNER_ERROR:
                ret += "EPG_SEARCH_TUNER_ERROR";
                break;
            case JDVBPlayer.EPG_SEARCH_ALLREADY_EXIST:
                ret += "EPG_SEARCH_ALLREADY_EXIST";
                break;
            case JDVBPlayer.EPG_SEARCH_IS_SEARCHING:
                ret += "EPG_SEARCH_IS_SEARCHING";
                break;
            case JDVBPlayer.EPG_SEARCH_ERROR:
                ret += "EPG_SEARCH_ERROR";
                break;
        }
        return ret;
    }

    /**
     * 初始化播放器
     * @return
     */
    public abstract int init();
    
    public abstract int uninit();
    
    public abstract void setDongleFileDesc(int desc);

    /**
     * 准备播放，初始化底层监测服务，第一次播放需要调用，{@link #stop(int)}后再次播放需要调用
     * 
     * @param tunerId
     * @return
     * @throws IllegalStateException
     */
    public abstract boolean prepare(int tunerId) throws IllegalStateException;

    /**
     * 设置播放频道
     * 
     * @param tunerId
     * @param channel
     * @throws IllegalStateException 如果不是播放状态调用，则抛出异常
     */
    public abstract void setChannel(int tunerId, DvbService channel) throws IllegalStateException;

    /**
     * 停止播放
     * 
     * @param tunerId
     * @throws IllegalStateException 如果不是播放状态调用，则抛出异常
     */
    public abstract void stop(int tunerId) throws IllegalStateException;

    /**
     * 获取是否在播放状态
     * 
     * @return
     */
    public abstract boolean isPlaying();

    public abstract void addOnMonitorListener(OnMonitorListener lis);

    public abstract void removeOnMonitorListener(OnMonitorListener lis);

    /**
     * 获取TS流EPG
     * 
     * @param tunerId
     * @param serviceId
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 如果数量为0或获取失败，返回NULL.
     */
    public abstract ArrayList<EpgEvent> getEpgDataByDuration(int tunerId, int serviceId, long startTime, long endTime);

    /**
     * 修改频道信息
     * 
     * @param tvIndex
     * @param channel
     * @return
     */
    public abstract int updateChannel(int tvIndex, DvbService channel);

    /**
     * TUNER_SIGNAL_ON 有信号</Br> TUNER_SIGNAL_OFF 无信号</Br> SERVICE_ERROR 失败</Br>
     * 
     * @see #TUNER_SIGNAL_ON
     * @see #TUNER_SIGNAL_OFF
     */
    public abstract int getTunerSignalStatus(int tunerId);

    public abstract boolean setKeepLastFrameEnable(int tunerId, boolean enable);

    /**
     * 获取所有频道
     * 
     * @return
     */
    public abstract ArrayList<DvbService> getAllChannel();

    /**
     * 删除所有频道
     * 
     * @return
     */
    public abstract boolean deleteAllChannel();

    /**
     * 获取频道数量
     * 
     * @return
     */
    public abstract int getChannelCount();

    /**
     * 获取TS流中本地频道分类
     * 
     * @return
     */
    public abstract ArrayList<ProgramCatalog> getAllChannelType();

    /**
     * 根据频道分类获取频道
     * 
     * @param cata
     * @return
     */
    public abstract ArrayList<DvbService> getChannelByChannelType(ProgramCatalog cata);

    public abstract DvbService getChannelByIndex(int channelIndex);

    /**
     * 根据频道号获取频道
     * 
     * @param number
     * @return
     */
    public abstract DvbService getChannelByNum(int number);

    /**
     * 根据频道号获取频道
     * 
     * @param number
     * @param type 过滤条件,serviceType
     * @return
     */

    public abstract DvbService getChannelByNum(int number, int type);

    /**
     * 获取当前频道
     * 
     * @return
     */
    public abstract DvbService getCurrentChannel();

    /**
     * 获取频道在DTVService中角标
     * 
     * @param number
     * @return
     */
    public abstract int getIndexByChannelNum(int number);

    public abstract int getNextChannelIndex(int index, int filter);

    /**
     * 设置运营商ID
     * 
     * @param operatorCode
     * @return
     */
    public abstract int setOperatorCode(String operatorCode);

    /**
     * 开始搜索频道
     * 
     * @param searchMode one of {@link #SEARCHMODE_MANUAL},
     *            {@link #SEARCHMODE_FULL}, {@link #SEARCHMODE_FAST},
     *            {@link #SEARCHMODE_FAST_S}, {@link #SEARCHMODE_MONITOR_PMT}
     * @param transponder 频点信息：频率 符号率 调制
     */
    public abstract int startSearchChannel(int searchMode, Transponder transponder);

    /**
     * 停止搜索频道
     * 
     * @param isSave 是否保存已搜索到频道
     * @return
     */
    public abstract int stopSearchChannel(boolean isSave);

    /**
     * 开始下载EPG
     * 
     * @param tunerId
     * @param param
     * @param type
     * @return one of {@link #EPG_SEARCH_BEGIN},
     *         {@link #EPG_SEARCH_MAIN_FREQ_ERROR},
     *         {@link #EPG_SEARCH_TUNER_ERROR},
     *         {@link #EPG_SEARCH_ALLREADY_EXIST},
     *         {@link #EPG_SEARCH_IS_SEARCHING}, {@link #EPG_SEARCH_ERROR}
     */
    public abstract int startSearchEPG(int tunerId, Transponder param);

    /**
     * 停止搜索
     * 
     * @return
     */
    public abstract int stopSearchEPG();

    /**
     * 设置EPG模式
     * 
     * @param mode
     * @return
     */
    public abstract int setEPGSourceMode(boolean mode);

    /**
     * 获取声道类型
     * 
     * @param tunerId
     * @return
     * @see #SOUNDTRACK_STEREO
     * @see #SOUNDTRACK_LEFT
     * @see #SOUNDTRACK_RIGHT
     * @see #SOUNDTRACK_MONO
     */
    public abstract int getSoundTrack(int tunerId);

    /**
     * 设置播放器声道类型
     * 
     * @param tunerId
     * @param Mode
     * @return
     * @see #SOUNDTRACK_STEREO
     * @see #SOUNDTRACK_LEFT
     * @see #SOUNDTRACK_RIGHT
     * @see #SOUNDTRACK_MONO
     */
    public abstract int setSoundTrack(int tunerId, int Mode);

    /**
     * 设置画面比例
     * 
     * @param tunerId TunerId
     * @param ratio 比例 one of {@link #VIDEOASPECTRATION_NORMAL},
     *            {@link #VIDEOASPECTRATION_NORMAL},
     *            {@link #VIDEOASPECTRATION_NORMAL}
     * @return
     */
    public abstract int setVideoAspectRatio(int tunerId, int ratio);

    /**
     * 设置播放窗口位置
     * 
     * @param tunerId
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    public abstract int setVideoWindow(int tunerId, int left, int top, int right, int bottom);

    /**
     * 修改密码
     * 
     * @param oldPwd
     * @param newPwd
     * @throws CAControlException
     */
    public abstract void changeCAcardPassword(String oldPwd, String newPwd) throws CAControlException;

    /**
     * @param id Email-Id
     * @return true:成功 false:失败
     */
    public abstract boolean deleteEmailById(int id);

    /**
     * 获取授权信息列表
     * 
     * @param operID 运营商ID
     * @throws CAControlException
     */
    public abstract Vector<LicenseInfo> getAuthorization(int operID) throws CAControlException;

    /**
     * 获取卡序列号，成功返回序号字符串，失败返回空字符串
     * 
     * @return CA卡序列号
     */
    public abstract String getCacardSN();

    public abstract EmailContent getEmailContentById(int emailId);

    public abstract ArrayList<EmailHead> getAllEmailHead();

    /**
     * 获取运营商列表
     * @return
     * @throws CAControlException
     */
    public abstract Vector<Integer> getOperatorID() throws CAControlException;

    /**
     * 获取邮件数量
     * @return 邮件总数
     */
    public abstract int getEmailCount();

    /**
     * 获取观看等级
     * @return
     */
    public abstract int getWatchLevel();

    /**
     * 设置观看等级
     * @param psd
     * @param level
     * @throws CAControlException
     */
    public abstract void setWatchLeve(String psd, int level) throws CAControlException;

    /**
     * 获取智能卡工作时间
     * @return
     * @throws CAControlException
     */
    public abstract WatchTime getValidWatchingTime() throws CAControlException;

    /**
     * 设置智能卡工作时间
     * @param pwd
     * @param watchTime
     * @throws CAControlException
     */
    public abstract void setValidWatchingTime(String pwd, WatchTime watchTime) throws CAControlException;

    public abstract int getStateByType(int tunerId, int type);
    
    public abstract int startTask(TaskInfo task);
    public abstract int cancleTask(TaskInfo task);
    public abstract int getTaskStatus(TaskInfo task);
    public abstract int getTaskProgress(TaskInfo task);
    public abstract String getDomainCode();
    
    public abstract int getLocalPackVersionCode(String url);
    public abstract String getLocalPackVersionName(String url);
    
    public static void setStrictMode(boolean mode) {
        mStrictMode = mode;
    }

    public static String getMonitorCallbackName(int type) {
        StringBuilder builder = new StringBuilder();
        switch (type) {
            case CALLBACK_MINEPG:
                builder.append(CALLBACK_MINEPG);
                builder.append("-");
                builder.append("CALLBACK_MINEPG");
                break;
            case CALLBACK_EPGCOMPLETE:
                builder.append(CALLBACK_EPGCOMPLETE);
                builder.append("-");
                builder.append("CALLBACK_EPGCOMPLETE");
                break;
            case CALLBACK_TUNER_SIGNAL:
                builder.append(CALLBACK_TUNER_SIGNAL);
                builder.append("-");
                builder.append("CALLBACK_TUNER_SIGNAL");
                break;
            case CALLBACK_UPDATE_SERVICE:
                builder.append(CALLBACK_UPDATE_SERVICE);
                builder.append("-");
                builder.append("CALLBACK_UPDATE_SERVICE");
                break;
            case CALLBACK_UPDATE_PROGRAM:
                builder.append(CALLBACK_UPDATE_PROGRAM);
                builder.append("-");
                builder.append("CALLBACK_UPDATE_PROGRAM");
                break;
            case CALLBACK_INIT:
                builder.append(CALLBACK_INIT);
                builder.append("-");
                builder.append("CALLBACK_INIT");
                break;
            case CALLBACK_BUYMSG:
                builder.append(CALLBACK_BUYMSG);
                builder.append("-");
                builder.append("CALLBACK_BUYMSG");
                break;
            case CALLBACK_OSD:
                builder.append(CALLBACK_OSD);
                builder.append("-");
                builder.append("CALLBACK_OSD");
                break;
            case CALLBACK_SHOW_FINGERPRINT:
                builder.append(CALLBACK_SHOW_FINGERPRINT);
                builder.append("-");
                builder.append("CALLBACK_SHOW_FINGERPRINT");
                break;
            case CALLBACK_SHOW_PROGRESSSTRIP:
                builder.append(CALLBACK_SHOW_PROGRESSSTRIP);
                builder.append("-");
                builder.append("CALLBACK_SHOW_PROGRESSSTRIP");
                break;
            case CALLBACK_MAIL_NOTIFY:
                builder.append(CALLBACK_MAIL_NOTIFY);
                builder.append("-");
                builder.append("CALLBACK_MAIL_NOTIFY");
                break;
            case CALLBACK_BUYIPP:
                builder.append(CALLBACK_BUYIPP);
                builder.append("-");
                builder.append("CALLBACK_BUYIPP");
                break;
            case CALLBACK_GENCYBROADCAST:
                builder.append(CALLBACK_GENCYBROADCAST);
                builder.append("-");
                builder.append("CALLBACK_GENCYBROADCAST");
                break;
            case CALLBACK_MOTHER_CARDPAIR:
                builder.append(CALLBACK_MOTHER_CARDPAIR);
                builder.append("-");
                builder.append("CALLBACK_MOTHER_CARDPAIR");
                break;
            case CALLBACK_AREAlOCK:
                builder.append(CALLBACK_AREAlOCK);
                builder.append("-");
                builder.append("CALLBACK_AREAlOCK");
                break;
            case CALLBACK_UPDATE_AREA:
                builder.append(CALLBACK_UPDATE_AREA);
                builder.append("-");
                builder.append("CALLBACK_UPDATE_AREA");
                break;
            case CALLBACK_ENTITLE_CHANGE:
                builder.append(CALLBACK_ENTITLE_CHANGE);
                builder.append("-");
                builder.append("CALLBACK_ENTITLE_CHANGE");
                break;
            case CALLBACK_DETITLE:
                builder.append(CALLBACK_DETITLE);
                builder.append("-");
                builder.append("CALLBACK_DETITLE");
                break;
            case CALLBACK_SERVICE_DIED:
                builder.append(CALLBACK_SERVICE_DIED);
                builder.append("-");
                builder.append("CALLBACK_SERVICE_DIED");
                break;
            case CALLBACK_RECEIVE_CHANNELS:
                builder.append(CALLBACK_RECEIVE_CHANNELS);
                builder.append("-");
                builder.append("CALLBACK_RECEIVE_CHANNELS");
                break;
            case CALLBACK_PROGRESS_CHANGE:
                builder.append(CALLBACK_PROGRESS_CHANGE);
                builder.append("-");
                builder.append("CALLBACK_PROGRESS_CHANGE");
                break;
            case CALLBACK_TRANSPONDER_INFO:
                builder.append(CALLBACK_TRANSPONDER_INFO);
                builder.append("-");
                builder.append("CALLBACK_TRANSPONDER_INFO");
                break;
            case CALLBACK_SIGNAL_INFO:
                builder.append(CALLBACK_SIGNAL_INFO);
                builder.append("-");
                builder.append("CALLBACK_SIGNAL_INFO");
                break;
            case CALLBACK_SEARCH_END:
                builder.append(CALLBACK_SEARCH_END);
                builder.append("-");
                builder.append("CALLBACK_SEARCH_END");
                break;
            case CALLBACK_ServiceType:
                builder.append(CALLBACK_ServiceType);
                builder.append("-");
                builder.append("CALLBACK_ServiceType");
                break;
            case CALLBACK_EPGCOMPLETE_SH:
                builder.append(CALLBACK_EPGCOMPLETE_SH);
                builder.append("-");
                builder.append("CALLBACK_EPGCOMPLETE_SH");
                break;
            case CALLBACK_EPGINFO_NET:
                builder.append(CALLBACK_EPGINFO_NET);
                builder.append("-");
                builder.append("CALLBACK_EPGINFO_NET");
                break;
            case CALLBACK_LOCALDATA_UPDATE_PROGRESS:
                builder.append(CALLBACK_LOCALDATA_UPDATE_PROGRESS);
                builder.append("-");
                builder.append("CALLBACK_LOCALDATA_UPDATE_PROGRESS");
                break;
            case CALLBACK_LOCALDATA_UPDATE_STATUS:
                builder.append(CALLBACK_LOCALDATA_UPDATE_STATUS);
                builder.append("-");
                builder.append("CALLBACK_LOCALDATA_UPDATE_STATUS");
                break;
            case CALLBACK_LOCALDATA_UPDATE_VERSION_FOUND:
                builder.append(CALLBACK_LOCALDATA_UPDATE_VERSION_FOUND);
                builder.append("-");
                builder.append("CALLBACK_LOCALDATA_UPDATE_VERSION_FOUND");
                break;
            default:
                break;
        }
        return builder.toString();
    }

}
