/**
 * =====================================================================
 *
 * @file   SystemUpgradeActivity.java
 * @Module Name   com.joysee.tvbox.settings.upgrade
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 15, 2014
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
 * wumingjun         @Apr 15, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.upgrade;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ProgressWheel;
import com.joysee.tvbox.settings.base.ProgressWheel.ProgressMode;
import com.joysee.tvbox.settings.upgrade.entity.UpdateData;

public class SystemUpgradeActivity extends BaseActivity implements OnClickListener {

    private RelativeLayout mCheckPanel;
    private RelativeLayout mCheckResult;

    private JTextViewWithTTF mJtxtCurrentVersion;
    private ProgressWheel mProgressWheel;
    private JTextViewWithTTF mDescription;
    private JTextViewWithTTF mCheckResultString;
    private JTextViewWithTTF mCheckResultVersion;
    private JButtonWithTTF mButton;

    private Context mContext;
    private boolean isVersionChecked;
    private boolean isDownloading;
    private boolean isDownloadCancelButton;
    private UpgradeManager mUpgradeManager;
    private AnimationSet mShowAnimationSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        mContext = this;
        mUpgradeManager = new UpgradeManager(mContext);

        setContentView(R.layout.activity_system_upgrade);
        mJtxtCurrentVersion = (JTextViewWithTTF) findViewById(R.id.txt_settings_title_description);
        mProgressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        mCheckPanel = (RelativeLayout) findViewById(R.id.content_check_layout);
        mCheckResult = (RelativeLayout) findViewById(R.id.update_check_result);
        mDescription = (JTextViewWithTTF) findViewById(R.id.text_progress_description);
        mButton = (JButtonWithTTF) findViewById(R.id.button_about_system_upgrade);
        mCheckResultString = (JTextViewWithTTF) findViewById(R.id.text_update_check_result_string);
        mCheckResultVersion = (JTextViewWithTTF) findViewById(R.id.text_update_check_result_version);

        String current = getResources().getString(R.string.title_system_upgrade_current_version);
        mJtxtCurrentVersion.setText(current + Build.ID);
        changeViewVisibility();

        mButton.requestFocus();
        mButton.setVisibility(View.INVISIBLE);
        mButton.setOnClickListener(this);

        mShowAnimationSet = buildWheelInAnimator();
        checkVersion(true);

    }

    @Override
    public void onClick(View v) {

        if (isDownloadCancelButton && isDownloading) {
            mButton.setText(R.string.about_system_version_update_continue);
            mUpgradeManager.cancelDownload();
            mDescription.setText(R.string.about_system_version_download_canceled);
            isDownloading = false;
            isDownloadCancelButton = false;

            return;
        }
        if (isVersionChecked && !isDownloading) {
            // 开始更新
            if (!isDownloadCancelButton) {
                isDownloadCancelButton = true;
            }
            mButton.setText(R.string.button_cancel);
            mDescription.setText(R.string.about_system_version_updating);
            mButton.setText(R.string.button_cancel);
            changeViewVisibility();
            mUpgradeManager.setDownloadListener(new DownloadProgressListener() {

                @Override
                public void onStart() {

                }

                @Override
                public void onProgressChange(final int progress) {
                    mProgressWheel.post(new Runnable() {

                        @Override
                        public void run() {
                            mProgressWheel.setProgress(progress);
                        }
                    });
                }

                @Override
                public void onFinish(boolean flag) {
                    mDescription.post(new Runnable() {
                        @Override
                        public void run() {
                            mDescription.setText(R.string.about_system_version_updated);
                            mButton.setText(R.string.button_done);
                            isDownloadCancelButton = false;
                            //Recovery.reboot(mContext, UpdateData.Path + UpdateData.Name);
                        }
                    });
                }

                @Override
                public void onError(int code) {
                    mDescription.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressWheel.stop();
                            mDescription.setText(R.string.about_system_version_can_download_fail);
                            mButton.setText(R.string.button_retry);
                            isDownloading = false;
                            isDownloadCancelButton = false;
                        }
                    });

                }
            });
            mProgressWheel.stop();
            mProgressWheel.setMode(ProgressMode.PERCENTAGE);
            mProgressWheel.start();
            mProgressWheel.setProgress(0);
            mProgressWheel.startAnimation(mShowAnimationSet);
            isDownloading = true;
            mUpgradeManager.startDownload();
        } else if (!isVersionChecked) {
            checkVersion(false);
        }
        changeViewVisibility();
    }

    private void checkVersion(boolean delay) {
        if (!isVersionChecked) {
            // 检测更新
            mDescription.setVisibility(View.VISIBLE);
            mDescription.setText(R.string.about_system_version_checking);
            mButton.setText(R.string.button_cancel);
            mUpgradeManager.setCheckListener(new DownloadProgressListener() {

                @Override
                public void onStart() {

                }

                @Override
                public void onProgressChange(int progress) {

                }

                @Override
                public void onFinish(final boolean flag) {
                    isVersionChecked = true;
                    mDescription.post(new Runnable() {
                        @Override
                        public void run() {

                            mProgressWheel.stop();
                            if (flag) {
                                mCheckResultString.setText(R.string.about_system_version_new);
                                mCheckResultVersion.setText(UpdateData.Version);
                                mButton.setVisibility(View.VISIBLE);
                                mButton.setText(R.string.about_system_version_update_now);
                            } else {
                                mCheckResultString.setText(R.string.about_system_version_good);
                                mCheckResultVersion.setText(Build.ID);
                                mButton.setVisibility(View.INVISIBLE);
                            }
                            changeViewVisibility();
                        }
                    });
                }

                @Override
                public void onError(int code) {
                    mDescription.post(new Runnable() {
                        @Override
                        public void run() {
                            mDescription.setText(R.string.about_system_version_can_not_connect_server);
                            mProgressWheel.stop();
                            mButton.setText(R.string.button_retry);
                        }
                    });
                }
            });

            mProgressWheel.setMode(ProgressMode.LOADING);
            mProgressWheel.start();
            mProgressWheel.startAnimation(mShowAnimationSet);
            int mill = 0;
            if (delay) {
                mill = 1000;
            }
            mDescription.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mUpgradeManager.startCheck();
                }
            }, mill);
        }
        changeViewVisibility();
    }

    private void changeViewVisibility() {
        if (isVersionChecked) {
            if (isDownloading) {
                mCheckPanel.setVisibility(View.VISIBLE);
                mCheckResult.setVisibility(View.INVISIBLE);
            } else {
                mCheckPanel.setVisibility(View.INVISIBLE);
                mCheckResult.setVisibility(View.VISIBLE);
            }
        } else {
            mCheckPanel.setVisibility(View.VISIBLE);
            mCheckResult.setVisibility(View.INVISIBLE);
        }
    }

    private AnimationSet buildWheelInAnimator() {
        AnimationSet set = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        set.setDuration(500);
        return set;
    }

}
