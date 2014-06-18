/**
 * =====================================================================
 *
 * @file   ImageAndSoundDisplayEreaActivity.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;

public class ImageAndSoundDisplayEreaActivity extends BaseActivity implements android.view.View.OnKeyListener {

    private final static String TAG = ImageAndSoundDisplayEreaActivity.class.getSimpleName();
    private static final String STR_OUTPUT_MODE = "ubootenv.var.outputmode";
    private final static String sel_480ioutput_x = "ubootenv.var.480ioutputx";
    private final static String sel_480ioutput_y = "ubootenv.var.480ioutputy";
    private final static String sel_480ioutput_width = "ubootenv.var.480ioutputwidth";
    private final static String sel_480ioutput_height = "ubootenv.var.480ioutputheight";
    private final static String sel_480poutput_x = "ubootenv.var.480poutputx";
    private final static String sel_480poutput_y = "ubootenv.var.480poutputy";
    private final static String sel_480poutput_width = "ubootenv.var.480poutputwidth";
    private final static String sel_480poutput_height = "ubootenv.var.480poutputheight";
    private final static String sel_576ioutput_x = "ubootenv.var.576ioutputx";
    private final static String sel_576ioutput_y = "ubootenv.var.576ioutputy";
    private final static String sel_576ioutput_width = "ubootenv.var.576ioutputwidth";
    private final static String sel_576ioutput_height = "ubootenv.var.576ioutputheight";
    private final static String sel_576poutput_x = "ubootenv.var.576poutputx";
    private final static String sel_576poutput_y = "ubootenv.var.576poutputy";
    private final static String sel_576poutput_width = "ubootenv.var.576poutputwidth";
    private final static String sel_576poutput_height = "ubootenv.var.576poutputheight";
    private final static String sel_720poutput_x = "ubootenv.var.720poutputx";
    private final static String sel_720poutput_y = "ubootenv.var.720poutputy";
    private final static String sel_720poutput_width = "ubootenv.var.720poutputwidth";
    private final static String sel_720poutput_height = "ubootenv.var.720poutputheight";
    private final static String sel_1080ioutput_x = "ubootenv.var.1080ioutputx";
    private final static String sel_1080ioutput_y = "ubootenv.var.1080ioutputy";
    private final static String sel_1080ioutput_width = "ubootenv.var.1080ioutputwidth";
    private final static String sel_1080ioutput_height = "ubootenv.var.1080ioutputheight";
    private final static String sel_1080poutput_x = "ubootenv.var.1080poutputx";
    private final static String sel_1080poutput_y = "ubootenv.var.1080poutputy";
    private final static String sel_1080poutput_width = "ubootenv.var.1080poutputwidth";
    private final static String sel_1080poutput_height = "ubootenv.var.1080poutputheight";
    private static final int OUTPUT480_FULL_WIDTH = 720;
    private static final int OUTPUT480_FULL_HEIGHT = 480;
    private static final int OUTPUT576_FULL_WIDTH = 720;
    private static final int OUTPUT576_FULL_HEIGHT = 576;
    private static final int OUTPUT720_FULL_WIDTH = 1280;
    private static final int OUTPUT720_FULL_HEIGHT = 720;
    private static final int OUTPUT1080_FULL_WIDTH = 1920;
    private static final int OUTPUT1080_FULL_HEIGHT = 1080;

    private final String ACTION_OUTPUTPOSITION_CHANGE = "android.intent.action.OUTPUTPOSITION_CHANGE";
    private final String ACTION_OUTPUTPOSITION_CANCEL = "android.intent.action.OUTPUTPOSITION_CANCEL";
    private final String ACTION_OUTPUTPOSITION_SAVE = "android.intent.action.OUTPUTPOSITION_SAVE";
    private final String ACTION_OUTPUTPOSITION_DEFAULT_SAVE = "android.intent.action.OUTPUTPOSITION_DEFAULT_SAVE";
    private final String OUTPUT_POSITION_X = "output_position_x";
    private final String OUTPUT_POSITION_Y = "output_position_y";
    private final String OUTPUT_POSITION_W = "output_position_w";
    private final String OUTPUT_POSITION_H = "output_position_h";
    private final String OUTPUT_POSITION_MODE = "output_position_mode";

    private static String FreeScaleOsd0File = "/sys/class/graphics/fb0/free_scale";
    private static String FreeScaleOsd1File = "/sys/class/graphics/fb1/free_scale";

    private final static double CHANGE_RATE = 0.08;
    private final static int ZOOM_PIXEL = 2;

    private Context mContext;

    private RelativeLayout mButtonPanel;
    private ImageView mImageCenter;
    private ImageView mImageTop;
    private ImageView mImageLeft;
    private ImageView mImageBottom;
    private ImageView mImageRight;

    // 是否是放大状态
    private boolean isZoomIn;
    private PositionCoordinate mCoordinatePer;
    private PositionCoordinate mCoordinate;
    private mOutputSize mOutputSize;

    private String pre_output_x = "";
    private String pre_output_y = "";
    private String pre_output_width = "";
    private String pre_output_height = "";

    private String[] models = new String[] { "480i", "480p", "576i", "576p", "720p", "1080i", "1080p", "720p50hz", "1080i50hz", "1080p50hz" };

    private class PositionCoordinate {
        private int left;
        private int top;
        private int right;
        private int bottom;
        private int width;
        private int height;
    }

    private class mOutputSize {
        private int width_min;
        private int height_min;
        private int width_max;
        private int height_max;
        private int width;
        private int height;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoordinatePer = new PositionCoordinate();
        mCoordinate = new PositionCoordinate();
        mOutputSize = new mOutputSize();

        mCoordinatePer.width = 0;
        mCoordinatePer.height = 0;
        mCoordinatePer.left = 0;
        mCoordinatePer.top = 0;
        mCoordinatePer.right = 0;
        mCoordinatePer.bottom = 0;

        initOutputSize();

        mCoordinate.width = Integer.valueOf(pre_output_width).intValue();
        mCoordinate.height = Integer.valueOf(pre_output_height).intValue();
        mCoordinate.left = Integer.valueOf(pre_output_x).intValue();
        mCoordinate.top = Integer.valueOf(pre_output_y).intValue();
        mCoordinate.right = mCoordinate.width + mCoordinate.left - 1;
        mCoordinate.bottom = mCoordinate.height + mCoordinate.top - 1;

        writeFile(FreeScaleOsd0File, "1");
        writeFile(FreeScaleOsd1File, "1");
    }

    @Override
    protected void setupView() {
        mContext = this;

        setContentView(R.layout.activity_image_sound_dispaly_area);
        mButtonPanel = (RelativeLayout) findViewById(R.id.settings_area_button_panel);
        mImageCenter = (ImageView) findViewById(R.id.settings_area_center_button);
        mImageTop = (ImageView) findViewById(R.id.settings_area_above_button);
        mImageLeft = (ImageView) findViewById(R.id.settings_area_left_button);
        mImageBottom = (ImageView) findViewById(R.id.settings_area_below_button);
        mImageRight = (ImageView) findViewById(R.id.settings_area_right_button);

        mButtonPanel.setOnKeyListener(this);

    }

    public void writeFile(String file, String value) {
        File OutputFile = new File(file);
        if (!OutputFile.exists()) {
            return;
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(OutputFile), 32);
            try {
                Log.d(TAG, "set" + file + ": " + value);
                out.write(value);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException when write " + OutputFile);
        }
    }

    private void initOutputSize() {
        String model = SystemProperties.get(STR_OUTPUT_MODE);
        if ((model.equals(models[0])) || (model.equals(models[1]))) {
            mOutputSize.width_min = (int) (720 * CHANGE_RATE);
            mOutputSize.width_max = (int) (720 * (1 + CHANGE_RATE));
            mOutputSize.width = OUTPUT480_FULL_WIDTH;
            mOutputSize.height_min = (int) (480 * CHANGE_RATE);
            mOutputSize.height_max = (int) (480 * (1 + CHANGE_RATE));
            mOutputSize.height = OUTPUT480_FULL_HEIGHT;
        } else if ((model.equals(models[2])) || (model.equals(models[3]))) {
            mOutputSize.width_min = (int) (720 * CHANGE_RATE);
            mOutputSize.width_max = (int) (720 * (1 + CHANGE_RATE));
            mOutputSize.width = OUTPUT576_FULL_WIDTH;
            mOutputSize.height_min = (int) (576 * CHANGE_RATE);
            mOutputSize.height_max = (int) (576 * (1 + CHANGE_RATE));
            mOutputSize.height = OUTPUT576_FULL_HEIGHT;
        } else if (model.equals(models[4]) || model.equals(models[7])) {
            mOutputSize.width_min = (int) (1280 * CHANGE_RATE);
            mOutputSize.width_max = (int) (1280 * (1 + CHANGE_RATE));
            mOutputSize.width = OUTPUT720_FULL_WIDTH;
            mOutputSize.height_min = (int) (720 * CHANGE_RATE);
            mOutputSize.height_max = (int) (720 * (1 + CHANGE_RATE));
            mOutputSize.height = OUTPUT720_FULL_HEIGHT;
        } else if ((model.equals(models[5])) || (model.equals(models[6])) || model.equals(models[8]) || model.equals(models[9])) {
            mOutputSize.width_min = (int) (1920 * CHANGE_RATE);
            mOutputSize.width_max = (int) (1920 * (1 + CHANGE_RATE));
            mOutputSize.width = OUTPUT1080_FULL_WIDTH;
            mOutputSize.height_min = (int) (1080 * CHANGE_RATE);
            mOutputSize.height_max = (int) (1080 * (1 + CHANGE_RATE));
            mOutputSize.height = OUTPUT1080_FULL_HEIGHT;
        }

        if (model.equals(models[0])) {
            pre_output_x = SystemProperties.get(sel_480ioutput_x);
            pre_output_y = SystemProperties.get(sel_480ioutput_y);
            pre_output_width = SystemProperties.get(sel_480ioutput_width);
            pre_output_height = SystemProperties.get(sel_480ioutput_height);
            if (pre_output_x.equals(""))
                pre_output_x = "0";
            if (pre_output_y.equals(""))
                pre_output_y = "0";
            if (pre_output_width.equals(""))
                pre_output_width = String.valueOf(OUTPUT480_FULL_WIDTH);
            if (pre_output_height.equals(""))
                pre_output_height = String.valueOf(OUTPUT480_FULL_HEIGHT);
        } else if (model.equals(models[1])) {
            pre_output_x = SystemProperties.get(sel_480poutput_x);
            pre_output_y = SystemProperties.get(sel_480poutput_y);
            pre_output_width = SystemProperties.get(sel_480poutput_width);
            pre_output_height = SystemProperties.get(sel_480poutput_height);
            if (pre_output_x.equals(""))
                pre_output_x = "0";
            if (pre_output_y.equals(""))
                pre_output_y = "0";
            if (pre_output_width.equals(""))
                pre_output_width = String.valueOf(OUTPUT480_FULL_WIDTH);
            if (pre_output_height.equals(""))
                pre_output_height = String.valueOf(OUTPUT480_FULL_HEIGHT);
        } else if (model.equals(models[2])) {
            pre_output_x = SystemProperties.get(sel_576ioutput_x);
            pre_output_y = SystemProperties.get(sel_576ioutput_y);
            pre_output_width = SystemProperties.get(sel_576ioutput_width);
            pre_output_height = SystemProperties.get(sel_576ioutput_height);
            if (pre_output_x.equals(""))
                pre_output_x = "0";
            if (pre_output_y.equals(""))
                pre_output_y = "0";
            if (pre_output_width.equals(""))
                pre_output_width = String.valueOf(OUTPUT576_FULL_WIDTH);
            if (pre_output_height.equals(""))
                pre_output_height = String.valueOf(OUTPUT576_FULL_HEIGHT);
        } else if (model.equals(models[3])) {
            pre_output_x = SystemProperties.get(sel_576poutput_x);
            pre_output_y = SystemProperties.get(sel_576poutput_y);
            pre_output_width = SystemProperties.get(sel_576poutput_width);
            pre_output_height = SystemProperties.get(sel_576poutput_height);
            if (pre_output_x.equals(""))
                pre_output_x = "0";
            if (pre_output_y.equals(""))
                pre_output_y = "0";
            if (pre_output_width.equals(""))
                pre_output_width = String.valueOf(OUTPUT576_FULL_WIDTH);
            if (pre_output_height.equals(""))
                pre_output_height = String.valueOf(OUTPUT576_FULL_HEIGHT);
        } else if (model.equals(models[4]) || model.equals(models[7])) {
            pre_output_x = SystemProperties.get(sel_720poutput_x);
            pre_output_y = SystemProperties.get(sel_720poutput_y);
            pre_output_width = SystemProperties.get(sel_720poutput_width);
            pre_output_height = SystemProperties.get(sel_720poutput_height);
            if (pre_output_x.equals(""))
                pre_output_x = "0";
            if (pre_output_y.equals(""))
                pre_output_y = "0";
            if (pre_output_width.equals(""))
                pre_output_width = String.valueOf(OUTPUT720_FULL_WIDTH);
            if (pre_output_height.equals(""))
                pre_output_height = String.valueOf(OUTPUT720_FULL_HEIGHT);
        } else if (model.equals(models[5]) || model.equals(models[8])) {
            pre_output_x = SystemProperties.get(sel_1080ioutput_x);
            pre_output_y = SystemProperties.get(sel_1080ioutput_y);
            pre_output_width = SystemProperties.get(sel_1080ioutput_width);
            pre_output_height = SystemProperties.get(sel_1080ioutput_height);
            if (pre_output_x.equals(""))
                pre_output_x = "0";
            if (pre_output_y.equals(""))
                pre_output_y = "0";
            if (pre_output_width.equals(""))
                pre_output_width = String.valueOf(OUTPUT1080_FULL_WIDTH);
            if (pre_output_height.equals(""))
                pre_output_height = String.valueOf(OUTPUT1080_FULL_HEIGHT);
        } else if (model.equals(models[6]) || model.equals(models[9])) {
            pre_output_x = SystemProperties.get(sel_1080poutput_x);
            pre_output_y = SystemProperties.get(sel_1080poutput_y);
            pre_output_width = SystemProperties.get(sel_1080poutput_width);
            pre_output_height = SystemProperties.get(sel_1080poutput_height);
            if (pre_output_x.equals(""))
                pre_output_x = "0";
            if (pre_output_y.equals(""))
                pre_output_y = "0";
            if (pre_output_width.equals(""))
                pre_output_width = String.valueOf(OUTPUT1080_FULL_WIDTH);
            if (pre_output_height.equals(""))
                pre_output_height = String.valueOf(OUTPUT1080_FULL_HEIGHT);
        }
    }

    @Override
    public boolean onKey(View v, int key, KeyEvent event) {
        if (event != null && event.getAction() == KeyEvent.ACTION_UP && key == KeyEvent.KEYCODE_BACK) {
            int x, y;
            x = mCoordinate.left + mCoordinatePer.left;
            if (x < 0)
                x = 0;
            y = mCoordinate.top + mCoordinatePer.top;
            if (y < 0)
                y = 0;
            mCoordinate.width = mCoordinate.right - mCoordinate.left + 1;
            mCoordinate.height = mCoordinate.bottom - mCoordinate.top + 1;
            if ((mCoordinate.width % 2) == 1) {
                mCoordinate.width--;
            }
            if ((mCoordinate.height % 2) == 1) {
                mCoordinate.height--;
            }
            if ((String.valueOf(x).equals(pre_output_x)) && (String.valueOf(y).equals(pre_output_y)) && (String.valueOf(mCoordinate.width).equals(pre_output_width))
                    && (String.valueOf(mCoordinate.height).equals(pre_output_height))) {

            } else {
                // 保存
                savePositionSettings(x, y);
            }
        } else if (event != null && event.getAction() == KeyEvent.ACTION_UP && key == KeyEvent.KEYCODE_MENU) {
            Intent intent_output_position = new Intent(ACTION_OUTPUTPOSITION_DEFAULT_SAVE);
            mContext.sendBroadcast(intent_output_position);
        } else if (event != null && event.getAction() == KeyEvent.ACTION_UP) {

            switch (key) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                isZoomIn = !isZoomIn;
                mImageCenter.setImageResource(R.drawable.position_button_zoom);
                mImageTop.setImageResource(isZoomIn ? R.drawable.position_button_up : R.drawable.position_button_down);
                mImageLeft.setImageResource(isZoomIn ? R.drawable.position_button_left : R.drawable.position_button_right);
                mImageBottom.setImageResource(isZoomIn ? R.drawable.position_button_down : R.drawable.position_button_up);
                mImageRight.setImageResource(isZoomIn ? R.drawable.position_button_right : R.drawable.position_button_left);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                mImageTop.setImageResource(isZoomIn ? R.drawable.position_button_up : R.drawable.position_button_down);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mImageLeft.setImageResource(isZoomIn ? R.drawable.position_button_left : R.drawable.position_button_right);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mImageBottom.setImageResource(isZoomIn ? R.drawable.position_button_down : R.drawable.position_button_up);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mImageRight.setImageResource(isZoomIn ? R.drawable.position_button_right : R.drawable.position_button_left);
                break;
            case KeyEvent.KEYCODE_ENTER:
                isZoomIn = !isZoomIn;
                mImageCenter.setImageResource(R.drawable.position_button_zoom);
                mImageTop.setImageResource(isZoomIn ? R.drawable.position_button_up : R.drawable.position_button_down);
                mImageLeft.setImageResource(isZoomIn ? R.drawable.position_button_left : R.drawable.position_button_right);
                mImageBottom.setImageResource(isZoomIn ? R.drawable.position_button_down : R.drawable.position_button_up);
                mImageRight.setImageResource(isZoomIn ? R.drawable.position_button_right : R.drawable.position_button_left);
                break;
            default:
                break;
            }

        } else if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {

            switch (key) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mImageCenter.setImageResource(R.drawable.position_button_zoom_hl);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                mImageTop.setImageResource(isZoomIn ? R.drawable.position_button_up_hl : R.drawable.position_button_down_hl);
                if (isZoomIn) {
                    if (mCoordinate.top > (-mCoordinatePer.top)) {
                        mCoordinate.top -= ZOOM_PIXEL;
                        if (mCoordinate.top < (-mCoordinatePer.top)) {
                            mCoordinate.top = -mCoordinatePer.top;
                        }
                        setPosition(mCoordinate.left, mCoordinate.top, mCoordinate.right, mCoordinate.bottom, 0);
                    }
                } else {
                    if (mCoordinate.top < (mOutputSize.height_min - mCoordinatePer.top)) {
                        mCoordinate.top += ZOOM_PIXEL;
                        if (mCoordinate.top > (mOutputSize.height_min - mCoordinatePer.top)) {
                            mCoordinate.top = mOutputSize.height_min - mCoordinatePer.top;
                        }
                        setPosition(mCoordinate.left, mCoordinate.top, mCoordinate.right, mCoordinate.bottom, 0);
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mImageLeft.setImageResource(isZoomIn ? R.drawable.position_button_left_hl : R.drawable.position_button_right_hl);
                if (isZoomIn) {
                    if (mCoordinate.left > (-mCoordinatePer.left)) {
                        mCoordinate.left -= ZOOM_PIXEL;
                        if (mCoordinate.left < (-mCoordinatePer.left)) {
                            mCoordinate.left = -mCoordinatePer.left;
                        }
                        setPosition(mCoordinate.left, mCoordinate.top, mCoordinate.right, mCoordinate.bottom, 0);
                    }
                } else {
                    if (mCoordinate.left < (mOutputSize.width_min - mCoordinatePer.left)) {
                        mCoordinate.left += ZOOM_PIXEL;
                        if (mCoordinate.left > (mOutputSize.width_min - mCoordinatePer.left)) {
                            mCoordinate.left = mOutputSize.width_min - mCoordinatePer.left;
                        }
                        setPosition(mCoordinate.left, mCoordinate.top, mCoordinate.right, mCoordinate.bottom, 0);
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mImageBottom.setImageResource(isZoomIn ? R.drawable.position_button_down_hl : R.drawable.position_button_up_hl);
                if (isZoomIn) {
                    if (mCoordinate.bottom < (mOutputSize.height - mCoordinatePer.top)) {
                        mCoordinate.bottom += ZOOM_PIXEL;
                        if (mCoordinate.bottom > (mOutputSize.height - mCoordinatePer.top)) {
                            mCoordinate.bottom = mOutputSize.height - mCoordinatePer.top;
                        }
                        setPosition(mCoordinate.left, mCoordinate.top, mCoordinate.right, mCoordinate.bottom, 0);
                    }
                } else {
                    if (mCoordinate.bottom > (mOutputSize.height - mCoordinatePer.top - mOutputSize.height_min)) {
                        mCoordinate.bottom -= ZOOM_PIXEL;
                        if (mCoordinate.bottom < (mOutputSize.height - mCoordinatePer.top - mOutputSize.height_min)) {
                            mCoordinate.bottom = mOutputSize.height - mCoordinatePer.top - mOutputSize.height_min;
                        }
                        setPosition(mCoordinate.left, mCoordinate.top, mCoordinate.right, mCoordinate.bottom, 0);
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mImageRight.setImageResource(isZoomIn ? R.drawable.position_button_right_hl : R.drawable.position_button_left_hl);
                if (isZoomIn) {
                    if (mCoordinate.right < (mOutputSize.width - mCoordinatePer.left)) {
                        mCoordinate.right += ZOOM_PIXEL;
                        if (mCoordinate.right > (mOutputSize.width - mCoordinatePer.left)) {
                            mCoordinate.right = mOutputSize.width - mCoordinatePer.left;
                        }
                        setPosition(mCoordinate.left, mCoordinate.top, mCoordinate.right, mCoordinate.bottom, 0);
                    }
                } else {
                    if (mCoordinate.right > (mOutputSize.width - mCoordinatePer.left - mOutputSize.width_min)) {
                        mCoordinate.right -= ZOOM_PIXEL;
                        if (mCoordinate.right < (mOutputSize.width - mCoordinatePer.left - mOutputSize.width_min)) {
                            mCoordinate.right = mOutputSize.width - mCoordinatePer.left - mOutputSize.width_min;
                        }
                        setPosition(mCoordinate.left, mCoordinate.top, mCoordinate.right, mCoordinate.bottom, 0);
                    }
                }
                break;
            case KeyEvent.KEYCODE_ENTER:
                mImageCenter.setImageResource(R.drawable.position_button_zoom_hl);
                break;
            default:
                break;
            }

        }
        return false;
    }

    private void setPosition(int l, int t, int r, int b, int mode) {
        Log.d(TAG, " l = " + l + " t = " + t + " r = " + r + " b = " + b + " mode = " + mode);
        Intent intent_output_position = new Intent(ACTION_OUTPUTPOSITION_CHANGE);
        intent_output_position.putExtra(OUTPUT_POSITION_X, l);
        intent_output_position.putExtra(OUTPUT_POSITION_Y, t);
        intent_output_position.putExtra(OUTPUT_POSITION_W, r);
        intent_output_position.putExtra(OUTPUT_POSITION_H, b);
        intent_output_position.putExtra(OUTPUT_POSITION_MODE, mode);
        mContext.sendBroadcast(intent_output_position);
    }

    public void savePositionSettings(final int x, final int y) {
        try {
            Intent intent_output_position = new Intent(ACTION_OUTPUTPOSITION_SAVE);
            intent_output_position.putExtra(OUTPUT_POSITION_X, x);
            intent_output_position.putExtra(OUTPUT_POSITION_Y, y);
            intent_output_position.putExtra(OUTPUT_POSITION_W, mCoordinate.width);
            intent_output_position.putExtra(OUTPUT_POSITION_H, mCoordinate.height);
            mContext.sendBroadcast(intent_output_position);
            Log.i(TAG, "--------------------------------position Set");
            Log.d(TAG, "--------------------------------set display axis x = " + x);
            Log.d(TAG, "--------------------------------set display axis y = " + y);
            Log.d(TAG, "--------------------------------set display axis width = " + mCoordinate.width);
            Log.d(TAG, "--------------------------------set display axis height = " + mCoordinate.height);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "  savePositionSettings  error ");
        }
    }
}
