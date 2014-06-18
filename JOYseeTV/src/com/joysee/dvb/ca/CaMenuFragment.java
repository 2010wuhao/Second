/**
 * =====================================================================
 *
 * @file  CaMenuFragment.java
 * @Module Name   com.example.catest
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月26日
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
 * wuhao         2014年2月26日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.ca;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.joysee.adtv.logic.CAControlException;
import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.WatchTime;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JEditTextWithTTF;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.widget.StyleDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaMenuFragment extends Fragment implements OnItemClickListener {
    public static final class CardOperateCode {
        /** 操作成功 */
        public static final int OK = 0;
        /** 未知错误 */
        public static final int UNKNOWN = 1;
        /** 指针为空 */
        public static final int POINTER_INVALID = 2;
        /** 智能卡不在机顶盒内或者是无效卡 */
        public static final int NO_CARD = 3;
        /** 输入pin 码无效 不在0x00~0x09之间 */
        public static final int PIN_INVALID = 4;
        /** 开始时间小于结束时间 */
        public static final int TIME_ERROR = 5;
        /** 密码长度错误 */
        public static final int PASSWORD_LENGTH_ERROR = 6;
        /** 两次输入不一致 */
        public static final int EQUALS_ERROR = 7;
    }

    private String TAG = "CaMenuFragment";
    private ListView mMenuListView;
    private SimpleAdapter mSimpleAdapter;
    private String[] mAdapterFrom = {
            "title", "content", "icon"
    };
    private int[] mAdapterTo = {
            R.id.ca_menu_item_title, R.id.ca_menu_item_content, R.id.ca_menu_item_right_icon
    };
    private JTextViewWithTTF mCaCardNumTv;

