/**
 * =====================================================================
 *
 * @file  Transponder.java
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

/**
 * 频点信息封装类
 */
public class Transponder {

    private int frequency;// 频率

    private int symbolRate;// 符号率

    private int modulation;// 调制方式

    public Transponder() {
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Transponder) {
            Transponder t = (Transponder) o;
            if (frequency == t.frequency && symbolRate == t.symbolRate && modulation == t.modulation) {
                return true;
            }
        }
        return super.equals(o);
    }

    public Transponder(int Frequency, int SymbolRate, int Modulation) {
        this.frequency = Frequency;
        this.symbolRate = SymbolRate;
        this.modulation = Modulation;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getModulation() {
        return modulation;
    }

    public int getSymbolRate() {
        return symbolRate;
    }

    public void readFromParcel(Parcel in) {
        this.frequency = in.readInt();
        this.modulation = in.readInt();
        this.symbolRate = in.readInt();
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setModulation(int modulation) {
        this.modulation = modulation;
    }

    public void setSymbolRate(int symbolRate) {
        this.symbolRate = symbolRate;
    }

    @Override
    public String toString() {
        return "[freq=" + frequency + ", mod=" + modulation + ", symb=" + symbolRate + "]";
    }
}
