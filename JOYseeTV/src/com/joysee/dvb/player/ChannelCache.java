/**
 * =====================================================================
 *
 * @file  ChannelCache.java
 * @Module Name   com.joysee.dvb.player
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月10日
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
 * YueLiang         2014年1月10日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.player;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;

import java.util.ArrayList;

class Channel {
    int mIndex;
    int mChannelNum;
    DvbService mChannel;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mChannel != null) {
            sb.append("Index = ");
            sb.append(mIndex);
            sb.append(" - ");
            sb.append("ChannelNum = ");
            sb.append(mChannelNum);
            sb.append(" - ");
            sb.append("ChannelName = ");
            sb.append(mChannel.getChannelName());
        } else {
            sb.append("Channel is Null.");
        }
        return sb.toString();
    }
}

@SuppressWarnings("serial")
public class ChannelCache extends ArrayList<Channel> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String TAG = JLog.makeTag(ChannelCache.class);
    Channel mCurChannel;

    @Override
    public void clear() {
        super.clear();
        this.mCurChannel = null;
    }

    Channel getChannelByNum(int num) {
        Channel channel = null;
        for (Channel c : this) {
            if (num == c.mChannelNum) {
                channel = c;
                break;
            }
        }
        return channel;
    }

    Channel getChannelBySid(int sid) {
        Channel channel = null;
        for (Channel c : this) {
            if (sid == c.mChannel.getServiceId()) {
                channel = c;
                break;
            }
        }
        return channel;
    }

    Channel getFirstChannel() {
        Channel channel = null;
        if (this.size() > 0) {
            channel = get(0);
        }
        return channel;
    }

    Channel getNext() {
        Channel ret = null;
        if (this.contains(mCurChannel)) {
            int curIndex = this.indexOf(mCurChannel);
            int nextIndex = 0;
            if (curIndex < this.size() - 1) {
                nextIndex = curIndex + 1;
            }
            ret = this.get(nextIndex);
        } else {

        }
        return ret;
    }

    Channel getPrevious() {
        Channel ret = null;
        if (this.contains(mCurChannel)) {
            int curIndex = this.indexOf(mCurChannel);
            int previousIndex = this.size() - 1;
            if (curIndex > 0) {
                previousIndex = curIndex - 1;
            }
            ret = this.get(previousIndex);
        } else {

        }
        return ret;
    }

    void setCurrent(Channel channel) {
        JLog.d(TAG, "setCurrent channel = " + channel);
        if (mCurChannel != channel) {
            mCurChannel = channel;
        }
    }

    void updateChannel(DvbService channel) {
        for (Channel c : this) {
            if (c.mChannel.equals(channel)) {
                c.mChannel = channel;
                break;
            }
        }
    }
}
