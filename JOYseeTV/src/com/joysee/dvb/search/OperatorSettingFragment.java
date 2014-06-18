/**
 * =====================================================================
 *
 * @file  SearchAreaLayout.java
 * @Module Name   com.joysee.adtv.ui.search
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013-12-21
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
 * benz          2013-12-21           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.search;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.TaskInfo;
import com.joysee.common.data.JHttpHelper;
import com.joysee.common.data.JHttpParserCallBack;
import com.joysee.common.data.JRequestParams;
import com.joysee.common.utils.JLocationClient;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JStyleDialog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.data.AreaReader;
import com.joysee.dvb.data.LocalInfoReader;
import com.joysee.dvb.search.wheelview.ArrayWheelAdapter;
import com.joysee.dvb.search.wheelview.OperatorParser;
import com.joysee.dvb.search.wheelview.ProvinceAdapter;
import com.joysee.dvb.search.wheelview.WheelView;
import com.joysee.dvb.search.wheelview.WheelView.OnWheelChangedListener;
import com.joysee.dvb.search.wheelview.WheelView.OnWheelScrollListener;
import com.joysee.dvb.widget.LoadingDialogUtil;
import com.joysee.dvb.widget.ProgressWheel;
import com.joysee.dvb.widget.StyleDialog;
import com.joysee.dvb.widget.ProgressWheel.ProgressMode;
import com.joysee.dvb.widget.shimmer.Shimmer;
import com.joysee.dvb.widget.shimmer.ShimmerTextView;

import java.util.ArrayList;
import java.util.HashSet;

public class OperatorSettingFragment extends BaseFragment {

    public enum Level {
        PROVINCS,
        CITY,
        COUNTY,
        OPERATOR
    }

    protected static final String TAG = JLog.makeTag(OperatorSettingFragment.class);
    private static final int MSG_GET_OPERATOR_CODE_SUCCESS = 0;
    private static final int MSG_LOAD_DATA_COMPLETED = 1;
    private static final int MSG_LOAD_DB_FAILED = 2;
    private static final int MSG_DELAY_TO_HIDE_DIALOG = 3;
    private static final int MSG_REFRESH_LOACALSETTING_UI = 4;

    private Level mLevel = Level.PROVINCS;
    private JStyleDialog mWaitDialog;
    private RelativeLayout mRootView;
    private FragmentImpl mFragmentImpl;
    private ShimmerTextView mOperatorSettingPoint;
    private JTextViewWithTTF mCurrentOperator;
    private Shimmer mShimmer;

    private ArrayWheelAdapter<String> mCityAdapter;
    private ArrayWheelAdapter<String> mCountyAdapter;
    private ArrayWheelAdapter<String> mOperatorAdapter;
    private WheelView mProvinceWheelView;
    private WheelView mCityWheelView;
    private WheelView mCountyWheelView;
    private WheelView mOperatorWheelView;
    private String mLastSelectCity;
    private String mLastSelectCounty;
    private String mLastSelectOPerator;
    private String mLastSelectProvinces;

    private boolean mIsScrolling;
    private boolean mIsSingle;
    private boolean isLoadingPosition;
    private boolean isLoadingData;

    private StyleDialog mLocalSettingDialog;
    private ViewGroup mLocalSettingUI;
    private TaskInfo mLocalSettingTaskInfo;

    private WeakHandler<OperatorSettingFragment> mWeakHandler = new WeakHandler<OperatorSettingFragment>(this) {

        protected void weakReferenceMessage(Message msg) {
            if (getActivity() == null) {
                return;
            }
            switch (msg.what) {
                case MSG_GET_OPERATOR_CODE_SUCCESS:
                    // 获取当前的区域码
                    String temp = mCountyAdapter.getCurrentItemValue(mCountyWheelView.getCurrentItem());
                    String currentOperatorCode = (temp != null && !"".equals(temp)) ? temp.split(":")[1] : "";
                    if (msg.arg1 == Integer.valueOf(currentOperatorCode)) {
                        String[] array = (String[]) msg.obj;
                        if (array == null || array.length == 0) {
                            refreshOperatorWheel(new String[] {
                                    ""
                            }, false);
                        } else {
                            refreshOperatorWheel(array, true);
                        }
                    }
                    break;
                case MSG_LOAD_DATA_COMPLETED:
                    if (!isLoadingData && !isLoadingPosition) {
                        drawViews();
                    }
                    break;
                case MSG_LOAD_DB_FAILED:
                    hideWaitDialog();
                    Toast.makeText(getActivity(), "load db fail", Toast.LENGTH_LONG).show();
                    exit();
                    break;
                case MSG_DELAY_TO_HIDE_DIALOG:
                    if (!isLoadingData && !isLoadingPosition) {
                        hideWaitDialog();
                    }
                    break;
                case MSG_REFRESH_LOACALSETTING_UI:
                    TaskInfo task = (TaskInfo) msg.obj;
                    if (task == null) {
                        mLocalSettingDialog.dismissByAnimation();
                        Toast.makeText(getActivity(), "TaskInfo is null", Toast.LENGTH_SHORT).show();
                    } else {
                        if (TvApplication.DEBUG_LOG) {
                            JLog.d(TAG, task.toString());
                        }
                        LocalSettingViewHolder holder = (LocalSettingViewHolder) mLocalSettingUI.getTag();
                        switch (task.mCBType) {
                            case JDVBPlayer.CALLBACK_LOCALDATA_UPDATE_STATUS:
                                if (task.mStatusCode == JDVBPlayer.TASK_STATUS_START_DOWNLOAD) {
                                    holder.progressWheel.stop();
                                    holder.progressWheel.setMode(ProgressMode.PERCENTAGE);
                                    holder.progressWheel.setProgress(task.mProgress);
                                    holder.progressWheel.start();
                                    holder.textView.setText(R.string.operator_begin_sync);
                                } else if (task.mStatusCode == JDVBPlayer.TASK_STATUS_DOWNLOAD_SUCCESS) {
                                    mLocalSettingDialog.refreshNegativeButtonText(mLocalSettingDialog.getBuilder(),
                                            R.string.jstyle_dialog_postive_bt);
                                    holder.textView.setText(R.string.operator_sync_success);
                                    JDVBPlayer.getInstance().setOperatorCode(LocalInfoReader.getOperatorCode(getActivity()) + "");
                                    Toast.makeText(getActivity(), R.string.operator_sync_success, Toast.LENGTH_SHORT).show();
                                } else {
                                    mLocalSettingDialog.refreshNegativeButtonText(mLocalSettingDialog.getBuilder(),
                                            R.string.jstyle_dialog_postive_bt);
                                    if (task.mStatusCode == JDVBPlayer.TASK_STATUS_NO_UPDATE_VERSION) {
                                        holder.textView.setText(R.string.opertaor_no_new_version);
                                    } else {
                                        holder.textView.setText(R.string.operator_sync_failed);
                                    }
                                    holder.progressWheel.stop();
                                }
                                break;
                            case JDVBPlayer.CALLBACK_LOCALDATA_UPDATE_VERSION_FOUND:
                                break;
                            case JDVBPlayer.CALLBACK_LOCALDATA_UPDATE_PROGRESS:
                                holder.progressWheel.setProgress(task.mProgress);
                                break;
                        }
                    }
                    break;
            }
        };
    };

    private class MyTaskInfoListener implements JDVBPlayer.OnTaskInfoListener {
        @Override
        public void onTaskInfo(TaskInfo task) {
            Message msg = new Message();
            msg.obj = task;
            msg.what = MSG_REFRESH_LOACALSETTING_UI;
            mWeakHandler.sendMessage(msg);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFragmentImpl = (FragmentImpl) activity;
        mIsSingle = getArguments().getBoolean(FragmentEvent.FragmentBundleTag, true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showWaitDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (RelativeLayout) inflater.inflate(R.layout.area_setting_fragment, null);
        performLayout();
        return mRootView;
    }

    private void beginLocate() {
        if (!LocalInfoReader.isSettingOperator(getActivity())) {
            isLoadingPosition = true;
            JLocationClient client = new JLocationClient(getActivity(), new JLocationClient.Callback() {
                @Override
                public void onDataReady(JLocationClient arg0) {
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG, "loadLocationInfo--->onDataReady : " + JLocationClient.getCity());
                    }
                    mLastSelectProvinces = JLocationClient.getProvince();
                    mLastSelectCity = JLocationClient.getCity();
                    mLastSelectCounty = JLocationClient.getCounty();
                    mLastSelectOPerator = null;
                    isLoadingPosition = false;
                    mWeakHandler.sendEmptyMessage(MSG_LOAD_DATA_COMPLETED);
                }

                @Override
                public void onGetError() {
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG, "loadLocationInfo--->onGetError");
                    }
                    isLoadingPosition = false;
                    mLastSelectProvinces = "北京市";
                    mLastSelectCity = "北京市";
                    mWeakHandler.sendEmptyMessage(MSG_LOAD_DATA_COMPLETED);
                }
            });
            client.setAk("79fa1f53d9427e161827d8e598e07e50");
            client.initDataSync();
        }
    }

    private void performLayout() {
        mShimmer = new Shimmer();
        mCurrentOperator = (JTextViewWithTTF) mRootView.findViewById(R.id.search_area_setting_current_operator);
        mOperatorSettingPoint = (ShimmerTextView) mRootView.findViewById(R.id.search_area_setting_prompt);
        if (!isSupportLocalPackUpdate()) {
            mOperatorSettingPoint.setText(R.string.search_msg_area_setting_point2);
        }
        mProvinceWheelView = (WheelView) mRootView.findViewById(R.id.province);
        mCityWheelView = (WheelView) mRootView.findViewById(R.id.city);
        mCountyWheelView = (WheelView) mRootView.findViewById(R.id.county);
        mOperatorWheelView = (WheelView) mRootView.findViewById(R.id.operator);
        mProvinceWheelView.setVisibleItems(7);
        mCityWheelView.setVisibleItems(7);
        mCountyWheelView.setVisibleItems(7);
        mOperatorWheelView.setVisibleItems(7);
        performLinstener();
        // 恢复上一次select
        readViewInstanceState();
        // 定位
        beginLocate();

        if (AreaReader.isLoadOver()) {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "performLayout - has memory data");
            }
            mWeakHandler.sendEmptyMessage(MSG_LOAD_DATA_COMPLETED);
        } else {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "performLayout - load DB data");
            }
            loadAreaData();
        }

    }

    private void performLinstener() {
        mProvinceWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!mIsScrolling) {
                    refreshCityWheel(wheel.getViewAdapter().getCurrentItemValue(newValue));
                }
            }
        });
        mProvinceWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingFinished(WheelView wheel, int currentItem) {
                mIsScrolling = false;
                refreshCityWheel(wheel.getViewAdapter().getCurrentItemValue(currentItem));
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "mProvinceWheelView  onScrollingFinished   select item = " + currentItem);
                }
            }

            @Override
            public void onScrollingStarted(WheelView wheel) {
                mIsScrolling = true;
            }
        });

        mCityWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!mIsScrolling) {
                    refreshCountyWheel(wheel.getViewAdapter().getCurrentItemValue(newValue));
                }
            }
        });
        mCityWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingFinished(WheelView wheel, int currentItem) {
                mIsScrolling = false;
                refreshCountyWheel(wheel.getViewAdapter().getCurrentItemValue(currentItem));
            }

            @Override
            public void onScrollingStarted(WheelView wheel) {
                mIsScrolling = true;
            }
        });

        mCountyWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!mIsScrolling) {
                    requesetOperator(mCountyWheelView.getViewAdapter().getCurrentItemValue(newValue));
                }
            }
        });
        mCountyWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingFinished(WheelView wheel, int currentItem) {
                mIsScrolling = false;
                requesetOperator(mCountyWheelView.getViewAdapter().getCurrentItemValue(currentItem));
            }

            @Override
            public void onScrollingStarted(WheelView wheel) {
                mIsScrolling = true;
            }
        });

        mOperatorWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
            }
        });
        mOperatorWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingFinished(WheelView wheel, int currentItem) {
                mIsScrolling = false;
            }

            @Override
            public void onScrollingStarted(WheelView wheel) {
                mIsScrolling = true;
            }
        });
    }

    /**
     * draw WheelView
     */
    private void drawViews() {
        refreshOperatorPoint(getOperatorInfo());

        ArrayList<String> provinces = AreaReader.getProvinces();
        int size = provinces.size();
        String[] provinceArray = new String[size];
        provinces.toArray(provinceArray);
        mProvinceWheelView.setCyclic(size > 5 ? true : false);
        mProvinceWheelView.setViewAdapter(new ProvinceAdapter(getActivity(), provinceArray));
        mProvinceWheelView.setCurrentItem(getSelectPositionByValue(provinceArray, mLastSelectProvinces));
        mProvinceWheelView.setCenterDrawable(getActivity().getResources().getDrawable(R.drawable.area_wheel_view_focus));
        mProvinceWheelView.requestFocus();

        mWeakHandler.sendEmptyMessageDelayed(MSG_DELAY_TO_HIDE_DIALOG, 500L);
    }

    private void readViewInstanceState() {
        mLastSelectProvinces = null;
        mLastSelectCity = null;
        mLastSelectCounty = null;
        mLastSelectOPerator = null;
        String[] dataArray = LocalInfoReader.getLocalInfoArray(getActivity());
        if (dataArray != null) {
            int lastStatusCount = dataArray.length;
            switch (lastStatusCount) {
                case 1:
                    mLastSelectProvinces = dataArray[0];
                    break;
                case 2:
                    mLastSelectProvinces = dataArray[0];
                    mLastSelectCity = dataArray[1];
                    break;
                case 3:
                    mLastSelectProvinces = dataArray[0];
                    mLastSelectCity = dataArray[1];
                    mLastSelectCounty = dataArray[2];
                    break;
            }
        }
        mLastSelectOPerator = LocalInfoReader.getOperatorName(getActivity());
    }

    private void exit() {
        if (mIsSingle) {
            mFragmentImpl.onPageKeyEvent(null);
        } else {
            FragmentEvent fragmentEvent = new FragmentEvent();
            Bundle bundle = new Bundle();
            bundle.putInt(FragmentEvent.FragmentBundleTag, FragmentEvent.SearchType_NET);
            fragmentEvent.nextFragment = new SearchPageCardsFragment();
            fragmentEvent.nextFragment.setArguments(bundle);
            fragmentEvent.eventCode = FragmentEvent.SWTICH_FRAGMENT;
            mFragmentImpl.onPageKeyEvent(fragmentEvent);
        }
    }

    private String getOperatorInfo() {
        String title = getString(R.string.search_msg_net_search_current_operator);
        String defaultConent = "空";
        String areaInfo = LocalInfoReader.getLocalInfo(getActivity());
        String operator = LocalInfoReader.getOperatorName(getActivity());
        String city = areaInfo != null && !areaInfo.isEmpty() ? areaInfo.split("-")[1] : "";
        return (operator == null || "".equals(operator)) ? (title + defaultConent) : (title + city + "," + operator);
    }

    private int getDefaultSelection(int totalCount) {
        int select = 0;
        if (totalCount < 3) {
            select = 0;
        } else if (totalCount == 3 || totalCount == 4) {
            select = 1;
        } else if (totalCount == 5) {
            select = 2;
        } else {
            select = totalCount / 2;
        }
        return select;
    }

    private int getSelectPositionByValue(String[] array, String value) {
        int size = array.length;
        int select = -1;
        if (value != null && !"".equals(value)) {
            for (int i = 0; i < size; i++) {
                if (array[i].contains(value)) {
                    select = i;
                    break;
                }
            }
        }
        select = select != -1 ? select : getDefaultSelection(size);
        return select;
    }

    private void hideWaitDialog() {
        if (mWaitDialog != null && mWaitDialog.isShowing()) {
            mWaitDialog.dismiss();
        }
        mShimmer.start(mOperatorSettingPoint);
    }

    private void loadAreaData() {
        isLoadingData = true;
        new Thread() {
            public void run() {
                Process.setThreadPriority(MIN_PRIORITY);
                long begin = 0;
                if (TvApplication.DEBUG_LOG) {
                    begin = JLog.methodBegin(TAG);
                }
                boolean isLoadDBFinish = AreaReader.loadDB(getActivity());
                isLoadingData = false;
                mWeakHandler.sendEmptyMessage(isLoadDBFinish ? MSG_LOAD_DATA_COMPLETED : MSG_LOAD_DB_FAILED);
                if (TvApplication.DEBUG_LOG) {
                    JLog.methodEnd(TAG, begin, "load area db, isLoadDBFinish=" + isLoadDBFinish);
                }
            };
        }.start();
    }

    public void logArray(String msg, String[] strs) {
        for (String s : strs) {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "  name-->" + s);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideWaitDialog();
    }

    private void refreshCityWheel(String province) {
        HashSet<String> citySet = AreaReader.getCitysByProvince(province);
        String[] cities = new String[citySet.size()];
        citySet.toArray(cities);
        int count = cities.length;
        int select;
        if (mCityAdapter == null) {
            mCityAdapter = new ArrayWheelAdapter<String>(getActivity(), cities);
            mCityWheelView.setViewAdapter(mCityAdapter);
            select = getSelectPositionByValue(cities, mLastSelectCity);
        } else {
            mCityAdapter.refreshData(cities);
            select = getDefaultSelection(count);
        }
        mCityAdapter.notifyDataInvalidatedEvent();
        mCityWheelView.setCurrentItem(select);
        mCityWheelView.setCyclic(count > 5 ? true : false);
    }

    private void refreshCountyWheel(String city) {
        ArrayList<String> countyList = AreaReader.getCountyByCity(city);
        String[] counties = new String[countyList.size()];
        countyList.toArray(counties);
        int count = counties.length;
        int select;
        if (mCountyAdapter == null) {
            mCountyAdapter = new ArrayWheelAdapter<String>(getActivity(), counties);
            mCountyWheelView.setViewAdapter(mCountyAdapter);
            select = getSelectPositionByValue(counties, mLastSelectCounty);
        } else {
            mCountyAdapter.refreshData(counties);
            select = getDefaultSelection(count);
        }
        mCountyAdapter.setSplitByTag(true);// 县、区传进去的字符串为：name:code
        // ，所以显示时需要把名字提取出来，采用String.split(":")[0]
        // ;
        mCountyAdapter.notifyDataInvalidatedEvent();
        mCountyWheelView.setCurrentItem(select);
        mCountyWheelView.setCyclic(count > 5 ? true : false);
    }

    private void refreshOperatorPoint(String operatorInfo) {
        if (mCurrentOperator != null) {
            mCurrentOperator.setText(operatorInfo);
        }
    }

    private void refreshOperatorWheel(String[] array, boolean hasOperator) {
        int count = array.length;
        int selectItem;

        if (mOperatorAdapter == null) {
            selectItem = getSelectPositionByValue(array, mLastSelectOPerator);
            mOperatorAdapter = new ArrayWheelAdapter<String>(getActivity(), array);
            mOperatorWheelView.setViewAdapter(mOperatorAdapter);
        } else {
            selectItem = getDefaultSelection(count);
            mOperatorAdapter.refreshData(array);
        }
        logArray("operator", array);
        mOperatorAdapter.setSplitByTag(hasOperator);
        mOperatorAdapter.setDataAvailable(hasOperator);
        mOperatorAdapter.notifyDataInvalidatedEvent();
        mOperatorWheelView.setCurrentItem(selectItem);
        mOperatorWheelView.setCyclic(count > 5 ? true : false);
    }

    private void requesetOperator(final String zoneCode) {
        if (zoneCode == null || "".equals(zoneCode)) {
            return;
        }
        JHttpHelper.cancelJsonRequests(getActivity(), true);
        JRequestParams params = new JRequestParams();
        final String code = zoneCode.split(":")[1];
        params.put("code", code);
        JHttpHelper.getJson(getActivity(), Constants.OPERATOR_INTERFACE, params, new JHttpParserCallBack(new OperatorParser()) {
            @Override
            public void onFailure(int arg0, Throwable e) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "refreshOperatorWheel   zoneCode=" + zoneCode + "    onFailure=" + e.toString());
                }
            }

            @Override
            public void onSuccess(Object obj) {
                String[] operatorList = (String[]) obj;
                mWeakHandler.sendMessage(mWeakHandler.obtainMessage(MSG_GET_OPERATOR_CODE_SUCCESS, Integer.valueOf(code), 0, obj));
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "refreshOperatorWheel   zoneCode=" + zoneCode + "    onSuccess=" + operatorList);
                }
            }
        });

        if (mOperatorAdapter != null) {
            String[] temps = {
                    ""
            };
            mOperatorAdapter.refreshData(temps);
            mOperatorAdapter.setDataAvailable(false);
            mOperatorAdapter.notifyDataInvalidatedEvent();
            mOperatorWheelView.setCurrentItem(0);
        }
    }

    private void saveLocalInfo() {
        String province = mProvinceWheelView.getViewAdapter().getCurrentItemValue(mProvinceWheelView.getCurrentItem());
        String city = mCityAdapter.getCurrentItemValue(mCityWheelView.getCurrentItem());
        String county = mCountyAdapter.getCurrentItemValue(mCountyWheelView.getCurrentItem());
        String operator = mOperatorAdapter.getCurrentItemValue(mOperatorWheelView.getCurrentItem());
        LocalInfoReader.save(getActivity(), province, city, county, operator);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mIsScrolling) {
            return true;
        }
        boolean ret = false;
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                scrollUp(mLevel);
                ret = true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                scrollDown(mLevel);
                ret = true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (mLevel == Level.PROVINCS) {
                    mLevel = Level.CITY;
                    mCityWheelView.requestFocus();
                    mCityWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_focus));
                    mProvinceWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_unfocus));
                } else if (mLevel == Level.CITY) {
                    mLevel = Level.COUNTY;
                    mCountyWheelView.requestFocus();
                    mCountyWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_focus));
                    mCityWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_unfocus));
                } else if (mLevel == Level.COUNTY) {
                    if (mOperatorAdapter != null && mOperatorAdapter.getItemsCount() > 0 && mOperatorAdapter.isAvailableData()) {
                        mLevel = Level.OPERATOR;
                        mOperatorWheelView.requestFocus();
                        mOperatorWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_focus));
                        mCountyWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_unfocus));
                    }
                }
                ret = true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (mLevel == Level.CITY) {
                    mLevel = Level.PROVINCS;
                    mProvinceWheelView.requestFocus();
                    mProvinceWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_focus));
                    mCityWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_unfocus));
                    mCountyWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_unfocus));
                } else if (mLevel == Level.COUNTY) {
                    mLevel = Level.CITY;
                    mCityWheelView.requestFocus();
                    mCityWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_focus));
                    mProvinceWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_unfocus));
                    mCountyWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_unfocus));
                } else if (mLevel == Level.OPERATOR) {
                    mLevel = Level.COUNTY;
                    mCountyWheelView.requestFocus();
                    mCountyWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_focus));
                    mOperatorWheelView.setCenterDrawable(getResources().getDrawable(R.drawable.area_wheel_view_unfocus));
                }
                ret = true;
            }
            else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (mOperatorAdapter != null && mOperatorAdapter.getItemsCount() > 0 && mOperatorAdapter.isAvailableData()) {
                    saveLocalInfo();
                    String point = getOperatorInfo();
                    refreshOperatorPoint(point);
                    LocalInfoReader.setOperatorHadBeenSet(getActivity(), true);

                    if (isSupportLocalPackUpdate()) {
                        showLocalSettingDialog();
                        mLocalSettingTaskInfo = new TaskInfo(Constants.getLocalDataTaskUrl(LocalInfoReader.getOperatorCode(getActivity())));
                        JDVBPlayer.getInstance().setOnTaskInfoListener(new MyTaskInfoListener());
                        JDVBPlayer.getInstance().startTask(mLocalSettingTaskInfo);
                    } else {
                        JDVBPlayer.getInstance().setOperatorCode(LocalInfoReader.getOperatorCode(getActivity()) + "");
                        Toast.makeText(getActivity(), point, Toast.LENGTH_SHORT).show();
                        exit();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.operator_current_no_operator_found, Toast.LENGTH_SHORT).show();
                }
                ret = true;
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                exit();
                ret = true;
            }
            else if (keyCode == KeyEvent.KEYCODE_MENU) {
                ret = true;
            }
        }
        return ret;
    }

    private void scrollDown(Level l) {
        switch (l) {
            case PROVINCS:
                mProvinceWheelView.setCurrentItem(mProvinceWheelView.getCurrentItem() + 1, true);
                break;
            case CITY:
                mCityWheelView.setCurrentItem(mCityWheelView.getCurrentItem() + 1, true);
                break;
            case COUNTY:
                mCountyWheelView.setCurrentItem(mCountyWheelView.getCurrentItem() + 1, true);
                break;
            case OPERATOR:
                mOperatorWheelView.setCurrentItem(mOperatorWheelView.getCurrentItem() + 1, true);
                break;
        }
    }

    private void scrollUp(Level l) {
        switch (l) {
            case PROVINCS:
                mProvinceWheelView.setCurrentItem(mProvinceWheelView.getCurrentItem() - 1, true);
                break;
            case CITY:
                mCityWheelView.setCurrentItem(mCityWheelView.getCurrentItem() - 1, true);
                break;
            case COUNTY:
                mCountyWheelView.setCurrentItem(mCountyWheelView.getCurrentItem() - 1, true);
                break;
            case OPERATOR:
                mOperatorWheelView.setCurrentItem(mOperatorWheelView.getCurrentItem() - 1, true);
                break;
        }
    }

    private void showWaitDialog() {
        mWaitDialog = LoadingDialogUtil.getProgressBarDialog(getActivity(), 0);
        mWaitDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                boolean ret = false;
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                        mWaitDialog.dismiss();
                        exit();
                        ret = true;
                    }
                }
                return ret;
            }
        });
        mWaitDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        JDVBPlayer.getInstance().setOnTaskInfoListener(null);
        if (mLocalSettingDialog != null) {
            mLocalSettingDialog.dismiss();
        }
        if (mWaitDialog != null) {
            mWaitDialog.dismiss();
        }
    }

    private class LocalSettingViewHolder {
        ProgressWheel progressWheel;
        TextView textView;
    }

    private void showLocalSettingDialog() {
        if (mLocalSettingDialog == null) {
            StyleDialog.Builder builder = new StyleDialog.Builder(getActivity());
            builder.setTitle(R.string.operator_sync_title);

            if (mLocalSettingUI == null) {
                mLocalSettingUI = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.area_localsetting_ui, null);
                LocalSettingViewHolder holder = new LocalSettingViewHolder();
                holder.progressWheel = (ProgressWheel) mLocalSettingUI.findViewById(R.id.progress_wheel);
                holder.textView = (TextView) mLocalSettingUI.findViewById(R.id.status_msg);
                mLocalSettingUI.setTag(holder);
            }

            if (mLocalSettingUI.getParent() != null) {
                ((ViewGroup) mLocalSettingUI.getParent()).removeView(mLocalSettingUI);
            }

            builder.setContentView(mLocalSettingUI);
            builder.setNegativeButton(R.string.jstyle_dialog_negatvie_bt);
            builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LocalSettingViewHolder holder = (LocalSettingViewHolder) mLocalSettingUI.getTag();
                    holder.progressWheel.stop();
                    mLocalSettingDialog.dismissByAnimation();
                }
            });
            mLocalSettingDialog = builder.show();
            mLocalSettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    JDVBPlayer.getInstance().cancleTask(mLocalSettingTaskInfo);
                    mShimmer.start(mOperatorSettingPoint);
                }
            });
        } else {
            mLocalSettingDialog.show();
        }

        LocalSettingViewHolder holder = (LocalSettingViewHolder) mLocalSettingUI.getTag();
        holder.textView.setText(R.string.operator_sync_checking);
        holder.progressWheel.stop();
        holder.progressWheel.setMode(ProgressMode.LOADING);
        holder.progressWheel.start();

        mShimmer.cancel();
    }

    private boolean isSupportLocalPackUpdate() {
        boolean isSupport = false;
        try {
            JDVBPlayer.getInstance().getLocalPackVersionCode("test");
            isSupport = true;
        } catch (Error e) {
        }
        return isSupport;
    }

//    private int isAllowtLocalPackUpdate(String url) {
//        int isAllow = -1;
//        try {
//            isAllow = JDVBPlayer.getInstance().getLocalPackVersionCode(url);
//        } catch (Error e) {
//            JLog.d(TAG, TvApplication.DEBUG_LOG, "not support method nativeGetLocalPacketVer()");
//        }
//        return isAllow;
//    }
}
