/**
 * =====================================================================
 *
 * @file  SearchEditNumText.java
 * @Module Name   com.joysee.dvb.search
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

package com.joysee.dvb.search;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JEditTextWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;

public class SearchEditNumText extends JEditTextWithTTF {

    private int mMinValue;
    private int mMaxValue;
    private String mDefualtValue;
    private InputMethodManager mImm;
    private boolean isHasSoftKey;

    public SearchEditNumText(Context context) {
        this(context, null);
    }

    public SearchEditNumText(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public SearchEditNumText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray localTypedArray = context.obtainStyledAttributes(attrs, R.styleable.JEditNumText);
        if (localTypedArray != null) {
            mMinValue = localTypedArray.getInt(R.styleable.JEditNumText_min, 0);
            mMaxValue = localTypedArray.getInt(R.styleable.JEditNumText_max, 9999);
        }
        localTypedArray.recycle();
        mImm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private boolean checkFormat() {
        boolean ret = true;
        String text = getText().toString().trim();
        if (text == null || "".equals(text)) {
            ret = false;
        } else {
            int value = Integer.parseInt(text);
            if (value < mMinValue || value > mMaxValue) {
                ret = false;
            }
        }
        return ret;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

//    @Override
//    public boolean dispatchKeyEventPreIme(KeyEvent event) {
//        int keyCode = event.getKeyCode();
//        int action = event.getAction();
//
//        if (action == KeyEvent.ACTION_DOWN) {
//            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
//                if (!isHasSoftKey) {
//                    isHasSoftKey = true;
//                    setInputType(InputType.TYPE_CLASS_NUMBER);
//                    mImm.showSoftInput(this, 0);
//                    setCursorVisible(true);
//                    setSelectToLastChar();
//                    return true;
//                }
//            }
//        }
//
//        if (action == KeyEvent.ACTION_UP) {
//            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
//                if (isHasSoftKey) {
//                    isHasSoftKey = false;
//                    mImm.hideSoftInputFromWindow(getWindowToken(), 0);
//                    setCursorVisible(false);
//                    setInputType(InputType.TYPE_NULL);
//                    return true;
//                }
//            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
//                // 弹出键盘时，不响应软键盘上的回车键
//                if (isHasSoftKey && keyCode == KeyEvent.KEYCODE_ENTER && TvApplication.sDestPlatform != DestPlatform.MITV_QCOM) {
//                    JLog.d("joysee", "return keySoft enter key");
//                    return true;
//                }
//            }
//        }
//        return super.dispatchKeyEventPreIme(event);
//    }

    private void notifyErrorInput() {
        Toast.makeText(getContext(), "参数错误， 输入应在 [" + mMinValue + "，" + mMaxValue+"] 之间", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused) {
            if (!checkFormat()) {
                this.setText(mDefualtValue);
                notifyErrorInput();
            } else {
                mDefualtValue = this.getText().toString();
            }
        }
    }

    public void setRange(int min, int max) {
        mMinValue = min;
        mMaxValue = max;
    }

    private void setSelectToLastChar() {
        if (getText().toString().trim() != null && !"".equals(getText().toString().trim())) {
            setSelection(getText().toString().trim().length());
        }
    }

    public void setTextNumber(int num) {
        mDefualtValue = num + "";
        this.setText(mDefualtValue);
    }

}
