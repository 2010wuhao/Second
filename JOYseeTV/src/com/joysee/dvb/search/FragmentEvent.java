/**
 * =====================================================================
 *
 * @file  FragmentEvent.java
 * @Module Name   com.joysee.dvb.search
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-29
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
 * benz          2014-1-29           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.search;

import android.os.Bundle;

import com.joysee.adtv.logic.JDVBPlayer;

public class FragmentEvent {

    public static final String FragmentBundleTag = "FragmentBundleTag";

    public static final int SearchType_FAST = JDVBPlayer.SEARCHMODE_FAST;
    public static final int SearchType_MANUAL = JDVBPlayer.SEARCHMODE_MANUAL;
    public static final int SearchType_FULL = JDVBPlayer.SEARCHMODE_FULL;
    public static final int SearchType_NET = JDVBPlayer.SEARCHMODE_NET;

    public int eventCode = -1;
    public static final int SWTICH_FRAGMENT = 0;
    public static final int SHOWMENU = 1;

    public Bundle bundle;
    public BaseFragment lastFragment;
    public BaseFragment nextFragment;

    public boolean showMenu;

    @Override
    public String toString() {
        return super.toString();
    }
}
