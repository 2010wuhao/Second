/**
 * =====================================================================
 *
 * @file   ImageAndSoundResolutionActivity.java
 * @Module Name   com.joysee.tvbox.settings.soundimage
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

package com.joysee.tvbox.settings.soundimage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationDescriptionModelAdapter;
import com.joysee.tvbox.settings.base.ListAnimationModelAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

public class ImageAndSoundResolutionActivity extends BaseActivity implements android.view.View.OnKeyListener {

    private final static String TAG = ImageAndSoundResolutionActivity.class.getSimpleName();
    private static final String STR_OUTPUT_VAR = "ubootenv.var.outputmode";

    private ListViewEx mList;
    private ListView mInnerList;
    private ArrayList<ListItem> mArray;

    private ListAnimationDescriptionModelAdapter mAdapter;
    private Context mContext;
    private String[] values = new String[] { "576i", "720p", "1080i", "1080p" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {
        mContext = this;

        setContentView(R.layout.activity_image_sound_resulotion);
        mList = (ListViewEx) findViewById(R.id.list_image_and_sound_resolution);
        mArray = new ArrayList<ListItem>();
        mAdapter = ListFactory.initDescriptionModelList(mArray, mList, Settings.Type.IMAGE_AND_SOUND_RESOLUTION);
        mInnerList = mList.getInnerList();
        if (mInnerList != null) {
            mInnerList.setOnKeyListener(this);
        }
        String model = SystemProperties.get(STR_OUTPUT_VAR);

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(model)) {
                setModel(i);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onKey(View v, int key, KeyEvent event) {
        if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER) {
            setModel(mList.getSelection());
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    private void setModel(int model) {
        Iterator<ListItem> iterator = mArray.iterator();
        ListItem item;
        ListItemData data;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item != null && item.getListType() == Settings.Type.IMAGE_AND_SOUND_RESOLUTION) {
                data = item.getDataMap().get(item.getSelectDataIndex());
                if ((item.getIndex() - 1) == model) {
                    String outputMode = values[model];
                    SystemProperties.set(STR_OUTPUT_VAR, outputMode);
                    data.setValue(1);
                } else {
                    data.setValue(0);
                }
            }
        }
    }
}
