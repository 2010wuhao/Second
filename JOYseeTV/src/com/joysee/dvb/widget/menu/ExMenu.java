/**
 * =====================================================================
 *
 * @file  ExMenu.java
 * @Module Name   com.joysee.dvb.widget.menu
 * @author Benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-4-11
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
 * benz          2014-4-11           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.widget.menu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;

import java.util.ArrayList;

public class ExMenu extends RelativeLayout implements ExMenuGroup.OnSelectListener, ExMenuItem.OnSelectListener {

    public interface OnMenuListener {
        public void onMenuClose(View lastFocusView);

        public void onItemSubClick(ExMenuSub exMenuSub);

        public void onMenuOpen();

        // groups以whichHideItems为中心展开
        public void onGroupExpand(ExMenuGroup whichHideItems);

        // groups以whichHideItems为中心收展
        public void onGroupCollapsed(ExMenuGroup whichShowItems);
    }

    private class GroupPanelSwitchListener implements AnimatorListener {

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (isGroupShowing) {
                if (mOnMenuListener != null) {
                    mOnMenuListener.onMenuOpen();
                }
                if (!hasLastOpenStatus) {
                    hasLastOpenStatus = true;
                }
            } else {
                mDecorView.removeView(ExMenu.this);
                if (isResetable && isItemsShowing) {
                    foldItems(mSelectGroup.getSubMenuList());
                }
                if (mOnMenuListener != null) {
                    mOnMenuListener.onMenuClose(mLastFocusView);
                }
            }
        }

    }

    private class ItemsPanelSwitchListener implements AnimatorListener {
        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!isItemsShowing) {
                mItemsRoot.setVisibility(View.GONE);
                unfoldGroups(mGroupViews);
            }
        }
    }

    private static final String TAG = JLog.makeTag(ExMenu.class);
    private ViewGroup mDecorView;
    @SuppressWarnings("unused")
    private ViewGroup mActivityView;
    private RelativeLayout mMenuPanel;
    private RelativeLayout mTitleRoot;
    private FrameLayout mItemsRoot;
    private LinearLayout mGroupLayout;
    private RelativeLayout mItemLayout;
    private ImageView mGroupSelectView;
    private ImageView mItemsSelectView;
    private ImageView mBackArrow;
    private TextView mTitleTv;

    private int mScreenW;
    private int mScreenH;
    private int mMenuW;
    private int mGroupW;
    private int mGroupH;
    private int mItemW;
    private int mItemH;
    private int mItemRootMoveOut;
    private int mTitleMoveEndX;
    private int mBackArrowMoveBegin;
    private int mBackArrowMoveEnd;

    private int mPanelSlideTime = 200;
    private int mMenuSubSlideTime = 200;
    private int mMenuSubFoldTime = 120;

    // mGroupSelectView(焦点view)在track中的角标
    private int mGroupSelectInTrack;
    // mItemSelectView(焦点view)在track中的角标
    private int mItemSelectInTrack;
    // 从点角标开始，往上做动画的group需要透明不可见
    private int mGroupAlphaDividerSelect;
    private int[] mGroupSelectViewLocation;
    private int[] mItemSelectViewLocation;

    private Animator mItemsPanelAnimatorIn;
    private Animator mItemsPanelAnimatorOut;
    private Animator mBackArrowAnimatorIn;
    private Animator mBackArrowAnimatorOut;
    private Animator mTitleAnimatorIn;
    private Animator mTitleAnimatorOut;
    private Animator mMenuPanelAnimatorIn;
    private Animator mMenuPanelAnimatorOut;

    private ArrayList<int[]> mItemsTrack;
    private ArrayList<ExMenuGroup> mGroupViews;
    private ArrayList<int[]> mGroupTrack;
    private ArrayList<Integer> mInitGroupTracks;
    private int mDefaultSelectGroup = 1;

    private ExMenuGroup mSelectGroup;
    private ExMenuItem mSelectItem;
    private View mLastFocusView;

    private boolean isGroupShowing = false;
    private boolean isGroupUnfold = true;
    private boolean isItemsShowing = false;
    private boolean isAttach2Window = false;

    private Activity mActivity;
    private LayoutInflater mInflater;
    private OnMenuListener mOnMenuListener;

    private boolean isResetable = true;
    private boolean isCancleable = true;
    private boolean isLeftRightControlable = true;
    private boolean hasLastOpenStatus = false;

    public ExMenu(Activity activity) {
        super(activity);
        initView(activity);
        initDataModel();
    }

    private void initView(Activity activity) {
        mActivity = activity;
        mInflater = activity.getLayoutInflater();
        // DisplayMetrics displayMetrics = new DisplayMetrics();
        // mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // mScreenW = displayMetrics.widthPixels;
        // mScreenH = displayMetrics.heightPixels;

        mInflater.inflate(R.layout.exmenu_layout, this);
        mDecorView = (ViewGroup) mActivity.getWindow().getDecorView();
        mActivityView = (ViewGroup) mDecorView.getChildAt(0);

        mMenuPanel = (RelativeLayout) findViewById(R.id.menu_root);
        mGroupLayout = (LinearLayout) mMenuPanel.findViewById(R.id.group_layout);
        mItemsRoot = (FrameLayout) findViewById(R.id.items_root);
        mItemLayout = (RelativeLayout) mMenuPanel.findViewById(R.id.item_layout);

        mTitleRoot = (RelativeLayout) mMenuPanel.findViewById(R.id.title_layout);
        mBackArrow = (ImageView) mMenuPanel.findViewById(R.id.back_arrow);
        mGroupSelectView = (ImageView) mMenuPanel.findViewById(R.id.groups_select);
        mItemsSelectView = (ImageView) mMenuPanel.findViewById(R.id.items_select);
        mTitleTv = (TextView) mMenuPanel.findViewById(R.id.title);

        // mDecorView.addView(this);
    }

    private void initDataModel() {
        mScreenW = (int) mActivity.getResources().getDimension(R.dimen.window_width);
        mScreenH = (int) mActivity.getResources().getDimension(R.dimen.window_height);
        mMenuW = (int) mActivity.getResources().getDimension(R.dimen.exmenu_panel_w);
        mGroupW = (int) mActivity.getResources().getDimension(R.dimen.exmenu_group_w);
        mGroupH = (int) mActivity.getResources().getDimension(R.dimen.exmenu_group_h);
        mItemW = (int) mActivity.getResources().getDimension(R.dimen.exmenu_item_w);
        mItemH = (int) mActivity.getResources().getDimension(R.dimen.exmenu_item_h);
        mItemRootMoveOut = (int) mActivity.getResources().getDimension(R.dimen.exmenu_item_panel_move_out);
        mTitleMoveEndX = (int) mActivity.getResources().getDimension(R.dimen.exmenu_title_move_x);
        mBackArrowMoveBegin = (int) mActivity.getResources().getDimension(R.dimen.exmenu_backarrow_move_begin);
        mBackArrowMoveEnd = (int) mActivity.getResources().getDimension(R.dimen.exmenu_backarrow_move_end);

        mItemsTrack = new ArrayList<int[]>();
        mItemSelectViewLocation = new int[] {
                0, mScreenH / 2 - mItemH / 2
        };

        mGroupViews = new ArrayList<ExMenuGroup>();
        mGroupTrack = new ArrayList<int[]>();
        mGroupSelectViewLocation = new int[] {
                0, mScreenH / 2 - mGroupH / 2
        };

        mInitGroupTracks = new ArrayList<Integer>();
    }

    // 是否每次显示时都复位
    public void setResetable(boolean resetable) {
        this.isResetable = resetable;
    }

    // 是否返回键响应 hide()
    public void setCancleable(boolean cancleable) {
        this.isCancleable = cancleable;
    }

    // 是否左右键呼出二级菜单
    public void setLeftRightControlable(boolean canControl) {
        this.isLeftRightControlable = canControl;
    }

    public void setMenuTitle(int titleId) {
        setMenuTitle(getResources().getString(titleId));
    }

    public void setMenuTitle(String title) {
        mTitleTv.setText(title);
        mTitleRoot.setVisibility(View.VISIBLE);
    }

    public void setDefaultGroupSelect(int position) {
        this.mDefaultSelectGroup = position;
    }

    public void addSubGroup(ExMenuGroup group) {
        mGroupViews.add(group);
        if (!mGroupViews.contains(group)) {
        }
    }

    public void addSubGroups(ArrayList<ExMenuGroup> groups) {
        mGroupViews.clear();
        mGroupViews.addAll(groups);
    }

    public void attach2Window() {
        performMenuPanel();
        performListener();
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        isAttach2Window = true;
    }

    private void performMenuPanel() {
        RelativeLayout.LayoutParams panelParams = (RelativeLayout.LayoutParams) mMenuPanel.getLayoutParams();
        int locationLeftMargin = -mMenuW;
        int locationRightMargin = mScreenW - mMenuW;
        panelParams.setMargins(locationLeftMargin, 0, locationRightMargin, 0);
        mMenuPanel.setLayoutParams(panelParams);
        performGroups();
        buildAnimationModel();
    }

    private void performGroups() {
        mGroupTrack.clear();
        mGroupLayout.removeAllViews();

        int count = mGroupViews.size();
        mDefaultSelectGroup = mDefaultSelectGroup >= (count - 1) ? (count - 1) : mDefaultSelectGroup;

        // set group track array
        for (int i = -count + 1; i < count; i++) {
            int[] track = new int[] {
                    0,
                    mGroupSelectViewLocation[1] + i * mGroupH
            };
            mGroupTrack.add(track);
        }

        mGroupSelectInTrack = count - 1;
        for (int i = 0; i < count; i++) {
            ExMenuGroup group = mGroupViews.get(i);
            int positionInTrack = i + (count - 1) - mDefaultSelectGroup;

            group.addLinstener(this);
            group.setSelectViewInTrack(mGroupSelectInTrack);
            group.setPositionInGroups(i);
            group.setPositionInTrack(positionInTrack);
            mInitGroupTracks.add(positionInTrack);

            // 定位
            if (i == 0) {
                LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(mGroupW, mGroupH);
                int topMargin = mGroupSelectViewLocation[1] - mGroupH * mDefaultSelectGroup;
                groupParams.setMargins(0, topMargin, 0, 0);
                mGroupLayout.addView(group, groupParams);
            } else {
                mGroupLayout.addView(group);
            }

            // 设置默认选中行
            if (i == mDefaultSelectGroup) {
                mSelectGroup = group;
                mSelectGroup.onFocusChanged(true);
            }

            // 最下面一个下划线不可见
            if (i == count - 1) {
                group.setDividerVisibility(View.INVISIBLE);
            }
        }
        mGroupAlphaDividerSelect = mGroupSelectInTrack - 2;
        performSlideControl();
    }

    private void performItems(ExMenuGroup group) {
        int lastSelect = group.getSelectSub();
        JLog.d(TAG, "performItems from group[" + group.getSubId() + "]  last select sub " + lastSelect);
        mItemsTrack.clear();

        final ArrayList<ExMenuItem> items = (ArrayList<ExMenuItem>) group.getSubMenuList();
        int count = items.size();
        // set item track
        for (int i = -count + 1; i < count; i++) {
            int[] track = new int[] {
                    0,
                    mItemSelectViewLocation[1] + i * mItemH
            };
            mItemsTrack.add(track);
        }

        mItemSelectInTrack = count - 1;
        for (int i = 0; i < count; i++) {
            ExMenuItem item = items.get(i);
            item.setMenuParent(group);
            item.addLinstener(this);
            item.setPositionInGroups(i);
            item.setPositionInTrack(i + (count - 1) - lastSelect);
            item.setSelectViewInTrack(mItemSelectInTrack);

            // 默认为折叠
            RelativeLayout.LayoutParams groupParams = new RelativeLayout.LayoutParams(mItemW, mItemH);
            groupParams.setMargins(0, mItemSelectViewLocation[1], 0, mScreenH - (mItemSelectViewLocation[1] + mItemH));
            mItemLayout.addView(item, groupParams);
            // 设置默认选中行
            if (i == lastSelect) {
                item.setChecked(true);
                mSelectItem = item;
            } else {
                // 非选中行收缩时不可见
                item.setVisibility(View.INVISIBLE);
                item.setChecked(false);
                item.onFocusChanged(false);
            }
        }

        unfolditems(items);
    }

    private void performListener() {
        mGroupSelectView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGroupUnfold) {
                    unfoldGroups(mGroupViews);
                } else {
                    if (mSelectGroup.isHasSub()) {
                        foldGroups(mGroupViews);
                    } else {
                        if (mOnMenuListener != null) {
                            mOnMenuListener.onItemSubClick(mSelectGroup);
                        }
                    }
                }
            }
        });

        mItemsSelectView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMenuListener != null) {
                    mSelectGroup.setSelection(mSelectItem.getPositionInGroups());
                    mOnMenuListener.onItemSubClick(mSelectItem);
                }
                refreshItemsCheckBoxStatus(mSelectItem);
            }
        });
    }

    private void refreshItemsCheckBoxStatus(ExMenuItem item) {
        if (item.isCheckBox()) {
            ArrayList<ExMenuItem> items = item.getMenuParent().getSubMenuList();
            int count = items.size();
            for (int i = 0; i < count; i++) {
                items.get(i).setChecked(i == item.getPositionInGroups());
            }
        }
    }

    private void performSlideControl() {
        mGroupSelectView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean handler = false;
                if (isGroupUnfold) {
                    int action = event.getAction();
                    if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        groupSlideDown(mGroupViews);
                        handler = true;
                    } else if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        groupSlideUp(mGroupViews);
                        handler = true;
                    }
                }
                return handler;
            }
        });

        mItemsSelectView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean handler = false;
                if (isItemsShowing) {
                    int action = event.getAction();
                    if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        itemSlideDown(mSelectGroup.getSubMenuList());
                        handler = true;
                    } else if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        itemSlideUp(mSelectGroup.getSubMenuList());
                        handler = true;
                    }
                }
                return handler;
            }
        });
    }

    private void buildAnimationModel() {
        int formX = -mMenuW;
        int toX = 0;
        // 一级面板
        mMenuPanelAnimatorIn = buildX_AxisMoveAnimator(mMenuPanel, formX, toX, mPanelSlideTime);
        mMenuPanelAnimatorOut = buildX_AxisMoveAnimator(mMenuPanel, toX, formX, mPanelSlideTime);
        mMenuPanelAnimatorIn.addListener(new GroupPanelSwitchListener());
        mMenuPanelAnimatorOut.addListener(new GroupPanelSwitchListener());

        // 二级面板
        mItemsPanelAnimatorIn = buildX_AxisMoveAnimator(mItemsRoot, 0, mItemRootMoveOut, mPanelSlideTime);
        mItemsPanelAnimatorOut = buildX_AxisMoveAnimator(mItemsRoot, mItemRootMoveOut, 0, mPanelSlideTime);
        mItemsPanelAnimatorIn.addListener(new ItemsPanelSwitchListener());
        mItemsPanelAnimatorOut.addListener(new ItemsPanelSwitchListener());

        // 箭头动画
        mBackArrowAnimatorIn = buildBackArrowAnimator(mBackArrow, mBackArrowMoveBegin, mBackArrowMoveEnd, mMenuSubFoldTime, true);
        mBackArrowAnimatorOut = buildBackArrowAnimator(mBackArrow, mBackArrowMoveEnd, mBackArrowMoveBegin, mMenuSubFoldTime, false);
        // 标题动画
        mTitleAnimatorOut = buildX_AxisMoveAnimator(mTitleRoot, 0, mTitleMoveEndX, mMenuSubFoldTime);
        mTitleAnimatorIn = buildX_AxisMoveAnimator(mTitleRoot, mTitleMoveEndX, 0, mMenuSubFoldTime);
    }

    private AnimatorSet buildX_AxisMoveAnimator(View target, int fromX, int toX, int duration) {
        /**
         * ofFloat(target, propertyName, values) 中 fromX : 起点的x坐标与parent 的margin
         * left toX : 终点的x坐标与parent 的margin left
         */
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator xAnim = ObjectAnimator.ofFloat(target, "x", fromX, toX).setDuration(duration);
        set.play(xAnim);
        set.setInterpolator(new DecelerateInterpolator());
        return set;
    }

    private AnimatorSet buildBackArrowAnimator(final View target, int fromX, int toX, int duration, final boolean visibility) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator xAnim = ObjectAnimator.ofFloat(target, "x", fromX, toX).setDuration(duration);
        float beginAlpha = 0.0f;
        float endAlpha = 1.0f;
        if (!visibility) {
            beginAlpha = 1.0f;
            endAlpha = 0.0f;
        }
        ObjectAnimator xAlpha = ObjectAnimator.ofFloat(target, "alpha", beginAlpha, endAlpha);
        set.playTogether(xAnim, xAlpha);
        set.play(xAnim);
        set.setInterpolator(new DecelerateInterpolator());

        set.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (visibility) {
                    target.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!visibility) {
                    target.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        return set;
    }

    private AnimatorSet buildFoldAnimatorSet(View target, int from, int to, boolean visibility) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator exAnim = ObjectAnimator.ofFloat(target, "y", from, to);
        float targetAlpha = visibility ? 1.0f : 0.0f;
        ObjectAnimator xAlpha = ObjectAnimator.ofFloat(target, "alpha", target.getAlpha(), targetAlpha);
        set.playTogether(exAnim, xAlpha);
        set.setInterpolator(new AccelerateInterpolator(0.80000001192092895508F));
        set.setDuration(mMenuSubFoldTime);
        return set;
    }

    public void resetDefaultGroupSelect() {
        int count = mGroupViews.size();
        if (count == 0 || mInitGroupTracks.size() == 0) {
            return;
        }
        int[] currentLocation = new int[2];
        for (int i = 0; i < count; i++) {
            ExMenuGroup group = mGroupViews.get(i);
            group.setAlpha(1.0f);
            group.getLocationInWindow(currentLocation);
            int positionInTrack = group.getPositionInTrack();
            int nextPositionInTrack = mInitGroupTracks.get(i);
            int fromY = mGroupTrack.get(positionInTrack)[1];
            int toY = mGroupTrack.get(nextPositionInTrack)[1];
            AnimatorSet set = new AnimatorSet();
            ObjectAnimator yAnim = ObjectAnimator.ofFloat(group, "y", fromY, toY);
            set.addListener(group.getSlideListener());
            set.play(yAnim);
            set.setDuration(0);
            set.start();
            group.setPositionInTrack(nextPositionInTrack);
        }
    }

    /**
     * keyCode down
     */
    private void groupSlideUp(ArrayList<ExMenuGroup> groups) {
        JLog.d(TAG, "slideGroupUp");
        int count = groups.size();
        int[] currentLocation = new int[2];
        for (int i = 0; i < count; i++) {
            ExMenuGroup group = groups.get(i);

            int arrowMinIndex = i;
            if (group.getPositionInTrack() <= arrowMinIndex) {
                return;
            }
            group.getLocationInWindow(currentLocation);
            group.clearAnimation();
            int positionInTrack = group.getPositionInTrack();
            int nextPositionInTrack = group.getPositionInTrack() - 1;

            // int[] lastMoveTarget = mGroupTrack.get(positionInTrack);
            // int unCompleted = currentLocation[1] - lastMoveTarget[1];
            // int toYWithLast = mGroupTrack.get(nextPositionInTrack)[1] -
            // unCompleted;

            int fromY = mGroupTrack.get(positionInTrack)[1];// currentLocation[1];
            int toY = mGroupTrack.get(nextPositionInTrack)[1];
            int delay = 10 * i;
            int duration = mMenuSubSlideTime;

            AnimatorSet set = new AnimatorSet();
            ObjectAnimator yAnim = ObjectAnimator.ofFloat(group, "y", fromY, toY);
            if (positionInTrack == mGroupAlphaDividerSelect) {
                float beginAlpha = 1.0f;
                float endAlpha = 0.0f;
                ObjectAnimator yAlpha = ObjectAnimator.ofFloat(group, "alpha", beginAlpha, endAlpha);
                set.playTogether(yAnim, yAlpha);
            } else {
                set.play(yAnim);
            }
            set.setDuration(duration);
            set.setStartDelay(delay);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(group.getSlideListener());
            set.start();
            group.setPositionInTrack(nextPositionInTrack);
        }
    }

    /**
     * keyCode up
     */
    private void groupSlideDown(ArrayList<ExMenuGroup> children) {
        JLog.d(TAG, "slideDown");
        int count = children.size();
        int[] currentLocation = new int[2];
        for (int i = count - 1; i >= 0; i--) {
            ExMenuGroup group = children.get(i);
            int arrowMaxIndex = i + mGroupSelectInTrack;
            if (group.getPositionInTrack() >= arrowMaxIndex) {
                return;
            }
            group.clearAnimation();
            group.getLocationInWindow(currentLocation);
            int positionInTrack = group.getPositionInTrack();
            int nextPositionInTrack = group.getPositionInTrack() + 1;

            // int[] lastMoveTarget = mGroupTrack.get(positionInTrack);
            // int unCompleted = lastMoveTarget[1] - currentLocation[1];
            // int toYWithLast = mGroupTrack.get(nextPositionInTrack)[1] +
            // unCompleted;

            int fromY = mGroupTrack.get(positionInTrack)[1];// currentLocation[1];
            int toY = mGroupTrack.get(nextPositionInTrack)[1];
            int delay = 10 * (count - 1 - i);
            int duration = mMenuSubSlideTime;

            AnimatorSet set = new AnimatorSet();
            ObjectAnimator yAnim = ObjectAnimator.ofFloat(group, "y", fromY, toY);
            if (nextPositionInTrack == mGroupAlphaDividerSelect) {
                float beginAlpha = 0.0f;
                float endAlpha = 1.0f;
                ObjectAnimator yAlpha = ObjectAnimator.ofFloat(group, "alpha", beginAlpha, endAlpha);
                set.playTogether(yAnim, yAlpha);
            } else {
                set.play(yAnim);
            }
            set.setDuration(duration);
            set.setStartDelay(delay);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(group.getSlideListener());
            set.start();
            group.setPositionInTrack(nextPositionInTrack);
        }
    }

    private void itemSlideUp(ArrayList<ExMenuItem> items) {
        JLog.d(TAG, "itemSlideUp");
        int count = items.size();
        int[] currentLocation = new int[2];
        for (int i = 0; i < count; i++) {
            ExMenuItem item = items.get(i);
            int arrowMinIndex = i;
            if (item.getPositionInTrack() <= arrowMinIndex) {
                return;
            }
            item.clearAnimation();
            item.getLocationInWindow(currentLocation);
            int positionInTrack = item.getPositionInTrack();
            int nextPositionInTrack = item.getPositionInTrack() - 1;

            // int[] lastMoveTarget = mItemsTrack.get(positionInTrack);
            // int unCompleted = currentLocation[1] - lastMoveTarget[1];
            // int toYWithLast = mItemsTrack.get(nextPositionInTrack)[1] -
            // unCompleted;
            int fromY = mItemsTrack.get(positionInTrack)[1];// currentLocation[1];
            int toY = mItemsTrack.get(nextPositionInTrack)[1];
            int delay = 10 * i;
            int duration = mMenuSubSlideTime;

            AnimatorSet set = new AnimatorSet();
            ObjectAnimator yAnim = ObjectAnimator.ofFloat(item, "y", fromY, toY);
            set.play(yAnim);
            set.setDuration(duration);
            set.setStartDelay(delay);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(item.getSlideListener());
            set.start();
            item.setPositionInTrack(nextPositionInTrack);
        }
    }

    private void itemSlideDown(ArrayList<ExMenuItem> items) {
        JLog.d(TAG, "itemSlideDown");
        int count = items.size();
        int[] currentLocation = new int[2];
        for (int i = count - 1; i >= 0; i--) {
            ExMenuItem item = items.get(i);
            int arrowMaxIndex = i + mItemSelectInTrack;
            if (item.getPositionInTrack() >= arrowMaxIndex) {
                return;
            }
            item.clearAnimation();
            item.getLocationInWindow(currentLocation);
            int positionInTrack = item.getPositionInTrack();
            int nextPositionInTrack = item.getPositionInTrack() + 1;

            // int[] lastMoveTarget = mItemsTrack.get(positionInTrack);
            // int unCompleted = lastMoveTarget[1] - currentLocation[1];
            // int toYWithLast = mItemsTrack.get(nextPositionInTrack)[1] +
            // unCompleted;

            int fromY = mItemsTrack.get(positionInTrack)[1];// currentLocation[1];
            int toY = mItemsTrack.get(nextPositionInTrack)[1];
            int delay = 10 * (count - 1 - i);
            int duration = mMenuSubSlideTime;

            AnimatorSet set = new AnimatorSet();
            ObjectAnimator yAnim = ObjectAnimator.ofFloat(item, "y", fromY, toY);
            set.play(yAnim);
            set.setDuration(duration);
            set.setStartDelay(delay);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(item.getSlideListener());
            set.start();
            item.setPositionInTrack(nextPositionInTrack);
        }
    }

    private void unfoldGroups(ArrayList<ExMenuGroup> groups) {
        isGroupUnfold = true;

        final int count = groups.size();
        for (int i = 0; i < count; i++) {
            final ExMenuGroup group = groups.get(i);
            int[] currentPosition = mGroupTrack.get(group.getPositionInTrack());
            final int unfoldFrom = mGroupSelectViewLocation[1];
            final int unfoldTo = currentPosition[1];
            if (unfoldFrom == unfoldTo && mOnMenuListener != null) {
                mOnMenuListener.onGroupExpand(group);
            }
            boolean visibility = group.getPositionInTrack() >= mGroupAlphaDividerSelect;
            AnimatorSet set = buildFoldAnimatorSet(group, unfoldFrom, unfoldTo, visibility);

            final int index = i;
            set.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (unfoldFrom == unfoldTo) {
                        group.setNameVisibility(View.VISIBLE);
                        group.onFocusChanged(true);
                        mItemsSelectView.setVisibility(View.GONE);
                        mGroupSelectView.setVisibility(View.VISIBLE);
                        mGroupSelectView.requestFocus();
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // 恢复下划线
                    group.setDividerVisibility(index == count - 1 ? View.INVISIBLE : View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            set.start();
        }
    }

    private void foldGroups(ArrayList<ExMenuGroup> groups) {
        JLog.d(TAG, "foldGroups");
        isGroupUnfold = false;
        mTitleAnimatorOut.start();

        int count = groups.size();

        for (int i = 0; i < count; i++) {
            final ExMenuGroup group = groups.get(i);
            int[] currentPosition = mGroupTrack.get(group.getPositionInTrack());
            // create animation
            final int foldFrom = currentPosition[1];
            final int foldTo = mGroupSelectViewLocation[1];
            if (foldFrom == foldTo && mOnMenuListener != null) {
                mOnMenuListener.onGroupCollapsed(group);
            }
            boolean visibility = group.getPositionInTrack() == mGroupSelectInTrack;
            AnimatorSet set = buildFoldAnimatorSet(group, foldFrom, foldTo, visibility);

            set.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // 动画开始，隐藏下划线
                    mItemLayout.removeAllViews();
                    group.setDividerVisibility(View.INVISIBLE);
                    group.onFocusChanged(false);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (foldFrom == foldTo) {
                        group.setNameVisibility(View.INVISIBLE);
                        mGroupSelectView.setVisibility(View.GONE);
                        mItemsSelectView.setVisibility(View.VISIBLE);
                        mItemsSelectView.requestFocus();
                        if (!isItemsShowing) {
                            isItemsShowing = true;
                            mItemsRoot.setVisibility(View.VISIBLE);
                            mItemsPanelAnimatorIn.start();
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    performItems(mSelectGroup);
                                }
                            }, 200);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            set.start();
            // Log.d(TAG, "expand " + foldFrom + "-->" + foldTo);
        }
    }

    private void unfolditems(ArrayList<ExMenuItem> items) {
        isItemsShowing = true;
        final int count = items.size();
        for (int i = 0; i < count; i++) {
            final ExMenuItem item = items.get(i);
            int[] currentPosition = mItemsTrack.get(item.getPositionInTrack());
            final int expandBegin = mGroupSelectViewLocation[1];
            final int expandEnd = currentPosition[1];
            AnimatorSet set = buildFoldAnimatorSet(item, expandBegin, expandEnd, true);
            final int index = i;
            set.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mBackArrowAnimatorIn.start();
                    item.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    item.setDividerVisibility(index == count - 1 ? View.INVISIBLE : View.VISIBLE);
                    if (expandBegin == expandEnd) {
                        mSelectItem.onFocusChanged(true);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }
            });
            set.start();
        }
    }

    private void foldItems(ArrayList<ExMenuItem> items) {
        isItemsShowing = false;
        int count = items.size();
        for (int i = 0; i < count; i++) {
            final ExMenuItem item = items.get(i);
            int[] currentPosition = mItemsTrack.get(item.getPositionInTrack());
            final int expandBegin = currentPosition[1];
            final int expandEnd = mGroupSelectViewLocation[1];
            boolean visibility = false;
            if (item.getPositionInTrack() == mItemSelectInTrack) {
                visibility = true;
            }
            AnimatorSet set = buildFoldAnimatorSet(item, expandBegin, expandEnd, visibility);
            set.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    item.setDividerVisibility(View.INVISIBLE);
                    mBackArrowAnimatorOut.start();
                    mTitleAnimatorIn.start();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (expandBegin == expandEnd) {
                        mItemsPanelAnimatorOut.start();
                    } else {
                        item.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            set.start();
        }
    }

    public void hide() {
        JLog.d(TAG, "hide");
        if (!isAttach2Window) {
            throw new IllegalArgumentException("menu is not attach 2 window");
        }
        if (isGroupShowing) {
            isGroupShowing = false;
            mMenuPanelAnimatorOut.start();
        }
    }

    public boolean isShowing() {
        return isGroupShowing;
    }

    public void registerMenuControl(OnMenuListener l) {
        mOnMenuListener = l;
    }

    private void resetMenuLastStatus() {
        mGroupSelectView.requestFocus();
        if (isResetable && hasLastOpenStatus) {
            resetDefaultGroupSelect();
        }
    }

    public void show() {
        JLog.d(TAG, "show");
        if (!isAttach2Window) {
            throw new IllegalArgumentException("menu is not attach 2 window");
        }
        if (!isGroupShowing) {
            isGroupShowing = true;
            mLastFocusView = mActivity.getWindow().getCurrentFocus();
            if (getParent() == null) {
                mDecorView.addView(this);
            }
            resetMenuLastStatus();
            mMenuPanelAnimatorIn.start();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handler = false;
        if (isGroupShowing) {
            int action = event.getAction();
            int keyCode = event.getKeyCode();
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (isGroupShowing && !isItemsShowing && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && isLeftRightControlable) {
                        if (mSelectGroup.isHasSub()) {
                            foldGroups(mGroupViews);
                        } else {
                            if (mOnMenuListener != null) {
                                mOnMenuListener.onItemSubClick(mSelectGroup);
                            }
                        }
                    }
                    if (isGroupShowing && isItemsShowing && keyCode == KeyEvent.KEYCODE_DPAD_LEFT && isLeftRightControlable) {
                        foldItems(mSelectGroup.getSubMenuList());
                    } /*
                       * else if (isGroupShowing && keyCode ==
                       * KeyEvent.KEYCODE_DPAD_LEFT && isCancleable) { hide(); }
                       */
                    handler = true;
                }
            } else {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    JLog.d(TAG, "onkey back  isGroupShowing=" + isGroupShowing + "  isItemsShowing=" + isItemsShowing);
                    if (isGroupShowing && isItemsShowing) {
                        foldItems(mSelectGroup.getSubMenuList());
                        handler = true;
                    } else if (isGroupShowing && !isItemsShowing && isCancleable) {
                        hide();
                        handler = true;
                    }
                }
            }
        }
        return handler ? handler : super.dispatchKeyEvent(event);
    }

    @Override
    public void onGroupSelect(ExMenuGroup group) {
        JLog.d(TAG, "onGroupSelect = " + group.getSubId());
        mSelectGroup = group;
    }

    @Override
    public void onItemSelect(ExMenuItem item) {
        JLog.d(TAG, "onItemSelect = " + item.getSubId());
        mSelectItem = item;
    }
}
