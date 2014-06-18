/**
 * =====================================================================
 *
 * @file  CaActivity.java
 * @Module Name   com.joysee.dvb.ca
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月26日
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
 * wuhao         2014年2月26日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.ca;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.data.DvbSettings;

import java.util.Arrays;

public class CaActivity extends Activity {
    private String TAG = "CaActivity";
    private JTextViewWithTTF mTitle;
    private JTextViewWithTTF mSubTitle;
    private ImageView mTitleArrowIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ca_main_layout);
        mTitle = (JTextViewWithTTF) findViewById(R.id.ca_main_layout_title_tv);
        mSubTitle = (JTextViewWithTTF) findViewById(R.id.ca_main_layout_subtitle_tv);
        mTitleArrowIcon = (ImageView) findViewById(R.id.ca_main_layout_title_arrowicon);
        setFragment();
    }

    @Override
    protected void onResume() {
        StringBuffer stringBuffer = new StringBuffer();
        String area_info = DvbSettings.System.getString(getContentResolver(), DvbSettings.System.LOCAL_AREA_INFO);
        if (area_info == null || area_info.equals("")) {
            stringBuffer.append(getString(R.string.ca_no_operator));
        } else {
            String[] arrays = area_info.split("-");
            if (arrays[0].equals(arrays[1])) {
                arrays = Arrays.copyOfRange(arrays, 1, arrays.length);
            }
            for (String string : arrays) {
                stringBuffer.append(string);
            }
            stringBuffer.append(DvbSettings.System.getString(getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_NAME));
        }
        replaceSubTitleText(stringBuffer.toString());
        super.onResume();
    }

    public void replaceSubTitleText(String title) {
        JLog.d(TAG, " replaceSubTitleText = " + title);
        if (mSubTitle != null) {
            mSubTitle.setText(getString(R.string.ca_subtitle_text, title));
        }
    }

    public void replaceTitleText(String title) {
        JLog.d(TAG, " replaceTitleText = " + title);
        if (mTitle != null) {
            mTitle.setText(title);
            if (title == null || "".endsWith(title)) {
                mTitleArrowIcon.setVisibility(View.INVISIBLE);
            } else {
                mTitleArrowIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.ca_main_container, new CaMenuFragment());
        ft.commit();
    }
}
