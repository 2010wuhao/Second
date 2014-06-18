/**
 * =====================================================================
 *
 * @file  EpgEvent.java
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
 * TS EPG包装类
 */
public class EpgEvent {
    private int id;
    private int serviceId;/* 服务id，每个频道有唯一的serviceId */
    private String name; /* 节目名称 */
    private int channelNumber;
    private long start_time; /* 节目开始时间 */
    private long end_time; /* 节目结束时间 */
    private String description; /* 节目描述 */

    public EpgEvent() {
    }

    /**
     * @param id 事件id.
     * @param programName 节目名.
     * @param startTime 开始时间.
     * @param endTime 结束时间.
     */
    public EpgEvent(int id, String programName, long startTime, long endTime) {
        this.id = id;
        this.name = programName;
        this.start_time = startTime;
        this.end_time = endTime;
    }

    /**
     * 获取频道号
     * @return
     */
    public int getChannelNumber() {
        return channelNumber;
    }

    /**
     * 获取节目结束时间，单位秒
     * @return
     */
    public long getEndTime() {
        return end_time;
    }

    public int getId() {
        return id;
    }

    public String getProgramDescription() {
        return description;
    }

    public String getProgramName() {
        return name;
    }

    public int getServiceId() {
        return serviceId;
    }

    /**
     * 获取节目开始时间，单位秒
     * @return
     */
    public long getStartTime() {
        return start_time;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public void setEndTime(long endTime) {
        this.end_time = endTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProgramDescription(String programDescription) {
        this.description = programDescription;
    }

    public void setProgramName(String programName) {
        this.name = programName;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public void setStartTime(long startTime) {
        this.start_time = startTime;
    }

    @Override
    public String toString() {
        return "EpgEvent [id=" + id + ", serviceId=" + serviceId + ", name=" + name + ", channelNumber=" + channelNumber + ", start_time="
                + start_time + ", end_time=" + end_time + ", description=" + description + "]";
    }

}
