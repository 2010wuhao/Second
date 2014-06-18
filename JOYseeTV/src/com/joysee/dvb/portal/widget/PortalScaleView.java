/**
 * =====================================================================
 *
 * @file  PortalScaleView.java
 * @Module Name   com.joysee.dvb.portal.widget
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013-12-12
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
 * wuhao         2013-12-12            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.joysee.common.data.JFetchBackListener;
import com.joysee.common.data.JHttpHelper;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.portal.PortalModle;
import com.joysee.dvb.portal.QuickAccessActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PortalScaleView extends FrameLayout implements View.OnFocusChangeListener,
        View.OnClickListener, AnimatorListener {
    /**
     * Interface definition for a callback to be invoked when focus reached page
     * edge of ViewFlow
     * 
     * @author wuhao
     */
    public interface onReachedEdgeListener {
        void onReachedBottomEdge(int parent_id);

        void onReachedLeftEdge(int parent_id);

        void onReachedRightEdge(int parent_id);

        void onReachedTopEdge(int parent_id);
        
        boolean onScaleViewClick(int viewType);
    }
    
    public static final int FOCUS_TYPE325 = 325;
    public static final int FOCUS_TYPE588 = 588;
    public static final int FOCUS_TYPE440 = 440;
    public static final int FOCUS_TYPE341 = 341;
    public static final int FOCUS_TYPE565 = 565;
    public static final int FOCUS_TYPE367 = 367;
    public static final int FOCUS_TYPE544 = 544;
    public static final int FOCUS_TYPE1364 = 1364;

    public static final int FOCUS_TYPE_VIDEO = 10000;
    public static final int RES_TYPE_DVB = 16;
    public static final int RES_TYPE_DVB_HISTORY = 17;
    public static final int RES_TYPE_DVB_RECOMMAND = 18;
    public static final int RES_TYPE_DVB_LIVEGUIDE = 19;
    public static final int RES_TYPE_DVB_CHANNEL_LIST = 20;
    public static final int RES_TYPE_DVB_PROGRAM_LIST = 21;
    public static final int RES_TYPE_DVB_SEARCH = 22;
    public static final int RES_TYPE_DVB_CA = 23;
    public static final int RES_TYPE_DVB_EMAIL = 24;
    public static final int RES_TYPE_DVB_OPERATOR = 25;
    public static final int RES_TYPE_DVB_LOOKBACK = 26;

    public static final int RES_TYPE_HELP = 32;
    private String TAG = JLog.makeTag(PortalScaleView.class);
    private String mResUrl;
    private int mResType;
    private boolean isLeftEdge;
    private boolean isRightEdge;
    private boolean isTopEdge;
    private boolean isBottomEdge;
    private int mParentID;
    private float mScaleX;
    private float mScaleY;
    private String mPackageName;
    private String mActivityName;
    private String mIntentKey;
    private String mIntentValue;

    private int mFocusType = -1;
    private ViewPropertyAnimator mPropertyAnimator;
    private WeakReference<onReachedEdgeListener> onReachedEdgeListener;
    private JTextViewWithTTF mLableView;
    private ImageView mContentView;
    private ImageView mFocusView;
    private Context mContext;
    private static boolean isScrolling = false;
    private boolean isAnimating = false;
    private int[] mAccessKeys = {
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_DOWN,

            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_RIGHT,

            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_DOWN,

            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_RIGHT,

            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_RIGHT,
    };

    private ArrayList<Integer> mKeys = new ArrayList<Integer>();

    public PortalScaleView(Context context) {
        this(context, null);
    }

    public PortalScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PortalScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " dispatchKeyEvent " + event.toString());
        }
        if (isQuickAccess(event)) {
            Intent intent = new Intent(getContext(), QuickAccessActivity.class);
            getContext().startActivity(intent);
            mKeys.clear();
            return true;
        }
        boolean result = false;
        switch (action) {
            case KeyEvent.ACTION_DOWN:
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (onReachedEdgeListener != null && isLeftEdge) {
                            isScrolling = true;
                            onReachedEdgeListener.get().onReachedLeftEdge(mParentID);
                            result = true;
                            isScrolling = false;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (onReachedEdgeListener != null && isRightEdge) {
                            isScrolling = true;
                            onReachedEdgeListener.get().onReachedRightEdge(mParentID);
                            result = true;
                            isScrolling = false;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (onReachedEdgeListener != null && isBottomEdge) {
                            onReachedEdgeListener.get().onReachedBottomEdge(mParentID);
                            result = true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (onReachedEdgeListener != null && isTopEdge) {
                            onReachedEdgeListener.get().onReachedTopEdge(mParentID);
                            result = true;
                        }
                        break;
                }
                break;
        }
        return result ? true : super.dispatchKeyEvent(event);
    }

    public onReachedEdgeListener getonReachedEdgeListener() {
        return onReachedEdgeListener.get();
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
                R.styleable.Protal_ScaleView);
        mContext = context;
        mResUrl = styledAttrs.getString(R.styleable.Protal_ScaleView_resUrl);
        mResType = styledAttrs.getInteger(R.styleable.Protal_ScaleView_resType, 0);
        isLeftEdge = styledAttrs.getBoolean(R.styleable.Protal_ScaleView_isLeftEdge, false);
        isRightEdge = styledAttrs.getBoolean(R.styleable.Protal_ScaleView_isRightEdge, false);
        mParentID = styledAttrs.getInteger(R.styleable.Protal_ScaleView_parentID, -1);
        mScaleX = styledAttrs.getInteger(R.styleable.Protal_ScaleView_scaleX, 0);
        mScaleY = styledAttrs.getInteger(R.styleable.Protal_ScaleView_scaleY, 0);
        mPackageName = styledAttrs.getString(R.styleable.Protal_ScaleView_packageName);
        mActivityName = styledAttrs.getString(R.styleable.Protal_ScaleView_activityName);
        isTopEdge = styledAttrs.getBoolean(R.styleable.Protal_ScaleView_isTopEdge, false);
        isBottomEdge = styledAttrs.getBoolean(R.styleable.Protal_ScaleView_isBottomEdge, false);
        mIntentKey = styledAttrs.getString(R.styleable.Protal_ScaleView_intentKey);
        mIntentValue = styledAttrs.getString(R.styleable.Protal_ScaleView_intentValue);
        mFocusType = styledAttrs.getInteger(R.styleable.Protal_ScaleView_focusType, -1);
        styledAttrs.recycle();
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " isLeftEdge = " + isLeftEdge + " isRightEdge = "
                    + isRightEdge + " mParentID = " + mParentID + " mPackageName = " +
                    mPackageName
                    + " mActivityName = " + mActivityName);
        }

        mPropertyAnimator = this.animate();
        mPropertyAnimator.setListener(this);
        setOnFocusChangeListener(this);
        setOnClickListener(this);

    }

    public boolean isLeftEdge() {
        return isLeftEdge;
    }

    public boolean isQuickAccess(KeyEvent event) {
        if ((mResType == RES_TYPE_HELP) && event.getAction() == KeyEvent.ACTION_DOWN) {
            mKeys.add(event.getKeyCode());
            if (mKeys.size() == mAccessKeys.length) {
                for (int i = 0; i < mAccessKeys.length; i++) {
                    if (mAccessKeys[i] != mKeys.get(i).intValue()) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean isRightEdge() {
        return isRightEdge;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        isAnimating = false;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        isAnimating = false;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onAnimationStart(Animator animation) {
        isAnimating = true;
    }

    @Override
    public void onClick(View v) {
        boolean hasDongle = PortalModle.getUsbDongleState(mContext);
        boolean hasChannel = AbsDvbPlayer.getChannelCount() > 0;
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " onClick mPackageName = " + mPackageName + " mActivityName = "
                    + mActivityName + " mIntentKey = " + mIntentKey + " mIntentValue = "
                    + mIntentValue + " mResType = " + mResType + " hasDongle = " + hasDongle);
        }
        if (!hasChannel) {
            if (mResType == RES_TYPE_DVB_LIVEGUIDE || mResType == RES_TYPE_DVB_CHANNEL_LIST || mResType == RES_TYPE_DVB_PROGRAM_LIST) {
                String msg = getResources().getString(R.string.portal_not_open_nochannel);
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (mPackageName != null && mActivityName != null) {
            if (hasDongle) {
                Intent intent = new Intent();
                intent.setClassName(mPackageName, mActivityName);
                if (mIntentKey != null && mIntentValue != null) {
                    intent.putExtra(mIntentKey, mIntentValue);
                }
                startActivitySafely(intent, null, false, false);
            } else {
                if ((RES_TYPE_DVB & mResType) == RES_TYPE_DVB) {
                    Toast.makeText(getContext(), getResources().getString(R.string.no_dongle),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent();
                    intent.setClassName(mPackageName, mActivityName);
                    if (mIntentKey != null && mIntentValue != null) {
                        intent.putExtra(mIntentKey, mIntentValue);
                    }
                    startActivitySafely(intent, null, false, false);
                }
            }
        } else {
            if (!onReachedEdgeListener.get().onScaleViewClick(mResType)) {
                String msg;
                if (mResType == RES_TYPE_DVB_HISTORY) {
                    msg = getResources().getString(R.string.portal_not_history);
                } else if (mResType == RES_TYPE_DVB_RECOMMAND) {
                    msg = getResources().getString(R.string.portal_not_recommand);
                } else {
                    msg = getResources().getString(R.string.portal_not_open_hint);
                }
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = (ImageView) this.findViewById(R.id.portal_scaleview_content);
        mFocusView = (ImageView) this.findViewById(R.id.portal_scaleview_focus);
        mLableView = (JTextViewWithTTF) this.findViewById(R.id.portal_scaleview_text);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        
        if (mResType == RES_TYPE_HELP && TvApplication.AS_LAUNCHER) {
            if (mContentView != null) {
                mContentView.setImageResource(R.drawable.portal_mytv_allapp);
            }
            if (mLableView != null) {
                mLableView.setText(R.string.portal_applist);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " onFocusChange " + hasFocus + " isScrolling = " + isScrolling
                    + " mFocusType = " + mFocusType);
        }
        if (hasFocus) {
            mKeys.clear();
            if (mFocusView != null && mFocusType != -1) {
                switch (mFocusType) {
                    case FOCUS_TYPE325:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_focus_325));
                        break;
                    case FOCUS_TYPE588:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_focus_588));
                        break;
                    case FOCUS_TYPE440:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_focus_440));
                        break;
                    case FOCUS_TYPE341:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_focus_341));
                        break;
                    case FOCUS_TYPE565:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_focus_565));
                        break;
                    case FOCUS_TYPE367:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_focus_367));
                        break;
                    case FOCUS_TYPE544:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_focus_544));
                        break;
                    case FOCUS_TYPE1364:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_focus_1364));
                        break;
                    case FOCUS_TYPE_VIDEO:
                        mFocusView.setBackgroundDrawable(mContext.getResources().getDrawable(
                                R.drawable.portal_tv_focus));
                        break;
                }
            }
            int duration = 300;
            final int width = this.getWidth();
            final int height = this.getHeight();
            if (!isScrolling && TvApplication.PORTAL_USE_ANIMATION) {
                mPropertyAnimator.scaleX((width + mScaleX) / width)
                        .scaleY((height + mScaleY) / height).setDuration(duration)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            } else {
                if (isAnimating) {
                    mPropertyAnimator.cancel();
                }
                PortalScaleView.this.setScaleX((width + mScaleX) / width);
                PortalScaleView.this.setScaleY((height + mScaleY) / height);
            }

            if (mLableView != null) {
                mLableView.setSelected(true);
            }
        } else {
            if (mFocusView != null) {
                mFocusView.setBackgroundDrawable(null);
            }
            int duration = 200;
            if (!isScrolling && TvApplication.PORTAL_USE_ANIMATION) {
                mPropertyAnimator.scaleX(1.0f).scaleY(1.0f).setDuration(duration)
                        .setInterpolator(new DecelerateInterpolator()).start();
            } else {
                if (isAnimating) {
                    mPropertyAnimator.cancel();
                }
                PortalScaleView.this.setScaleX(1.0f);
                PortalScaleView.this.setScaleY(1.0f);
            }

            if (mLableView != null) {
                mLableView.setSelected(false);
            }
        }
    }

    public void setIntent(String pkgName, String activityName, String intentKey, String intentValue) {
        this.mPackageName = pkgName;
        this.mActivityName = activityName;
        this.mIntentKey = intentKey;
        this.mIntentValue = intentValue;
    }

    public void setonReachedEdgeListener(onReachedEdgeListener onReachedBorderListener) {
        this.onReachedEdgeListener = new WeakReference<PortalScaleView.onReachedEdgeListener>(
                onReachedBorderListener);
        PortalPageViewItem pageViewItem = (PortalPageViewItem) getParent();
        mParentID = pageViewItem.mID;
    }

    public boolean startActivitySafely(Intent intent, Object tag, boolean goToDvb,
            boolean checkCaStatus) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            JLog.d(TAG, "startActivitySafely intent = " + intent.toUri(0));
            if (intent.getComponent() != null) {
                JLog.d(TAG, "packageName = " + intent.getComponent().getPackageName());
            }
            getContext().startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            JLog.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        } catch (SecurityException e) {
            JLog.e(TAG, "Launcher does not have the permission to launch " + intent
                    + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                    + "or use the exported attribute for this activity. " + "tag=" + tag
                    + " intent=" + intent, e);
        } catch (Exception e) {
            JLog.e(TAG, "startActivitySafely catch an Exception...", e);
        }
        return false;
    }

    public void updateIcon(String imagePath) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " updateIcon imagePath = " + imagePath);
        }
        if (mContentView != null) {
            WeakReference<Context> weakReference = new WeakReference<Context>(getContext());
            JHttpHelper.getImage(weakReference.get(), imagePath, new
                    JFetchBackListener() {
                        @Override
                        public void fetchSuccess(String arg0, final BitmapDrawable arg1) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    mContentView.setImageDrawable(arg1);
                                }
                            });
                        }
                    });
        }
    }

    public void updateText(String text) {
        if (mLableView != null) {
            mLableView.setText(text);
        }
    }
}
