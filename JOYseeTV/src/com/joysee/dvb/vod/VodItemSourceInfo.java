/**
 * =====================================================================
 *
 * @file  SourceInfo.java
 * @Module Name   com.joysee.dvb.sm
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-6-9
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
 * benz          2014-6-9           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.vod;

public class VodItemSourceInfo {

    public String vid;
    public String number;
    public String sourceId;
    public String sourceName;
    public String sourceHomePage;
    public int sourceIconRes;
    public String[] guoYuUrls;
    public String[] enUrls;
    public String[] otUrls;

    public String getVid() {
        return vid;
    }

    public String getNumber() {
        return number;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceHomePage() {
        return sourceHomePage;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setSourceHomePage(String sourceHomePage) {
        this.sourceHomePage = sourceHomePage;
    }

    public String[] getGuoYuUrls() {
        return guoYuUrls;
    }

    public String[] getEnUrls() {
        return enUrls;
    }

    public String[] getOtUrls() {
        return otUrls;
    }

    public void setGuoYuUrls(String[] guoYuUrls) {
        this.guoYuUrls = guoYuUrls;
    }

    public void setEnUrls(String[] enUrls) {
        this.enUrls = enUrls;
    }

    public void setOtUrls(String[] otUrls) {
        this.otUrls = otUrls;
    }

    public int getSourceIconRes() {
        return sourceIconRes;
    }

    public void setSourceIconRes(int sourceIconRes) {
        this.sourceIconRes = sourceIconRes;
    }
    
}
