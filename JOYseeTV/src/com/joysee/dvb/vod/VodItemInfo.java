/**
 * =====================================================================
 *
 * @file  VagVideo.java
 * @Module Name   com.joysee.dvb.vag
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-6-3
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
 * benz          2014-6-3           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.vod;

public class VodItemInfo {

    private int vId;
    private String name;
    private String year;
    private String actor;
    private String playType;
    private String typeCode;
    private String sourceId;
    private String sourceName;
    private String joyseeSourceId;
    private int totalEpisode;
    private int updatedEpisode;
    private String director;
    private String description;
    private String posterUrl;
    private String errorMsg;

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public String getJoyseeSourceId() {
        return joyseeSourceId;
    }

    public void setJoyseeSourceId(String joyseeSourceId) {
        this.joyseeSourceId = joyseeSourceId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getvId() {
        return vId;
    }

    public void setvId(int vId) {
        this.vId = vId;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public String getActor() {
        return actor;
    }

    public String getPlayType() {
        return playType;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getSourceId() {
        return sourceId;
    }

    public int getTotalEpisode() {
        return totalEpisode;
    }

    public int getUpdatedEpisode() {
        return updatedEpisode;
    }

    public String getDirector() {
        return director;
    }

    public String getDescription() {
        return description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setTotalEpisode(int totalCount) {
        this.totalEpisode = totalCount;
    }

    public void setUpdatedEpisode(int updatedCount) {
        this.updatedEpisode = updatedCount;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}
