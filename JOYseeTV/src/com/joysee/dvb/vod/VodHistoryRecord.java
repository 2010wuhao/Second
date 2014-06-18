/**
 * =====================================================================
 *
 * @file  HistoryRecord.java
 * @Module Name   com.joysee.dvb.sm
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-6-12
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
 * benz          2014-6-12           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.vod;

public class VodHistoryRecord {

    public int vid;
    public String name;
    public String sourceId;
    public String sourceName;
    public String joyseeSourceId;
    public int date;
    public int offset;
    public int duration;
    public int episode;
    public String poster;
    public String clearLevel;

    public int getVid() {
        return vid;
    }

    public String getName() {
        return name;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getJoyseeSourceId() {
        return joyseeSourceId;
    }

    public int getDate() {
        return date;
    }

    public int getOffset() {
        return offset;
    }

    public int getEpisode() {
        return episode;
    }

    public String getPoster() {
        return poster;
    }

    public String getClearLevel() {
        return clearLevel;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setJoyseeSourceId(String joyseeSourceId) {
        this.joyseeSourceId = joyseeSourceId;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public void setClearLevel(String clearLevel) {
        this.clearLevel = clearLevel;
    }
    
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
