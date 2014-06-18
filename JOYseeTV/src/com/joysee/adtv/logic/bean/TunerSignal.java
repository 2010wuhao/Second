/**
 * =====================================================================
 *
 * @file  TunerSignal.java
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

import android.os.Parcel;

public class TunerSignal {

    /**
     * 信号强度
     */
    private int level; //
    /**
     * 信号质量或者信燥比 它与信号强度决定信号能不能使用（播放或者搜台）
     */
    private int cn;

    /**
     * 误码率
     */
    private int errRate;

    public TunerSignal() {
    }

    public TunerSignal(int le, int cn, int err) {
        this.level = le;
        this.cn = cn;
        this.errRate = err;
    }

    public int getCN() {
        return cn;
    }

    public int getErrRate() {
        return errRate;
    }

    public int getLevel() {
        return level;
    }

    public void readFromParcel(Parcel in) {
        this.level = in.readInt();
        this.cn = in.readInt();
        this.errRate = in.readInt();
    }

    public void setCN(int cn) {
        this.cn = cn;
    }

    public void setErrRate(int errRate) {
        this.errRate = errRate;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "TunerSignal [level=" + level + ", cn=" + cn + ", errRate=" + errRate + "]";
    }
}
