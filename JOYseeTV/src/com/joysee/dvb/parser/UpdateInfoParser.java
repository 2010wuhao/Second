/**
 * =====================================================================
 *
 * @file  UpdateInfoParser.java
 * @Module Name   com.joysee.dvb.parser
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-3-27
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
 * benz          2014-3-27           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.parser;

import android.content.Context;
import android.os.Environment;

import com.joysee.common.data.JBaseParser;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.bean.UpdateInfo;
import com.joysee.dvb.update.UpdateClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class UpdateInfoParser extends JBaseParser<UpdateInfo> {

    private static final String TAG = JLog.makeTag(UpdateInfoParser.class);
    private Context mContext;

    public UpdateInfoParser(Context context) {
        this.mContext = context;
    }

    @Override
    public String checkResponse(String json) throws JSONException {
        return null;
    }

    @Override
    public UpdateInfo parseJSON(String json) throws JSONException {
        if (json == null || "".equals(json)) {
            return null;
        }
        JLog.d(TAG, "parseJSON->" + json);
        JSONObject jsonObj = new JSONObject(json);
        String returnCode = jsonObj.getString("code");
        if ("2".equals(returnCode) || "3".equals(returnCode)) {
            return null;
        }
        UpdateInfo info = new UpdateInfo();
        info.instructions = jsonObj.getString("note");
        info.updateName = jsonObj.getString("versionName");
        info.md5 = jsonObj.getString("encode");
        info.url = jsonObj.getString("filePath");
        info.version = jsonObj.getString("versionCode");
        info.enforce = (jsonObj.has("updateFlag") ? jsonObj.getInt("updateFlag") : 0) != 0 ? UpdateClient.TYPE_ENFORCE_YES
                : UpdateClient.TYPE_ENFORCE_NO;
        String[] tempStr = info.url.split("/");
        info.apkFileName = tempStr[tempStr.length - 1];
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            info.apkSavePath = mContext.getExternalCacheDir().getPath() + File.separator + "update_file";
        } else {
            info.apkSavePath = mContext.getCacheDir().getPath() + File.separator + "update_file";
        }
        info.apkPath = info.apkSavePath + File.separator + info.apkFileName;
        return info;
    }

}
