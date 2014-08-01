/**
 * =====================================================================
 *
 * @file  DvbSettings.java
 * @Module Name   com.joysee.adtv.settings
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Dec 4, 2013
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
 * yueliang         Dec 4, 2013            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.AndroidException;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.db.DvbProvider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DvbSettings {
    private static class NameValueCache {
        private final Uri mUri;

        private static final String[] SELECT_VALUE =
                new String[] {
                        DvbSettings.NameValueTable.VALUE
                };
        private static final String NAME_EQ_PLACEHOLDER = "name=?";

        private final HashMap<String, String> mValues = new HashMap<String, String>();

        public NameValueCache(Uri uri) {
            mUri = uri;
        }

        public void cleanCache(Map<String, String> map) {
            synchronized (this) {
                Iterator<Entry<String, String>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    mValues.remove(it.next().getKey());
                }
            }
        }

        public void cleanCache(String name) {
            synchronized (this) {
                if (mValues.containsKey(name)) {
                    JLog.d(TAG, "NameValueCache clean cache : " + name);
                    mValues.remove(name);
                }
            }
        }

        public String getString(ContentResolver cr, String name) {
            synchronized (this) {
                if (mValues.containsKey(name)) {
                    return mValues.get(name); // Could be null, that's OK --
                    // negative caching
                }
            }

            Cursor c = null;
            try {
                c = cr.query(mUri, SELECT_VALUE, NAME_EQ_PLACEHOLDER,
                        new String[] {
                            name
                        }, null);
                if (c == null) {
                    JLog.d(TAG, "Can't get key " + name + " from " + mUri);
                    return null;
                }

                String value = c.moveToNext() ? c.getString(0) : null;
                synchronized (this) {
                    mValues.put(name, value);
                }
                JLog.d(TAG, "cache miss [" + mUri.getLastPathSegment() + "]: " +
                        name + " = " + (value == null ? "(null)" : value));
                return value;
            } catch (Exception e) {
                JLog.e(TAG, "Can't get key " + name + " from " + mUri, e);
                return null; // Return null, but don't cache it.
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        public Map<String, String> getStringArray(ContentResolver cr, String[] keys) {

            int count = keys.length;
            Map<String, String> retMap = new HashMap<String, String>(count);
            for (int i = 0; i < count; i++) {
                synchronized (this) {
                    if (mValues.containsKey(keys[i])) {
                        retMap.put(keys[i], mValues.get(keys[i]));
                    }
                }
                if (!retMap.containsKey(keys[i])) {
                    Cursor c = null;
                    try {
                        c = cr.query(mUri, SELECT_VALUE, NAME_EQ_PLACEHOLDER, new String[] {
                                keys[i]
                        }, null);
                        if (c == null) {
                            retMap.put(keys[i], null);
                        } else {
                            String value = c.moveToNext() ? c.getString(0) : null;
                            synchronized (this) {
                                retMap.put(keys[i], value);
                            }
                        }
                    } catch (Exception e) {
                        JLog.d(TAG, "getStringArray error key " + keys[i], e);
                    } finally {
                    	if (c != null) {
							c.close();
						}
                    }
                }
            }
            return retMap;
        }
    }

    /**
     * Common base for tables of name/value settings.
     */
    public static class NameValueTable implements BaseColumns {
        public static final String NAME = "name";
        public static final String VALUE = "value";

        public static Uri getUriFor(Uri uri, String name) {
            return Uri.withAppendedPath(uri, name);
        }

        protected static boolean putString(ContentResolver resolver, Uri uri,
                String name, String value) {
            // The database will take care of replacing duplicates.
            try {
                ContentValues values = new ContentValues();
                values.put(NAME, name);
                values.put(VALUE, value);
                resolver.insert(uri, values);
                return true;
            } catch (SQLException e) {
                JLog.d(TAG, "Can't set key " + name + " in " + uri, e);
                return false;
            }
        }

        protected static boolean putStringArray(ContentResolver resolver, Uri uri, Map<String, String> map) {
            try {
                Iterator<Entry<String, String>> it = map.entrySet().iterator();
                ContentValues[] valuesArray = new ContentValues[map.size()];
                int i = -1;
                while (it.hasNext()) {
                    i++;
                    Entry<String, String> entry = it.next();
                    ContentValues values = new ContentValues();
                    values.put(NAME, entry.getKey());
                    values.put(VALUE, entry.getValue());
                    valuesArray[i] = values;
                }
                resolver.bulkInsert(uri, valuesArray);
                return true;
            } catch (Exception e) {
                JLog.d(TAG, "Can't set map", e);
                return false;
            }
        }
    }

    public static class SettingNotFoundException extends AndroidException {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public SettingNotFoundException(String msg) {
            super(msg);
        }
    }

    public static final class System extends NameValueTable {

        public static final String DEFAULT_FREQUENCY = "default_frequency";
        public static final String DEFAULT_SYMBOL_RATE = "default_symbol_rate";
        public static final String DEFAULT_MODULATION = "default_modulation";
        public static final String LOCAL_PACK_INFO = "local_pack_info";
        public static final String LOCAL_PACK_APKS_NAME = "local_pack_apks_name";
        public static final String LOCAL_PACK_ZIP_DOWN_ID = "local_pack_zip_down_id";

        public static final String LOCAL_AREA_INFO = "local_area_info";
        public static final String LOCAL_ZONE_CODE = "local_zone_code";
        public static final String LOCAL_OPERATOR_CODE = "local_operator_code";
        public static final String LOCAL_OPERATOR_NAME = "local_operator_name";

        public static final String FAST_SEARCH_PARAMS = "fast_search_params";
        public static final String FULL_SEARCH_PARAMS = "full_search_params";
        public static final String MANUAL_SEARCH_PARAMS = "manual_search_params";

        public static final String DEFAULT_CHANNEL_VOLUME = "default_channel_volume";

        public static final String VIDEO_ASPECTRATIO_TUNER_0 = "video_aspectratio_tuner_0";
        public static final String VIDEO_ASPECTRATIO_TUNER_1 = "video_aspectratio_tuner_1";
        public static final String VIDEO_ASPECTRATIO_TUNER_2 = "video_aspectratio_tuner_2";

        public static final String APK_UPDATE_FILE_PATH = "apk_update_file_path";
        public static final String APK_UPDATE_FILE_MD5 = "apk_update_file_md5";

        public static final String LOCAL_OPERATOR_SET_SUCCESSFULLY = "local_operator_set_successfully";

        /**
         * 1:true 0:false
         */
        public static final String DEBUG_MODE = "debug_mode";
        /**
         * 1:true 0:false
         */
        public static final String DEBUG_LOG = "debug_log";
        /**
         * 1:true 0:false
         */
        public static final String VOD_ENABLE = "vod_enable";
        /**
         * 1:true 0:false
         */
        public static final String PORTAL_USE_ANIMATION = "portal_use_animation";
        /**
         * 1:true 0:false
         */
        public static final String FORCE_TS_EPG = "force_ts_epg";
        /**
         * 1:true 0:false
         */
        public static final String FORCE_TS_CHANNELTYPE = "force_ts_channeltype";

        private static NameValueCache sNameValueCache = null;

        public static final Uri CONTENT_URI = DvbProvider.CONTENT_URI_SETTINGS;
        public static final Uri CONTENT_URI_NO_NOTIFICATION = DvbProvider.CONTENT_URI_SETTINGS_NO_NOTIFICATION;

        public synchronized static boolean getBoolean(ContentResolver cr, String name) {
            String valString = getString(cr, name);
            boolean value;
            try {
                value = valString != null ? Boolean.parseBoolean(valString) : false;
            } catch (Exception e) {
                value = false;
            }
            return value;
        }

        public static float getFloat(ContentResolver cr, String name)
                throws SettingNotFoundException {
            String v = getString(cr, name);
            if (v == null) {
                throw new SettingNotFoundException(name);
            }
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            String v = getString(cr, name);
            try {
                return v != null ? Float.parseFloat(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static int getInt(ContentResolver cr, String name)
                throws SettingNotFoundException {
            String v = getString(cr, name);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                JLog.e(TAG, "DvbSettings getInt catch Exception", e);
                return def;
            }
        }

        public static long getLong(ContentResolver cr, String name)
                throws SettingNotFoundException {
            String valString = getString(cr, name);
            try {
                return Long.parseLong(valString);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            String valString = getString(cr, name);
            long value;
            try {
                value = valString != null ? Long.parseLong(valString) : def;
            } catch (NumberFormatException e) {
                value = def;
            }
            return value;
        }

        /**
         * Look up a name in the database.
         * 
         * @param resolver to access the database with
         * @param name to look up in the table
         * @return the corresponding value, or null if not present
         */
        public synchronized static String getString(ContentResolver resolver, String name) {
            if (sNameValueCache == null) {
                sNameValueCache = new NameValueCache(CONTENT_URI);
            }
            return sNameValueCache.getString(resolver, name);
        }

        public synchronized static Map<String, String> getStringArray(ContentResolver resolver, String[] names) {
            if (sNameValueCache == null) {
                sNameValueCache = new NameValueCache(CONTENT_URI);
            }
            return sNameValueCache.getStringArray(resolver, names);
        }

        public static boolean putBoolean(ContentResolver resolver, String name, boolean value) {
            sNameValueCache.cleanCache(name);
            return putString(resolver, CONTENT_URI, name, String.valueOf(value));
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putString(cr, name, Integer.toString(value));
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            sNameValueCache.cleanCache(name);
            return putString(resolver, CONTENT_URI, name, value);
        }

        public static boolean putStringArray(ContentResolver resolver, Map<String, String> array) {
            sNameValueCache.cleanCache(array);
            return putStringArray(resolver, CONTENT_URI, array);
        }
    }

    private static final String TAG = JLog.makeTag(DvbSettings.class);
}
