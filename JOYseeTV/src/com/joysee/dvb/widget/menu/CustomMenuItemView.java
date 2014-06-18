/**
 * =====================================================================
 *
 * @file  CustomMenuItemView.java
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.dvb.R;

public class CustomMenuItemView extends RelativeLayout {

    private String mTitle;
    private Bitmap mIcon;

    private TextView mTitleTv;
    private ImageView mIconIv;

    public CustomMenuItemView(Context context) {
        this(context, null);
    }

    public CustomMenuItemView(Context context, int title) {
        this(context, context.getResources().getString(title));
    }

    public CustomMenuItemView(Context context, String title) {
        this(context, title, null);
    }

    public CustomMenuItemView(Context context, String title, Bitmap icon) {
        super(context);
        mTitle = title;
        mIcon = icon;
        initView(context);
    }

    public CustomMenuItemView(Context context, String title, int icon) {
        this(context, title, BitmapFactory.decodeResource(context.getResources(), icon));
    }

    public TextView getTitleView() {
        return mTitleTv;
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_menu_item, this);
        mTitleTv = (TextView) findViewById(R.id.title);
        mIconIv = (ImageView) findViewById(R.id.icon);

        if (mIcon != null && !mIcon.isRecycled()) {
            mIconIv.setVisibility(View.VISIBLE);
            mIconIv.setImageBitmap(mIcon);
        }

        mTitleTv.setText(mTitle);
    }

    public void setIcon(Bitmap icon) {
        mIconIv.setVisibility(View.VISIBLE);
        mIconIv.setImageBitmap(icon);
    }

    public void setIcon(int icon) {
        mIconIv.setVisibility(View.VISIBLE);
        mIconIv.setImageResource(icon);
    }

    public void setTitle(int title) {
        mTitleTv.setText(title);
    }

    public void setTitle(String title) {
        mTitle = title;
        mTitleTv.setText(title);
    }

    @Override
    public String toString() {
        return "CustomMenuItemView [mTitle=" + mTitle + "]";
    }
}
