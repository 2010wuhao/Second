/**
 * =====================================================================
 *
 * @file  DVBErrorReportView.java
 * @Module Name   com.joysee.adtv.ui.epg
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Dec 18, 2013
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
 * yueliang         Dec 18, 2013            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JStyleDialog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.activity.DvbPlaybackActivity;
import com.joysee.dvb.activity.SearchChannelActivity;
import com.joysee.dvb.controller.DvbMessage;
import com.joysee.dvb.controller.IDvbBaseView;

public class DVBErrorReportView extends FrameLayout implements IDvbBaseView {
    public enum ErrorState {
        NOERROR, NOCHANNEL, NOSIGNAL, CAERROR,
    }

    private static final String TAG = JLog.makeTag(DVBErrorReportView.class);

    private boolean mPause;

    private JTextViewWithTTF mErrorMsgView;
    // private DVBPlayManager mDvbPlayManager;
    private JDVBPlayer mDvbPlayer;
//    private ChannelManager mChannelManager;

//    private CaManager mCaManager;

    private JStyleDialog mNoChannelDialog;

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
                    updateErrorStatus(msg.arg1);
                    break;

                default:
                    break;
            }
        };
    };

    public DVBErrorReportView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void clear() {
        JLog.d(TAG, "clear");
        mErrorMsgView.setText("");
        mState = ErrorState.NOERROR;
    }

    private void dismiss() {
        JLog.d(TAG, "dismiss");
        if (mNoChannelDialog != null && mNoChannelDialog.isShowing()) {
            mNoChannelDialog.dismiss();
        }
        this.setVisibility(View.INVISIBLE);
        clear();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        JLog.d(TAG, "dispatchKeyEvent keyCode = " + keyCode + "-"
                + KeyEvent.keyCodeToString(keyCode)
                + " action = " + action);
        boolean ret = false;
        ret = super.dispatchKeyEvent(event);
        if (!ret) {
            if (action == KeyEvent.ACTION_UP) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ESCAPE:
                    case KeyEvent.KEYCODE_BACK:
                        break;
                    default:
                        if (mState == ErrorState.NOCHANNEL) {
                            ret = true;
                        }
                        break;
                }
            } else if (action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ESCAPE:
                    case KeyEvent.KEYCODE_BACK:
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        int direction = keyCode == KeyEvent.KEYCODE_DPAD_LEFT ? FOCUS_LEFT : FOCUS_RIGHT;
                        View nextFocus = focusSearch(getFocusedChild(), direction);
                        if (nextFocus != null) {
                            JLog.d(TAG, "dispatchKeyEvent nextFocus = " + nextFocus);
                            nextFocus.requestFocus();
                        }
                        ret = true;
                        break;
                    default:
                        if (mState == ErrorState.NOCHANNEL) {
                            ret = true;
                        }
                        break;
                }
            }
        }
        return ret;
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
        mErrorMsgView = (JTextViewWithTTF) findViewById(R.id.error_report_view_errormsg);

        StyleDialog.Builder builder = new StyleDialog.Builder(getContext());
        builder.setDefaultContentMessage(R.string.nochannel_notify_message);
        builder.setDefaultContentPoint(R.string.nochannel_notify_point);
        builder.setPositiveButton("确定");
        builder.setNegativeButton("取消");
        builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JLog.d(TAG, "mNoChannelDialog onClick which = " + which);
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mNoChannelDialog.dismiss();
                    Intent intent = new Intent(getContext(), SearchChannelActivity.class);
                    getContext().startActivity(intent);
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    mNoChannelDialog.dismiss();
                    ((Activity) getContext()).finish();
                }
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                boolean handle = false;
                if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        mNoChannelDialog.dismiss();
                        ((Activity) getContext()).finish();
                        handle = true;
                    }
                }
                return handle;
            }
        });

        mNoChannelDialog = builder.show();
    }

    @Override
    public void processMessage(DvbMessage msg) {
        JLog.d(TAG, "processMessage msg = " + DvbMessage.getMessageName(msg));
        switch (msg.what) {
            case DvbMessage.REFRESH_DVB_NOTIFY:
                Message m = obtainUpdateMsg();
                m.arg1 = msg.arg1;
                mWorkHandler.removeMessages(MSG_REFRESH_STATE);
                mWorkHandler.sendMessage(m);
                break;
            case DvbMessage.ERROR_WITHOUT_CHANNEL:
                showNoSpecialChannelMsg(msg.arg1);
                break;
            case DvbMessage.DVB_INIT_FAILED:
                showDTVServiceInitFailedMsg();
                break;
            case DvbMessage.ONRESUME:
            case DvbMessage.START_PLAY_BC:
            case DvbMessage.START_PLAY_TV:
                mPause = false;
                break;
            case DvbMessage.STOP_PLAY:
            case DvbMessage.ONPAUSE:
                mPause = true;
                dismiss();
                break;
            default:
                break;
        }
    }

    private void showCAMsg(String notifyString) {
        mState = ErrorState.CAERROR;
        mErrorMsgView.setText(notifyString);
        setVisibility(View.VISIBLE);
    }

    private void showDTVServiceInitFailedMsg() {
        mErrorMsgView.setText(getResources().getString(R.string.init_failed));
        setVisibility(View.VISIBLE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                Context context = getContext();
                if (context instanceof DvbPlaybackActivity) {
                    ((DvbPlaybackActivity) context).finish();
                }
            }
        }, 3000);
    }

    private void showNoChannelMsg() {
        if (mNoChannelDialog != null) {
            mState = ErrorState.NOCHANNEL;
            mNoChannelDialog.show();
        }
    }

    private void showNoSpecialChannelMsg(int num) {
        mErrorMsgView.setText(getResources().getString(R.string.without_this_channel, num));
        setVisibility(View.VISIBLE);
        mWorkHandler.removeMessages(MSG_REFRESH_STATE);
        mWorkHandler.sendMessageDelayed(obtainUpdateMsg(), 3000);
    }

    private Message obtainUpdateMsg() {
        Message msg = Message.obtain();
        msg.what = MSG_REFRESH_STATE;
        msg.arg1 = DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CA | DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CHANNEL
                | DvbMessage.FLAG_REFRESH_DVB_NOTIFY_TUNERSIGNAL;
        return msg;
    }

    private void showSymbolMsg() {
        mState = ErrorState.NOSIGNAL;
        mErrorMsgView.setText(R.string.no_signal);
        setVisibility(View.VISIBLE);
    }

    public void updateErrorStatus(int flag) {
        final long begin = JLog.methodBegin(TAG);
        if (mDvbPlayer == null) {
            mDvbPlayer = mDvbPlayer.getInstance();
            // mDvbPlayManager = DVBPlayManager.getInstance();
//            mChannelManager = ChannelManager.getInstance();
//            mCaManager = CaManager.getInstance();
        }
        if (TvApplication.DEBUG_LOG) {
            StringBuilder flagS = new StringBuilder();
            if ((flag & DvbMessage.FLAG_REFRESH_DVB_NOTIFY_TUNERSIGNAL) != 0) {
                flagS.append("FLAG_REFRESH_DVB_NOTIFY_TUNERSIGNAL-");
            }
            if ((flag & DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CHANNEL) != 0) {
                flagS.append("FLAG_REFRESH_DVB_NOTIFY_CHANNEL-");
            }
            if ((flag & DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CA) != 0) {
                flagS.append("FLAG_REFRESH_DVB_NOTIFY_CA-");
            }
            JLog.d(TAG, "updateErrorStatus flag = " + flag + " " + flagS.toString());
        }
        final boolean tunerSignal = (flag & DvbMessage.FLAG_REFRESH_DVB_NOTIFY_TUNERSIGNAL) == 0 ? true :
                mDvbPlayer.getTunerSignalStatus(JDVBPlayer.TUNER_0) == JDVBPlayer.TUNER_SIGNAL_ON;
        final boolean hasChannel = (flag & DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CHANNEL) == 0 ? true :
                mDvbPlayer.getChannelCount() > 0;
        final int caStatus = (flag & DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CA) == 0 ? JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE
                : mDvbPlayer.getStateByType(JDVBPlayer.TUNER_0, JDVBPlayer.CALLBACK_BUYMSG);

        boolean postRet = mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateErrorStatusInternal(tunerSignal, hasChannel, caStatus);
            }
        });
        JLog.d(TAG, "post to main thread error update postRet = " + postRet);
        if (!tunerSignal && !mPause) {
            mWorkHandler.removeMessages(MSG_REFRESH_STATE);
            mWorkHandler.sendMessageDelayed(obtainUpdateMsg(), 3000);
        }
        JLog.methodEnd(TAG, begin);
    }

    private void updateErrorStatusInternal(boolean tunerSignal, boolean hasChannel, int caStatus) {
        JLog.d(TAG, "updateErrorStatusInternal tunerSignal = " + tunerSignal + " hasChannel = " + hasChannel +
                " caStatus = " + caStatus);
        clear();
        if (!mPause) {
            if (hasChannel && tunerSignal && caStatus == JDVBPlayer.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE) {
                JLog.d(TAG, "updateErrorStatus no error.");
                dismiss();
                mState = ErrorState.NOERROR;
            } else {
                if (!hasChannel) {
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
}
