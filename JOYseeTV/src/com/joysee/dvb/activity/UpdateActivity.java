/**
 * =====================================================================
 *
 * @file  UpdateActivity.java
 * @Module Name   com.joysee.dvb.activity
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-3-28
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
 * benz          2014-3-28           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.update.UpdateClient;
import com.joysee.dvb.widget.ProgressWheel;
import com.joysee.dvb.widget.ProgressWheel.ProgressMode;

public class UpdateActivity extends Activity implements OnClickListener {

    private enum BtStatus {
        NORMAL, CHECKING, CHECK_NEW, CHECK_NO_NEW, CHECK_FAILED, DOWNLOADING, DOWNLOAD_FAILED, DOWNLOADED
    }

    private UpdateClient mUpdateClient;
    private BtStatus mStatus = BtStatus.NORMAL;
    private JButtonWithTTF mExecuteBt;
    private TextView mCurrentVersionInfo;
    private TextView mRepairIns;
    private ProgressWheel mProgressWheel;
    private TextView mProgressWheelInfo;
    private TextView mResultTitle;
    private TextView mTitle;

    private TextView mResultTitlePoint;

    private AnimationSet mShowAnimationSet;

    private boolean checkNow;

    private AnimationSet buildWheelInAnimator() {
        AnimationSet set = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        set.setDuration(500);
        return set;
    }

    private void handlerButtonClick() {
        switch (mStatus) {
            case NORMAL:
                mTitle.setText(R.string.update_check_new);
                mStatus = BtStatus.CHECKING;
                findViewById(R.id.content_version_layout).setVisibility(View.GONE);
                findViewById(R.id.content_result_layout).setVisibility(View.GONE);
                findViewById(R.id.content_check_layout).setVisibility(View.VISIBLE);
                mProgressWheelInfo.setText(R.string.update_checking);
                mExecuteBt.setText(R.string.update_cancel);
                mProgressWheel.stop();
                mProgressWheel.setMode(ProgressMode.LOADING);
                mProgressWheel.start();
                mUpdateClient.checkAsync();
                mProgressWheel.startAnimation(mShowAnimationSet);
                break;
            case CHECKING:
                if (checkNow) {
                    finish();
                    return;
                }
                mTitle.setText(R.string.update_activity_title);
                mStatus = BtStatus.NORMAL;
                findViewById(R.id.content_result_layout).setVisibility(View.GONE);
                findViewById(R.id.content_check_layout).setVisibility(View.GONE);
                findViewById(R.id.content_version_layout).setVisibility(View.VISIBLE);
                mExecuteBt.setText(R.string.update_check_new);
                mProgressWheel.stop();
                mUpdateClient.cancelRequest();
                break;
            case CHECK_NEW:
                mStatus = BtStatus.DOWNLOADING;
                findViewById(R.id.content_version_layout).setVisibility(View.GONE);
                findViewById(R.id.content_result_layout).setVisibility(View.GONE);
                findViewById(R.id.content_check_layout).setVisibility(View.VISIBLE);
                mExecuteBt.setText(R.string.update_cancel);
                mProgressWheelInfo.setText(R.string.update_downloading);
                mProgressWheel.stop();
                mProgressWheel.setMode(ProgressMode.PERCENTAGE);
                mProgressWheel.start();
                mProgressWheel.setProgress(0);
                mProgressWheel.startAnimation(mShowAnimationSet);
                mUpdateClient.down();
                break;
            case CHECK_NO_NEW:
            case CHECK_FAILED:
                mStatus = BtStatus.NORMAL;
                finish();
                break;
            case DOWNLOADING:
                if (checkNow) {
                    finish();
                    return;
                }
                mTitle.setText(R.string.update_activity_title);
                mStatus = BtStatus.NORMAL;
                findViewById(R.id.content_version_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.content_result_layout).setVisibility(View.GONE);
                findViewById(R.id.content_check_layout).setVisibility(View.GONE);
                mExecuteBt.setText(R.string.update_check_new);
                mProgressWheel.stop();
                mUpdateClient.removeUnCompletedDoanload();
                break;
            case DOWNLOADED:
                boolean ret = mUpdateClient.requestInstall();
                if (!ret) {
                    Toast.makeText(this, R.string.update_parse_apk_failed, Toast.LENGTH_LONG).show();
                }
                break;
            case DOWNLOAD_FAILED:
                mStatus = BtStatus.NORMAL;
                finish();
                break;
            default:
                break;
        }
    }

    private String getVersionName() {
        String ret = "";
        PackageInfo localApkInfo = null;
        try {
            localApkInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (localApkInfo != null) {
                ret = localApkInfo.versionName;
            }
        }
        return ret;
    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.title);
        mCurrentVersionInfo = (TextView) findViewById(R.id.current_version_info);
        mCurrentVersionInfo.setText(getVersionName());
        mRepairIns = (TextView) findViewById(R.id.current_version_repair_ins_info);
        mRepairIns.setText(TvApplication.getRepairInstructions());
        mProgressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        mProgressWheelInfo = (TextView) findViewById(R.id.progress_wheel_point);
        mResultTitle = (TextView) findViewById(R.id.result);
        mResultTitlePoint = (TextView) findViewById(R.id.result_point);
        mExecuteBt = (JButtonWithTTF) findViewById(R.id.execute_bt);
        mExecuteBt.setOnClickListener(this);

        mShowAnimationSet = buildWheelInAnimator();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.execute_bt:
                handlerButtonClick();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_layout);
        initView();
        initUpdateClient();
        checkNow = getIntent().getBooleanExtra("check_now", false);
        if (checkNow) {
            mStatus = BtStatus.NORMAL;
            handlerButtonClick();
        }
    }

    private void initUpdateClient() {
        mUpdateClient = new UpdateClient(this) {
            @Override
            public void handlerCheckState(int status, Object attach) {
                if (mStatus == BtStatus.CHECKING) {
                    if (isDownloadSuccess()) {
                        mStatus = BtStatus.DOWNLOADED;
                        mExecuteBt.setText("确定");
                        findViewById(R.id.content_version_layout).setVisibility(View.GONE);
                        findViewById(R.id.content_check_layout).setVisibility(View.GONE);
                        findViewById(R.id.content_result_layout).setVisibility(View.VISIBLE);
                        mResultTitle.setText(R.string.update_have_native_apk);
                        mResultTitlePoint.setText("");
                    } else {
                        findViewById(R.id.content_version_layout).setVisibility(View.GONE);
                        findViewById(R.id.content_check_layout).setVisibility(View.GONE);
                        findViewById(R.id.content_result_layout).setVisibility(View.VISIBLE);
                        switch (status) {
                            case UpdateClient.STATUS_CHECK_AN_NEW_VERSION:
                                mStatus = BtStatus.CHECK_NEW;
                                mResultTitle.setText(R.string.update_title_has_version);
                                mResultTitlePoint.setText(attach.toString());
                                mExecuteBt.setText(R.string.update_upgrading_now);
                                break;
                            case UpdateClient.STATUS_CHECK_NO_UPDATE:
                                mStatus = BtStatus.CHECK_NO_NEW;
                                mResultTitle.setText(R.string.update_no_update);
                                mResultTitlePoint.setText("");
                                mExecuteBt.setText("确定");
                                break;
                            case UpdateClient.STATUS_CHECK_FAILURE:
                                mStatus = BtStatus.CHECK_FAILED;
                                mResultTitle.setText(R.string.update_title_check_failed);
                                mResultTitlePoint.setText("");
                                mExecuteBt.setText(R.string.update_exit);
                                break;
                        }
                    }
                }
            }

            @Override
            public void handlerDownloadState(int status) {
                if (mStatus == BtStatus.DOWNLOADING) {
                    switch (status) {
                        case UpdateClient.STATUS_DOWNLOAD_SUCCESS:
                            mStatus = BtStatus.DOWNLOADED;
                            mExecuteBt.setText("确定");
                            mProgressWheelInfo.setText(R.string.update_down_over);
                            break;
                        case UpdateClient.STATUS_DOWNLOAD_FAILURE:
                            mStatus = BtStatus.DOWNLOAD_FAILED;
                            mExecuteBt.setText(R.string.update_exit);
                            mProgressWheelInfo.setText(R.string.update_down_failed);
                            break;
                    }
                }
            }

            @Override
            public void handlerDownloadProgress(int progress) {
                if (mStatus == BtStatus.DOWNLOADING) {
                    mProgressWheel.setProgress(progress);
                }
            }
        };
        mUpdateClient.registerDownloadReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUpdateClient.cancelRequest();
        mUpdateClient.unRegisterDownloadReceiver();
        mUpdateClient = null;
    }

}
