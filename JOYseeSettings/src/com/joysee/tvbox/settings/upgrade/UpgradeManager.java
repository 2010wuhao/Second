/**
 * =====================================================================
 *
 * @file   UpgradeManager.java
 * @Module Name   com.joysee.tvbox.settings.upgrade
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  May 22, 2014
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
 * wumingjun         @May 22, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.upgrade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class UpgradeManager {

    private final static String TAG = UpgradeManager.class.getSimpleName();

    private final static String EXTERNAL_SDCARD = "/mnt/external_sdcard/";

    private final static String DIR_MNT = "/mnt";
    private final static String UPDATE_FILE_NAME = "update.zip";

    private Context mContext;

    private DownloadProgressListener mListener;

    private DownloadProgressListener mCheckListener;

    private LocalUpdateCheckListener mLocalUpdateCheckListener;

    private UpgradeHttpClient mClient;

    public UpgradeManager(Context context) {
        super();
        this.mContext = context;
        mClient = new UpgradeHttpClient(mContext);
    }

    public void setDownloadListener(DownloadProgressListener listener) {
        mListener = listener;
    }

    public void setCheckListener(DownloadProgressListener listener) {
        mCheckListener = listener;
    }

    public void setLocalCheckListener(LocalUpdateCheckListener listener) {
        mLocalUpdateCheckListener = listener;
    }

    public void startCheck() {
        (new Thread(new Runnable() {

            @Override
            public void run() {
                mClient.startCheck(mCheckListener);
            }
        })).start();
    }

    public void startDownload() {
        mClient.startDownload(mListener);
    }

    public void cancelDownload() {
        mClient.cacelDownload();
    }

    /**
     * 本地升级
     */
    public void localUpdate() {
        ArrayList<String> array = new ArrayList<String>();
        array = findUpdateFile();
        boolean foundNew = false;
        int updateFileCount = 0;
        for (int i = 0; i < array.size(); i++) {
            if (mClient.checkLocalUpdate_xml(0, array.get(i))) {
                mLocalUpdateCheckListener.onCheckFinish(LocalUpdateCheckListener.VERSION_NEW);
                foundNew = true;
                updateFileCount++;
            }
        }

        if (!foundNew) {
            mLocalUpdateCheckListener.onCheckFinish(LocalUpdateCheckListener.VERSION_LATEST);
        }
    }

    public ArrayList<String> findUpdateFile() {
        Log.d(TAG, " findUpdateFile begin ");
        File rootFile = new File(DIR_MNT);
        ArrayList<String> paths = new ArrayList<String>();
        String[] rootFiles = rootFile.list();
        if (rootFiles != null) {
            for (int i = 0; i < rootFiles.length; i++) {
                Log.d(TAG, " findUpdateFile files[" + i + "]" + rootFiles[i]);
                if (rootFiles[i].startsWith("sd") && !rootFiles[i].equals("sdcard")) {
                    paths.add(rootFiles[i]);
                }
                if (rootFiles[i].equals("VIRTUAL_CDROM")) {
                    execCmd("vdc loop unmount");
                    break;
                }
            }
        }
        String filepath = null;
        ArrayList<String> datapath = new ArrayList<String>();
        for (int j = 0; j < paths.size(); j++) {
            filepath = DIR_MNT + "/" + paths.get(j) + "/";
            Log.d(TAG, " paths.get " + j + " = " + filepath);
            File updateFile = new File(filepath + UPDATE_FILE_NAME);
            if (updateFile.exists()) {
                datapath.add(filepath);
            }
        }
        File updateFile = new File(EXTERNAL_SDCARD + UPDATE_FILE_NAME);
        if (updateFile.exists()) {
            datapath.add(EXTERNAL_SDCARD);
        }
        Log.d(TAG, " findUpdateFile datepath.size = " + datapath.size());
        return datapath;
    }

    public void execCmd(String cmd) {
        int ch;
        Process p = null;
        Log.d(TAG, "exec command: " + cmd);
        try {
            p = Runtime.getRuntime().exec(cmd);
            InputStream in = p.getInputStream();
            InputStream err = p.getErrorStream();
            StringBuffer sb = new StringBuffer(512);
            while ((ch = in.read()) != -1) {
                sb.append((char) ch);
            }
            if (sb.toString() != "")
                Log.d(TAG, "exec out:" + sb.toString());
            while ((ch = err.read()) != -1) {
                sb.append((char) ch);
            }
            if (sb.toString() != "")
                Log.d(TAG, "exec error:" + sb.toString());
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.toString());
        }
    }

}
