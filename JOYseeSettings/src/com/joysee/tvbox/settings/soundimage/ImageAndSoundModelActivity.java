/**
 * =====================================================================
 *
 * @file   ImageAndSoundModelActivity.java
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

public class ImageAndSoundModelActivity extends BaseActivity implements android.view.View.OnKeyListener {

    private final static String TAG = ImageAndSoundModelActivity.class.getSimpleName();
    private final static String PREFERENCE_MODEL = "image_and_sound_model";

    /**
     * 亮度
     */
    private final static String PATH_BRIGHTNESS = "/sys/class/video/brightness";
    /**
     * 模式
     */
    private int mModel;

    private SharedPreferences mPreferences;

    private ListViewEx mList;
    private ListView mInnerList;
    private ArrayList<ListItem> mArray;

    private ListAnimationModelAdapter mAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {
        mContext = this;
        mPreferences = Settings.Preferences.getSharedPreferences(mContext);

        setContentView(R.layout.activity_image_sound_model);
        mList = (ListViewEx) findViewById(R.id.list_image_and_sound_model);
        mArray = new ArrayList<ListItem>();
        mAdapter = ListFactory.initModelNarrowList(mArray, mList, Settings.Type.IMAGE_AND_SOUND_MODEL);
        mInnerList = mList.getInnerList();
        if (mInnerList != null) {
            mInnerList.setOnKeyListener(this);
        }

        mModel = mPreferences.getInt(PREFERENCE_MODEL, 0);
        setModel(mModel);

    }

    @Override
    public boolean onKey(View v, int key, KeyEvent event) {
        if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER) {
            mModel = mList.getSelection();
            SharedPreferences.Editor editor = mPreferences.edit();

            editor.putInt(PREFERENCE_MODEL, mModel);
            editor.apply();
            setModel(mModel);
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
            if (item != null && item.getListType() == Settings.Type.IMAGE_AND_SOUND_MODEL) {
                data = item.getDataMap().get(item.getSelectDataIndex());
                if ((item.getIndex() - 1) == model) {
                    switch (model) {
                    case 0:
                        writeSysfs(PATH_BRIGHTNESS, "0");
                        break;
                    case 1:
                        writeSysfs(PATH_BRIGHTNESS, "10");
                        break;
                    case 2:
                        writeSysfs(PATH_BRIGHTNESS, "20");
                        break;
                    case 3:
                        writeSysfs(PATH_BRIGHTNESS, "5");
                        break;
                    default:
                        break;
                    }
                    data.setValue(1);
                } else {
                    data.setValue(0);
                }
            }
        }
    }

    public static void writeSysfs(String path, String val) {

        if (!new File(path).exists()) {

            Log.e(TAG, "File not found: " + path);

            File file = new File(path);

            try {

                file.createNewFile();

            } catch (IOException e) {

                Log.e(TAG, "writeSysfs createNewFile ", e);

            }

            FileUtils.setPermissions(path, 0777, -1, -1);

        }

        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(path), 64);

            try {

                writer.write(val);

            } finally {

                writer.close();

            }

        } catch (IOException e) {

            Log.e(TAG, "IO Exception when write: " + path, e);

        }

    }

    public static String readSysfs(String path) {

        if (!new File(path).exists()) {

            Log.e(TAG, "File not found: " + path);

            return "";

        }

        try {

            BufferedReader reader = new BufferedReader(new FileReader(path), 62);

            try {

                String ret = reader.readLine();

                return ret != null ? ret : "";

            } finally {

                reader.close();

            }

        } catch (IOException e) {

            Log.e(TAG, "IO Exception when write: " + path, e);

        }

        return "";

    }

}