//    private CaManager mCaManager;
    private JDVBPlayer mDvbPlayer;
    private View mDialogContentView;

    private StyleDialog styleDialog;
    private JEditTextWithTTF mPassWordEt;

    private JTextViewWithTTF mHindTv;

    public static String mPasswordStr = null;

    private LayoutInflater inflater;

    private int mWhichItem;

    private String mCardNumber;

    private List<Map<String, Object>> getData() {
        JLog.d(TAG, " getData beging ");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(mAdapterFrom[0], getActivity().getResources().getText(R.string.ca_work_time_text));
        WatchTime watchTime = null;
        try {
            watchTime = mDvbPlayer.getValidWatchingTime();
        } catch (CAControlException e) {
            e.printStackTrace();
            watchTime = new WatchTime();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%02d", watchTime.startHour));
        stringBuilder.append(":");
        stringBuilder.append(String.format("%02d", watchTime.startMin));
        stringBuilder.append("-");
        stringBuilder.append(String.format("%02d", watchTime.endHour));
        stringBuilder.append(":");
        stringBuilder.append(String.format("%02d", watchTime.endMin));
        map.put(mAdapterFrom[1], stringBuilder.toString());
        map.put(mAdapterFrom[2], R.drawable.search_page_next);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(mAdapterFrom[0], getActivity().getResources().getText(R.string.ca_watch_level_text));
        int level = mDvbPlayer.getWatchLevel();//mCaManager.nativeGetWatchLevel();
        map.put(mAdapterFrom[1], getString(R.string.ca_watch_over_level, level));
        map.put(mAdapterFrom[2], R.drawable.search_page_next);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(mAdapterFrom[0], getActivity().getResources().getText(R.string.ca_authorise_info_text));
        map.put(mAdapterFrom[1], "");
        map.put(mAdapterFrom[2], R.drawable.search_page_next);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(mAdapterFrom[0], getActivity().getResources().getText(R.string.ca_modify_password_text));
        map.put(mAdapterFrom[1], "");
        map.put(mAdapterFrom[2], R.drawable.search_page_next);
        list.add(map);
        JLog.d(TAG, " getData end ");
        return list;
    }

    public boolean isRightPassWord() {
        boolean right = false;
        mPasswordStr = mPassWordEt.getText().toString();
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " isRightPassWord password = " + mPasswordStr);
        }
        if (mPasswordStr != null) {
            if (mPasswordStr.length() < 6) {
                mHindTv.setText(getString(R.string.ca_password_length_error_text));
                right = false;
            } else {
                int operateCode = 0;
                try {
                    mDvbPlayer.changeCAcardPassword(mPasswordStr, mPasswordStr);
                } catch (CAControlException e) {
                    e.printStackTrace();
                    operateCode = e.getErrorCode();
                }
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, " isRightPassWord operateCode = " + operateCode);
                }
                showOperateMsg(operateCode);
                if (operateCode == CardOperateCode.OK) {
                    right = true;
                }
            }
        }
        return right;
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
    public void onCreate(Bundle savedInstanceState) {
        JLog.d(TAG, TAG + " onCreate ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        JLog.d(TAG, TAG + " onCreateView ");
        this.inflater = inflater;
//        mCaManager = CaManager.getInstance();
        mDvbPlayer = JDVBPlayer.getInstance();
        View rootView = inflater.inflate(R.layout.ca_menu_fragment_layout, container, false);
        mMenuListView = (ListView) rootView.findViewById(R.id.ca_menu_listview);
        mCaCardNumTv = (JTextViewWithTTF) rootView.findViewById(R.id.ca_card_num_tv);
        mCardNumber = mDvbPlayer.getCacardSN();
        if (mCardNumber == null || mCardNumber.equals("")) {
            mCaCardNumTv.setText(getResources().getString(R.string.ca_card_num_text, getString(R.string.ca_card_not_finded)));
            Toast.makeText(getActivity(), getString(R.string.ca_card_not_finded), Toast.LENGTH_SHORT).show();
        } else {
            mCaCardNumTv.setText(getResources().getString(R.string.ca_card_num_text, mCardNumber));
        }
        ((CaActivity) getActivity()).replaceTitleText(null);
        mSimpleAdapter = new SimpleAdapter(this.getActivity(), getData(),
                R.layout.ca_menu_listview_item,
                mAdapterFrom,
                mAdapterTo);
        mMenuListView.setAdapter(mSimpleAdapter);
        mMenuListView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        JLog.d(TAG, TAG + " onDestroyView ");
        super.onDestroyView();
        mMenuListView = null;
        mSimpleAdapter = null;
        mCaCardNumTv = null;
        mDialogContentView = null;
    }

    @Override
    public void onDetach() {
        JLog.d(TAG, TAG + " onDetach ");
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        mWhichItem = arg2;
        boolean needUpdate = false;
        if (mCardNumber == null || mCardNumber.equals("")) {
            needUpdate = true;
        }
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " onItemClick mCardNumber = " + mCardNumber + " needUpdate = " + needUpdate);
        }
        mCardNumber = mDvbPlayer.getCacardSN();
        if (mCardNumber == null || mCardNumber.equals("")) {
            mCaCardNumTv.setText(getResources().getString(R.string.ca_card_num_text, getString(R.string.ca_card_not_finded)));
            Toast.makeText(getActivity(), getString(R.string.ca_card_not_finded), Toast.LENGTH_SHORT).show();
        } else {
            if (needUpdate) {
                mMenuListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSimpleAdapter = new SimpleAdapter(getActivity(), getData(),
                                R.layout.ca_menu_listview_item,
                                mAdapterFrom,
                                mAdapterTo);
                        mMenuListView.setAdapter(mSimpleAdapter);
                        mSimpleAdapter.notifyDataSetInvalidated();
                        mCaCardNumTv.setText(getResources().getString(R.string.ca_card_num_text, mCardNumber));
                    }
                }, 500);
            }
            showDialog();
        }
    }

    @Override
    public void onPause() {
        JLog.d(TAG, TAG + " onPause ");
        super.onPause();
    }

    @Override
    public void onResume() {
        JLog.d(TAG, TAG + " onResume ");
        mMenuListView.requestFocus();
        super.onResume();
    }

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.ca_main_container, fragment);
        ft.commit();
    }

    public void showDialog() {
        if (styleDialog == null) {
            mDialogContentView = inflater.inflate(R.layout.dialog_center_edittext_with_hint_view, null);
            mPassWordEt = (JEditTextWithTTF) mDialogContentView.findViewById(R.id.dialog_ca_content_view_edittext);
            mHindTv = (JTextViewWithTTF) mDialogContentView.findViewById(R.id.dialog_ca_content_view_hint);

            StyleDialog.Builder builder = new StyleDialog.Builder(getActivity());
            builder.setContentView(mDialogContentView,
                    new RelativeLayout.LayoutParams(getResources().getInteger(R.integer.portal_ca_dialog_width), getResources().getInteger(
                            R.integer.portal_ca_dialog_height)));
            builder.setPositiveButton(getString(R.string.ca_dialog_ok));
            builder.setNegativeButton(getString(R.string.ca_dialog_cancle));
            builder.setBlurView(getActivity().getWindow().getDecorView());
            builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    JLog.d(TAG, " onClick which = " + which + " mWhichItem = " + mWhichItem);
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            if (isRightPassWord()) {
                                dialog.dismiss();
                                switch (mWhichItem) {
                                    case 0:
                                        setFragment(new CaWorkTimeFragment());
                                        break;
                                    case 1:
                                        setFragment(new CaWatchLevel());
                                        break;
                                    case 2:
                                        setFragment(new CaAuthoriseFragment());
                                        break;
                                    case 3:
                                        setFragment(new CaPassWordFragment());
                                        break;
                                }
                            }
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            });
            styleDialog = builder.show();
        }
        mPassWordEt.setText("");
        mHindTv.setText("");
        mPassWordEt.setNextFocusDownId(R.id.dialog_bt_yes);

        if (!styleDialog.isShowing()) {
            styleDialog.show();
        }
        mPassWordEt.requestFocus();
    }

    public void showOperateMsg(int witch) {
        switch (witch) {
            case CardOperateCode.OK:
                break;
            case CardOperateCode.NO_CARD:
                mHindTv.setText(getString(R.string.ca_card_not_finded));
                break;
            case CardOperateCode.PIN_INVALID:
                mHindTv.setText(getString(R.string.ca_password_error_text));
                break;
            default:
                mHindTv.setText(getString(R.string.ca_operate_msg_unknow_error));
                break;
        }
    }
}
