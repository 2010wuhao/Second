/**
 * =====================================================================
 *
 * @file  QuickAccessActivity.java
 * @Module Name   com.joysee.dvb.portal
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月25日
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
 * wuhao         2014年2月25日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.data.DvbSettings;
import com.joysee.dvb.data.SearchParamsReader;

import java.util.ArrayList;
import java.util.List;

public class QuickAccessActivity extends Activity implements OnCheckedChangeListener, OnClickListener {
    String TAG = JLog.makeTag(QuickAccessActivity.class);
    ToggleButton mPortalScaleButton;
    ToggleButton mDebugButton;
    ToggleButton mPrintLogButton;
    ToggleButton mTsEpgButton;
    ToggleButton mTsChannelButton;
    ToggleButton mVodEnableButton;

    EditText mEditText1;
    EditText mEditText2;
    EditText mEditText3;
    Button mButton;
    Spinner mSpinner;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.portal_animation_toggleButton:
                DvbSettings.System.putInt(getContentResolver(),
                        DvbSettings.System.PORTAL_USE_ANIMATION,
                        isChecked ? TvApplication.ON_VALUE : TvApplication.OFF_VALUE);
                break;
            case R.id.debug_mode_togglebutton:
                DvbSettings.System.putInt(getContentResolver(),
                        DvbSettings.System.DEBUG_MODE,
                        isChecked ? TvApplication.ON_VALUE : TvApplication.OFF_VALUE);
                break;
            case R.id.print_log_togglebutton:
                DvbSettings.System.putInt(getContentResolver(),
                        DvbSettings.System.DEBUG_LOG,
                        isChecked ? TvApplication.ON_VALUE : TvApplication.OFF_VALUE);
                break;
            case R.id.ts_epg_togglebutton:
                DvbSettings.System.putInt(getContentResolver(),
                        DvbSettings.System.FORCE_TS_EPG,
                        isChecked ? TvApplication.ON_VALUE : TvApplication.OFF_VALUE);
                break;
            case R.id.ts_channel_togglebutton:
                DvbSettings.System.putInt(getContentResolver(),
                        DvbSettings.System.FORCE_TS_CHANNELTYPE,
                        isChecked ? TvApplication.ON_VALUE : TvApplication.OFF_VALUE);
                break;
            case R.id.vod_enable_togglebutton:
                DvbSettings.System.putInt(getContentResolver(), 
                        DvbSettings.System.VOD_ENABLE, 
                        isChecked ? TvApplication.ON_VALUE : TvApplication.OFF_VALUE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.quick_save) {
            String frequency = mEditText1.getText().toString();// 频率
            String symbolRate = mEditText2.getText().toString();// 符号率
            String modulation = mEditText3.getText().toString();// 调制方式
            try {
                Transponder transponder = new Transponder(Integer.parseInt(frequency),
                        Integer.parseInt(symbolRate),
                        Integer.parseInt(modulation));
                SearchParamsReader.updateTransponder(this, mSpinner.getSelectedItemPosition(), transponder);
            } catch (Exception e) {
                e.printStackTrace();
                JLog.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_access);
        mPortalScaleButton = (ToggleButton) findViewById(R.id.portal_animation_toggleButton);
        mPortalScaleButton.setChecked(TvApplication.PORTAL_USE_ANIMATION);
        mPortalScaleButton.setOnCheckedChangeListener(this);

        mDebugButton = (ToggleButton) findViewById(R.id.debug_mode_togglebutton);
        mDebugButton.setChecked(TvApplication.DEBUG_MODE);
        mDebugButton.setOnCheckedChangeListener(this);

        mPrintLogButton = (ToggleButton) findViewById(R.id.print_log_togglebutton);
        mPrintLogButton.setChecked(TvApplication.DEBUG_LOG);
        mPrintLogButton.setOnCheckedChangeListener(this);

        mTsEpgButton = (ToggleButton) findViewById(R.id.ts_epg_togglebutton);
        mTsEpgButton.setChecked(TvApplication.FORCE_TS_EPG);
        mTsEpgButton.setOnCheckedChangeListener(this);

        mTsChannelButton = (ToggleButton) findViewById(R.id.ts_channel_togglebutton);
        mTsChannelButton.setChecked(TvApplication.FORCE_TS_CHANNELTYPE);
        mTsChannelButton.setOnCheckedChangeListener(this);
        
        mVodEnableButton = (ToggleButton) findViewById(R.id.vod_enable_togglebutton);
        mVodEnableButton.setChecked(TvApplication.VOD_ENABLE);
        mVodEnableButton.setOnCheckedChangeListener(this);

        mEditText1 = (EditText) findViewById(R.id.editText_frequency);
        mEditText2 = (EditText) findViewById(R.id.editText_symbolRate);
        mEditText3 = (EditText) findViewById(R.id.editText_modulation);
        mButton = (Button) findViewById(R.id.quick_save);
        mButton.setOnClickListener(this);
        mSpinner = (Spinner) findViewById(R.id.search_spinner);
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("快速");
        arrayList.add("全频");
        arrayList.add("手动");
        setSpinnerValue(arrayList, mSpinner);
    }

    private void setSpinnerValue(final List<String> list, Spinner sp) {
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.quick_access_spinner_item, list);
        sp.setAdapter(adapter);
    }
}
