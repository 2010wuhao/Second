
package com.joysee.adtv.logic;

class TimeShiftJniManager {
    public interface OnMonitorListener {
        void onMonitor(int monitorType, Object message);
    }

    public static final short PLAY_FORWARD = 0;// 快进

    public static final short PLAY_BACK = -1;// 快退;

    private static TimeShiftJniManager mTimeShiftManager;

    private static OnMonitorListener onMonitorListener;

    public synchronized static void callBack(final int monitorType, final Object message) {
        if (onMonitorListener != null) {
            onMonitorListener.onMonitor(monitorType, message);
        }
    }

    // private static Handler mHandler = new Handler();
    public static TimeShiftJniManager getInstance() {
        if (mTimeShiftManager == null) {
            synchronized (TimeShiftJniManager.class) {
                if (mTimeShiftManager == null) {
                    mTimeShiftManager = new TimeShiftJniManager();
                    return mTimeShiftManager;
                } else {
                    return mTimeShiftManager;
                }
            }
        } else {
            return mTimeShiftManager;
        }
    }

    private TimeShiftJniManager() {
    }

    /**
     * 快进，快退
     * 
     * @param direction 0快进 -1快退
     * @return
     */
    public native int fastPlay(short direction);

    /**
     * 取时移相关的信息，参数待定
     * 
     * @param url
     * @return
     */
    public native String getMediaInfo();

    /**
     * 获得播放状态,异步方法，通过消息获得返回值
     * 
     * @return 当前播放状态
     */
    public native int getPlayState(int tunerId);

    public native int playNext();

    public native int playPrevious();

    // private static Handler mHandler;
    public void setOnMonitorListener(OnMonitorListener lis) {
        onMonitorListener = lis;
    }

    /**
     * 暂停时移
     * 
     * @return
     */
    public native int timeShiftPause();

    /**
     * 时移播放接口,调用时不需要stop正在播放的节目，底层会处理。
     * 
     * @param url vod_url = "timeshift://mainfreq=682000,serviceid=100";
     * @return
     */
    public native int timeShiftPlay(int tunerId, String url);

    /**
     * 恢复播放
     * 
     * @return
     */
    public native int timeShiftResumePlay();

    /**
     * 跳转到possition位置
     * 
     * @param possition
     * @return
     */
    public native int timeShiftSeekTo(String position);

    public native int timeShiftStop(int tunerId);

}
