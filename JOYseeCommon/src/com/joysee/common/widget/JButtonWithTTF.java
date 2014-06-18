/**
 * =====================================================================
 *
 * @file  JButtonWithTTF.java
 * @Module Name   com.joysee.common.widget
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-20
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
 * YueLiang          2014-1-20           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.common.widget;

import com.joysee.common.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;

public class JButtonWithTTF extends JTextViewWithTTF {

	private Context mContext;
    protected static final JTypeFaceMgr SFontMgr = new JTypeFaceMgr();
    private String mFocusTypeface;
    private String mNomalTypeface;
	
	public JButtonWithTTF(Context context, String s) {
		this(context, null, android.R.attr.buttonStyle);
		setTypeface(SFontMgr.getTypeface(context, s));
	}

	public JButtonWithTTF(Context context, AttributeSet attr) {
		super(context, attr, android.R.attr.buttonStyle);
		mContext = context;
		TypedArray localTypedArray = context.obtainStyledAttributes(attr, R.styleable.JTextViewWithTTF);
		mFocusTypeface = localTypedArray.getString(R.styleable.JTextViewWithTTF_ttf_focus);
		mNomalTypeface = localTypedArray.getString(R.styleable.JTextViewWithTTF_ttf);
		localTypedArray.recycle();
	}

	public JButtonWithTTF(Context context, AttributeSet attr, int style) {
		super(context, attr, style);
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if(mFocusTypeface==null || mNomalTypeface==null){
			return;
		}
		if(focused) {
			setTypeface(SFontMgr.getTypeface(mContext, mFocusTypeface));
		}else {
			setTypeface(SFontMgr.getTypeface(mContext, mNomalTypeface));
		}
	}
}
