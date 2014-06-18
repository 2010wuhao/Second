/**
 * =====================================================================
 *
 * @file  SearchPageCardsFragment.java
 * @Module Name   com.joysee.dvb.search
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-21
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
 * benz          2014-1-21           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.search;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.common.utils.JLog;
import com.joysee.common.utils.JNet;
import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.data.DvbSettings;
import com.joysee.dvb.data.SearchParamsReader;
import com.joysee.dvb.data.SearchParamsReader.ModulationType;
import com.joysee.dvb.search.wheelview.AbstractWheelTextAdapter;
import com.joysee.dvb.search.wheelview.WheelView;

public class SearchPageCardsFragment extends BaseFragment {

    class QamAdapter extends AbstractWheelTextAdapter {
        private String[] provinces;

        public QamAdapter(Context context, String[] provinces) {
            super(context, R.layout.search_pull_down_list_item, R.id.search_down_list_textview);
            this.provinces = provinces;
        }

        @Override
        public String getCurrentItemValue(int currentIndex) {
            return provinces[currentIndex];
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            return view;
        }

        @Override
        public int getItemsCount() {
            return provinces.length;
        }

        @Override
        protected CharSequence getItemText(int index) {
            return provinces[index];
        }

        @Override
        public boolean isAvailableData() {
            return false;
        }

        @Override
        public void setDataAvailable(boolean availa) {
        }
    }

    private static final String TAG = JLog.makeTag(SearchPageCardsFragment.class);;

    private int mSearchType;
    private RelativeLayout mRootView;
    private JTextViewWithTTF mTitleTv;
    private JButtonWithTTF mSearchBt;
    private SearchEditNumText mSymbolEditText;
    private SearchEditNumText mFrequencyEditText;
    private TextView mParamQamTv;
    private LinearLayout mParamQamSelectLayout;
    private Transponder mTransponder;
    private String mTitleStr;

    private JTextViewWithTTF mCurrentOperatorInfo;

    private FragmentImpl mFragmentImpl;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean ret = false;
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                mFragmentImpl.onPageKeyEvent(null);
                ret = true;
            }
        }
        return ret;
    }

    private void getDefaultParams() {
        StringBuffer sb = new StringBuffer();
        sb.append("switch search type ---> ");
        switch (mSearchType) {
            case FragmentEvent.SearchType_FAST:
                mTitleStr = getResources().getString(R.string.search_tv_mode_fast);
                mTransponder = SearchParamsReader.getTransponderBySearchMode(getActivity(), SearchParamsReader.FAST_SEARCH);
                sb.append("AUTO");
                break;
            case FragmentEvent.SearchType_MANUAL:
                mTitleStr = getResources().getString(R.string.search_tv_mode_manual);
                mTransponder = SearchParamsReader.getTransponderBySearchMode(getActivity(), SearchParamsReader.MANUAL_SEARCH);
                sb.append("MANUAL");
                break;
            case FragmentEvent.SearchType_FULL:
                mTitleStr = "";// getResources().getString(R.string.search_tv_mode_full);
                mTransponder = SearchParamsReader.getTransponderBySearchMode(getActivity(), SearchParamsReader.FULL_SEARCH);
                sb.append("ALL");
                break;
            case FragmentEvent.SearchType_NET:
                mTitleStr = getResources().getString(R.string.search_tv_mode_net);
                mTransponder = null;
                sb.append("NET");
                break;
            default:
                mTitleStr = "";
                sb.append("UNKOWN");
                break;
        }
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, sb.toString());
        }
    }

    private int getOperatorCode() {
        return DvbSettings.System.getInt(getActivity().getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_CODE, -1);
    }

    private String getOperatorInfo() {
        String title = getString(R.string.search_msg_net_search_current_operator);
        String defaultConent = "空";
        String areaInfo = DvbSettings.System.getString(getActivity().getContentResolver(), DvbSettings.System.LOCAL_AREA_INFO);
        String city = areaInfo != null && !"".equals(areaInfo) ? areaInfo.split("-")[1] : "";
        String operator = DvbSettings.System.getString(getActivity().getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_NAME);
        return (operator == null || "".equals(operator)) ? (title + defaultConent) : (title + city + "," + operator);
    }

    private String getQamByModulationType(int type) {
        String qamStr;
        switch (type) {
            case ModulationType.MODULATION_64QAM:
                qamStr = getString(R.string.search_tv_qam_64);
                break;
            case ModulationType.MODULATION_128QAM:
                qamStr = getString(R.string.search_tv_qam_128);
                break;
            case ModulationType.MODULATION_256QAM:
                qamStr = getString(R.string.search_tv_qam_256);
                break;
            case ModulationType.MODULATION_DTMB:
                qamStr = getString(R.string.search_tv_qam_dtmb);
                break;
            default:
                qamStr = "";
                break;
        }
        return qamStr;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        performLinstener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFragmentImpl = (FragmentImpl) activity;
        mSearchType = getArguments().getInt(FragmentEvent.FragmentBundleTag, FragmentEvent.SearchType_FAST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (RelativeLayout) inflater.inflate(R.layout.search_page_cards_fragment, null);
        performData();
        performLayout();
        return mRootView;
    }

    private void performData() {
        getDefaultParams();
    }

    private void performLayout() {
        mTitleTv = (JTextViewWithTTF) mRootView.findViewById(R.id.title);
        mTitleTv.setText(mTitleStr);
        mSearchBt = (JButtonWithTTF) mRootView.findViewById(R.id.search_bt);
        if (mSearchType == FragmentEvent.SearchType_FULL || mSearchType == FragmentEvent.SearchType_MANUAL) {
            mRootView.findViewById(R.id.search_params_layout).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.button_be_margin_top).setVisibility(View.VISIBLE);
            if (mSearchType == FragmentEvent.SearchType_FULL) {
                mRootView.findViewById(R.id.frequency_layout).setVisibility(View.GONE);
            } else {
                mFrequencyEditText = (SearchEditNumText) mRootView.findViewById(R.id.search_frequency_edit);
                mFrequencyEditText.setTextNumber(mTransponder.getFrequency() / 1000);
            }
            mSymbolEditText = (SearchEditNumText) mRootView.findViewById(R.id.search_symbol_edit);
            mParamQamTv = (TextView) mRootView.findViewById(R.id.search_param_qam);
            mParamQamSelectLayout = (LinearLayout) mRootView.findViewById(R.id.search_param_qam_layout);
            mSymbolEditText.setTextNumber(mTransponder.getSymbolRate());
            mParamQamTv.setText(getQamByModulationType(mTransponder.getModulation()));
        }
        if (mSearchType == FragmentEvent.SearchType_NET) {
            if (getOperatorCode() == -1) {
                mSearchBt.setText(getResources().getString(R.string.search_tv_mode_operator));
            }
            Toast.makeText(getActivity(), getOperatorInfo(), Toast.LENGTH_LONG).show();
        }
        mSearchBt.requestFocus();

    }

    private void performLinstener() {
        mSearchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveParamsValue();
                if (mSearchType == FragmentEvent.SearchType_NET && getOperatorCode() == -1) {
                    if (JNet.isConnected(getActivity())) {
                        FragmentEvent event = new FragmentEvent();
                        Bundle bundle = new Bundle();
                        boolean isSingle = false;
                        bundle.putBoolean(FragmentEvent.FragmentBundleTag, isSingle);
                        event.nextFragment = new OperatorSettingFragment();
                        event.nextFragment.setArguments(bundle);
                        event.eventCode = FragmentEvent.SWTICH_FRAGMENT;
                        mFragmentImpl.onPageKeyEvent(event);
                    } else {
                        Toast.makeText(getActivity(), "当前网络不可用", Toast.LENGTH_LONG).show();
                    }
                } else {
                    FragmentEvent event = new FragmentEvent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(FragmentEvent.FragmentBundleTag, mSearchType);
                    event.nextFragment = new SearchProcessFragment();
                    event.nextFragment.setArguments(bundle);
                    event.eventCode = FragmentEvent.SWTICH_FRAGMENT;
                    mFragmentImpl.onPageKeyEvent(event);
                }
            }
        });
        if (mParamQamSelectLayout != null) {
            mParamQamSelectLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showQamSelect(mParamQamTv.getText().toString());
                }
            });
        }
    }

    private void saveParamsValue() {
        if (mTransponder != null && (mSearchType == FragmentEvent.SearchType_FULL || mSearchType == FragmentEvent.SearchType_MANUAL)) {
            String qamStr = mParamQamTv.getText().toString();
            String symbolRateStr = mSymbolEditText.getText().toString();

            mTransponder.setSymbolRate(Integer.parseInt(symbolRateStr));
            if ("64".equals(qamStr)) {
                mTransponder.setModulation(ModulationType.MODULATION_64QAM);
            } else if ("128".equals(qamStr)) {
                mTransponder.setModulation(ModulationType.MODULATION_128QAM);
            } else if ("256".equals(qamStr)) {
                mTransponder.setModulation(ModulationType.MODULATION_256QAM);
            } else if ("DTMB".equals(qamStr)) {
                mTransponder.setModulation(ModulationType.MODULATION_DTMB);
            }

            switch (mSearchType) {
                case FragmentEvent.SearchType_FULL:
                    SearchParamsReader.updateTransponder(getActivity(), SearchParamsReader.FULL_SEARCH, mTransponder);
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG, "saveFullTransponer ---> " + "    symbolRateStr = " + symbolRateStr + "modulationStr = " + qamStr);
                    }
                    break;
                case FragmentEvent.SearchType_MANUAL:
                    String frequencyStr = mFrequencyEditText.getText().toString();
                    mTransponder.setFrequency(Integer.parseInt(frequencyStr) * 1000);
                    SearchParamsReader.updateTransponder(getActivity(), SearchParamsReader.MANUAL_SEARCH, mTransponder);
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG, "saveManualTransponerToXml ---> " + "frequencyStr = " + frequencyStr + " symbolRateStr = " + 
                                symbolRateStr + "modulationStr = " + qamStr);
                    }
                    break;
            }
        }
    }

    private void showQamSelect(String lastQamStr) {
        final Dialog dialog = new Dialog(getActivity(), R.style.search_pull_down_list_dialog);

        String[] qams = {
                "64", "128", "256", "DTMB"
        };
        int lastSelect = 0;
        for (int i = 0; i < qams.length; i++) {
            if (lastQamStr.equals(qams[i])) {
                lastSelect = i;
                break;
            }
        }
        final WheelView wheelView = (WheelView) LayoutInflater.from(getActivity()).inflate(R.layout.search_pull_down_list_layout, null);
        final QamAdapter adapter = new QamAdapter(getActivity(), qams);
        wheelView.setCyclic(true);
        wheelView.setViewAdapter(adapter);
        wheelView.setCurrentItem(lastSelect);
        wheelView.setCenterDrawable(getResources().getDrawable(R.drawable.button_default_selected));
        wheelView.requestFocus();
        wheelView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mParamQamTv.setText(adapter.getCurrentItemValue(wheelView.getCurrentItem()));
                dialog.dismiss();
            }
        });
        wheelView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean ret = false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        wheelView.setCurrentItem(wheelView.getCurrentItem() - 1, true);
                        ret = true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        wheelView.setCurrentItem(wheelView.getCurrentItem() + 1, true);
                        ret = true;
                    }
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                        mParamQamTv.setText(adapter.getCurrentItemValue(wheelView.getCurrentItem()));
                        dialog.dismiss();
                        ret = true;
                    }
                }
                return ret;
            }
        });
        dialog.setContentView(wheelView);

        int[] location = new int[2];
        mParamQamSelectLayout.getLocationInWindow(location);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        int x = (int) getResources().getDimension(R.dimen.screen_width) / 2;
        int y = (int) getResources().getDimension(R.dimen.screen_height) / 2;
        lp.width = mParamQamSelectLayout.getWidth();
        lp.height = mParamQamSelectLayout.getHeight() * 3;
        lp.dimAmount = 0.8f;
        lp.flags = android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.x = location[0] - x + lp.width / 2;
        lp.y = location[1] + mParamQamSelectLayout.getHeight() / 2 - y;
        window.setAttributes(lp);
        dialog.show();
    }

}
