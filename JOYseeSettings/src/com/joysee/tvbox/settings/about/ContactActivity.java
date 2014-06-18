/**
 * =====================================================================
 *
 * @file   ContactActivity.java
 * @Module Name   com.joysee.tvbox.settings.about
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 29, 2014
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
 * wumingjun         @Apr 29, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.about;

import android.os.Bundle;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;

public class ContactActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        setContentView(R.layout.activity_about_contact);

    }

}
