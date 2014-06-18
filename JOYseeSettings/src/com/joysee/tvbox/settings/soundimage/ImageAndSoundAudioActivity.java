/**
 * =====================================================================
 *
 * @file   ImageAndSoundAudioActivity.java
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
import com.joysee.tvbox.settings.base.ListAnimationModelAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

public class ImageAndSoundAudioActivity extends BaseActivity implements android.view.View.OnKeyListener {

    private final static String TAG = ImageAndSoundAudioActivity.class.getSimpleName();
    private static final String STR_DIGIT_AUDIO_OUTPUT = "ubootenv.var.digitaudiooutput";
    private static String DigitalRawFile = "/sys/class/audiodsp/digital_raw";

    private ListViewEx mList;
    private ListView mInnerList;
    private ArrayList<ListItem> mArray;

    private ListAnimationModelAdapter mAdapter;
    private Context mContext;
    private int mAudioModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {
        mContext = this;

        setContentView(R.layout.activity_image_sound_audio);
        mList = (ListViewEx) findViewById(R.id.list_image_and_sound_audio);
        mArray = new ArrayList<ListItem>();
        mAdapter = ListFactory.initModelList(mArray, mList, Settings.Type.IMAGE_AND_SOUND_AUDIO);
        mInnerList = mList.getInnerList();
        if (mInnerList != null) {
            mInnerList.setOnKeyListener(this);
        }
        mAudioModel = getOuputMode();
        setModel(mAudioModel);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onKey(View v, int key, KeyEvent event) {
        if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER) {
            mAudioModel = mList.getSelection();
            setModel(mAudioModel);
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
            if (item != null && item.getListType() == Settings.Type.IMAGE_AND_SOUND_AUDIO) {
                data = item.getDataMap().get(item.getSelectDataIndex());
                if ((item.getIndex() - 1) == model) {
                    setAudioOutputMode(model);
                    data.setValue(1);
                } else {
                    data.setValue(0);
                }
            }
        }
    }

    public void setAudioOutputMode(int value) {
        if (value == 1) {
            SystemProperties.set(STR_DIGIT_AUDIO_OUTPUT, "PCM");
            File digitalRawFile = new File(DigitalRawFile);
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(digitalRawFile), 32);
                try {
                    out.write("0");
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException when write " + digitalRawFile);
            }
        } else if (value == 0) {
            SystemProperties.set(STR_DIGIT_AUDIO_OUTPUT, "RAW");
            File digitalRawFile = new File(DigitalRawFile);
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(digitalRawFile), 32);
                try {
                    out.write("1");
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException when write " + digitalRawFile);
            }
        }
    }

    public int getOuputMode() {
        int curadio = 1;
        try {
            String str = SystemProperties.get(STR_DIGIT_AUDIO_OUTPUT, "PCM");
            Log.d(TAG, " getOuputMode  str = " + str);
            if (str.equals("PCM")) {
                curadio = 1;
            } else if (str.equals("RAW")) {
                curadio = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curadio;
    }

}
