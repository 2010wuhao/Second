package com.joysee.dvb.vod;

import java.lang.reflect.Field;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import android.widget.OverScroller;

public class VodScrollView extends HorizontalScrollView {

	public VodScrollView(Context context) {
		super(context);
		init(context);
	}

	public VodScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public VodScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private OverScroller scroller;
	
	private void init(Context context){
		
		setSmoothScrollingEnabled(true);
		
		Field mScroller;
        try {
			mScroller = (Field) HorizontalScrollView.class.getDeclaredField("mScroller");
			mScroller.setAccessible(true);
			scroller = new OverScroller(context);
			mScroller.set(this, scroller);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isScrollingFinish(){
		if(scroller!=null){
			return scroller.isFinished();
		}
		return true;
	}
}
