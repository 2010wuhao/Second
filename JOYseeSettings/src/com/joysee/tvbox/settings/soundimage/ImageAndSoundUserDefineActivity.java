/**
 * =====================================================================
 *
 * @file   ImageAndSoundUserDefineActivity.java
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

import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationProgressAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.base.SettingsApplication;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

public class ImageAndSoundUserDefineActivity extends BaseActivity implements android.view.View.OnKeyListener {

    private final static String TAG = ImageAndSoundUserDefineActivity.class.getSimpleName();

    /**
     * 亮度
     */
    private final static String PATH_BRIGHTNESS = "/sys/class/video/brightness";
    /**
     * 对比度
     */
    private final static String PATH_CONTRAST = "/sys/class/video/contrast";
    /**
     * 饱和度
     */
    private final static String PATH_SATURATION = "/sys/class/video/saturation";
    /**
     * 清晰度
     */
    private final static String PATH_DEFINITION = "";
    /**
     * 色调
     */
    private final static String PATH_SHADES = "";

    ListViewEx mList;
    ListView mInnerList;
    ArrayList<ListItem> mArray;
    ListAnimationProgressAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        setContentView(R.layout.activity_image_sound_user_define);
        mList = (ListViewEx) findViewById(R.id.list_image_and_sound_user_define);
        mArray = new ArrayList<ListItem>();
        mAdapter = ListFactory.initProgressList(mArray, mList, Settings.Type.IMAGE_AND_SOUND_USER_DEFINE);
        mInnerList = mList.getInnerList();
        if (mInnerList != null) {
            mInnerList.setOnKeyListener(this);
        }
        getImageSettings();
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onKey(View v, int key, KeyEvent event) {
        int index = mList.getSelection();
        if (index < mArray.size() && event.getAction() == event.ACTION_DOWN) {
            ListItem item = mArray.get(index);
            ListItemData data = item.getDataMap().get(item.getSelectDataIndex());
            int value = data.getValue();
            if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_LEFT_ARROW_MARK) {

                if (value > 0) {
                    value--;
                    setListItemData(data, value, index);
                }

            } else if (key == KeyEvent.KEYCODE_DPAD_RIGHT || key == KeyEvent.KEYCODE_RIGHT_ARROW_MARK) {

                if (value < 100) {
                    value++;
                    setListItemData(data, value, index);
                }

            }
        }
        return false;
    }

    private void setListItemData(ListItemData data, int value, int itemIndex) {
        data.setValue(value);
        updateImageSettings(itemIndex, value);
    }

    private void updateImageSettings(int index, int value) {
        switch (index) {
        case 0:
            writeSysfs(PATH_BRIGHTNESS, value + "");
            break;
        case 1:
            writeSysfs(PATH_CONTRAST, value + "");
            break;
        case 2:
            writeSysfs(PATH_SATURATION, value + "");
            break;
        default:
            break;
        }
        getImageSettings();
        mAdapter.notifyDataSetChanged();
    }

    private void getImageSettings() {
        Iterator<ListItem> iterator = mArray.iterator();
        ListItem item;
        ListItemData data;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item != null && item.getListType() == Settings.Type.IMAGE_AND_SOUND_USER_DEFINE) {
                data = item.getDataMap().get(item.getSelectDataIndex());
                switch (item.getIndex() - 1) {
                case 0:
                    String brightness = readSysfs(PATH_BRIGHTNESS);
                    data.setValue(Integer.parseInt(brightness));
                    break;
                case 1:
                    String contrast = readSysfs(PATH_CONTRAST);
                    data.setValue(Integer.parseInt(contrast));
                    break;
                case 2:
                    String saturation = readSysfs(PATH_SATURATION);
                    data.setValue(Integer.parseInt(saturation));
                    break;
                default:
                    break;
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

            // return;

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
