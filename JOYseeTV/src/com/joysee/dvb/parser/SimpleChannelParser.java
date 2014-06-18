/**
 * =====================================================================
 *
 * @file  SimpleChannelParser.java
 * @Module Name   com.joysee.dvb.parser
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

package com.joysee.dvb.parser;

import com.joysee.common.data.JBaseParser;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.bean.SimpleChannel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SimpleChannelParser extends JBaseParser<ArrayList<SimpleChannel>> {
    private static final String TAG = JLog.makeTag(SimpleChannelParser.class);

    @Override
    public String checkResponse(String arg0) throws JSONException {
        return null;
    }

    @Override
    public ArrayList<SimpleChannel> parseJSON(String json) throws JSONException {
        ArrayList<SimpleChannel> channels = null;
        JLog.d(TAG, "parseJSON json = " + json);
        if (json != null && !json.trim().equals("")) {
            JSONObject root = new JSONObject(json);
            JSONArray jsonChannels = root.getJSONArray("data");
            JLog.d(TAG, "jsonChannels size = " + jsonChannels.length());
            if (jsonChannels != null && jsonChannels.length() > 0) {
                channels = new ArrayList<SimpleChannel>();
                SimpleChannel channel;
                for (int i = 0; i < jsonChannels.length(); i++) {
                    JSONObject jsonChannel = jsonChannels.getJSONObject(i);
                    if (jsonChannel != null) {
                        channel = new SimpleChannel();
                        channel.mChannelName = jsonChannel.getString("tvName");
                        channel.mTvId = jsonChannel.getInt("tvId");
                        channel.mTypeId = jsonChannel.getInt("tvTypeCode");
                        channel.mTypeName = jsonChannel.getString("tvTypeName");
                        channels.add(channel);
                        JLog.d(TAG, "channel = " + channel);
                    }
                }
            }
        }
        return channels;
    }

}
