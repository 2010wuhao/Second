/**
 * =====================================================================
 *
 * @file  WatchTime.java
 * @Module Name   com.joysee.adtv.logic.bean
 * @author wuhao
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
 * wuhao          2014年3月20日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

public class WatchTime {
    public int startHour;
    public int endHour;
    public int startMin;
    public int endMin;
    public int startSec;
    public int endSec;

    public WatchTime() {
    }

    public WatchTime(int startHour, int startMin, int startSec, int endHour, int endMin, int endSec) {
        this.startHour = startHour;
        this.startMin = startMin;
        this.startSec = startSec;
        this.endHour = endHour;
        this.endMin = endMin;
        this.endSec = endSec;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMin() {
        return endMin;
    }

    public int getEndSec() {
        return endSec;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMin() {
        return startMin;
    }

    public int getStartSec() {
        return startSec;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public void setEndMin(int endMin) {
        this.endMin = endMin;
    }

    public void setEndSec(int endSec) {
        this.endSec = endSec;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public void setStartMin(int startMin) {
        this.startMin = startMin;
    }

    public void setStartSec(int startSec) {
        this.startSec = startSec;
    }
}
