/**
 * =====================================================================
 *
 * @file  PlaybackDebugEPGUpdate.java
 * @Module Name   com.joysee.dvb.widget
 * @author yl
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月15日
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
 * yl         2014年3月15日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.service.EPGUpdateService.EPGUpdateStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaybackDebugEPGUpdate extends FrameLayout {
    private static final String TAG = JLog.makeTag(PlaybackDebugEPGUpdate.class);
    private static final String TIME_FORMAT = "HH:mm";
    private static final SimpleDateFormat mTimeFormat = new SimpleDateFormat(TIME_FORMAT);

    private TextView mCurrentStatus;
    private TextView mEPGUpdateProgress;

    public PlaybackDebugEPGUpdate(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCurrentStatus = (TextView) findViewById(R.id.playback_debug_epgupdate_status_value);
        mEPGUpdateProgress = (TextView) findViewById(R.id.playback_debug_epgupdate_progress_value);
    }

    public void setEPGUpdateStatus(EPGUpdateStatus status) {
        JLog.d(TAG, "setEPGUpdateStatus status = " + status);
        if (status != null) {
            mCurrentStatus.setText(status.running ? "正在运行" :
                    mTimeFormat.format(new Date(status.nextUpdateBeginTime)) + " 开始运行");
            mEPGUpdateProgress.setText(status.currentChannel + "/" + status.channelSize);
        }
    }

}
