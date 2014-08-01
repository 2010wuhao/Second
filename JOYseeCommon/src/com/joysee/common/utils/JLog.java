/**
 * =====================================================================
 *
 * @file  JLog.java
 * @Module Name   com.joysee.common.utils
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年10月28日
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
 * YueLiang          2013年10月28日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.common.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class JLog {

    public static final String App_Tag = "TVDongle";

    public static boolean USE_APPTAG = false;
    public static boolean LogSD = false;
    public static boolean MANUAL_POWER = false;
    public static boolean DBG_METHOD = true;

    private static void logD(String tag, String msg) {
        android.util.Log.d(tag, msg);
    }

    private static void logD(String tag, String msg, Throwable tr) {
        android.util.Log.d(tag, msg, tr);
    }

    private static void logW(String tag, String msg, Throwable tr) {
        android.util.Log.w(tag, msg, tr);
    }

    private static void logE(String tag, String msg) {
        android.util.Log.e(tag, msg);
    }

    private static void logE(String tag, String msg, Throwable tr) {
        android.util.Log.e(tag, msg, tr);
    }

    private static String formatMsg(String tag, String msg) {
        return tag + " - " + msg;
    }

    public static void log2Disk(String tag, String msg) {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            String file = "/sdcard/" + App_Tag + ".txt";
            java.io.File SDFile = new java.io.File(file);
            if (!SDFile.exists()) {
                try {
                    SDFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(file, true);
                outputStream
                        .write((new Date().toLocaleString() + "[" + tag + "]" + ": " + msg + "\r\n")
                                .getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void d(String tag, String msg) {
        if (USE_APPTAG) {
            JLog.logD(App_Tag, formatMsg(tag, msg));
        } else {
            JLog.logD(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (USE_APPTAG) {
            JLog.logD(App_Tag, formatMsg(tag, msg), tr);
        } else {
            JLog.logD(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (USE_APPTAG) {
            JLog.logW(App_Tag, formatMsg(tag, msg), tr);
        } else {
            JLog.logW(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        if (USE_APPTAG) {
            JLog.logE(App_Tag, formatMsg(tag, msg));
        } else {
            JLog.logE(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (USE_APPTAG) {
            JLog.logE(App_Tag, formatMsg(tag, msg), tr);
        } else {
            JLog.logE(tag, msg, tr);
        }
    }

    @SuppressWarnings("rawtypes")
    public static String makeTag(Class cls) {
        return cls.getSimpleName();
    }

    public static long methodBegin(String tag) {
        long currentTime = 0;
        if (DBG_METHOD) {
            currentTime = System.currentTimeMillis();
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            StringBuilder msg = new StringBuilder();
            msg.append(methodName).append(" begin");
            if (USE_APPTAG) {
                JLog.logD(App_Tag, formatMsg(tag, msg.toString()));
            } else {
                JLog.logD(tag, msg.toString());
            }
        }
        return currentTime;
    }

    public static void methodEnd(String tag, long begin) {
        if (DBG_METHOD) {
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            StringBuilder msg = new StringBuilder();
            msg.append("wait ");
            msg.append(System.currentTimeMillis() - begin);
            msg.append(" ms ");
            msg.append("for method ");
            msg.append(methodName);
            if (USE_APPTAG) {
                JLog.logD(App_Tag, formatMsg(tag, msg.toString()));
            } else {
                JLog.logD(tag, msg.toString());
            }
        }
    }

    public static void methodEnd(String tag, long begin, String obj) {
        if (DBG_METHOD) {
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            StringBuilder msg = new StringBuilder();
            msg.append("wait ");
            msg.append(System.currentTimeMillis() - begin);
            msg.append(" ms ");
            msg.append("for method  ");
            msg.append(methodName);
            msg.append("  to deal ");
            msg.append(obj);
            if (USE_APPTAG) {
                JLog.logD(App_Tag, formatMsg(tag, msg.toString()));
            } else {
                JLog.logD(tag, msg.toString());
            }
        }
    }

}
