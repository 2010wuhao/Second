/**
 * =====================================================================
 *
 * @file  CustomMenu.java
 * @Module Name   com.joysee.dvb.widget
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-2-21
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
 * benz          2014-2-21           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget.menu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;

import java.util.ArrayList;

public class CustomMenu extends RelativeLayout implements AnimatorListener {

    public enum Location {
        LEFT, RIGHT
    }

    private class MenuAdapter extends BaseAdapter {

        public MenuAdapter() {

        }

        @Override
        public int getCount() {
            return mMenuItemBeans.size();
        }

        @Override
        public Object getItem(int position) {
            return mMenuItemBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            CustomMenuItemView itemView;
            if (convertView == null) {
                itemView = new CustomMenuItemView(getContext(), "", null);
            } else {
                itemView = (CustomMenuItemView) convertView;
            }

            CustomMenuItemBean bean = mMenuItemBeans.get(position);
            itemView.setId(bean.getId());
            itemView.setTitle(bean.getTitle());
            if (bean.getIcon() != null) {
                itemView.setIcon(bean.getIcon());
            }

            /*
             * if (position == mCurrentSelectItem) {
             * itemView.getTitleView().setTypeface
             * (SFontMgr.getTypeface(getContext(), "fzltch.TTF")); } else {
             * itemView
             * .getTitleView().setTypeface(SFontMgr.getTypeface(getContext(),
             * "fzltzh.TTF")); }
             */

            return itemView;
        }

    }

    public interface OnMenuListener {
        public void closeMenu(View lastFocusView);

        public void onClickItem(View v, int position, int id);

        public void openMenu();
    }

    private static final String TAG = JLog.makeTag(CustomMenu.class);
    private ViewGroup mDecorView;
    public ViewGroup mActivityView;
    private RelativeLayout mMenuPanel;

    private ListView mMenuList;
    private MenuAdapter mAdapter;

    private View mLastFocusView;
    private Animator mMenuInAnimatorSet;
    private Animator mMenuOutAnimatorSet;
    private int mDuration = 200;
    private int mMenuW = 592;
    private int mLastClickItem;
    private int mSelectItem;
    private Activity mActivity;
    private boolean isShowing = false;
    private boolean isAttach2Window;

    private boolean isCancleable = true;
    private OnMenuListener mOnMenuListener;

    private DisplayMetrics mDisplayMetrics;

    private ArrayList<CustomMenuItemBean> mMenuItemBeans;

    private Location mLocation = Location.RIGHT;

    public CustomMenu(Activity activity, Location l) {
        super(activity);
        initView(activity, l);
    }

    public void addItem(ArrayList<CustomMenuItemBean> items) {
        mMenuItemBeans.clear();
        mMenuItemBeans.addAll(items);
    }

    public void addItem(CustomMenuItemBean item) {
        mMenuItemBeans.add(item);
    }

    public void attach2Window() {
        performMenuPanel();
        performListener();
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        isAttach2Window = true;
    }

    private void buildAnimation() {
        int formX = 0;
        int toX = 0;
        if (mLocation == Location.LEFT) {
            formX = -mMenuW;
            toX = 0;
        } else if (mLocation == Location.RIGHT) {
            formX = mDisplayMetrics.widthPixels;
            toX = mDisplayMetrics.widthPixels - mMenuW;
        }
        mMenuInAnimatorSet = buildMenuAnimator(mMenuPanel, formX, toX);
        mMenuOutAnimatorSet = buildMenuAnimator(mMenuPanel, toX, formX);
        mMenuInAnimatorSet.addListener(this);
        mMenuOutAnimatorSet.addListener(this);
    }

    private AnimatorSet buildMenuAnimator(View target, int fromX, int toX) {
        /**
         * ofFloat(target, propertyName, values) 中 fromX : 起点的x坐标与parent 的margin
         * left toX : 终点的x坐标与parent 的margin left
         */
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator xAnim = ObjectAnimator.ofFloat(target, "x", fromX, toX).setDuration(mDuration);
        set.play(xAnim);
        set.setInterpolator(new DecelerateInterpolator());
        return set;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        boolean handle = false;
        if (isShowing) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (mSelectItem == 0) {
                    handle = true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (mSelectItem == mAdapter.getCount() - 1) {
                    handle = true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                if (isCancleable) {
                    if (action == KeyEvent.ACTION_UP) {
                        this.hide();
                        handle = true;
                    }
                }
            }
        }
        return handle ? true : super.dispatchKeyEvent(event);
    }

    public void hide() {
        if (!isAttach2Window) {
            throw new IllegalArgumentException("menu is not attach 2 window");
        }
        if (isShowing) {
            isShowing = false;
            mMenuOutAnimatorSet.start();
        }
    }

    private void initView(Activity activity, Location l) {
        mLocation = l;
        mActivity = activity;
        mDisplayMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mMenuItemBeans = new ArrayList<CustomMenuItemBean>();
        mAdapter = new MenuAdapter();
        mDecorView = (ViewGroup) mActivity.getWindow().getDecorView();
        mActivityView = (ViewGroup) mDecorView.getChildAt(0);
        mActivity.getLayoutInflater().inflate(R.layout.custom_menu_layout, this);
        mMenuPanel = (RelativeLayout) findViewById(R.id.menu_backgound);
        mMenuList = (ListView) findViewById(R.id.menu_listview);
        mMenuList.setAdapter(mAdapter);
        mDecorView.addView(this);
        
        mMenuW = (int) mActivity.getResources().getDimension(R.dimen.custom_menu_width);
    }

    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (isShowing) {
            if (mOnMenuListener != null) {
                mOnMenuListener.openMenu();
            }
        } else {
            mDecorView.removeView(this);
            if (mOnMenuListener != null) {
                mOnMenuListener.closeMenu(mLastFocusView);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    private void performListener() {
        mMenuList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLastClickItem = position;
                if (mOnMenuListener != null) {
                    mOnMenuListener.onClickItem(view, position, view.getId());
                }
            }
        });
        mMenuList.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectItem = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void performMenuPanel() {
        RelativeLayout.LayoutParams panelParams = (RelativeLayout.LayoutParams) mMenuPanel.getLayoutParams();
        int mLocationLeftMargin = 0;
        int mLocationRightMargin = 0;
        int mLocationLeftPadding = 0;
        int mLocationRightPadding = 0;
        int bgResId = R.drawable.custom_menu_right_bg;
        if (mLocation == Location.LEFT) {
            mLocationLeftMargin = -mMenuW;
            mLocationRightMargin = mDisplayMetrics.widthPixels - mMenuW;
            mLocationLeftPadding = 0;
            mLocationRightPadding = 35;
            bgResId = R.drawable.custom_menu_right_bg;
        } else if (mLocation == Location.RIGHT) {
            mLocationLeftMargin = mDisplayMetrics.widthPixels;
            mLocationRightMargin = -mMenuW;
            mLocationLeftPadding = 35;
            mLocationRightPadding = 0;
            bgResId = R.drawable.custom_menu_right_bg;
        }
        panelParams.setMargins(mLocationLeftMargin, 0, mLocationRightMargin, 0);
        mMenuPanel.setLayoutParams(panelParams);
        mMenuPanel.setPadding(mLocationLeftPadding, 0, mLocationRightPadding, 0);
        mMenuPanel.setBackgroundResource(bgResId);

        buildAnimation();
    }

    public void registerMenuControl(OnMenuListener l) {
        mOnMenuListener = l;
    }

    private void resetMenuLastStatus() {
        mMenuList.requestFocus();
        mMenuList.setSelection(mLastClickItem);
    }

    public void setAnimationDuration(int duration) {
        mDuration = duration;
    }

    public void setCancleAble(boolean able) {
        this.isCancleable = able;
    }

    public void show() {
        if (!isAttach2Window) {
            throw new IllegalArgumentException("menu is not attach 2 window");
        }
        if (!isShowing) {
            isShowing = true;
            mLastFocusView = mActivity.getWindow().getCurrentFocus();
            if (getParent() == null) {
                mDecorView.addView(this);
            }
            mAdapter.notifyDataSetChanged();
            resetMenuLastStatus();
            mMenuInAnimatorSet.start();
        }
    }
}
