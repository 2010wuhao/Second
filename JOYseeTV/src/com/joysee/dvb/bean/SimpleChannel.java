/**
 * =====================================================================
 *
 * @file  SimpleChannel.java
 * @Module Name   com.joysee.dvb.bean
 * @author yl
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月4日
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
 * yl         2014年3月4日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.bean;

public class SimpleChannel {

    public String mChannelName;
    public int mTvId;
    public int mTypeId;
    public String mTypeName;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SimpleChannel.class.getSimpleName());
        sb.append(" : mChannelName = ");
        sb.append(mChannelName);
        sb.append(" mTvId = ");
        sb.append(mTvId);
        sb.append(" mTypeId = ");
        sb.append(mTypeId);
        return sb.toString();
    }
}
