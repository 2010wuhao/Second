/**
 * =====================================================================
 *
 * @file  UpdateClient.java
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

package com.joysee.dvb.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.joysee.common.data.JHttpHelper;
import com.joysee.common.data.JHttpParserCallBack;
import com.joysee.common.data.JRequestParams;
import com.joysee.common.download.JDownloadManager;
import com.joysee.common.download.JDownloadDBHelper.JDownloadTaskColumnIndex;
import com.joysee.common.utils.JLog;
import com.joysee.common.utils.JMD5;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.bean.UpdateInfo;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.parser.UpdateInfoParser;

import java.io.File;

public abstract class UpdateClient {

    private class DownloadStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (JDownloadManager.ACTION_DOWNLOAD_PROGRESS.equals(action)) {
                int pro = intent.getIntExtra(JDownloadManager.COLUMN_PROGRESS, 0);
                handlerDownloadProgress(pro);
            } else if (JDownloadManager.ACTION_DOWNLOAD_COMPLETED.equals(action)) {
                long downId = intent.getLongExtra(JDownloadManager.COLUMN_DOWNLOAD_ID, -1);
                int status = intent.getIntExtra(JDownloadManager.COLUMN_STATUS, -1);
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "onReceive  id=" + downId + "  status=" + status);
                }
                if (mUpdateInfo != null && mUpdateInfo.downId == downId) {
                    handlerDownloadState(status == JDownloadManager.STATUS_SUCCESSFUL ? STATUS_DOWNLOAD_SUCCESS : STATUS_DOWNLOAD_FAILURE);
                }
            }
        }
    }

    protected static final String TAG = JLog.makeTag(UpdateClient.class);
    public static final int STATUS_CHECK_NO_UPDATE = 0;
    public static final int STATUS_CHECK_FAILURE = 1;
    public static final int STATUS_CHECK_AN_NEW_VERSION = 2;
    public static final int STATUS_DOWNLOAD_SUCCESS = 3;
    public static final int STATUS_DOWNLOAD_IN_QUEUE = 4;
    public static final int STATUS_DOWNLOAD_FAILURE = 5;

    public static final int TYPE_ENFORCE_YES = 1;
    public static final int TYPE_ENFORCE_NO = 2;

    private Context mContext;
    private Handler mMainLooper;
    private JDownloadManager mJDownloadManager;
    private DownloadStateReceiver mDownloadReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;
    private UpdateInfo mUpdateInfo;
    private String mCheckRequest;
    private boolean fileExists = false;
    private boolean mDownloadStarting;

    public UpdateClient(Context context) {
        this.mContext = context;
        this.mMainLooper = new Handler(context.getMainLooper());
        mJDownloadManager = new JDownloadManager(context);
        dealCloseExceptionTask();
    }

    /**
     * callback on Main Thread
     */
    public abstract void handlerCheckState(int status, Object attach);

    /**
     * callback on Main Thread
     */
    public void handlerDownloadState(int status) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "deal download status in UpdateClient, status=" + status);
        }
        if (status == STATUS_DOWNLOAD_SUCCESS) {
            // requestInstall();
        }
    }

    /**
     * callback on Main Thread
     */
    public void handlerDownloadProgress(int progress) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "deal download progress in UpdateClient, progress=" + progress);
        }
    }

    public final void registerDownloadReceiver() {
        if (mLocalBroadcastManager == null) {
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        }
        if (mDownloadReceiver == null) {
            mDownloadReceiver = new DownloadStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(JDownloadManager.ACTION_DOWNLOAD_COMPLETED);
        filter.addAction(JDownloadManager.ACTION_DOWNLOAD_PROGRESS);
        mLocalBroadcastManager.registerReceiver(mDownloadReceiver, filter);
    }

    public final void unRegisterDownloadReceiver() {
        if (mDownloadReceiver != null) {
            if (mLocalBroadcastManager != null) {
                mLocalBroadcastManager.unregisterReceiver(mDownloadReceiver);
                mLocalBroadcastManager = null;
            }
            mDownloadReceiver = null;
        }
    }

    public final void cancelRequest() {
        if (mCheckRequest != null) {
            JHttpHelper.cancelJsonRequests(mCheckRequest, true);
        }
    }

    public final void checkAsync() {
        final long begin = SystemClock.currentThreadTimeMillis();
        JRequestParams params = new JRequestParams();
        params.put("sn", "ACDBDA040288");
        params.put("packageName", mContext.getPackageName());
        params.put("versionCode", getLocalVersion(mContext) + "");
        JHttpHelper.setJsonTimeout(5 * 1000);
        JHttpHelper.getJson(Constants.UPDATE_INTERFACE, params, new JHttpParserCallBack(new UpdateInfoParser(mContext)) {
            @Override
            public void onSuccess(final Object arg0) {
                long delay = (SystemClock.currentThreadTimeMillis() - begin) < 2000 ? 2000 : 0;
                handlerCheckSuccess(arg0, delay);
            }

            @Override
            public void onFailure(final int arg0, Throwable arg1) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "checkAsync onFailure, code = " + arg0, arg1);
                }
                long delay = (SystemClock.currentThreadTimeMillis() - begin) < 2000 ? 2000 : 0;
                handlerCheckFailure(arg0, delay);
            }
        });
        mCheckRequest = Constants.UPDATE_INTERFACE + "?" + params.toString();
    }

    private void handlerCheckFailure(final int retCode, long delay) {
        mMainLooper.postDelayed(new Runnable() {
            @Override
            public void run() {
                handlerCheckState(retCode == 200 ? STATUS_CHECK_NO_UPDATE : STATUS_CHECK_FAILURE, null);
            }
        }, delay);
    }

    private void handlerCheckSuccess(Object arg0, long delayed) {
        final UpdateInfo info = (UpdateInfo) arg0;
        mUpdateInfo = info;

        int localVersion = getLocalVersion(mContext);
        fileExists = false;

        // check net version
        if (Integer.valueOf(info.version) <= localVersion) {
            mMainLooper.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handlerCheckState(STATUS_CHECK_NO_UPDATE, null);
                }
            }, delayed);
            return;
        }
        // check local APK file
        if (!TextUtils.isEmpty(info.apkPath)) {
            File file = new File(info.apkPath);
            if (file.exists() && JMD5.check(file, info.md5)) {
                if (getApkFileVersion(mContext, info.apkPath) > localVersion) {
                    fileExists = true;
                }
            }
            if (!fileExists) {
                file.delete();
            }
        }
        mMainLooper.postDelayed(new Runnable() {
            @Override
            public void run() {
                handlerCheckState(STATUS_CHECK_AN_NEW_VERSION, info.version);
            }
        }, delayed);
    }

    public final void down() {
        if (mUpdateInfo == null || mDownloadStarting) {
            return;
        }
        new Runnable() {
            @Override
            public void run() {
                mDownloadStarting = true;
                clearCache();
                JDownloadManager.Request apkRequest = new JDownloadManager.Request(Uri.parse(mUpdateInfo.url));
                apkRequest.setDestinationUri(Uri.parse(mUpdateInfo.apkPath));
                apkRequest.setDescription(mUpdateInfo.instructions);
                apkRequest.setTitle(mUpdateInfo.updateName);
                long id = mJDownloadManager.addQueue(apkRequest);
                mUpdateInfo.downId = id;
                mUpdateInfo.writeLocalInfo(mContext);
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "begin down, id=" + id + " path=" + mUpdateInfo.apkPath);
                }
                mDownloadStarting = false;
            }
        }.run();
    }

    public final boolean requestInstall() {
        boolean ret = false;
        if (mUpdateInfo != null && !TextUtils.isEmpty(mUpdateInfo.apkPath)) {
            File file = new File(mUpdateInfo.apkPath);
            if (file.exists()) {
                PackageManager pm = mContext.getPackageManager();
                boolean available = false;
                PackageInfo packageInfo = null;
                final String apkPath = mUpdateInfo.apkPath;
                try {
                    packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
                } catch (Exception e) {
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG, "takeInstall getPackageArchiveInfo", e);
                    }
                } finally {
                    if (packageInfo != null) {
                        int downFileVersion = packageInfo.versionCode;
                        int localVersion = getLocalVersion(mContext);
                        available = downFileVersion > localVersion;
                        if (TvApplication.DEBUG_LOG) {
                            JLog.d(TAG, "local version=" + localVersion + "   down file version=" + downFileVersion);
                        }
                    }
                    if (available) {
                        ret = true;
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.parse("file://" + apkPath), "application/vnd.android.package-archive");
                        mContext.startActivity(intent);
                    } else {
                        file.delete();
                        new UpdateInfo().writeLocalInfo(mContext);
                        mUpdateInfo = null;
                    }
                }
            }
        } else {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "requestInstall, file is not exists");
            }
        }
        return ret;
    }

    private int getLocalVersion(Context context) {
        int ret = -1;
        PackageInfo localApkInfo = null;
        try {
            localApkInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (localApkInfo != null) {
                ret = localApkInfo.versionCode;
            }
        }
        return ret;
    }

    private int getApkFileVersion(Context context, String path) {
        int version = -1;
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "popInstall getPackageArchiveInfo", e);
            }
        } finally {
            if (packageInfo != null) {
                version = packageInfo.versionCode;
            }
        }
        return version;
    }

    public final boolean isDownloadSuccess() {
        return fileExists;
    }

    /**
     * when UI onDestory , delete unCompleted task
     */
    public final void removeUnCompletedDoanload() {
        if (mUpdateInfo != null && mUpdateInfo.downId != -1) {
            if (mUpdateInfo.downId != -1) {
                Cursor c = null;
                try {
                    c = mJDownloadManager.queryTask(mUpdateInfo.downId);
                    if (c != null && c.moveToFirst()) {
                        int status = c.getInt(JDownloadTaskColumnIndex.STATUS);
                        if (TvApplication.DEBUG_LOG) {
                            JLog.d(TAG, "check delete task or not   status=" + status);
                        }
                        if (status != JDownloadManager.STATUS_SUCCESSFUL) {
                            if (TvApplication.DEBUG_LOG) {
                                JLog.d(TAG, "removeDoanload " + mUpdateInfo.downId);
                            }
                            mJDownloadManager.remove(mUpdateInfo.downId);
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    /**
     * 处理退出时未执行removeUnCompletedDoanload()的task , 例如断电
     */
    private void dealCloseExceptionTask() {
        UpdateInfo lastInfo = new UpdateInfo();
        lastInfo.readLocalInfo(mContext);
        if (lastInfo.downId != -1) {
            Cursor c = null;
            try {
                c = mJDownloadManager.queryTask(lastInfo.downId);
                if (c != null && c.moveToFirst()) {
                    int status = c.getInt(JDownloadTaskColumnIndex.STATUS);
                    if (status != JDownloadManager.STATUS_SUCCESSFUL) {
                        if (TvApplication.DEBUG_LOG) {
                            JLog.d(TAG, "dealCloseExceptionTask, id=" + lastInfo.downId);
                        }
                        mJDownloadManager.remove(lastInfo.downId);
                        File file = new File(lastInfo.apkPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    private void clearCache() {
        long begin = 0;
        if (TvApplication.DEBUG_LOG) {
            begin = JLog.methodBegin(TAG);
        }
        int clearCount = 0;
        if (mUpdateInfo != null && mUpdateInfo.apkSavePath != null) {
            File file = new File(mUpdateInfo.apkSavePath);
            String[] arrayFiles = file.list();
            try {
                if (arrayFiles != null && arrayFiles.length > 0) {
                    for (int i = 0; i < arrayFiles.length; i++) {
                        clearCount++;
                        File f = new File(mUpdateInfo.apkSavePath + File.separator + arrayFiles[i]);
                        if (f.exists()) {
                            if (TvApplication.DEBUG_LOG) {
                                JLog.d(TAG, "delete -- >" + f.toString());
                            }
                            f.delete();
                        }
                    }
                }
            } catch (Exception e) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "clearCache", e);
                }
            }
        }
        if (TvApplication.DEBUG_LOG) {
            JLog.methodEnd(TAG, begin, "delete files " + clearCount);
        }
    }

    public final int getUpdateType() {
        if (mUpdateInfo != null) {
            return mUpdateInfo.enforce;
        }
        return -1;
    }
}
