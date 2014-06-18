/**
 * =====================================================================
 *
 * @file  ProgramTypeCategoryItem.java
 * @Module Name   com.joysee.dvb.liveguide
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月16日
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
 * yueliang         2014年2月16日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.liveguide;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.ProgramType;

import java.util.HashMap;

public class ProgramTypeCategoryItem extends JTextViewWithTTF {
    public interface OnSelectedListener {
        void onSelected(ProgramTypeCategoryItem type);
    }

    private static final String TAG = JLog.makeTag(ProgramTypeCategoryItem.class);

    private static Typeface getTypeface(Context context, String paramString) {
        Typeface localObject = null;
        if (mTypefaces.containsKey(paramString)) {
            localObject = mTypefaces.get(paramString);
        }

        if (localObject == null) {
            try {
                Typeface localTypeface = Typeface.createFromAsset(context.getAssets(),
                        "fonts/" + paramString);
                mTypefaces.put(paramString, localTypeface);
                localObject = localTypeface;
            } catch (Exception localException) {
                localException.printStackTrace();
                localObject = null;
            }
        }
        return localObject;
    }

    public ProgramType mType;
    static HashMap<String, Typeface> mTypefaces = new HashMap<String, Typeface>();

    private OnSelectedListener mSelectedLis;

    private boolean mSelected = false;
    private int mNormalTextSize;
    private int mSelectedTextSize;

    public ProgramTypeCategoryItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mNormalTextSize = getResources().getDimensionPixelSize(R.dimen.liveguide_programtypecategory_item_textsize_normal);
        mSelectedTextSize = getResources().getDimensionPixelSize(R.dimen.liveguide_programtypecategory_item_textsize_selected);
    }

    public void clear() {
        setSelectedEx(false);
        mType = null;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            // setTextColor(Color.WHITE);
        } else {
            if (!mSelected) {
                setTextColor(getResources().getColor(R.color.white_alpha_50));
            }
        }
    }

    public void setOnSelectedLis(OnSelectedListener lis) {
        this.mSelectedLis = lis;
    }

    public void setSelectedEx(boolean select) {
        if (mSelected != select) {
            JLog.d(TAG, "setSelectedEx item = " + this + " select = " + select);
            mSelected = select;
            if (mSelected) {
                if (mSelectedLis != null) {
                    mSelectedLis.onSelected(this);
                }
                setTextSize(TypedValue.COMPLEX_UNIT_PX, mSelectedTextSize);
                setTextColor(Color.WHITE);
                setTypeface(getTypeface(getContext(), "fzltch.TTF"));
            } else {
                
                setTextSize(TypedValue.COMPLEX_UNIT_PX, mNormalTextSize);
                setTextColor(getResources().getColor(R.color.white_alpha_50));
                setTypeface(getTypeface(getContext(), "fzltzh.TTF"));
            }
        } else {
            JLog.d(TAG, "invalid  setSelectedEx item = " + this + " select = " + select);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProgramTypeCategoryItem.class.getSimpleName());
        sb.append("[ type = ");
        sb.append(mType);
        sb.append(" ]");
        return sb.toString();
    }

    public void updateWithData(ProgramTypeCategoryData data, int dataIndex) {
        if (dataIndex >= 0) {
            mType = data.mCategorys.get(dataIndex);
            setText(mType.getTypeName());
            if (dataIndex == data.mCurrentPos) {
                setSelectedEx(true);
            }
        }
    }
}
