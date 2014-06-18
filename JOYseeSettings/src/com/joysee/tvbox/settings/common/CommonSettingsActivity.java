/**
 * =====================================================================
 *
 * @file   CommonSettingsActivity.java
 * @Module Name   com.joysee.tvbox.settings.common
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 15, 2014
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
 * wumingjun         @Apr 15, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.base.StyleDialog;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

public class CommonSettingsActivity extends BaseActivity implements OnItemClickListener, OnKeyListener {

    private final static String TAG = CommonSettingsActivity.class.getSimpleName();

    private final static String PREFERENCE_PICTURE_IN_PICTURE_SELECT_INDEX = "common_pip_select_index";
    private final static String PREFERENCE_SCREEN_LOCK_SELECT_INDEX = "common_screen_lock_select_index";
    // private final static String PREFERENCE_DORMANT_SELECT_INDEX =
    // "common_dormant_select_index";

    private ListViewEx mList;
    private ListView mInnerList;
    private ArrayList<ListItem> mArray;
    private Context mContext;

    private ListAnimationAdapter mAdapter;
    private SharedPreferences mPreferences;
    private StyleDialog mRadioDialog;
    private RadioGroup mRadioGroup;

    /**
     * 输入法
     */
    private InputMethodManager mImm;
    private List<InputMethodInfo> mInputInfoList;
    private ArrayList<String> mInputInfoStringList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        mContext = this;
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputInfoList = mImm.getInputMethodList();
        mPreferences = Settings.Preferences.getSharedPreferences(mContext);

        setContentView(R.layout.activity_common_settings);
        mList = (ListViewEx) findViewById(R.id.list_common);
        mInnerList = mList.getInnerList();
        mArray = new ArrayList<ListItem>();
        mAdapter = ListFactory.initList(mArray, mList, Settings.Type.COMMON);
        if (mInnerList != null) {
            mInnerList.setOnItemClickListener(this);
            mInnerList.setOnKeyListener(this);
        }
        updateData();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();
        mAdapter.notifyDataSetChanged();
    }

    private void updateData() {
        Iterator<ListItem> iterator = mArray.iterator();
        ListItem item;
        ListItemData data;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item != null && item.getListType() == Settings.Type.COMMON) {

                if (item.getIndex() == 1) {
                    int selectIndex = mPreferences.getInt(CommonDeviceNameActivity.PREFERENCE_DEVICE_NAME_INDEX, 0);
                    item.setSelectDataIndex(selectIndex + 1);
                    if (selectIndex == 4) {
                        data = item.getDataMap().get(item.getSelectDataIndex());
                        String deviceName = mPreferences.getString(CommonDeviceNameActivity.PREFERENCE_DEVICE_NAME_USER_DEFINE, "");
                        data.setTitle(deviceName);
                    } else {

                    }
                } else if (item.getIndex() == 2) {
                    int selectIndex = mPreferences.getInt(PREFERENCE_PICTURE_IN_PICTURE_SELECT_INDEX, 0);
                    item.setSelectDataIndex(selectIndex + 1);
                } else if (item.getIndex() == 3) {
                    // 输入法
                    updateInputInfo();
                } else if (item.getIndex() == 4) {
                    int selectIndex = mPreferences.getInt(PREFERENCE_SCREEN_LOCK_SELECT_INDEX, 0);
                    item.setSelectDataIndex(selectIndex + 1);

                    try {
                        int state = android.provider.Settings.System.getInt(mContext.getContentResolver(), "dim_screen");
                        if (state == 0) {
                            item.setSelectDataIndex(1);
                        } else if (state == 1) {
                            int mills = android.provider.Settings.System.getInt(mContext.getContentResolver(), "dim_screen_timeout");
                            HashMap<Integer, ListItemData> map = item.getDataMap();
                            for (int i = 1; i <= map.size(); i++) {
                                data = map.get(i);
                                if (data != null && data.getValue() == mills) {
                                    item.setSelectDataIndex(data.getIndex());
                                }
                            }
                        }
                    } catch (SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (item.getIndex() == 5) {
                    // int selectIndex =
                    // mPreferences.getInt(PREFERENCE_DORMANT_SELECT_INDEX, 0);
                    // item.setSelectDataIndex(selectIndex + 1);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent();
        int index = mList.getSelection();

        if (index >= 0 && index < mArray.size()) {
            ListItem item = mArray.get(index);
            switch (index) {
            case 0:
                intent.setClass(view.getContext(), CommonDeviceNameActivity.class);
                view.getContext().startActivity(intent);
                break;
            case 1:
                showRadioListDialog(item);
                break;
            case 2:
                showRadioListDialog(item);
                break;
            case 3:
                showRadioListDialog(item);
                break;
            case 4:
                showRadioListDialog(item);
                break;

            default:
                break;
            }
        }
    }

    @Override
    public boolean onKey(View v, int key, KeyEvent event) {
        if ((key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) && event.getAction() == KeyEvent.ACTION_DOWN) {
            int change = 0;
            if (key == KeyEvent.KEYCODE_DPAD_LEFT) {
                change = -1;
            } else if (key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                change = 1;
            }
            int selectIndex = mList.getSelection();
            ListItem item = mArray.get(selectIndex);
            int size = item.getDataMap().size();
            int dataSelectIndex = item.getSelectDataIndex();
            dataSelectIndex += change;
            switch (item.getIndex()) {
            case 1:
                break;
            case 2:
                updateItemData(item, dataSelectIndex, size);
                break;
            case 3:
                updateItemData(item, dataSelectIndex, size);
                break;
            case 4:
                updateItemData(item, dataSelectIndex, size);
                break;
            case 5:
                // updateItemData(item, dataSelectIndex, size);
                break;
            default:
                break;
            }
        }
        return false;
    }

    private void updateItemData(ListItem item, int index, int size) {
        if (index > 0 && index <= size) {
            item.setSelectDataIndex(index);
            ListItemData data;
            SharedPreferences.Editor editor = mPreferences.edit();
            switch (item.getIndex()) {
            case 1:
                break;
            case 2:
                editor.putInt(PREFERENCE_PICTURE_IN_PICTURE_SELECT_INDEX, index - 1);
                break;
            case 3:
                setDefaultMethod(index - 1);
                updateInputInfo();
                break;
            case 4:
                // 屏保
                editor.putInt(PREFERENCE_SCREEN_LOCK_SELECT_INDEX, index - 1);
                data = item.getDataMap().get(item.getSelectDataIndex());
                if (data.getValue() == 0) {
                    // 0 是关闭状态
                    android.provider.Settings.System.putInt(mContext.getContentResolver(), "dim_screen", 0);
                } else {
                    android.provider.Settings.System.putInt(mContext.getContentResolver(), "dim_screen", 1);
                    android.provider.Settings.System.putInt(mContext.getContentResolver(), "dim_screen_timeout", data.getValue());
                }
                break;
            case 5:
                // editor.putInt(PREFERENCE_DORMANT_SELECT_INDEX, index - 1);
                break;

            default:
                break;
            }
            editor.apply();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showRadioListDialog(final ListItem item) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_dialog_radio_list, null);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
        initRadioList(mRadioGroup, item);
        StyleDialog.Builder builder = new StyleDialog.Builder(this);
        builder.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        builder.setTitle(item.getName());
        builder.setPositiveButton(R.string.jstyle_dialog_postive_bt);
        builder.setNegativeButton(R.string.jstyle_dialog_negatvie_bt);
        mRadioDialog = builder.show();
        builder.setOnButtonClickListener(new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == DialogInterface.BUTTON_POSITIVE) {

                    int selectId = mRadioGroup.getCheckedRadioButtonId();
                    item.setSelectDataIndex(selectId);
                    int index = mList.getSelection();

                    if (index >= 0 && index < mArray.size()) {
                        ListItem item = mArray.get(index);
                        SharedPreferences.Editor editor = mPreferences.edit();
                        switch (index) {
                        case 1:
                            editor.putInt(PREFERENCE_PICTURE_IN_PICTURE_SELECT_INDEX, selectId - 1);
                            break;
                        case 2:
                            setDefaultMethod(selectId - 1);
                            updateInputInfo();
                            break;
                        case 3:
                            editor.putInt(PREFERENCE_SCREEN_LOCK_SELECT_INDEX, selectId - 1);
                            break;
                        case 4:
                            // editor.putInt(PREFERENCE_DORMANT_SELECT_INDEX,
                            // selectId - 1);
                            break;

                        default:
                            break;
                        }
                        editor.apply();
                    }
                    mAdapter.notifyDataSetChanged();

                }
                mRadioDialog.dismissByAnimation();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {

                    mRadioDialog.dismissByAnimation();
                }
                return false;
            }
        });
        builder.setContentCanFocus(true);

        mRadioGroup.requestFocus();
    }

    private void initRadioList(RadioGroup group, ListItem item) {

        HashMap<Integer, ListItemData> dataMap = item.getDataMap();
        int selectIndex = item.getSelectDataIndex();
        ListItemData data;
        for (int i = 1; i <= dataMap.size(); i++) {
            data = dataMap.get(i);
            if (data != null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                RadioButton radio = (RadioButton) inflater.inflate(R.layout.view_radio_item, null);
                radio.setId(i);
                radio.setText(data.getTitle());
                if (selectIndex == data.getIndex()) {
                    radio.setChecked(true);
                } else {
                    radio.setChecked(false);
                }
                group.addView(radio);
            }

        }

    }

    private void updateInputInfo() {
        int selectIndex = 2;
        ListItem item = mArray.get(selectIndex);
        ListItemData data;
        HashMap<Integer, ListItemData> dataMap = item.getDataMap();
        int defaultId = getDefaultMethod();
        dataMap.clear();
        for (int i = 1; i <= mInputInfoList.size(); i++) {
            data = new ListItemData(i, i);
            int index = i - 1;
            InputMethodInfo info = mInputInfoList.get(index);
            String label = (String) info.loadLabel(mContext.getPackageManager());
            if (label != null) {
                data.setTitle(label);
                if (index == defaultId) {
                    item.setSelectDataIndex(data.getIndex());
                }
                dataMap.put(i, data);
            }
        }
    }

    private ArrayList<String> getMethodList() {
        mInputInfoStringList.clear();
        for (InputMethodInfo imi : mInputInfoList) {
            String label = (String) imi.loadLabel(mContext.getPackageManager());
            if (label != null)
                mInputInfoStringList.add(label);
        }

        return mInputInfoStringList;

    }

    private Intent getInputMethodIntent(int index) {
        Intent intent;
        String settingsActivity = mInputInfoList.get(index).getSettingsActivity();
        intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(mInputInfoList.get(index).getPackageName(), settingsActivity);
        return intent;

    }

    private int getDefaultMethod() {

        final String currentInputMethodId = android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);
        for (int i = 0; i < mInputInfoList.size(); i++) {
            if (mInputInfoList.get(i).getId().equals(currentInputMethodId)) {
                return i;
            }
        }
        return 0;
    }

    private void setDefaultMethod(int pos) {

        String currentInputMethodId = mInputInfoList.get(pos).getId();
        android.provider.Settings.Secure.putString(mContext.getContentResolver(), android.provider.Settings.Secure.DEFAULT_INPUT_METHOD, currentInputMethodId != null ? currentInputMethodId : "");

        mInputInfoList = mImm.getInputMethodList();
    }
}
