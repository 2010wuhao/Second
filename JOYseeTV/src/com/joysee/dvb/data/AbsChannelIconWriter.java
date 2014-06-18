/**
 * =====================================================================
 *
 * @file  AbsChannelIconWriter.java
 * @Module Name   com.joysee.dvb.data
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年4月4日
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
 * YueLiang         2014年4月4日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.data;

import java.util.HashMap;

public abstract class AbsChannelIconWriter {

    AbsChannelIconWriter() {
    }

    public abstract void fillChannelIcon(HashMap<String, String> icons);
}
