/**
 * =====================================================================
 *
 * @file   ListItemType.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 22, 2014
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
 * wumingjun         @Apr 22, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    /**
     * For ContentProvider
     */
    public final static int CONTENT_TYPE_ALL_ITEM_CODE = 1;
    public final static int CONTENT_TYPE_ALL_DATA_OF_ITEM_CODE = 2;

    public class Type {

        public final static int UNKNOWN = 0;
        public final static int NETWORK = 1;
        public final static int IMAGE_AND_SOUND = 2;
        public final static int SECURE = 3;
        public final static int COMMON = 4;
        public final static int SYSTEM_UPGRADE = 5;
        public final static int ABOUT = 6;
        public final static int IMAGE_AND_SOUND_USER_DEFINE = 7;
        public final static int IMAGE_AND_SOUND_MODEL = 8;
        public final static int COMMON_DEVICE_NAME = 9;
        public final static int ABOUT_LAW_INFO = 10;
        public final static int IMAGE_AND_SOUND_RESOLUTION = 11;
        public final static int IMAGE_AND_SOUND_DISPLAY_AREA = 12;
        public final static int IMAGE_AND_SOUND_AUDIO = 13;

    }

    public class ItemType {

        public final static int UNKNOWN = 0;
        public final static int START_ACTIVITY = 1;
        public final static int SHOW_DIALOG = 2;
        public final static int CHECKBOX = 3;
        public final static int INFO = 4;
        public final static int NETWORK_CONNECTED = 5;
        public final static int NETWORK_UNCONNECTED = 6;
        public final static int START_ACTIVITY_OPTION_RIGHT_OF_NAME = 7;
        public final static int SHOW_DIALOG_DETAIL_BOTTOM = 8;
        public final static int PROGRESS = 9;
        public final static int DETAIL_CHECKBOX = 10;
        public final static int LEFT_DESCRIPTION_CHECKBOX = 11;

    }

    public static class Preferences {

        private SharedPreferences mPreference;

        public final static String SHARED_PREFERENCES_NAME = "settings_config_pref";

        public static SharedPreferences getSharedPreferences(Context context) {

            return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }

    }

}
