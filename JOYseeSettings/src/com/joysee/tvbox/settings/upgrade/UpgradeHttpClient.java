/**
 * =====================================================================
 *
 * @file   UpgradeHttpClient.java
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import com.joysee.tvbox.settings.upgrade.entity.ErrorCode;
import com.joysee.tvbox.settings.upgrade.entity.Firmware;
import com.joysee.tvbox.settings.upgrade.entity.Patch;
import com.joysee.tvbox.settings.upgrade.entity.Patchs;
import com.joysee.tvbox.settings.upgrade.entity.UpdateData;
import com.joysee.tvbox.settings.upgrade.entity.Version;
import com.joysee.tvbox.settings.upgrade.entity.Versions;
import com.joysee.tvbox.settings.util.FileUtils;
import com.joysee.tvbox.settings.util.MD5;
import com.joysee.tvbox.settings.util.PullParserXml;

public class UpgradeHttpClient {

    public static final String TAG = UpgradeHttpClient.class.getSimpleName();
    public static final String URL_NULL = "";
    public static final String URL_PARAMETER = "?Action=Android&DeviceId=";
    public static final String URL_VERSION = "&Version=";

    private String mAttr;

    private DownloadProgressListener mListener;

    private DownloadProgressListener mCheckListener;

    private Context mContext;

    private FileUtils mFileUtils;

    private Thread mDownloadThread;

    public UpgradeHttpClient(Context context) {
        super();
        this.mContext = context;
        mFileUtils = new FileUtils();
    }

    public void setDownloadListener(DownloadProgressListener listener) {
        mListener = listener;
    }

    /**
     * 在线更新检查
     */
    private boolean checkVersionOnline() {
        String str;
        String serial_number;
        String url;
        Object obj = null;
        Versions versions;
        mCheckListener.onStart();
        if (!isNetwordConnected()) {
            mCheckListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
            return false;
        }

        url = getCheckVersionUrl();
        obj = getServerData(url);

        if (obj == null || obj instanceof Error || !(obj instanceof Versions)) {
            mCheckListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
            return false;
        }

        versions = (Versions) obj;
        if (!checkVersion(versions)) {
            mCheckListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
            return false;
        }

        int filesizeInByte = getDownloadSizeByURL(UpdateData.URL);
        int filesize = filesizeInByte / 1024;
        Log.d(TAG, new UpdateData().toString());

        if (filesize <= 0) {
            mCheckListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
            return false;
        }
        if ((UpdateData.SizeInKb != filesize) && (filesize > 0)) {
            UpdateData.SizeInKb = filesize;
        }

        if (!checkMemory(UpdateData.SizeInKb, FileUtils.DATA_PARH)) {
            try {
                Runtime.getRuntime().exec(" busybox rm cache/ -rf ");
            } catch (Exception e) {
                e.printStackTrace();
                mCheckListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
                return false;
            }

            if (!checkMemory(UpdateData.SizeInKb, FileUtils.DATA_PARH)) {
                mCheckListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
                return false;
            }

        }
        mCheckListener.onFinish(true);
        return true;
    }

    /**
     * 本地更新检查
     */
    public boolean checkLocalUpdate_xml(int type, String path) {
        Log.d(TAG, " checkLocalUpdate_xml type = " + type + " path = " + path);
        String str;
        String zipPath;
        File file;
        File xmlFile = null;
        Object obj = null;
        InputStream inputStream = null;
        UpdateData.clearUpdateData();
        // sdcard升级
        if (type == 0) {
            // 检查sdcard
            Log.d(TAG, " Environment.getExternalStorage2State() = " + Environment.getExternalStorage2State() + " getExternalStorage2Directory()  = " + Environment.getExternalStorage2Directory());
            if (!Environment.getExternalStorage2State().equals(Environment.MEDIA_MOUNTED)) {
                return false;
            }
        }
        xmlFile = new File(path + "update.xml");
        if (!xmlFile.exists()) {
            return false;
        } else {
            try {
                inputStream = new FileInputStream(xmlFile);
                PullParserXml handler = new PullParserXml();
                obj = handler.getServerData(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (obj == null || obj instanceof Error || !(obj instanceof Versions)) {
            return false;
        }

        Versions versions = (Versions) obj;
        if (!checkVersion(versions)) {
            return false;
        }
        zipPath = path + "update.zip";
        file = new File(zipPath);
        if (file.exists()) {
            if (!checkMD5(zipPath, UpdateData.Md5)) {
                return false;
            }
            if (Recovery.verifyPackage(file)) {
                UpdateData.Path = zipPath;
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    /**
     * 获取下载地址
     */
    private String getDownloadUrl() {
        return "";
    }

    /**
     * 获取版本信息地址
     */
    private String getCheckVersionUrl() {
        String url = SystemProperties.get("online.ip", URL_NULL);
        url = url + URL_PARAMETER + getMac() + URL_VERSION + Build.ID;
        return url;
    }

    /**
     * 获取MAC地址
     */
    private String getMac() {
        String mac;
        if (SystemProperties.get("ubootenv.var.ethaddr", "") != null) {
            mac = SystemProperties.get("ubootenv.var.ethaddr", "");
        } else {
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            mac = info.getMacAddress();
        }
        return mac;
    }

    /**
     * 检测网络状态
     */
    private boolean isNetwordConnected() {
        ConnectivityManager conManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        if (networkInfo != null) {
            for (int i = 0; i < networkInfo.length; i++) {
                if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public Object getServerData(String url) {
        InputStream inputStream = null;
        Object obj = null;

        if (url == null) {
            Log.e(TAG, "getServerData is null!");
            return null;
        }

        inputStream = getInputStreamByGet(url);
        if (inputStream == null) {
            Log.e(TAG, "getServerData: can not get inputstream!");
            return null;
        }

        try {
            PullParserXml handler = new PullParserXml();
            obj = handler.getServerData(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorCode(ErrorCode.ERROR_PARSER);
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

    public InputStream getInputStreamByGet(String url) {
        InputStream inputStream = null;
        HttpGet httpRequest = null;

        try {
            httpRequest = new HttpGet(url);
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                inputStream = httpResponse.getEntity().getContent();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    private boolean checkVersion(Versions versions) {
        int ret = -1;
        int i, count;

        List<Version> list = versions.getVersions();
        count = list.size();
        if (count < 1) {
            return false;
        }
        for (i = 0; i < count; i++) {
            Firmware firmware = list.get(i).getFirmware();
            if (firmware == null) {
                continue;
            }
            String id = firmware.getId();
            mAttr = firmware.getAttribute();
            ret = checkFirmwareVersion(firmware.getId());
            if (ret == 1) {
                if (i == (count - 1)) {
                    setUpdataData(firmware);
                    return true;
                }
            } else if ((ret == 0) && (i == (count - 1))) {
                Patchs patchs = list.get(i).getPatchs();
                return checkPatchs(patchs);
            }
        }
        return false;
    }

    public int checkFirmwareVersion(String version) {
        String current_version = Build.ID;
        Log.d(TAG, " checkFirmwareVersion()  current_version = " + current_version + " Server version = " + version);
        if (current_version.equals(version.trim())) {
            return 0;
        } else if (current_version.compareTo(version.trim()) < 0) {
            return 1;
        }
        return -1;
    }

    public boolean checkPatchs(Patchs patchs) {
        int i, count;
        Log.e(TAG, "patchs = " + patchs);
        if (patchs == null) {
            return false;
        }
        List<Patch> list = patchs.getPatchs();
        count = list.size();
        if (count < 1) {
            return false;
        }
        for (i = 0; i < count; i++) {
            Patch patch = list.get(i);
            if (patch == null) {
                continue;
            }
            mAttr = patch.getAttribute();
            if (checkPatchVersion(patch.getId())) {
                if ((mAttr.equals(UpdateStatus.UPDATE_ATTRIBUTE_IMPORTANC)) || (i == (count - 1))) {
                    setUpdataData(patch);
                    return true;
                }
            }
        }
        return false;
    }

    public void setUpdataData(Patch patch) {
        UpdateData.clearUpdateData();
        UpdateData.Version = patch.getVersion();
        String size = patch.getSizeInKB();
        UpdateData.SizeInKb = Long.parseLong(size);
        UpdateData.Id = patch.getId();
        UpdateData.Name = patch.getName();
        UpdateData.URL = patch.getURL();
        UpdateData.Md5 = patch.getMD5();
        UpdateData.attr = patch.getAttribute();
        if (UpdateStatus.DEBUG) {
            Log.d(TAG, "update id = " + patch.getId());
            Log.d(TAG, "update name = " + patch.getName());
            Log.d(TAG, "update version = " + patch.getVersion());
            Log.d(TAG, "update md5 = " + patch.getMD5());
            Log.d(TAG, "update size in kb = " + patch.getSizeInKB());
            Log.d(TAG, "update url = " + patch.getURL());
            Log.d(TAG, "update filesize = " + patch + "KB");
            Log.d(TAG, "update attr = " + patch.getAttribute());
        }
    }

    public void setUpdataData(Firmware firmware) {
        UpdateData.clearUpdateData();
        UpdateData.Version = firmware.getVersion();
        String size = firmware.getSizeInKB();
        UpdateData.SizeInKb = Long.parseLong(size);
        UpdateData.Id = firmware.getId();
        UpdateData.Name = firmware.getName();
        UpdateData.URL = firmware.getURL();
        UpdateData.Md5 = firmware.getMD5();
        UpdateData.attr = firmware.getAttribute();
        if (UpdateStatus.DEBUG) {
            Log.d(TAG, "update id = " + firmware.getId());
            Log.d(TAG, "update name = " + firmware.getName());
            Log.d(TAG, "update version = " + firmware.getVersion());
            Log.d(TAG, "update md5 = " + firmware.getMD5());
            Log.d(TAG, "update size in kb = " + firmware.getSizeInKB());
            Log.d(TAG, "update url = " + firmware.getURL());
            Log.d(TAG, "update filesize = " + firmware + "KB");
            Log.d(TAG, "update attr = " + firmware.getAttribute());
        }
    }

    public boolean checkPatchVersion(String id) {
        String filename = Patch.PATCH_PATH + id + ".txt";
        Log.d(TAG, "Patch file = " + filename);
        File file = new File(filename);
        if (file.exists()) {
            return false;
        }
        return true;
    }

    public int getDownloadSizeByURL(String url) {
        int fileSize = 0;
        if (null == url || "".equals(url)) {
            return 0;
        }
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpRequest = new HttpGet(url);
            HttpResponse httpResponse = httpclient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                fileSize = (int) httpResponse.getEntity().getContentLength();
                if (UpdateStatus.DEBUG)
                    Log.d(TAG, "get size:" + fileSize);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileSize;
    }

    public boolean checkMemory(Long size, String path) {
        String str;
        long free_size = 0;
        free_size = mFileUtils.getFreeSpaceInKB(path);
        Log.d(TAG, " checkMemory path = " + path + " size = " + size + " free_size = " + free_size);
        if (free_size > size) {
            UpdateData.Path = path;
        } else {
            return false;
        }
        return true;
    }

    public boolean checkMD5(String file, String md5) {
        MD5 mMD5 = new MD5();
        String md5_str = mMD5.md5sum(file);
        if (md5_str == null) {
            Log.e(TAG, "get download file md5 string error!");
            return false;
        } else {
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "file md5:" + md5_str + " and url md5:" + md5);
            if (md5_str.equalsIgnoreCase(md5)) {
                return true;
            } else {
                Log.e(TAG, "the download file md5 string is not equal string from network!");
                return false;
            }
        }
    }

    public void startCheck(DownloadProgressListener listener) {
        this.mCheckListener = listener;
        checkVersionOnline();
    }

    public void startDownload(DownloadProgressListener listener) {
        this.mListener = listener;
        mDownloadThread = new Thread(mDownloadRunnable);
        mDownloadThread.start();

    }

    public void cacelDownload() {

        mDownloadThread = null;
    }

    private Runnable mDownloadRunnable = new Runnable() {

        public void run() {
            boolean mConnectStatus = isNetwordConnected();
            boolean mStop = false;

            long mCurSize = 0;
            final int mConnectTimeOut = 100000;
            final int mReadTimeOut = 600000;

            http_request_loop: while (mConnectStatus) {
                File file;
                FileOutputStream fileOutputStream = null;
                InputStream inputStream = null;
                BufferedInputStream bufferedInputStream = null;
                BufferedOutputStream bufferedOutputStream = null;
                String str = "";
                try {
                    Thread.sleep(1000);
                    File updateFile = new File(UpdateData.Path + UpdateData.Name);
                    // 如果升级文件已经存在
                    if (updateFile.exists()) {
                        Log.d(TAG, " mDownloadThread updateFile.exists() " + UpdateData.Path + " mFileName = " + UpdateData.Name);
                        FileInputStream fis = new FileInputStream(updateFile);
                        mCurSize = fis.available();
                        if ((mCurSize / 1024) == UpdateData.SizeInKb) {
                            UpdateStatus.clearUpdateStatus();
                            if (checkMD5(UpdateData.Path + UpdateData.Name, UpdateData.Md5)) {
                                UpdateStatus.clearUpdateStatus();
                                if (mListener != null) {
                                    mListener.onProgressChange(100);
                                    mListener.onFinish(true);
                                }
                                mStop = true;
                            } else {
                                updateFile.delete();
                                break;
                            }
                        }
                    }
                    // Log.d(TAG, " mDownloadThread mPath = " + mPath +
                    // " mFileName = " + mFileName + " filename = " + filename);
                    // Log.d(TAG, " mDownloadThread mCurSize=" + mCurSize +
                    // " mCurSize/1024=" + mCurSize / 1024 +
                    // " UpdateData.SizeInKb= " +
                    // UpdateData.SizeInKb);
                    String filename = getFileName(UpdateData.Path, UpdateData.Name);
                    file = new File(filename);
                    android.os.FileUtils.setPermissions(filename, 0666, -1, -1);
                    if (file.exists()) {
                        FileInputStream fis = new FileInputStream(file);
                        mCurSize = fis.available();
                    } else {
                        file.createNewFile();
                        mCurSize = 0;
                    }
                    if ((mCurSize / 1024) == UpdateData.SizeInKb) {
                        if (!checkMD5(filename, UpdateData.Md5)) {
                            file.delete();
                            mCurSize = 0;
                            continue http_request_loop;
                        }
                    } else if ((mCurSize / 1024) > UpdateData.SizeInKb) {
                        file.delete();
                        mCurSize = 0;
                        continue http_request_loop;
                    } else {
                        URL url = new URL(UpdateData.URL);
                        Log.d(TAG, " mDownloadThread mUrl = " + UpdateData.URL + " mStop = " + mStop);
                        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                        // Log.d(TAG, " httpConnection.getContentLength() " +
                        // httpConnection.getContentLength());
                        httpConnection.setRequestProperty("User-Agent", "Android");
                        httpConnection.setConnectTimeout(mConnectTimeOut);
                        // 下载包的时候，将sockettimeout时间设长，防止一段时间下载失败。
                        httpConnection.setReadTimeout(mReadTimeOut);
                        // 下载包的时候，将sockettimeout时间设长，防止一段时间下载失败。
                        String sProperty = "bytes=" + mCurSize + "-";
                        // 因为这个是post请求,设立需要设置为true
                        httpConnection.setDoOutput(true);
                        httpConnection.setDoInput(true);
                        // 设置以POST方式
                        httpConnection.setRequestMethod("POST");
                        // Post 请求不能使用缓存
                        httpConnection.setUseCaches(false);
                        httpConnection.setInstanceFollowRedirects(true);
                        httpConnection.setRequestProperty("RANGE", sProperty);
                        httpConnection.connect();
                        inputStream = httpConnection.getInputStream();
                        bufferedInputStream = new BufferedInputStream(inputStream);
                        fileOutputStream = new FileOutputStream(file, true);
                        bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        byte[] buf = new byte[1024 * 8];
                        if (bufferedInputStream != null) {
                            int ch = -1;
                            while (((ch = bufferedInputStream.read(buf)) != -1) && mConnectStatus && !mStop) {
                                if (mDownloadThread == null) {
                                    break http_request_loop;
                                }
                                bufferedOutputStream.write(buf, 0, ch);
                                bufferedOutputStream.flush();
                                mCurSize += ch;
                                if (mListener != null) {
                                    int progress = getProgress(mCurSize / 1024, UpdateData.SizeInKb);
                                    if (progress == 100) {
                                        progress = 99;
                                    }
                                    mListener.onProgressChange(progress);
                                }
                            }
                        }
                    }
                    bufferedOutputStream.close();
                    fileOutputStream.close();
                    bufferedInputStream.close();
                    inputStream.close();
                    Log.d(TAG, "download firmware ok!");
                    if (mStop) {
                        break;
                    }
                    Log.d(TAG, "checking firmware!");
                    if (!checkMD5(filename, UpdateData.Md5)) {
                        file.delete();
                        break;
                    }
                    file.renameTo(new File(UpdateData.Path + UpdateData.Name));
                    boolean isDelete = file.delete();
                    Log.d(TAG, "check firmware ok! file.delete() = " + isDelete);

                    mStop = true;
                } catch (InterruptedException e) {
                    Log.e(TAG, " InterruptedException ");
                    e.printStackTrace();
                    mListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
                    break;
                } catch (FileNotFoundException e) {
                    Log.e(TAG, " FileNotFoundException ");
                    e.printStackTrace();
                    mListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    mConnectStatus = isNetwordConnected();
                    Log.e(TAG, " SocketTimeoutException mConnectStatus = " + mConnectStatus);
                    UpdateStatus.clearUpdateStatus();
                    mListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
                } catch (Exception e) {
                    e.printStackTrace();
                    mConnectStatus = isNetwordConnected();
                    Log.e(TAG, " mConnectStatus = " + mConnectStatus);
                    UpdateStatus.clearUpdateStatus();
                    mListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
                } finally {
                    Log.e(TAG, " finally ");
                    mStop = true;
                    mConnectStatus = false;
                    if (null != bufferedOutputStream) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "close bufferedOutputStream fail!");
                        }
                    }
                    if (null != fileOutputStream) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "close fileoutputstream fail!");
                        }
                    }
                    if (null != bufferedInputStream) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "close bufferedInputStream fail!");
                        }
                    }
                    if (null != inputStream) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "close inputStream fail!");
                        }
                    }
                    try {
                        this.finalize();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        mListener.onError(DownloadProgressListener.ERROR_CODE_UNKNOW);
                    }
                }
            }
        }
    };

    // private Thread mDownloadThread = new Thread() {
    // };

    private String getFileName(String path, String name) {
        int end = name.lastIndexOf(".");
        String str;
        if (end <= 0) {
            str = path + name + ".tmp";
        } else {
            str = path + name.substring(0, end) + ".tmp";
        }
        Log.d(TAG, "getFileName name = " + str);
        return str;
    }

    private int getProgress(long cur_size, long total_size) {
        return (int) ((cur_size * 100) / total_size);
    }
}
