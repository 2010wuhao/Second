/**
 * =====================================================================
 *
 * @file  BaseFragment.java
 * @Module Name   com.joysee.dvb.search
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-2-8
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
 * benz          2014-2-8           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.search;

import android.app.Fragment;
import android.view.KeyEvent;

public abstract class BaseFragment extends Fragment {

    public abstract boolean dispatchKeyEvent(KeyEvent event);
}
