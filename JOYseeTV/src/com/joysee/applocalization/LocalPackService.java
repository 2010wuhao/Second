/**
 * =====================================================================
 *
 * @file  LocalizationManager.java
 * @Module Name   com.joysee.applocalization
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013-12-10
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
 * benz          2013-12-10           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.applocalization;

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
import android.os.Message;
import android.os.Process;

import com.joysee.common.data.JHttpHelper;
import com.joysee.common.data.JHttpJsonCallBack;
import com.joysee.common.data.JRequestParams;
import com.joysee.common.download.JDownloadDBHelper.JDownloadTaskColumnIndex;
import com.joysee.common.download.JDownloadManager;
import com.joysee.common.download.JDownloadManager.Request;
import com.joysee.common.utils.JLog;
import com.joysee.common.utils.JMD5;
import com.joysee.common.utils.JMachineTag;
import com.joysee.common.utils.JZip;
import com.joysee.dvb.data.DvbSettings;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

public class LocalPackService {
    private class AsyncLocalPackCheck extends Thread {

        PackAttach attach;

        public AsyncLocalPackCheck(PackAttach attach) {
            this.attach = attach;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
            synchronized (mLock) {
                try {
                    JLog.d(TAG, "---Local Pack Check is Look---");
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (attach.zipFile.exists()) {
                    parserZip(attach);
                }
            }
        }
    }

    private class LocalPackReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            if (JDownloadManager.ACTION_DOWNLOAD_COMPLETED.equals(action)) {
               /* String downTaskTitle = intent.getStringExtra(JDownloadManager.co);
                long completeId = intent.getLongExtra(JDownloadManager.EXTRA_DOWNLOAD_ID, -1);
                long downId = DvbSettings.System.getLong(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_ZIP_DOWN_ID, -1);
                JLog.d(TAG, "LocalPackReceiver ---> onReceive title=" + downTaskTitle + "   completeId=" + completeId + "  nativeId="
                        + downId);
                if (LocalParams.DOWN_ZIP_TITLE.equals(downTaskTitle) && (downId == completeId)) {
                    int downStatus = intent.getIntExtra(JDownloadManager.COLUMN_STATUS, -1);
                    String packDescription = intent.getStringExtra(JDownloadManager.COLUMN_DESCRIPTION);
                    *//** 下载成功，更新本地配置信息 *//*
                    if (downStatus == Impl.STATUS_SUCCESS) {
                        DvbSettings.System.putString(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_INFO, packDescription);
                    }
                    *//** 开启线程检测ZIP包 *//*
                    startLocalPackCheck(substringPackAttach(packDescription));

                    *//** 任务完成，设任务ID为-1 *//*
                    DvbSettings.System.putLong(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_ZIP_DOWN_ID, -1);
                }*/
            }

            else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                JLog.d(TAG, "ACTION_PACKAGE_ADDED");
            }

            else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                JLog.d(TAG, "ACTION_PACKAGE_CHANGED");
            }
        }

    }

    public class LocalParams {
        public static final int DEFAULT_VERSION = -1;

        public static final String SPLIT_TAG = "&";
        public static final String DOWN_ZIP_TITLE = "com.joysee.local.pack.zip";
        public static final String DOWN_ZIP_FILE_NAME = "local_pack.zip";
        public static final String DOWN_ZIP_URL = "http://qd.baidupcs.com/file/049c94bdfb709de543fe3c0ed8b215e1?xcode=6154845e7c70d910b2085cba13a0dd93b4edaa0533fac581&fid=2349846395-250528-3889346027&time=1387172299&sign=FDTAXER-DCb740ccc5511e5e8fedcff06b081203-MztoW083cavhfB1CDEyG%2Fc8G9Hk%3D&to=qb&fm=Q,B,U,ny&expires=8h&rt=pr&r=509049272&logid=1910156360&vuk=2349846395&fn=apk.zip";
        public static final String DOWN_ZIP_PATH = "/cache/tv-dongle-local/";
    }

    public class PackAttach {

        public int version;
        public File zipFile;
        public String md5;
        public String path;
        public String name;
        public String zipUrl;

        public PackAttach(String[] value) { // version name md5 path zipUrl
            this.version = Integer.parseInt(value[0]);
            this.name = value[1];
            this.md5 = value[2];
            this.path = value[3];
            this.zipUrl = value[4];
            this.zipFile = new File(path + File.separator + name);
            JLog.d(TAG, "Create PackAttach = " + this.toString());
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("name=" + name + " - ");
            sb.append("version=" + version + " - ");
            sb.append("path=" + path + " - ");
            sb.append("md5=" + md5 + " - ");
            sb.append("zipUrl=" + zipUrl);
            return sb.toString();
        }
    }

    private static class WorkHandler extends Handler {
        private final WeakReference<LocalPackService> weakReference;

        public WorkHandler(LocalPackService manager) {
            weakReference = new WeakReference<LocalPackService>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            LocalPackService manager = weakReference.get();
            if (manager != null) {
                manager.handlerMessage(msg);
            }
        }
    }

    /**
     * ----> 检测流程 <---- Boolean A_lock = new Boolean(); Object B_lock = new
     * Object(); check()-------------->A_lock ----true---->退出 | false |
     * 检测本地配置包信息 | | 有 没有 | | | | 获取版本号 使用默认version | | | | -----> <----- |
     * 请求服务器版本 | | 失败、没有新版本 有 | | |
     * ---A_lock=true--->下载ZIP包----->下载成功---->更新本地配置包信息(包括版本号) | | | | | | |
     * 下载失败 | | | | | | |
     * |<--------<----------------------------A_lock=false<----
     * -----------------< | |<-----------wait | | synchronized(B_lock)--> | |
     * 检测是否存在ZIP包 | | |--------- | 有 没有 | | 解压删除ZIP 检测是否有已解压的包 | | |
     * |<-----------有 没有---->退出 | | | 安装、并删除APK | | notify B_lock | 退出 ---->
     * 安装流程 <---- 开始队列安装 | | queue ---------> 判断APK版本号 ——————————————现版本为最新版本 ^
     * | | | 未安装、版本号高于已安装版本 | | | | | 安装 | | | |
     * <----------------删除安装包------------------------< ----> 异常处理 <----
     * ----->MD5校验失败-> <--解压失败<------- | | | | 删除ZIP包、清除本地ZIP包信息
     */
    private static final String TAG = JLog.makeTag(LocalPackService.class);
    public static final int MSG_GET_SER_PACK_TRUE = 0;
    public static final int MSG_GET_SER_PACK_FALSE = 1;
    private static String mMacAddress;

    public static LocalPackService getInstance(Context ctx) {
        if (mLocalPackService == null) {
            synchronized (LocalPackService.class) {
                if (mLocalPackService == null) {
                    mLocalPackService = new LocalPackService(ctx);
                }
            }
        }
        return mLocalPackService;
    }

    private Context mCtx;

    private Object mLock = new Object();

    private WorkHandler mWorkHandler;

    private LocalPackReceiver mLocalPackReceiver;

    private static LocalPackService mLocalPackService;

    private JDownloadManager mJDownloadManager;

    private LocalPackService(Context ctx) {
        mCtx = ctx;
        mWorkHandler = new WorkHandler(this);
        mJDownloadManager = new JDownloadManager(mCtx);
        mLocalPackReceiver = new LocalPackReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(JDownloadManager.ACTION_DOWNLOAD_COMPLETED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        ctx.registerReceiver(mLocalPackReceiver, filter);
    }

    public void check() {

        if (getDownTaskLock()) {
            return;
        }

        String localpackInfo = DvbSettings.System.getString(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_INFO);
        PackAttach packAttach = substringPackAttach(localpackInfo);
        int localVersion = packAttach != null ? packAttach.version : LocalParams.DEFAULT_VERSION;

        JRequestParams params = new JRequestParams();
        params.add("mac", getMacAddress());
        params.add("area", "");
        params.add("version", localVersion + "");

        JHttpHelper.postJson(mCtx, LocalParams.DOWN_ZIP_URL, params, new JHttpJsonCallBack() {
            @Override
            public void onFailure(int arg0, Throwable arg1) {
                Message msg = obtainMessage(MSG_GET_SER_PACK_FALSE, new Object());
                mWorkHandler.sendMessage(msg);
            }

            @Override
            public void onSuccess(byte[] arg0) {
                Message msg = obtainMessage(MSG_GET_SER_PACK_TRUE, new Object());
                mWorkHandler.sendMessage(msg);
            }
        });
    }

    /**
     * @param serPackAttach 服务器的配置包信息
     */
    private void downZip(PackAttach serPackAttach) {
        JDownloadManager.Request zipRequest = new JDownloadManager.Request(Uri.parse(serPackAttach.zipUrl));
        Uri uri = Uri.withAppendedPath(Uri.fromFile(new File(LocalParams.DOWN_ZIP_PATH)), serPackAttach.name);

        /** 下载包描述 */
        StringBuffer sbDescription = new StringBuffer();
        sbDescription.append(serPackAttach.version + LocalParams.SPLIT_TAG);
        sbDescription.append(serPackAttach.name + LocalParams.SPLIT_TAG);
        sbDescription.append(serPackAttach.md5 + LocalParams.SPLIT_TAG);
        sbDescription.append(serPackAttach.path + LocalParams.SPLIT_TAG);
        sbDescription.append(serPackAttach.zipFile.getAbsolutePath());

        /** 下载任务标题，做为接收完成广播的标识 */
        zipRequest.setTitle(LocalParams.DOWN_ZIP_TITLE);
        zipRequest.setDescription(sbDescription.toString());
        zipRequest.setDestinationUri(uri);
        long id = mJDownloadManager.addQueue(zipRequest);
        DvbSettings.System.putLong(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_ZIP_DOWN_ID, id);
    }

    public void exit() {
        JHttpHelper.cancelJsonRequests(mCtx, true);
        if (mLocalPackReceiver != null) {
            mCtx.unregisterReceiver(mLocalPackReceiver);
            mLocalPackReceiver = null;
        }
    }

    /**
     * 获取下载任务状态 , 若true不做任务操作; false则按流程来检测
     * 
     * @return 下载任务是否在运行
     */
    private boolean getDownTaskLock() {
        boolean lockStatus = true;
        long downId = DvbSettings.System.getLong(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_ZIP_DOWN_ID, -1);

        if (downId != -1) {
            Cursor c = null;
            try {
                c = mJDownloadManager.queryTask(downId);
                if (c != null && c.moveToFirst()) {
                    int status = c.getInt(JDownloadTaskColumnIndex.STATUS);
                    /*lockStatus = !JDownloads.Impl.isStatusCompleted(status);
                    if (!lockStatus) {
                        *//** 当前下载任务已结束，设任务ID为-1 *//*
                        DvbSettings.System.putLong(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_ZIP_DOWN_ID, -1);
                    }

                    *//** 下载成功，更新本地配置信息 *//*
                    if (status == Impl.STATUS_SUCCESS) {
                        String packDescription = c.getString(c.getColumnIndex(JDownloadManager.COLUMN_DESCRIPTION));
                        DvbSettings.System.putString(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_INFO, packDescription);
                    }*/
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        } else {
            lockStatus = false;
        }
        JLog.d(TAG, "--------getDownTaskLock = " + lockStatus);
        return lockStatus;
    }

    /**
     * 获取已安装应用版本信息
     * 
     * @param pkg
     * @return versionCode
     */
    private int getLocalAPkVersion(String pkg, PackageManager pm) {
        int ret = -1;
        PackageInfo localApkInfo = null;
        try {
            localApkInfo = pm.getPackageInfo(pkg, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (localApkInfo != null) {
                ret = localApkInfo.versionCode;
            }
        }
        return ret;
    }

    private String getMacAddress() {
        if (mMacAddress == null && "".equals(mMacAddress)) {
            mMacAddress = JMachineTag.getMACAddress();
        }
        return mMacAddress;
    }

    private void handlerMessage(Message msg) {
        switch (msg.what) {
            case MSG_GET_SER_PACK_TRUE:
                JLog.d(TAG, "MSG_GET_SER_PACK_TRUE");
                String[] value = {
                        "1", LocalParams.DOWN_ZIP_FILE_NAME, "abcdefghijklmnopqistuvwsyz", LocalParams.DOWN_ZIP_PATH,
                        LocalParams.DOWN_ZIP_URL
                };
                PackAttach attach = new PackAttach(value);
                downZip(attach);
                break;
            case MSG_GET_SER_PACK_FALSE:
                JLog.d(TAG, "MSG_GET_SER_PACK_FALSE");
                String[] value1 = {
                        "1", LocalParams.DOWN_ZIP_FILE_NAME, "abcdefghijklmnopqistuvwsyz", LocalParams.DOWN_ZIP_PATH,
                        LocalParams.DOWN_ZIP_URL
                };
                PackAttach attach1 = new PackAttach(value1);
                downZip(attach1);
                break;
        }
    }

    private Message obtainMessage(int msgId, Object msgData) {
        Message msg;
        if (mWorkHandler != null) {
            msg = mWorkHandler.obtainMessage(msgId, msgData);
        } else {
            msg = Message.obtain();
            msg.what = msgId;
            msg.obj = msgData;
        }
        if (msg == null) {
            msg = new Message();
            msg.what = msgId;
            msg.obj = msgData;
        }
        return msg;
    }

    /**
     * @param packAttach
     */
    private void parserZip(final PackAttach packAttach) {
        JLog.d(TAG, "---parserZip : " + packAttach.zipFile.getAbsolutePath());
        if (JMD5.check(packAttach.zipFile, packAttach.md5)) {
            boolean ret = false;
            try {
                ret = JZip.zipFile(packAttach.zipFile);
            } catch (ZipException e) {
                e.printStackTrace();
                ret = false;
            }
            if (ret) {
                File dir = new File(packAttach.path);
                String[] nameList = dir.list();
                ArrayList<String> pathList = new ArrayList<String>();
                for (String name : nameList) {
                    if (name.endsWith(".apk")) {
                        JLog.d(TAG, "------parser ret apk : " + name + " ------");
                        pathList.add(packAttach.path + File.separator + name);
                    }
                }
                if (pathList.size() > 0) {
                    startInstall(pathList);
                }
            } else {
                /** 解压失败 */
                if (packAttach.zipFile.exists()) {
                    packAttach.zipFile.delete();
                }
                DvbSettings.System.putString(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_INFO, "");
            }
        } else {
            // 验证失败
            if (packAttach.zipFile.exists()) {
                packAttach.zipFile.delete();
            }
            DvbSettings.System.putString(mCtx.getContentResolver(), DvbSettings.System.LOCAL_PACK_INFO, "");
        }

        mLock.notifyAll();
    }

    private void startInstall(List<String> apkPath) {

        PackageManager pm = mCtx.getPackageManager();
        for (String path : apkPath) {
            boolean tag = false;
            PackageInfo info = null;
            try {
                info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
            } finally {
                if (info != null) {
                    String pkg = info.packageName;
                    int localApkVersionCode = info.versionCode;
                    int installedVersionCode = getLocalAPkVersion(pkg, pm);
                    if (localApkVersionCode > installedVersionCode) {
                        tag = true;
                    }
                }

                if (tag) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
                    mCtx.startActivity(intent);
                } else {
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }

    }

    private void startLocalPackCheck(PackAttach attach) {
        // if(mZipThread!=null && mZipThread.isAlive() &&
        // mZipThread.getState()!=Thread.State.TERMINATED){
        // mZipThread.stop();
        // mZipThread = null;
        // }
        new AsyncLocalPackCheck(attach).start();
    }

    /**
     * @param nameSet
     * @return
     */
    @SuppressWarnings("unused")
    private String[] substringAppName(String nameSet) {
        if (nameSet == null || "".equals(nameSet)) {
            return null;
        }
        return nameSet.split(LocalParams.SPLIT_TAG);
    }

    /**
     * @param infoSet : version name md5 path (不包括 zipUrl，因字符串里可能会包含分隔符'&')
     * @return PackAttach
     */
    private PackAttach substringPackAttach(String infoSet) {
        if (infoSet == null || "".equals(infoSet)) {
            return null;
        }
        return new PackAttach(infoSet.split(LocalParams.SPLIT_TAG));
    }

}
