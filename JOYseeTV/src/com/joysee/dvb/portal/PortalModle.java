/**
 * =====================================================================
 *
 * @file  PortalModle.java
 * @Module Name   com.joysee.dvb.portal
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月18日
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
 * wuhao         2014年2月18日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.data.JHttpHelper;
import com.joysee.common.data.JHttpParserCallBack;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.data.DvbSettings;
import com.joysee.dvb.data.LocalInfoReader;
import com.joysee.dvb.parser.ProgramParser;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.portal.appcache.AllAppsList;
import com.joysee.dvb.portal.appcache.ApplicationInfo;
import com.joysee.dvb.portal.appcache.IconCache;
import com.joysee.dvb.update.UpdateClient;
import com.xiaomi.market.sdk.XiaomiUpdateAgent;

import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PortalModle extends BroadcastReceiver {
    public interface PortalCallbacks {
        public void bindDataToItem();

        public void onGetAppVersion(UpdateClient client);

        public void onGetRecommand(ArrayList<Program> programs);

        /**
         * @param true: mounted false:unmount
         */
        public void onUsbDongleStateChange(boolean mounted);

        void onDonglePermChanged(boolean gain);
    }

    public interface AppListListener {
        public void onAllAppsGot(ArrayList<ApplicationInfo> apps);

        public void onAppsAdded(ArrayList<ApplicationInfo> apps);

        public void onAppsRemoved(ArrayList<ApplicationInfo> apps);

        public boolean isLoadOnResume();
    }

    public static String TAG = JLog.makeTag(PortalModle.class);

    /**
     * get USB Dongle mounted state
     * 
     * @param context
     * @return return true if mounted
     */
    public static boolean getUsbDongleState(Context context) {
        if (TvApplication.sDestPlatform == TvApplication.DestPlatform.MStar) {
            return true;
        }
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, " device = " + device.toString());
            }
            if (device.getProductId() == PRODUCT_ID && device.getVendorId() == VENDOR_ID) {
                return true;
            }
        }
        return false;
    }

    private TvApplication mTvApplication;
    private final Object mLock = new Object();
    private WeakReference<PortalCallbacks> mCallbacks;
    private AppListListener mAppListListener;
    public static String ACTION_GET_RECOMMEND = "com.joysee.dvb.portal.ACTION_GET_RECOMMEND";
    
    private int mBatchSize = 0;
    private IconCache mIconCache;
    private LoaderTask mLoaderTask;
    private AllAppsList mAllAppsList;
    private static final Collator sCollator = Collator.getInstance();
    
    public static final int PRODUCT_ID = 8194;
    public static final int VENDOR_ID = 1297;
    private static final HandlerThread sWorkerThread = new HandlerThread("portal-loader");

    static {
        sWorkerThread.start();
    }

    private Handler mUIHandler = new Handler();
    private Handler mWorkHandler = new Handler(sWorkerThread.getLooper());

    public PortalModle(TvApplication application) {
        mTvApplication = application;
        mIconCache = new IconCache(application);
        mAllAppsList = new AllAppsList(mIconCache);
    }

    public ArrayList<Program> fillOutValidPrograms(ArrayList<Program> programs) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<Program> validPrograms = null;
        if (programs != null && programs.size() > 0) {
            ArrayList<DvbService> channels = AbsDvbPlayer.getAllChannel();
            if (channels != null && channels.size() > 0) {
                HashMap<String, DvbService> channelnames = new HashMap<String, DvbService>();
                for (DvbService channel : channels) {
                    channelnames.put(channel.getChannelName(), channel);
                }
                validPrograms = new ArrayList<Program>();
                DvbService c = null;
                for (Program p : programs) {
                    c = channelnames.get(p.channelName);
                    if (c != null) {
                        p.logicNumber = c.getLogicChNumber();
                        p.serviceId = c.getServiceId();
                        validPrograms.add(p);
                    } else {
                        JLog.d(TAG, "drop program :  " + p.programName + "-" + p.channelName);
                    }
                }
            }
        }
        JLog.methodEnd(TAG, begin);
        return validPrograms;
    }

    public void getAppVersionOnServerSync(Context context) {
        if (TvApplication.sDestPlatform == TvApplication.DestPlatform.MITV_QCOM ||
                TvApplication.sDestPlatform == TvApplication.DestPlatform.MITV_2) {
            XiaomiUpdateAgent.update(context);
        } else {
            UpdateClient client = new UpdateClient(context) {
                @Override
                public void handlerCheckState(int status, Object attach) {
                    if (status == UpdateClient.STATUS_CHECK_AN_NEW_VERSION) {
                        mCallbacks.get().onGetAppVersion(this);
                    }
                }
            };
            client.checkAsync();
        }
    }

    public void getRecommands() {
        long current = System.currentTimeMillis();
        int localOperatorCode = LocalInfoReader.getOperatorCode(mTvApplication);
        String url = Constants.getRecommendURL(100003, 16, current, current, localOperatorCode);
        JHttpHelper.getJson(url, new JHttpParserCallBack(new ProgramParser()) {
            @Override
            public void onFailure(int arg0, Throwable arg1) {
                JLog.d(TAG, " getRecommands onFailure arg0 = " + arg0 + " arg1 = " + arg1.getMessage());
            }

            @Override
            public void onSuccess(Object arg0) {
                JLog.d(TAG, " getRecommands onSuccess arg0 = " + arg0);
                if (arg0 != null && arg0 instanceof ArrayList) {
                    final ArrayList<Program> programs = fillOutValidPrograms((ArrayList<Program>) arg0);
                    if (programs.size() > 0) {
                        JLog.d(TAG, arg0.toString());
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCallbacks.get().onGetRecommand(programs);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(PortalCallbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<PortalCallbacks>(callbacks);
        }
    }

    public boolean isHasLocalOperator() {
        return DvbSettings.System.getBoolean(mTvApplication.getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_SET_SUCCESSFULLY);
    }

    public int getDongleFileDescriptor(int productId, int vendorId) {
        int desc = -1;
        if (checkDonglePermission(productId, vendorId)) {
            UsbManager usbManager = (UsbManager) mTvApplication.getSystemService(Context.USB_SERVICE);
            UsbDevice device = getDongleDevice(productId, vendorId);
            UsbDeviceConnection conn = usbManager.openDevice(device);
            if (conn != null) {
                UsbInterface tInterface = device.getInterface(0);
                conn.claimInterface(tInterface, true);
                desc = conn.getFileDescriptor();
            }
        }

        return desc;
    }

    public boolean checkDonglePermission(int productId, int vendorId) {
        boolean permission = false;
        UsbManager usbManager = (UsbManager) mTvApplication.getSystemService(Context.USB_SERVICE);
        UsbDevice device = getDongleDevice(productId, vendorId);
        if (device != null) {
            permission = usbManager.hasPermission(device);
        }
        return permission;
    }

    private UsbDevice getDongleDevice(int productId, int vendorId) {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) mTvApplication.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devList = usbManager.getDeviceList();
        Iterator<UsbDevice> devIter = devList.values().iterator();
        while (devIter.hasNext()) {
            device = devIter.next();
            if (device.getProductId() == productId && device.getVendorId() == vendorId) {
                return device;
            }
        }
        return null;
    }

    public void requestDonglePermission(int productId, int vendorId) {
        JLog.d(TAG, "requestDonglePermission productId = " + productId + ", vendorId = " + vendorId);
        UsbManager usbManager = (UsbManager) mTvApplication.getSystemService(Context.USB_SERVICE);
        UsbDevice device = getDongleDevice(productId, vendorId);
        if (device != null) {
            PendingIntent intent = PendingIntent.getBroadcast(mTvApplication, 0, new Intent(TvApplication.ACTION_DONGLE_PERM_CHANGED), 0);
            usbManager.requestPermission(device, intent);
        }
    }

    PortalCallbacks tryGetCallbacks(PortalCallbacks oldCallbacks) {
        synchronized (mLock) {
            if (mCallbacks == null) {
                return null;
            }

            final PortalCallbacks callbacks = mCallbacks.get();
            if (callbacks != oldCallbacks) {
                return null;
            }
            if (callbacks == null) {
                Log.w(TAG, "no mCallbacks");
                return null;
            }

            return callbacks;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        JLog.d(TAG, " onReceive " + action);
        if (ACTION_GET_RECOMMEND.equals(action)) {
            getRecommands();
        } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action) || UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, " device = " + device.toString());
                }
                if (device.getProductId() == PRODUCT_ID && device.getVendorId() == VENDOR_ID) {
                    if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                        onUsbDongleStateChange(true);
                    } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                        onUsbDongleStateChange(false);
                    }
                }
            }
        } else if (TvApplication.ACTION_DONGLE_PERM_CHANGED.equals(action)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
            onDonglePermChanged(granted);
        } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {

            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            int op = PackageUpdatedTask.OP_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will
                // be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_ADD;
                } else {
                    op = PackageUpdatedTask.OP_UPDATE;
                }
            }
            if (op != PackageUpdatedTask.OP_NONE) {
                mWorkHandler.post(new PackageUpdatedTask(op, new String[] {
                        packageName
                }));
            }
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            // If we have changed locale we need to clear out the labels in all
            // apps/workspace
            forceReload();
        }
    }

    private void onDonglePermChanged(boolean gain) {
        JLog.d(TAG, "onDonglePermChanged gain = " + gain);
        final PortalCallbacks oldCallback = mCallbacks.get();
        PortalCallbacks callback = tryGetCallbacks(oldCallback);
        if (callback != null) {
            callback.onDonglePermChanged(gain);
        }
    }

    private void onUsbDongleStateChange(boolean mounted) {
        mCallbacks.get().onUsbDongleStateChange(mounted);
    }

    public void registerAppListListener(AppListListener l) {
        this.mAppListListener = l;
    }

    public void unRegisterAppListListener() {
        this.mAppListListener = null;
    }

    public void startLoader(Context context, boolean isLaunching) {
        synchronized (mLock) {
            if (mAppListListener != null) {
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(context, isLaunching);
                sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                mWorkHandler.post(mLoaderTask);
            }
        }
    }

    private void forceReload() {
        synchronized (mLock) {
            stopLoaderLocked();
        }
        startLoaderFromBackground();
    }

    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mAppListListener != null) {
            if (mAppListListener.isLoadOnResume()) {
                runLoader = true;
            }
        }
        if (runLoader) {
            startLoader(mTvApplication, false);
        }
    }

    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    private class LoaderTask implements Runnable {

        private Context mContext;
        private boolean mStopped;
        private boolean mIsLaunching;
        private HashMap<Object, CharSequence> mLabelCache;
        private String[] mSpecialApps = {
                "com.joysee.dvb"
        };

        public LoaderTask(Context context, boolean isLaunching) {
            this.mContext = context;
            this.mIsLaunching = isLaunching;
            this.mLabelCache = new HashMap<Object, CharSequence>();
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        @Override
        public void run() {
            
            keep_running: {
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(mIsLaunching ? Process.THREAD_PRIORITY_DEFAULT
                            : Process.THREAD_PRIORITY_BACKGROUND);
                }
                if (mStopped) {
                    break keep_running;
                }
                loadAllAppsByBatch();
            }
        
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        private void loadAllAppsByBatch() {
            final long t = SystemClock.uptimeMillis();
            final PackageManager packageManager = mContext.getPackageManager();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> apps = null;
            int N = Integer.MAX_VALUE;
            int startIndex;
            int i = 0;
            int batchSize = -1;
            while (i < N && !mStopped) {
                if (i == 0) {
                    mAllAppsList.clear();
                    final long qiaTime = SystemClock.uptimeMillis();
                    apps = packageManager.queryIntentActivities(mainIntent, 0);
                    JLog.d(TAG, "queryIntentActivities took " + (SystemClock.uptimeMillis() - qiaTime) + "ms");
                    if (apps == null) {
                        return;
                    }
                    filterOutSpecialApp(apps);
                    N = apps.size();
                    JLog.d(TAG, "queryIntentActivities got " + N + " apps");
                    if (N == 0) {
                        return;
                    }
                    if (mBatchSize == 0) {
                        batchSize = N;
                    } else {
                        batchSize = mBatchSize;
                    }

                    final long sortTime = SystemClock.uptimeMillis();
                    Collections.sort(apps, new ShortcutNameComparator(packageManager, mLabelCache));
                    JLog.d(TAG, "sort took " + (SystemClock.uptimeMillis() - sortTime) + "ms");
                }

                final long t2 = SystemClock.uptimeMillis();

                startIndex = i;
                for (int j = 0; i < N && j < batchSize; j++) {
                    mAllAppsList.add(new ApplicationInfo(packageManager, apps.get(i), mIconCache, mLabelCache));
                    i++;
                }

                final ArrayList<ApplicationInfo> added = mAllAppsList.added;
                mAllAppsList.added = new ArrayList<ApplicationInfo>();

                if (mAppListListener != null) {
                    mAppListListener.onAllAppsGot(added);
                }

                JLog.d(TAG, "bound " + added.size() + " apps in " + (SystemClock.uptimeMillis() - t) + "ms");
                JLog.d(TAG, "batch of " + (i - startIndex) + " icons processed in " + (SystemClock.uptimeMillis() - t2) + "ms");
                for (int j = 0; j < added.size(); j++) {
                    JLog.d(TAG, " app---" + added.get(j).title);
                }
            }
        }

        private void filterOutSpecialApp(List<ResolveInfo> apps) {
            JLog.d(TAG, "filterOutSpecialApp begin...");
            ArrayList<ResolveInfo> removed = new ArrayList<ResolveInfo>();
            for (ResolveInfo info : apps) {
                final String pkgName = info.activityInfo.applicationInfo.packageName;
                for (String s : mSpecialApps) {
                    if (s.equals(pkgName)) {
                        removed.add(info);
                    }
                }
            }
            for (ResolveInfo info : removed) {
                apps.remove(info);
            }
        }
    }

    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted

        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        @Override
        public void run() {
            final Context context = mTvApplication;
            final String[] packages = mPackages;
            final int N = packages.length;
            switch (mOp) {
                case OP_ADD:
                    for (int i = 0; i < N; i++) {
                        mAllAppsList.addPackage(context, packages[i]);
                    }
                    break;
                case OP_UPDATE:
                    for (int i = 0; i < N; i++) {
                        mAllAppsList.updatePackage(context, packages[i]);
                    }
                    break;
                case OP_REMOVE:
                case OP_UNAVAILABLE:
                    for (int i = 0; i < N; i++) {
                        mAllAppsList.removePackage(packages[i]);
                    }
                    break;
            }

            ArrayList<ApplicationInfo> added = null;
            ArrayList<ApplicationInfo> removed = null;
            ArrayList<ApplicationInfo> modified = null;

            if (mAllAppsList.added.size() > 0) {
                added = mAllAppsList.added;
                mAllAppsList.added = new ArrayList<ApplicationInfo>();
            }
            if (mAllAppsList.removed.size() > 0) {
                removed = mAllAppsList.removed;
                mAllAppsList.removed = new ArrayList<ApplicationInfo>();
                for (ApplicationInfo info : removed) {
                    mIconCache.remove(info.intent.getComponent());
                }
            }
            if (mAllAppsList.modified.size() > 0) {
                modified = mAllAppsList.modified;
                mAllAppsList.modified = new ArrayList<ApplicationInfo>();
            }

            if (mAppListListener == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }

            if (added != null) {
                final ArrayList<ApplicationInfo> addedFinal = added;
                mAppListListener.onAppsAdded(addedFinal);
            }
            if (removed != null) {
                final ArrayList<ApplicationInfo> removedFinal = removed;
                mAppListListener.onAppsRemoved(removedFinal);
            }
            if (modified != null) {
                JLog.d(TAG, "---------on app update got-------------");
            }
        }
    }

    public static class ShortcutNameComparator implements Comparator<ResolveInfo> {
        private PackageManager mPackageManager;
        private HashMap<Object, CharSequence> mLabelCache;

        ShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, CharSequence>();
        }

        ShortcutNameComparator(PackageManager pm, HashMap<Object, CharSequence> labelCache) {
            mPackageManager = pm;
            mLabelCache = labelCache;
        }

        @Override
        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence labelA, labelB;
            ComponentName keyA = getComponentNameFromResolveInfo(a);
            ComponentName keyB = getComponentNameFromResolveInfo(b);
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA);
            } else {
                labelA = a.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB);
            } else {
                labelB = b.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyB, labelB);
            }
            return sCollator.compare(labelA, labelB);
        }
    };

    public static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }
}
