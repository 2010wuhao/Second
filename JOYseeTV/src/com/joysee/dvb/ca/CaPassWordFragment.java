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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.joysee.adtv.logic.CAControlException;
import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.common.widget.JEditTextWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.ca.CaMenuFragment.CardOperateCode;

public class CaPassWordFragment extends Fragment implements View.OnClickListener {
    String TAG = "CaWorkTimeFragment";
    private JEditTextWithTTF mPassWord;
    private JEditTextWithTTF mPassWordConfirm;
    private JButtonWithTTF mSaveButton;
//    private CaManager mCaManager;
    private JDVBPlayer mDvbPlayer;

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
            String psw = mPassWord.getText().toString();
            String confirm = mPassWordConfirm.getText().toString();
            if (psw == null || confirm == null || psw.length() < 6 || confirm.length() < 6) {
                showOperateMsg(CardOperateCode.PASSWORD_LENGTH_ERROR);
            } else if (!psw.equals(confirm)) {
                showOperateMsg(CardOperateCode.EQUALS_ERROR);
            } else {
                int code = 0;
                try {
                    mDvbPlayer.changeCAcardPassword(CaMenuFragment.mPasswordStr, confirm);
                } catch (CAControlException e) {
                    e.printStackTrace();
                    code = e.getErrorCode();
                }
                showOperateMsg(code);
            }
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
        View rootView = inflater.inflate(R.layout.ca_password_layout, container, false);
        CaActivity caActivity = (CaActivity) getActivity();
        caActivity.replaceTitleText(getString(R.string.ca_modify_password_text));
        mPassWord = (JEditTextWithTTF) rootView.findViewById(R.id.ca_password_et);
        mPassWordConfirm = (JEditTextWithTTF) rootView.findViewById(R.id.ca_password_confirm_et);

        mSaveButton = (JButtonWithTTF) rootView.findViewById(R.id.ca_password_bt);
        mSaveButton.setOnClickListener(this);

        mPassWord.requestFocus();

//        mCaManager = CaManager.getInstance();
        mDvbPlayer = JDVBPlayer.getInstance();
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
            case CardOperateCode.PASSWORD_LENGTH_ERROR:
                msg = getString(R.string.ca_password_length_error_text);
                break;
            case CardOperateCode.EQUALS_ERROR:
                msg = getString(R.string.ca_operate_msg_equals_error);
                break;
            default:
                msg = getString(R.string.ca_operate_msg_error);
                break;

        }
        JLog.d(TAG, " showOperateMsg operateCode = " + operateCode + " msg = " + msg);
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
