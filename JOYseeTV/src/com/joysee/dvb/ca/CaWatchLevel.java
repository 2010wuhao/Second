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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.joysee.adtv.logic.CAControlException;
import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.ca.CaMenuFragment.CardOperateCode;
import com.joysee.dvb.search.wheelview.ArrayWheelAdapter;
import com.joysee.dvb.search.wheelview.WheelView;

public class CaWatchLevel extends Fragment implements View.OnClickListener {
    private String TAG = "CaWatchLevel";
    private JButtonWithTTF mSaveButton;
    private WheelView mWheelView;
    private String[] mLevelArray = new String[15];
//    private CaManager mCaManager;
    private JDVBPlayer mDvbPlayer;
    private ArrayWheelAdapter wheelAdapter;

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
            int current = mWheelView.getCurrentItem() + 1;
            JLog.d(TAG, " onClick current = " + current);
            int code = 0 ;
            try {
                mDvbPlayer.setWatchLeve(CaMenuFragment.mPasswordStr, current + 3);
            } catch (CAControlException e) {
                e.printStackTrace();
                code = 0;
            }
            showOperateMsg(code);
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
        for (int i = 0; i < mLevelArray.length; i++) {
            mLevelArray[i] = getString(R.string.ca_watch_over_level, i + 4);
        }
        mDvbPlayer = JDVBPlayer.getInstance();
//        mCaManager = CaManager.getInstance();

        View rootView = inflater.inflate(R.layout.ca_watchlevel_layout, container, false);
        CaActivity caActivity = (CaActivity) getActivity();
        caActivity.replaceTitleText(getString(R.string.ca_watch_level_text));

        mSaveButton = (JButtonWithTTF) rootView.findViewById(R.id.ca_watchlevel_bt);
        mSaveButton.setOnClickListener(this);
        mWheelView = (WheelView) rootView.findViewById(R.id.ca_level_wheelview);
        wheelAdapter = new ArrayWheelAdapter<String>(getActivity(), mLevelArray);
        mWheelView.setViewAdapter(wheelAdapter);
        mWheelView.setVisibleItems(3);
        wheelAdapter.setTextColor(android.R.color.black);
        mWheelView.setCenterDrawable(getResources().getDrawable(
                R.drawable.edittext_defalut_selected));

        int level = mDvbPlayer.getWatchLevel();
        JLog.d(TAG, " mCaManager.nativeGetWatchLevel() = " + level);
        mWheelView.setCurrentItem(level - 4);
        mWheelView.setCyclic(true);

        mWheelView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean ret = false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mWheelView.setCurrentItem(mWheelView.getCurrentItem() + 1);
                        ret = true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        mWheelView.setCurrentItem(mWheelView.getCurrentItem() - 1);
                        ret = true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        int current = mWheelView.getCurrentItem() + 1;
                        JLog.d(TAG, " onClick current = " + current);
                        int code = 0;
                        try {
                            mDvbPlayer.setWatchLeve(CaMenuFragment.mPasswordStr, current + 3);
                        } catch (CAControlException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            code = e.getErrorCode();
                        }
                        showOperateMsg(code);
                    }
                }
                return ret;
            }
        });

        mWheelView.requestFocus();
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
}
