/**
 * =====================================================================
 *
 * @file  ExMenuItem.java
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.joysee.dvb.R;

public class ExMenuItem extends ExMenuSub {

    public interface OnSelectListener {
        public void onItemSelect(ExMenuItem item);
    }

    private int mIconNormal;
    private int mIconSelected;
    private boolean isCheckBox;

    private int mPositionInTrack;
    private int mPositionInGroups;
    private int mSelectViewInTrack;

    private TextView mNameTv;
    private ImageView mIconIv;
    private ImageView mCustomCheckBox;
    private View mDivider;
    private ExMenuGroup mParent;
    private SlideListener mSlideListener;
    private OnSelectListener mSelectListener;

    public ExMenuItem(Context context, int nameId, boolean isCheckBox, int iconNormal, int iconSelected) {
        this(context, nameId, context.getString(nameId), isCheckBox, iconNormal, iconSelected);
    }

    private ExMenuItem(Context context, int id, String name, boolean isCheckBox, int iconNormal, int iconSelected) {
        super(context, id, name);
        this.isCheckBox = isCheckBox;
        this.mIconNormal = iconNormal;
        this.mIconSelected = iconSelected;
        performItem();
    }

    private void performItem() {
        LayoutInflater.from(mContext).inflate(R.layout.exmenu_item, this);
        mSlideListener = new SlideListener();
        mNameTv = (TextView) findViewById(R.id.name);
        mIconIv = (ImageView) findViewById(R.id.icon);
        mCustomCheckBox = (ImageView) findViewById(R.id.custom_checkbox);
        mDivider = findViewById(R.id.divider);
        mNameTv.setText(mName);
        if (!isCheckBox) {
            mIconIv.setVisibility(View.VISIBLE);
            mIconIv.setImageResource(mIconNormal);
        } else {
            mCustomCheckBox.setVisibility(View.VISIBLE);
        }
    }

    public void addLinstener(OnSelectListener l) {
        this.mSelectListener = l;
    }

    public void setPositionInTrack(int position) {
        this.mPositionInTrack = position;
    }

    public void setPositionInGroups(int position) {
        this.mPositionInGroups = position;
    }

    public void setMenuParent(ExMenuGroup group) {
        this.mParent = group;
    }

    public void setSelectViewInTrack(int position) {
        this.mSelectViewInTrack = position;
    }

    public void setDividerVisibility(int visibility) {
        this.mDivider.setVisibility(visibility);
    }

    @Override
    public void onFocusChanged(boolean gainFocus) {
        if (!isCheckBox) {
            mIconIv.setImageResource(gainFocus ? mIconSelected : mIconNormal);
        }
        mNameTv.setAlpha(gainFocus ? 1.0f : 0.7f);
    }

    public void setChecked(boolean isChecked) {
        if (isCheckBox) {
            mCustomCheckBox.setImageResource(isChecked ? mIconSelected : mIconNormal);
        }
    }

    public int getPositionInTrack() {
        return this.mPositionInTrack;
    }

    public int getPositionInGroups() {
        return this.mPositionInGroups;
    }

    public ExMenuGroup getMenuParent() {
        return this.mParent;
    }

    public boolean isCheckBox() {
        return isCheckBox;
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
                if (mSelectListener != null) {
                    mSelectListener.onItemSelect(ExMenuItem.this);
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
