/**
 * =====================================================================
 *
 * @file   ApplicationDetail.java
 * @Module Name   com.joysee.tvbox.settings.apps
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Jun 12, 2014
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
 * wumingjun         @Jun 12, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.apps;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.apps.ApplicationsState.AppEntry;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.SettingsApplication;
import com.joysee.tvbox.settings.base.StyleDialog;

public class ApplicationDetailActivity extends BaseActivity implements OnClickListener {

    private final static String TAG = ApplicationDetailActivity.class.getSimpleName();

    private Context mContext;

    private AppEntry mEntry;

    private ImageView mIcon;
    private JTextViewWithTTF mName;
    private JTextViewWithTTF mInstallDate;

    private JButtonWithTTF mButtonUninstall;
    private JButtonWithTTF mClearData;
    private JButtonWithTTF mClearCache;

    private JTextViewWithTTF mAppSize;
    private JTextViewWithTTF mDataSize;
    private JTextViewWithTTF mCacheSize;

    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;
    private ClearCacheObserver mClearCacheObserver;
    private ClearDataObserver mClearDataObserver;
    private PackageDeleteObserver mPackageDeleteObserver;

    private StyleDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mPackageManager = mContext.getPackageManager();
        mClearCacheObserver = new ClearCacheObserver();
        mClearDataObserver = new ClearDataObserver();
        mPackageDeleteObserver = new PackageDeleteObserver();
    }

    @Override
    protected void setupView() {
        mContext = this;
        mEntry = SettingsApplication.mEntry;
        if (mEntry == null) {
            finish();
        }
        setContentView(R.layout.activity_app_detail);

        mIcon = (ImageView) findViewById(R.id.image_app_icon);
        mName = (JTextViewWithTTF) findViewById(R.id.txt_settings_app_name);
        mInstallDate = (JTextViewWithTTF) findViewById(R.id.txt_settings_app_install_date);

        mAppSize = (JTextViewWithTTF) findViewById(R.id.txt_settings_app_size_description);
        mButtonUninstall = (JButtonWithTTF) findViewById(R.id.button_app_uninstall);

        mDataSize = (JTextViewWithTTF) findViewById(R.id.txt_settings_app_data_size_description);
        mClearData = (JButtonWithTTF) findViewById(R.id.button_app_clear_data);

        mCacheSize = (JTextViewWithTTF) findViewById(R.id.txt_settings_app_cache_size_description);
        mClearCache = (JButtonWithTTF) findViewById(R.id.button_app_clear_cache);

        mIcon.setImageDrawable(mEntry.icon);
        mName.setText(mEntry.label);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        Date curDate = new Date(mEntry.time);
        mInstallDate.setText(formatter.format(curDate));

        mAppSize.setText(mEntry.sizeStr);
        mDataSize.setText(getSizeStr(mEntry.dataSize));
        mCacheSize.setText(getSizeStr(mEntry.cacheSize));

        mButtonUninstall.setOnClickListener(this);
        mClearData.setOnClickListener(this);
        mClearCache.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        String packageName = mEntry.info.packageName;
        if (v == mButtonUninstall) {

            showUninstallDialog();

        } else if (v == mClearData) {

            showClearDataDialog();

        } else if (v == mClearCache) {
            mPackageManager.deleteApplicationCacheFiles(packageName, mClearCacheObserver);
        }

    }

    private String getSizeStr(long size) {
        return Formatter.formatFileSize(mContext, size);
    }

    private void showClearDataDialog() {

        if (mEntry != null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.view_dialog_description_view, null);
            JTextViewWithTTF text = (JTextViewWithTTF) view.findViewById(R.id.content_view_message);
            StyleDialog.Builder builder = new StyleDialog.Builder(this);
            builder.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            builder.setTitle(mEntry.label);
            text.setText(R.string.app_manager_clear_data_confirm);
            builder.setPositiveButton(R.string.jstyle_dialog_postive_bt);
            builder.setNegativeButton(R.string.jstyle_dialog_negatvie_bt);
            builder.setOnButtonClickListener(new android.content.DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {

                        boolean flag = mActivityManager.clearApplicationUserData(mEntry.info.packageName, mClearDataObserver);
                        if (!flag) {
                            // 清除失败
                        }

                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                        mDialog.dismissByAnimation();
                    }
                }
            });
            builder.setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {
                        mDialog.dismissByAnimation();
                    }
                    return false;
                }
            });
            builder.setContentCanFocus(true);

            mDialog = builder.show();
        }

    }

    private void showUninstallDialog() {

        if (mEntry != null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.view_dialog_description_view, null);
            JTextViewWithTTF text = (JTextViewWithTTF) view.findViewById(R.id.content_view_message);
            StyleDialog.Builder builder = new StyleDialog.Builder(this);
            builder.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            builder.setTitle(mEntry.label);
            text.setText(R.string.app_manager_uninstall_confirm);
            builder.setPositiveButton(R.string.jstyle_dialog_postive_bt);
            builder.setNegativeButton(R.string.jstyle_dialog_negatvie_bt);
            builder.setOnButtonClickListener(new android.content.DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        mPackageManager.deletePackage(mEntry.info.packageName, mPackageDeleteObserver, 0);
                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                        mDialog.dismissByAnimation();
                    }
                }
            });
            builder.setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {
                        mDialog.dismissByAnimation();
                    }
                    return false;
                }
            });
            builder.setContentCanFocus(true);

            mDialog = builder.show();
        }

    }

    class ClearCacheObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            if (succeeded) {
                mEntry.cacheSize = 0;
                mCacheSize.post(new Runnable() {

                    @Override
                    public void run() {
                        mCacheSize.setText(getSizeStr(mEntry.cacheSize));
                    }
                });
            } else {
                // 清除失败
            }
        }
    }

    class ClearDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            if (succeeded) {
                mEntry.dataSize = 0;
                mDataSize.post(new Runnable() {

                    @Override
                    public void run() {
                        mDialog.dismissByAnimation();
                        mDataSize.setText(getSizeStr(mEntry.dataSize));
                    }
                });
            }
        }
    }

    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        String pkgpath = null;

        public void packageDeleted(String packageName, int returnCode) {
            if (returnCode == PackageManager.DELETE_SUCCEEDED) {
                ApplicationDetailActivity.this.finish();
            } else {
                Log.d("TAG", "packageDeleted error returnCode : " + returnCode);
            }
        }
    }

    @Override
    protected void onDestroy() {
        SettingsApplication.mEntry = null;
        super.onDestroy();
    }

}
