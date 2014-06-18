/**
 * =====================================================================
 *
 * @file  JEditTextWithTTF.java
 * @Module Name   com.joysee.common.widget
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-21
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
 * benz          2014-1-21           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.common.widget;

import com.joysee.common.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

public class JEditTextWithTTF extends EditText {

	private Context mContext;
	protected static final JTypeFaceMgr SFontMgr = new JTypeFaceMgr();

	public JEditTextWithTTF(Context context) {
		this(context, null);
	}

	public JEditTextWithTTF(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public JEditTextWithTTF(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		TypedArray localTypedArray = context.obtainStyledAttributes(attrs, R.styleable.JTextViewWithTTF);
		setTypeface(SFontMgr.getTypeface(mContext, localTypedArray.getString(R.styleable.JTextViewWithTTF_ttf)));
		localTypedArray.recycle();
	}

}
