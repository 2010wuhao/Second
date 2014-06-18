/**
 * =====================================================================
 *
 * @file  MiniEpgNotify.java
 * @Module Name   com.joysee.adtv.logic.bean
 * @author songwenxuan
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月20日
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
 * songwenxuan          2014年3月20日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

/**
 * MiniEpg 用于承载播放频道时pf信息
 *
 */
public class MiniEpgNotify {

    String CurrentEventName; // 当前节目名称
    long CurrentEventStartTime; // 当前节目开始时间
    long CurrentEventEndTime; // 当前节目结束时间
    String NextEventName; // 后继节目名称
    long NextEventStartTime; // 后继节目开始时间
    long NextEventEndTime; // 后继节目结束时间
    int serviceId;

    public MiniEpgNotify() {

    }

    public MiniEpgNotify(String CurrentEventName, long CurrentEventStartTime,
            long CurrentEventEndTime,
            String NextEventName, long NextEventStartTime, long NextEventEndTime) {

        this.CurrentEventName = CurrentEventName;
        this.CurrentEventStartTime = CurrentEventStartTime;
        this.CurrentEventEndTime = CurrentEventEndTime;
        this.NextEventName = NextEventName;
        this.NextEventStartTime = NextEventStartTime;
        this.NextEventEndTime = NextEventEndTime;

    }

    public long getCurrentEventEndTime() {
        return CurrentEventEndTime;
    }

    public String getCurrentEventName() {
        return CurrentEventName;
    }

    public long getCurrentEventStartTime() {
        return CurrentEventStartTime;
    }

    public long getNextEventEndTime() {
        return NextEventEndTime;
    }

    public String getNextEventName() {
        return NextEventName;
    }

    public long getNextEventStartTime() {
        return NextEventStartTime;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setCurrentEventEndTime(long currentEventEndTime) {
        CurrentEventEndTime = currentEventEndTime;
    }

    public void setCurrentEventName(String currentEventName) {
        CurrentEventName = currentEventName;
    }

    public void setCurrentEventStartTime(long currentEventStartTime) {
        CurrentEventStartTime = currentEventStartTime;
    }

    public void setNextEventEndTime(long nextEventEndTime) {
        NextEventEndTime = nextEventEndTime;
    }

    public void setNextEventName(String nextEventName) {
        NextEventName = nextEventName;
    }

    public void setNextEventStartTime(long nextEventStartTime) {
        NextEventStartTime = nextEventStartTime;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "MiniEpgNotify [CurrentEventName=" + CurrentEventName
                + ", CurrentEventStartTime=" + CurrentEventStartTime
                + ", CurrentEventEndTime=" + CurrentEventEndTime
                + ", NextEventName=" + NextEventName + ", NextEventStartTime="
                + NextEventStartTime + ", NextEventEndTime=" + NextEventEndTime
                + ", serviceId=" + serviceId + "]";
    }
}
