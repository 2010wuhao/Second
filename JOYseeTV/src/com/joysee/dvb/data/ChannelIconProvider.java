/**
 * =====================================================================
 *
 * @file  ChannelIconProvider.java
 * @Module Name   com.joysee.dvb.data
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月16日
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
 * yueliang         2014年2月16日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.joysee.common.utils.JLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class ChannelIconProvider {
    private static final String TAG = JLog.makeTag(ChannelIconProvider.class);

    private static final HashMap<String, String> mChannelIcons = new HashMap<String, String>();

    static {
        AbsChannelIconWriter writer = new ChannelIconWriter_BJ_SC();
        writer.fillChannelIcon(mChannelIcons);

        writer = new ChannelIconWriter_BJ_GH();
        writer.fillChannelIcon(mChannelIcons);
    }

    public static Bitmap getChannelIcon(Context c, String channelName) {
        Bitmap b = null;
        if (mChannelIcons.containsKey(channelName)) {
            String iconName = mChannelIcons.get(channelName);
            try {
                InputStream in = c.getAssets().open("channelicon/" + iconName);
                b = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                JLog.e(TAG, "getChannelIcon channel = " + channelName + " catch Exception.", e);
            }
        }
        return b;
    }

    public ChannelIconProvider() {
    }

}
