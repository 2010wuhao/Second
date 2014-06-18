/**
 * =====================================================================
 *
 * @file  OsdInfo.java
 * @Module Name   com.joysee.adtv.logic.bean
 * @author wuh
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
 * wuh          2014年3月20日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

public class OsdInfo {
    public static final int OSD_STATE_SHOW = 0;
    public static final int OSD_STATE_DISMISS = 1;
    public static final int OSD_STATE_MASK = 0xFFFF;

    /**
     * OSD风格：显示在屏幕上方
     */
    public static final int OSD_POSITION_TOP = 1;
    /**
     * OSD风格：显示在屏幕下方
     */
    public static final int OSD_POSITION_BOTTOM = 2;
    /**
     * OSD风格：整屏显示
     */
    public static final int OSD_POSITION_FULL_SCREEN = 3;
    /**
     * OSD风格：半屏显示
     */
    public static final int OSD_POSITION_HALF_SCREEN = 4;

    /**
     * OSD显示位置和显示状态 高16位：显示位置 Top Bottom 底16位：显示状态 0：隐藏 1：显示
     */
    private int osdPosAndState;
    /**
     * OSD的显示内容
     */
    private String osdMsg;

    public int getOsdInfo() {
        return osdPosAndState;
    }

    public String getOsdMsg() {
        return osdMsg;
    }

    /**
     * 获得OSD的显示状态
     * 
     * @return int 0：隐藏 1：显示
     */
    public int getShowOrHide() {
        int temp = osdPosAndState;
        return temp & OSD_STATE_MASK;
    }

    /**
     * 获得OSD的显示位置和显示方式（半/满屏）
     * 
     * @return 显示位置
     * @see #OSD_POSITION_TOP
     * @see #OSD_POSITION_BOTTOM
     * @see #OSD_POSITION_FULL_SCREEN
     * @see #OSD_POSITION_HALF_SCREEN
     */
    public int getShowPosition() {
        int temp = osdPosAndState;
        return temp >> 16;
    }

    public void setOsdInfo(int osdInfo) {
        this.osdPosAndState = osdInfo;
    }

    public void setOsdMsg(String osdMsg) {
        this.osdMsg = osdMsg;
    }
}
