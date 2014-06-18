/**
 * =====================================================================
 *
 * @file  CaWorkTimeFragment.java
 * @Module Name   com.example.catest
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月27日
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
 * wuhao         2014年2月27日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.ca;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.joysee.adtv.logic.CAControlException;
import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.WatchTime;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.common.widget.JEditTextWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.ca.CaMenuFragment.CardOperateCode;

public class CaWorkTimeFragment extends Fragment implements OnClickListener {
    String TAG = "CaWorkTimeFragment";
    private JEditTextWithTTF mBeginTimeHour;
    private JEditTextWithTTF mBeginTimeMinute;
    private JEditTextWithTTF mEndTimeHour;
    private JEditTextWithTTF mEndTimeMinute;
    private JButtonWithTTF mSaveButton;
    private JDVBPlayer mDvbPlayer;
//    private CaManager mCaManager;

    private TextWatcher mHourTextWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            String input = s.toString();
            if (input.length() > 0) {
                int hour = Integer.parseInt(s.toString());
                if (hour > 23) {
                    s.replace(0, s.length(), "23");
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };
    private TextWatcher mMinuteTextWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            String input = s.toString();
            if (input.length() > 0) {
                int minute = Integer.parseInt(input);
                if (minute > 59) {
                    s.replace(0, s.length(), "59");
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        JLog.d(TAG, TAG + " onActivityCreated ");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        JLog.d(TAG, TAG + " onAttach ");
        super.onAttach(activity);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mSaveButton.getId()) {
            workTimeSave();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        JLog.d(TAG, TAG + " onCreate ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        JLog.d(TAG, TAG + " onCreateView ");
        View rootView = inflater.inflate(R.layout.ca_worktime_layout, container, false);
        CaActivity caActivity = (CaActivity) getActivity();
        caActivity.replaceTitleText(getString(R.string.ca_work_time_text));
        mBeginTimeHour = (JEditTextWithTTF) rootView.findViewById(R.id.ca_worktime_begintime_hour);
        mBeginTimeMinute = (JEditTextWithTTF) rootView.findViewById(R.id.ca_worktime_begintime_minute);
        mEndTimeHour = (JEditTextWithTTF) rootView.findViewById(R.id.ca_worktime_endtime_hour);
        mEndTimeMinute = (JEditTextWithTTF) rootView.findViewById(R.id.ca_worktime_endtime_minute);
        mSaveButton = (JButtonWithTTF) rootView.findViewById(R.id.ca_worktime_bt);
        mSaveButton.setOnClickListener(this);

        mBeginTimeHour.addTextChangedListener(mHourTextWatcher);
        mBeginTimeMinute.addTextChangedListener(mMinuteTextWatcher);
        mEndTimeHour.addTextChangedListener(mHourTextWatcher);
        mEndTimeMinute.addTextChangedListener(mMinuteTextWatcher);

        mDvbPlayer = JDVBPlayer.getInstance();
//        mCaManager = CaManager.getInstance();
        WatchTime watchTime = null;
        try {
            watchTime = mDvbPlayer.getValidWatchingTime();
        } catch (CAControlException e) {
            e.printStackTrace();
        }
        mBeginTimeHour.setText(String.format("%02d", watchTime.startHour));
        mBeginTimeMinute.setText(String.format("%02d", watchTime.startMin));
        mEndTimeHour.setText(String.format("%02d", watchTime.endHour));
        mEndTimeMinute.setText(String.format("%02d", watchTime.endMin));

        rootView.requestFocus();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        JLog.d(TAG, TAG + " onDestroyView ");
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        JLog.d(TAG, TAG + " onDetach ");
        super.onDetach();
    }

    @Override
    public void onPause() {
        JLog.d(TAG, TAG + " onPause ");
        super.onPause();
    }

    @Override
    public void onResume() {
        JLog.d(TAG, TAG + " onResume ");
        super.onResume();
    }

    public void showOperateMsg(int operateCode) {
        String msg = null;
        switch (operateCode) {
            case CardOperateCode.OK:
                msg = getString(R.string.ca_operate_msg_ok);
                break;
            case CardOperateCode.NO_CARD:
                msg = getString(R.string.ca_card_not_finded);
                break;
            case CardOperateCode.PIN_INVALID:
                msg = getString(R.string.ca_password_error_text);
                break;
            case CardOperateCode.TIME_ERROR:
                msg = getString(R.string.ca_operate_msg_time_error);
                break;
            default:
                msg = getString(R.string.ca_operate_msg_error);
                break;

        }
        JLog.d(TAG, " showOperateMsg operateCode = " + operateCode + " msg = " + msg);
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void workTimeSave() {
        String sth = mBeginTimeHour.getText().toString();
        String stm = mBeginTimeMinute.getText().toString();
        String seh = mEndTimeHour.getText().toString();
        String sem = mEndTimeMinute.getText().toString();
        int bh = Integer.valueOf(sth);
        int bm = Integer.valueOf(stm);
        int eh = Integer.valueOf(seh);
        int em = Integer.valueOf(sem);
        int begin_time = bh * 60 + bm;
        int end_time = eh * 60 + em;
        // 判断开始时间是不是小于等于结束时间
        if (end_time > begin_time) {
            int operateCode = 0;
            try {
                mDvbPlayer.setValidWatchingTime(CaMenuFragment.mPasswordStr,
                        new WatchTime(bh, bm, 0, eh, em, 0));
            } catch (CAControlException e) {
                e.printStackTrace();
                operateCode = e.getErrorCode();
            }
            showOperateMsg(operateCode);
        } else {
            showOperateMsg(CardOperateCode.TIME_ERROR);
        }
    }
}
