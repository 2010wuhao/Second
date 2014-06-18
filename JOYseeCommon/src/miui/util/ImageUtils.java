/**
 * =====================================================================
 *
 * @file  ImageUtils.java
 * @Module Name   miui.util
 * @author MIUI
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-16
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
 * MIUI          2014-1-16           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package miui.util;

import com.joysee.common.utils.JLog;

import android.graphics.Bitmap;

public class ImageUtils {

    static {
        JLog.d("ImageUtils", "load  imageutilities_jni.so  start");
        System.loadLibrary("imageutilities_jni");
        JLog.d("ImageUtils", "load  imageutilities_jni.so  end");
    }

    private static ImageUtils mImageUtils;

    public static ImageUtils getInstance() {
        if (mImageUtils == null) {
            synchronized (ImageUtils.class) {
                if (mImageUtils == null) {
                    mImageUtils = new ImageUtils();
                }
            }
        }
        return mImageUtils;
    }

    public void createFastBlur(Bitmap paramBitmap1, Bitmap paramBitmap2, int blurLevel) {
        native_fastBlur(paramBitmap1, paramBitmap2, blurLevel);
    }

    private native void native_fastBlur(Bitmap paramBitmap1, Bitmap paramBitmap2, int paramInt);
}
