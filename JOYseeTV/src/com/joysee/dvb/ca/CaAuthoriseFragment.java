/**
 * =====================================================================
 *
 * @file  CaAuthoriseFragment.java
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
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.joysee.adtv.logic.CAControlException;
import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.LicenseInfo;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JEditTextWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.ca.CaMenuFragment.CardOperateCode;
import com.joysee.dvb.search.wheelview.ArrayWheelAdapter;
import com.joysee.dvb.search.wheelview.WheelView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CaAuthoriseFragment extends Fragment {
    private String TAG = "CaAuthoriseFragment";
    private ListView mListView;
    private SimpleAdapter mSimpleAdapter;
    private JEditTextWithTTF mOperatorEt;
    private String[] mAdapterFrom = {
            "pid", "deadline", "record_flag"
    };
    private int[] mAdapterTo = {
            R.id.ca_authorise_list_item_pid,
            R.id.ca_authorise_list_item_deadline,
            R.id.ca_authorise_list_item_record_flag
    };
    private WheelView mWheelView;
    private ArrayWheelAdapter wheelAdapter;

//    private CaManager mCaManager;
    private JDVBPlayer mDvbPlayer;
    private Vector<Integer> mOperateID = new Vector<Integer>();
    private static final int MSG_GET_ID = 0;
    private static final int MSG_REFAUSE = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_ID:
                    int code = 0;
                    try {
                        mOperateID = mDvbPlayer.getOperatorID();
                    } catch (CAControlException e) {
                        code = e.getErrorCode();
                    }
                    showOperateMsg(code);
                    break;
                case MSG_REFAUSE:
                    mSimpleAdapter = null;
                    mOperatorEt.setText("" + mOperateID.get(mWheelView.getCurrentItem()));
                    mSimpleAdapter = new SimpleAdapter(getActivity(),
                            getData(mOperateID.get(mWheelView.getCurrentItem())),
                            R.layout.ca_authorise_listview_item,
                            mAdapterFrom,
                            mAdapterTo);
                    mListView.setAdapter(mSimpleAdapter);
                    break;
            }
        }
    };

    private List<Map<String, Object>> getData(int operID) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Vector<LicenseInfo> vector = new Vector<LicenseInfo>();
        int code = 0;
        try {
            vector = mDvbPlayer.getAuthorization(operID);
        } catch (CAControlException e) {
            code = e.getErrorCode();
        }
        showOperateMsg(code);
        JLog.d(TAG, " operID = " + operID + " getData " + vector.size());
        for (int i = 0; i < vector.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(mAdapterFrom[0], "" + vector.get(i).product_id);
            map.put(mAdapterFrom[1], getDateString(vector.get(i).expired_time));
            if (vector.get(i).is_record) {
                map.put(mAdapterFrom[2], "是");
            } else {
                map.put(mAdapterFrom[2], "否");
            }
            list.add(map);
        }
        return list;
    }

    private String getDateString(int day) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy" + getString(R.string.ca_charge_interval_year) +
                "MM" + getString(R.string.ca_charge_interval_month) + "dd" + getString(R.string.ca_charge_interval_day));
        cal.set(2000, 0, 1);// 从1月1号开始算
        cal.add(Calendar.DATE, day);
        Date d = new Date();
        d = cal.getTime();
        String date = format.format(d);
        return date;
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        JLog.d(TAG, TAG + " onCreateView ");
//        mCaManager = CaManager.getInstance();
        mDvbPlayer = JDVBPlayer.getInstance();
        int code = 0;
        try {
            mOperateID = mDvbPlayer.getOperatorID();
        } catch (CAControlException e) {
            code = e.getErrorCode();
        }//mCaManager.nativeGetOperatorID(mOperateID);
        Message msg = new Message();
        msg.what = MSG_GET_ID;
        msg.arg1 = code;
        mHandler.sendEmptyMessageDelayed(MSG_GET_ID, 500);

        View rootView = inflater.inflate(R.layout.ca_authorise_layout, container, false);
        CaActivity caActivity = (CaActivity) getActivity();
        caActivity.replaceTitleText(getString(R.string.ca_authorise_info_text));
        mListView = (ListView) rootView.findViewById(R.id.ca_authorise_listview);
        mWheelView = (WheelView) rootView.findViewById(R.id.ca_operate_id_wheelview);
        mWheelView.setVisibility(View.INVISIBLE);
        mWheelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWheelView.getVisibility() == View.INVISIBLE) {
                    mWheelView.setVisibility(View.VISIBLE);
                } else {
                    mWheelView.setVisibility(View.VISIBLE);
                }
            }
        });
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
                        if (mWheelView.getVisibility() == View.VISIBLE) {
                            mWheelView.setVisibility(View.INVISIBLE);
                            mHandler.sendEmptyMessage(MSG_REFAUSE);
                        }
                        ret = true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (mWheelView.getVisibility() == View.VISIBLE) {
                            mWheelView.setVisibility(View.INVISIBLE);
                            mHandler.sendEmptyMessage(MSG_REFAUSE);
                        }
                        ret = true;
                    }
                }
                return ret;
            }
        });
        mOperatorEt = (JEditTextWithTTF) rootView.findViewById(R.id.ca_operate_id_tv);
        mOperatorEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWheelView.getVisibility() == View.INVISIBLE) {
                    mWheelView.setVisibility(View.VISIBLE);
                    mWheelView.requestFocus();
                }
            }
        });

        Integer[] integers = new Integer[mOperateID.size()];
        mOperateID.toArray(integers);
        JLog.d(TAG, Arrays.toString(integers));
        mOperatorEt.setText("" + mOperateID.get(0));
        wheelAdapter = new ArrayWheelAdapter<Integer>(getActivity(), integers);
        mWheelView.setViewAdapter(wheelAdapter);
        mWheelView.setVisibleItems(3);
        wheelAdapter.setTextColor(android.R.color.black);
        mWheelView.setCenterDrawable(getResources().getDrawable(
                R.drawable.edittext_defalut_selected));

        mSimpleAdapter = new SimpleAdapter(this.getActivity(),
                getData(mOperateID.get(mWheelView.getCurrentItem())),
                R.layout.ca_authorise_listview_item,
                mAdapterFrom,
                mAdapterTo);
        mListView.setAdapter(mSimpleAdapter);

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
                msg = getString(R.string.ca_operate_msg_getdata_ok);
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
                msg = getString(R.string.ca_operate_msg_getdata_error);
                break;

        }
        JLog.d(TAG, " showOperateMsg operateCode = " + operateCode + " msg = " + msg);
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
