/**
 * =====================================================================
 *
 * @file  JTextViewWithTTF.java
 * @Module Name   com.joysee.common.widget
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年10月28日
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
 * YueLiang          2013年10月28日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.joysee.common.R;

public class JTextViewWithTTF extends TextView {

    private Context mContext;
    protected static final JTypeFaceMgr SFontMgr = new JTypeFaceMgr();

    public JTextViewWithTTF(Context context, AttributeSet attr) {
		this(context, attr, 0);
    }
    
    

    public JTextViewWithTTF(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		TypedArray localTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.JTextViewWithTTF);
		setTypeface(SFontMgr.getTypeface(mContext,
				localTypedArray.getString(R.styleable.JTextViewWithTTF_ttf)));
		localTypedArray.recycle();
	}



	public JTextViewWithTTF(Context context, String s) {
        super(context);
        mContext = context;
        setTypeface(SFontMgr.getTypeface(mContext, s));
    }

    public void draw(Canvas c) {
        super.draw(c);
    }

    public void setTypeface(Typeface typeface) {
        super.setTypeface(typeface);
    }

}
