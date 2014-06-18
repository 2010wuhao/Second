/**
 * =====================================================================
 *
 * @file  UpdateInfo.java
 * @Module Name   com.joysee.dvb.update
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

package com.joysee.dvb.bean;

import android.content.Context;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.data.DvbSettings;
import com.joysee.dvb.update.UpdateClient;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class UpdateInfo {

    public static final String URL = "update_url";
    public static final String MD5 = "update_md5";
    public static final String UPDATENAME = "update_taskName";
    public static final String APKSAVEPATH = "update_apkSavePath";
    public static final String APKFILENAME = "update_apkFileName";
    public static final String INSTRUCTIONS = "update_instructions";
    public static final String DOWNID = "update_downid";
    public static final String APKPATH = "update_apkPath";
    public static final String VERSION = "update_version";
    public static final String ENFORCE = "update_enforce";
    private static final String TAG = JLog.makeTag(UpdateInfo.class);

    final String[] keys = {
            URL, MD5, UPDATENAME, APKSAVEPATH, APKFILENAME, //
            INSTRUCTIONS, DOWNID, VERSION, ENFORCE
    };

    public String url;
    public String md5;
    public String updateName;
    public String apkSavePath;
    public String apkFileName;
    public String instructions;
    public long downId;
    public String apkPath;
    public String version;
    public int enforce;// 是否强制

    public boolean readLocalInfo(Context ctx) {
        Map<String, String> infoSet = DvbSettings.System.getStringArray(ctx.getContentResolver(), keys);
        if (infoSet != null) {
            Iterator<Entry<String, String>> it = infoSet.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                String key = entry.getKey();
                String value = entry.getValue();
                // JLog.d(TAG, "read  key=" + key + "  value=" + value);
                if (URL.equals(key)) {
                    this.url = value;
                } else if (MD5.equals(key)) {
                    this.md5 = value;
                } else if (UPDATENAME.equals(key)) {
                    this.updateName = value;
                } else if (APKSAVEPATH.equals(key)) {
                    this.apkSavePath = value;
                } else if (APKFILENAME.equals(key)) {
                    this.apkFileName = value;
                } else if (INSTRUCTIONS.equals(key)) {
                    this.instructions = value;
                } else if (DOWNID.equals(key)) {
                    this.downId = (value != null && !"".equals(value)) ? Integer.valueOf(value) : -1;
                } else if (VERSION.equals(key)) {
                    this.version = value;
                } else if (ENFORCE.equals(key)) {
                    this.enforce = (value != null && !"".equals(value)) ? Integer.valueOf(value) : UpdateClient.TYPE_ENFORCE_NO;
                }
            }
            this.apkPath = this.apkSavePath + File.separator + this.apkFileName;
        }
        JLog.d(TAG, "---------readLocalInfo----------- \n" + this.toString());
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mCheckInterface=" + url + "\n");
        sb.append("md5=" + md5 + "\n");
        sb.append("apkPath=" + apkPath + "\n");
        sb.append("updateName=" + updateName + "\n");
        sb.append("apkSavePath=" + apkSavePath + "\n");
        sb.append("apkFileName=" + apkFileName + "\n");
        sb.append("instructions=" + instructions + "\n");
        sb.append("version=" + version + "\n");
        sb.append("downid=" + downId + "\n");
        sb.append("----------------------------------\n");
        return sb.toString();
    }

    public boolean writeLocalInfo(Context ctx) {
        Map<String, String> map = new HashMap<String, String>(keys.length);
        map.put(URL, this.url);
        map.put(MD5, this.md5);
        map.put(APKPATH, this.apkPath);
        map.put(UPDATENAME, this.updateName);
        map.put(APKSAVEPATH, this.apkSavePath);
        map.put(APKFILENAME, this.apkFileName);
        map.put(INSTRUCTIONS, this.instructions);
        map.put(DOWNID, this.downId + "");
        map.put(VERSION, this.version);
        map.put(ENFORCE, this.enforce + "");
        JLog.d(TAG, "---------writeLocalInfo----------- \n" + this.toString());
        return DvbSettings.System.putStringArray(ctx.getContentResolver(), map);
    }

}
