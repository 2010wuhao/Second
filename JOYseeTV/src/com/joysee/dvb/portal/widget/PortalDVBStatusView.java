/**
 * =====================================================================
 *
 * @file  PortalDVBStatusView.java
 * @Module Name   com.joysee.dvb.portal.widget
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月20日
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
 * yueliang         2014年2月20日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal.widget;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.portal.PortalModle;
import com.joysee.dvb.widget.DVBErrorReportView;

public class PortalDVBStatusView extends FrameLayout {

    public enum ErrorState {
        NOERROR, NOCHANNEL, NOSIGNAL, CAERROR, NODONGLE
    }

    private static final String TAG = JLog.makeTag(PortalDVBStatusView.class);

    private TextView mErrorMsgView;
    private JDVBPlayer mDvbPlayer;
//    private DVBPlayManager mDvbPlayManager;
//    private ChannelManager mChannelManager;

//    private CaManager mCaManager;

    private ErrorState mState;

    private static final int MSG_REFRESH_STATE = 1;
    private static HandlerThread mHandlerThread = new HandlerThread(DVBErrorReportView.class.getSimpleName());
    static {
        mHandlerThread.start();
    }
    private Handler mHandler = new Handler();
    private Handler mWorkHandler = new Handler(mHandlerThread.getLooper()) {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_STATE:
                    updateErrorStatus();
                    break;

                default:
                    break;
            }
        };
    };

    public PortalDVBStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void clear() {
        JLog.d(TAG, "clear");
        mErrorMsgView.setText("");
        mState = ErrorState.NOERROR;
    }

    private void dismiss() {
        JLog.d(TAG, "dismiss");
        this.setVisibility(View.INVISIBLE);
        clear();
    }

    public String getCaNotifyString(int notify) {

        switch (notify) {
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_BADCARD_TYPE:

                return getResources().getString(R.string.ca_message_badcard_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_BLACKOUT_TYPE:

                return getResources().getString(R.string.ca_message_blackout_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_CALLBACK_TYPE:

                return getResources().getString(R.string.ca_message_callback_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE:

                return getResources().getString(R.string.ca_message_cancel_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_DECRYPTFAIL_TYPE:

                return getResources().getString(R.string.ca_message_decryptfail_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_ERRCARD_TYPE:

                return getResources().getString(R.string.ca_message_errcard_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_ERRREGION_TYPE:

                return getResources().getString(R.string.ca_message_errregion_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_EXPICARD_TYPE:

                return getResources().getString(R.string.ca_message_expicard_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_FREEZE_TYPE:

                return getResources().getString(R.string.ca_message_freeze_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_INSERTCARD_TYPE:

                return getResources().getString(R.string.ca_message_insertcard_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_LOWCARDVER_TYPE:

                return getResources().getString(R.string.ca_message_lowcardver_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_MAXRESTART_TYPE:

                return getResources().getString(R.string.ca_message_maxrestart_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_NEEDFEED_TYPE:

                return getResources().getString(R.string.ca_message_needfeed_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_NOENTITLE_TYPE:

                return getResources().getString(R.string.ca_message_noentitle_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_NOMONEY_TYPE:

                return getResources().getString(R.string.ca_message_nomoney_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_NOOPER_TYPE:

                return getResources().getString(R.string.ca_message_nooper_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_OUTWORKTIME_TYPE:

                return getResources().getString(R.string.ca_message_outworktime_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_PAIRING_TYPE:

                return getResources().getString(R.string.ca_message_pairing_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_STBFREEZE_TYPE:

                return getResources().getString(R.string.ca_message_stbfreeze_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_STBLOCKED_TYPE:

                return getResources().getString(R.string.ca_message_stblocked_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_UPDATE_TYPE:

                return getResources().getString(R.string.ca_message_update_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_VIEWLOCK_TYPE:

                return getResources().getString(R.string.ca_message_viewlock_type);
            case JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_WATCHLEVEL_TYPE:

                return getResources().getString(R.string.ca_message_watchlevel_type);
            default:
                return "未知错误   " + notify;
        }

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mErrorMsgView = (TextView) findViewById(R.id.portal_tv_item_tv_status_view_msg);
    }

    public void refreshDvbStatus() {
        Message m = Message.obtain();
        m.what = MSG_REFRESH_STATE;
        mWorkHandler.removeMessages(MSG_REFRESH_STATE);
        mWorkHandler.sendMessage(m);
    }

    private void showCAMsg(String notifyString) {
        mState = ErrorState.CAERROR;
        mErrorMsgView.setText(notifyString);
        setVisibility(View.VISIBLE);
    }

    private void showNoChannelMsg() {
        mState = ErrorState.NOCHANNEL;
        mErrorMsgView.setText(R.string.nochannel_notify_message);
        setVisibility(View.VISIBLE);
    }

    private void showNoDongleMsg() {
        mState = ErrorState.NODONGLE;
        mErrorMsgView.setText(R.string.no_dongle);
        setVisibility(View.VISIBLE);
    }

    private void showSymbolMsg() {
        mState = ErrorState.NOSIGNAL;
        mErrorMsgView.setText(R.string.no_signal);
        setVisibility(View.VISIBLE);
    }

    public void updateErrorStatus() {
        final long begin = JLog.methodBegin(TAG);
        if (mDvbPlayer == null) {
            mDvbPlayer = JDVBPlayer.getInstance();
//            mChannelManager = ChannelManager.getInstance();
//            mCaManager = CaManager.getInstance();
        }
        final boolean tunerSignal = mDvbPlayer.getTunerSignalStatus(JDVBPlayer.TUNER_0) == JDVBPlayer.TUNER_SIGNAL_ON;
        final boolean hasChannel = mDvbPlayer.getChannelCount() > 0;
        final int caStatus = mDvbPlayer.getStateByType(JDVBPlayer.TUNER_0, JDVBPlayer.CALLBACK_BUYMSG);
        final boolean hasDongle = PortalModle.getUsbDongleState(getContext());

        boolean postRet = mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateErrorStatusInternal(hasDongle, tunerSignal, hasChannel, caStatus);
            }
        });
        JLog.d(TAG, "post to main thread error update postRet = " + postRet);
        if (!tunerSignal) {
            mWorkHandler.removeMessages(MSG_REFRESH_STATE);
            mWorkHandler.sendEmptyMessageDelayed(MSG_REFRESH_STATE, 3000);
        }
        JLog.methodEnd(TAG, begin);
    }

    private void updateErrorStatusInternal(boolean hasDongle, boolean tunerSignal, boolean hasChannel, int caStatus) {
        JLog.d(TAG, "updateErrorStatusInternal hasDongle = " + hasDongle + " tunerSignal = " + tunerSignal + " hasChannel = " + hasChannel +
                " caStatus = " + caStatus);
        clear();
        if (hasDongle && hasChannel && tunerSignal && caStatus == JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE) {
            JLog.d(TAG, "updateErrorStatus no error.");
            dismiss();
            mState = ErrorState.NOERROR;
        } else {
            if (!hasDongle) {
                showNoDongleMsg();
            } else if (!hasChannel) {
                showNoChannelMsg();
            } else if (!tunerSignal) {
                showSymbolMsg();
            } else if (caStatus != JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE &&
                    caStatus != -1) {
                showCAMsg(getCaNotifyString(caStatus));
            }
        }
    }

}
