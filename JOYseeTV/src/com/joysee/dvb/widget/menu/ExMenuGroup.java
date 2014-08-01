/**
 * =====================================================================
 *
 * @file  ExMenuGroup.java
 * @Module Name   com.joysee.dvb.widget.menu
 * @author benz
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
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;

import java.util.ArrayList;

public class ExMenuGroup extends ExMenuSub {

    protected interface OnSelectListener {
        public void onGroupSelect(ExMenuGroup group);
    }

    private static final String TAG = JLog.makeTag(ExMenuGroup.class);
    private int mIconNormal;
    private int mIconSelected;
    private int mSelectItemId;
    private boolean mHasSub = false;
    private ArrayList<ExMenuItem> mSubItems;

    private TextView mNameTv;
    private ImageView mIconIv;
    private View mDivider;
    private int mPositionInTrack;
    private int mPositionInGroups;
    private int mSelectViewInTrack;
    private SlideListener mSlideListener;
    private OnSelectListener mSelectListener;

    public ExMenuGroup(Context context, int nameId, int iconNormal, int iconSelected) {
        this(context, nameId, context.getString(nameId), iconNormal, iconSelected);
    }

    private ExMenuGroup(Context context, int id, String name, int iconNormal, int iconSelected) {
        super(context, id, name);
        this.mIconNormal = iconNormal;
        this.mIconSelected = iconSelected;
        this.mSubItems = new ArrayList<ExMenuItem>();
        performGroup();
    }

    private void performGroup() {
        LayoutInflater.from(mContext).inflate(R.layout.exmenu_group, this);
        mSlideListener = new SlideListener();
        mNameTv = (TextView) findViewById(R.id.name);
        mIconIv = (ImageView) findViewById(R.id.icon);
        mDivider = findViewById(R.id.divider);

        mNameTv.setText(mName);
        if (mIconNormal != -1) {
            mIconIv.setImageResource(mIconNormal);
        } else {
            RelativeLayout iconParent = (RelativeLayout) mIconIv.getParent();
            iconParent.removeView(mIconIv);
            iconParent.setGravity(Gravity.CENTER);
        }
    }

    public void setSelection(int position) {
        mSelectItemId = position < 0 ? 0 : position >= mSubItems.size() ? mSubItems.size() - 1 : position;
    }

    public void setSelectionById(int id) {
        if (mSubItems != null) {
            for (int i = 0; i < mSubItems.size(); i++) {
                if (id == mSubItems.get(i).getSubId()) {
                    setSelection(i);
                    break;
                }
            }
        }
    }

    public void setSelectionByName(String name) {
        if (mSubItems != null) {
            for (int i = 0; i < mSubItems.size(); i++) {
                if (mSubItems.get(i).getSubName().equals(name)) {
                    setSelection(i);
                    break;
                }
            }
        }
    }

    public void setSelectViewInTrack(int position) {
        this.mSelectViewInTrack = position;
    }

    public void addSubMenu(ExMenuItem item) {
        if (!this.mSubItems.contains(item)) {
            this.mSubItems.add(item);
        }
        mHasSub = true;
    }

    public void clearItems() {
        mHasSub = false;
        mSubItems.clear();
    }

    public void setDividerVisibility(int visibility) {
        this.mDivider.setVisibility(visibility);
    }

    public void setNameVisibility(int visibility) {
        this.mNameTv.setVisibility(visibility);
    }

    public void setPositionInTrack(int position) {
        this.mPositionInTrack = position;
    }

    public void setPositionInGroups(int position) {
        this.mPositionInGroups = position;
    }

    @Override
    public void onFocusChanged(boolean gainFocus) {
        if (mIconSelected != -1 && mIconNormal != -1) {
            mIconIv.setImageResource(gainFocus ? mIconSelected : mIconNormal);
        }
        mNameTv.setAlpha(gainFocus ? 1.0f : 0.7f);
    }

    protected void addLinstener(OnSelectListener l) {
        mSelectListener = l;
    }

    public boolean isHasSub() {
        return this.mHasSub;
    }

    public ExMenuItem getSubMenu(int subMenuId) {
        if (subMenuId < mSubItems.size() - 1) {
            return mSubItems.get(subMenuId);
        }
        return null;
    }

    public ArrayList<ExMenuItem> getSubMenuList() {
        return this.mSubItems;
    }

    public int getPositionInTrack() {
        return this.mPositionInTrack;
    }

    public int getPositionInGroups() {
        return this.mPositionInGroups;
    }

    public int getSelectSub() {
        return this.mSelectItemId;
    }

    public Animator.AnimatorListener getSlideListener() {
        return mSlideListener;
    }

    public class SlideListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mPositionInTrack == mSelectViewInTrack) {
                onFocusChanged(true);
                Log.d(TAG, "onSelect " + ExMenuGroup.this.mName);
                if (mSelectListener != null) {
                    mSelectListener.onGroupSelect(ExMenuGroup.this);
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        @Override
        public void onAnimationStart(Animator animation) {
            onFocusChanged(false);
        }

    }

}
