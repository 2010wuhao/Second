/**
 * =====================================================================
 *
 * @file   CommonDeviceNameActivity.java
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
import java.util.Iterator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.common.widget.JEditTextWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationModelAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.base.StyleDialog;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

public class CommonDeviceNameActivity extends BaseActivity implements android.view.View.OnKeyListener {

    public final static String PREFERENCE_DEVICE_NAME_INDEX = "common_device_name_index";
    public final static String PREFERENCE_DEVICE_NAME_USER_DEFINE = "common_device_name_user_define";

    private int mSelectIndex;
    private String mUserDefineName;

    private ListViewEx mList;
    private ListView mInnerList;
    private ArrayList<ListItem> mArray;
    private Context mContext;
    private ListAnimationModelAdapter mAdapter;
    private SharedPreferences mPreferences;

    private StyleDialog mSetNameDialog;
    private JEditTextWithTTF mJETDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        mContext = this;
        mPreferences = Settings.Preferences.getSharedPreferences(mContext);

        setContentView(R.layout.activity_common_device_name);
        mList = (ListViewEx) findViewById(R.id.list_common_device_name);
        mArray = new ArrayList<ListItem>();
        mAdapter = ListFactory.initDeviceNameList(mArray, mList, Settings.Type.COMMON_DEVICE_NAME);

        mInnerList = mList.getInnerList();

        if (mInnerList != null) {
            mInnerList.setOnKeyListener(this);
        }
        mSelectIndex = mPreferences.getInt(PREFERENCE_DEVICE_NAME_INDEX, 0);
        setDeviceName(mSelectIndex);
    }

    @Override
    public boolean onKey(View v, int key, KeyEvent event) {
        if ((key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER) && event.getAction() == KeyEvent.ACTION_UP) {
            mSelectIndex = mList.getSelection();
            SharedPreferences.Editor editor = mPreferences.edit();

            editor.putInt(PREFERENCE_DEVICE_NAME_INDEX, mSelectIndex);
            if (mSelectIndex == 4) {
                mUserDefineName = mPreferences.getString(PREFERENCE_DEVICE_NAME_USER_DEFINE, "");
                showInputDialog(mUserDefineName);
            }
            editor.apply();
            setDeviceName(mSelectIndex);
            mAdapter.notifyDataSetChanged();
            return true;
        }
        if (key == KeyEvent.KEYCODE_BACK) {

            return false;
        }
        return true;

    }

    private void setDeviceName(int index) {
        Iterator<ListItem> iterator = mArray.iterator();
        ListItem item;
        ListItemData data;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item != null && item.getListType() == Settings.Type.COMMON_DEVICE_NAME) {
                data = item.getDataMap().get(item.getSelectDataIndex());
                if ((item.getIndex() - 1) == index) {
                    if (index == 4) {
                        mUserDefineName = mPreferences.getString(PREFERENCE_DEVICE_NAME_USER_DEFINE, "");
                    }
                    data.setValue(1);
                } else {
                    data.setValue(0);
                }
            }
        }
    }

    private void showInputDialog(String name) {

        if (mSetNameDialog == null) {
            StyleDialog.Builder builder = new StyleDialog.Builder(this);
            String title = mContext.getResources().getString(R.string.user_define);
            builder.setTitle(title);
            builder.setPositiveButton(R.string.jstyle_dialog_postive_bt);
            builder.setNegativeButton(R.string.jstyle_dialog_negatvie_bt);
            mSetNameDialog = builder.show();
            mJETDeviceName = (JEditTextWithTTF) mSetNameDialog.findViewById(R.id.content_view_edittext);
            mJETDeviceName.setText(name);
            mJETDeviceName.setFocusable(true);
            mJETDeviceName.setSelection(name.length() == 0 ? 0 : name.length() - 1);
            mJETDeviceName.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setOnButtonClickListener(new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == DialogInterface.BUTTON_POSITIVE) {

                        mUserDefineName = mJETDeviceName.getText().toString();
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putString(PREFERENCE_DEVICE_NAME_USER_DEFINE, mUserDefineName);
                        editor.apply();
                        mSetNameDialog.dismissByAnimation();
                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {

                        mSetNameDialog.dismissByAnimation();
                    }
                }
            });
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {

                        mSetNameDialog.dismissByAnimation();
                    }
                    return false;
                }
            });
            builder.setContentCanFocus(true);
            mJETDeviceName.requestFocus();
            mJETDeviceName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        mUserDefineName = mJETDeviceName.getText().toString();
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putString(PREFERENCE_DEVICE_NAME_USER_DEFINE, mUserDefineName);
                        editor.apply();
                        mSetNameDialog.dismissByAnimation();
                        return true;
                    }
                    return false;
                }
            });
        } else {
            mSetNameDialog.show();
            mJETDeviceName.setText(name);
            mJETDeviceName.requestFocus();
            mJETDeviceName.setSelection(name.length());
        }
    }
}
