/**
 * =====================================================================
 *
 * @file  StreamMediaPreview.java
 * @Module Name   com.joysee.dvb.sm
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-6-6
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
 * benz          2014-6-6           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.vod;

public class VodRelatedItemInfo {

    private int vId;
    private String name;
    private String posterUrl;
    private String SourceId;
    private int errorNo;
    private String errorMsg;

    public int getvId() {
        return vId;
    }

    public String getName() {
        return name;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getSourceId() {
        return SourceId;
    }

    public void setvId(int id) {
        this.vId = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosterUrl(String imageUrl) {
        this.posterUrl = imageUrl;
    }

    public void setSourceId(String sourceId) {
        SourceId = sourceId;
    }

    public int getErrorNo() {
        return errorNo;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorNo(int errorNo) {
        this.errorNo = errorNo;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    
}
