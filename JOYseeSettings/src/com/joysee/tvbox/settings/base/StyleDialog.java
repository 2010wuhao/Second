/**
 * =====================================================================
 *
 * @file  StyleDialog.java
 * @Module Name   com.joysee.dvb.widget
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-2-25
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
 * benz          2014-2-25           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.common.widget.JStyleDialog;
import com.joysee.tvbox.settings.R;

public class StyleDialog extends JStyleDialog {

    public static class Builder {
        private Context mCtx;
        private CharSequence mTitle;
        private CharSequence mDefaultContentMessage;
        private CharSequence mDefaultContentPoint;
        private CharSequence mPositiveButtonText;
        private CharSequence mNegativeButtonText;
        private DialogInterface.OnClickListener mButtonListener;
        private DialogInterface.OnKeyListener mOnKeyListener;

        private View mContentView;
        private RelativeLayout.LayoutParams mContentViewLayoutParams;
        private boolean mContentCanFocus;
        private View mDefaultFocusView;

        private View mBlurView;
        private int mBlurLevel;
        private int mScreenW;
        private int mScreenH;

        private int mTheme;

        public Builder(Context c) {
            this(c, R.style.JStyleDialog_DVB);
        }

        public Builder(Context c, int theme) {
            mCtx = c;
            mTheme = theme;
            fetchDisplayParams();
        }

        public StyleDialog create() {
            final StyleDialog dialog = new StyleDialog(mCtx, mTheme);
            LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View root = inflater.inflate(R.layout.dialog_layout, null);

            if (mTitle != null) {
                TextView tv = (TextView) root.findViewById(R.id.dialog_title);
                tv.setText(mTitle);
                root.findViewById(R.id.dialog_title_line).setVisibility(View.VISIBLE);
                tv.setVisibility(View.VISIBLE);
            }

            if (mContentView == null) {
                if (mDefaultContentMessage != null) {
                    TextView messageTv = (TextView) root.findViewById(R.id.content_view_message);
                    messageTv.setText(mDefaultContentMessage);
                }
                if (mDefaultContentPoint != null) {
                    TextView messagePointTv = (TextView) root.findViewById(R.id.content_view_message_point);
                    messagePointTv.setText(mDefaultContentPoint);
                }
            } else {
                ViewGroup dialogContent = (ViewGroup) root.findViewById(R.id.dialog_center_view_root);
                dialogContent.removeAllViews();
                dialogContent.setFocusable(mContentCanFocus);
                if (mContentViewLayoutParams != null) {
                    dialogContent.addView(mContentView, mContentViewLayoutParams);
                } else {
                    dialogContent.addView(mContentView);
                }
            }

            if (mPositiveButtonText != null) {
                JButtonWithTTF p = (JButtonWithTTF) root.findViewById(R.id.dialog_bt_yes);
                p.setText(mPositiveButtonText);
                if (mNegativeButtonText == null) {
                    p.setBackgroundResource(R.drawable.dialog_center_bt_default);
                    root.findViewById(R.id.dialog_bt_no).setVisibility(View.GONE);
                    root.findViewById(R.id.dialog_button_dvider).setVisibility(View.GONE);
                }
                p.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mButtonListener != null) {
                            mButtonListener.onClick(dialog, BUTTON_POSITIVE);
                        }
                    }
                });
                if (mDefaultFocusView == null) {
                    mDefaultFocusView = p;
                }
            }
            if (mNegativeButtonText != null) {
                JButtonWithTTF n = (JButtonWithTTF) root.findViewById(R.id.dialog_bt_no);
                n.setText(mNegativeButtonText);
                if (mPositiveButtonText == null) {
                    n.setBackgroundResource(R.drawable.dialog_center_bt_default);
                    root.findViewById(R.id.dialog_bt_yes).setVisibility(View.GONE);
                    root.findViewById(R.id.dialog_button_dvider).setVisibility(View.GONE);
                }
                n.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mButtonListener != null) {
                            mButtonListener.onClick(dialog, BUTTON_NEGATIVE);
                        }
                    }
                });
            }

            if (mOnKeyListener != null) {
                dialog.setOnKeyListener(mOnKeyListener);
            }

            dialog.setBlurView(mBlurView, mBlurLevel);
            dialog.setAnimationPanel(R.id.dialog_layout, 300);
            dialog.setContentView(root);
            return dialog;
        }

        private void fetchDisplayParams() {
            mScreenW = (int) mCtx.getResources().getDimension(R.dimen.screen_width);
            mScreenH = (int) mCtx.getResources().getDimension(R.dimen.screen_height);
        }

        private void fetchFocusOnView() {
            if (mDefaultFocusView != null) {
                mDefaultFocusView.requestFocus();
            }
        }

        protected boolean isHasDefaultContentMessage() {
            return mDefaultContentMessage != null ? true : false;
        }

        protected boolean isHasDefaultContentPoint() {
            return mDefaultContentPoint != null ? true : false;
        }

        protected boolean isHasDefaultContentView() {
            return mContentView != null ? true : false;
        }

        protected boolean isHasNegasitionButton() {
            return mNegativeButtonText != null ? true : false;
        }

        protected boolean isHasPositionButton() {
            return mPositiveButtonText != null ? true : false;
        }

        protected boolean isHasTitle() {
            return mTitle != null ? true : false;
        }

        public void reset(final StyleDialog root) {
            if (mTitle != null) {
                TextView tv = (TextView) root.findViewById(R.id.dialog_title);
                tv.setText(mTitle);
                root.findViewById(R.id.dialog_title_line).setVisibility(View.VISIBLE);
                tv.setVisibility(View.VISIBLE);
            }

            if (mContentView == null) {
                if (mDefaultContentMessage != null) {
                    TextView messageTv = (TextView) root.findViewById(R.id.content_view_message);
                    messageTv.setText(mDefaultContentMessage);
                }
                if (mDefaultContentPoint != null) {
                    TextView messagePointTv = (TextView) root.findViewById(R.id.content_view_message_point);
                    messagePointTv.setText(mDefaultContentPoint);
                }
            }

            if (mPositiveButtonText != null) {
                JButtonWithTTF p = (JButtonWithTTF) root.findViewById(R.id.dialog_bt_yes);
                p.setVisibility(View.VISIBLE);
                p.setText(mPositiveButtonText);
                if (mNegativeButtonText == null) {
                    p.setBackgroundResource(R.drawable.dialog_center_bt_default);
                    root.findViewById(R.id.dialog_bt_no).setVisibility(View.GONE);
                    root.findViewById(R.id.dialog_button_dvider).setVisibility(View.GONE);
                } else {
                    p.setBackgroundResource(R.drawable.dialog_right_bt_default);
                }
                p.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mButtonListener != null) {
                            mButtonListener.onClick(root, BUTTON_POSITIVE);
                        }
                    }
                });
                mDefaultFocusView = p;
            }
            if (mNegativeButtonText != null) {
                JButtonWithTTF n = (JButtonWithTTF) root.findViewById(R.id.dialog_bt_no);
                n.setVisibility(View.VISIBLE);
                n.setText(mNegativeButtonText);
                if (mPositiveButtonText == null) {
                    n.setBackgroundResource(R.drawable.dialog_center_bt_default);
                    root.findViewById(R.id.dialog_bt_yes).setVisibility(View.GONE);
                    root.findViewById(R.id.dialog_button_dvider).setVisibility(View.GONE);
                } else {
                    n.setBackgroundResource(R.drawable.dialog_left_bt_default);
                }
                n.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mButtonListener != null) {
                            mButtonListener.onClick(root, BUTTON_NEGATIVE);
                        }
                    }
                });
            }
            if (mOnKeyListener != null) {
                root.setOnKeyListener(mOnKeyListener);
            }

            mDefaultFocusView.requestFocus();
        }

        public Builder setBlurView(View v) {
            this.setBlurView(v, 10);
            return this;
        }

        public Builder setBlurView(View v, int level) {
            this.mBlurView = v;
            this.mBlurLevel = level;
            return this;
        }

        public Builder setContentCanFocus(boolean canFocus) {
            this.mContentCanFocus = canFocus;
            return this;
        }

        public Builder setContentView(View v) {
            this.setContentView(v, null);
            return this;
        }

        public Builder setContentView(View v, RelativeLayout.LayoutParams l) {
            this.mContentView = v;
            this.mContentViewLayoutParams = l;
            return this;
        }

        public Builder setDefaultContentMessage(CharSequence text) {
            this.mDefaultContentMessage = text;
            return this;
        }

        public Builder setDefaultContentMessage(int textId) {
            this.mDefaultContentMessage = mCtx.getText(textId);
            return this;
        }

        public Builder setDefaultContentPoint(CharSequence text) {
            this.mDefaultContentPoint = text;
            return this;
        }

        public Builder setDefaultContentPoint(int textId) {
            this.mDefaultContentPoint = mCtx.getText(textId);
            return this;
        }

        public Builder setDefaultFocusView(View v) {
            this.mDefaultFocusView = v;
            return this;
        }

        public Builder setNegativeButton(CharSequence text) {
            this.mNegativeButtonText = text;
            return this;
        }

        public Builder setNegativeButton(int textId) {
            this.mNegativeButtonText = mCtx.getText(textId);
            return this;
        }

        public Builder setOnButtonClickListener(DialogInterface.OnClickListener l) {
            this.mButtonListener = l;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener l) {
            this.mOnKeyListener = l;
            return this;
        }

        public Builder setPositiveButton(CharSequence text) {
            this.mPositiveButtonText = text;
            return this;
        }

        public Builder setPositiveButton(int textId) {
            this.mPositiveButtonText = mCtx.getText(textId);
            return this;
        }

        public Builder setTitle(CharSequence text) {
            this.mTitle = text;
            return this;
        }

        public Builder setTitle(int titldId) {
            this.mTitle = this.mCtx.getText(titldId);
            return this;
        }

        public StyleDialog show() {
            StyleDialog dialog = create();
            dialog.getWindow().setLayout(mScreenW, mScreenH);
            dialog.show();
            fetchFocusOnView();
            return dialog;
        }

    }

    public StyleDialog(Context context) {
        super(context);
    }

    public StyleDialog(Context context, boolean cancelable, Message cancelCallback) {
        super(context, cancelable, cancelCallback);
    }

    public StyleDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public StyleDialog(Context context, int theme) {
        super(context, theme);
    }

    public void refreshContentView(Builder builder, View v) {
        if (builder != null && builder.isHasDefaultContentView()) {
            ViewGroup dialogContent = (ViewGroup) findViewById(R.id.dialog_center_view_root);
            dialogContent.removeAllViews();
            dialogContent.addView(v);
        }
    }

    public void refreshDefaultMessage(Builder builder, int textId) {
        if (builder != null && builder.isHasDefaultContentMessage()) {
            TextView tv = (TextView) findViewById(R.id.content_view_message);
            tv.setText(textId);
        }
    }

    public void refreshDefaultMessage(Builder builder, String text) {
        if (builder != null && builder.isHasDefaultContentMessage()) {
            TextView tv = (TextView) findViewById(R.id.content_view_message);
            tv.setText(text);
        }
    }

    public void refreshDefaultPoint(Builder builder, int textId) {
        if (builder != null && builder.isHasDefaultContentMessage()) {
            TextView tv = (TextView) findViewById(R.id.content_view_message_point);
            tv.setText(textId);
        }
    }

    public void refreshDefaultPoint(Builder builder, String text) {
        if (builder != null && builder.isHasDefaultContentMessage()) {
            TextView tv = (TextView) findViewById(R.id.content_view_message_point);
            tv.setText(text);
        }
    }

    public void refreshNegativeButtonText(Builder builder, int textId) {
        if (builder != null) {
            TextView tv = (TextView) findViewById(R.id.dialog_bt_no);
            tv.setVisibility(View.VISIBLE);
            tv.setText(textId);
        }
    }

    public void refreshNegativeButtonText(Builder builder, int textId, boolean only) {
        if (builder != null) {
            TextView tv = (TextView) findViewById(R.id.dialog_bt_no);
            tv.setVisibility(View.VISIBLE);
            tv.setText(textId);
            if (only) {
                findViewById(R.id.dialog_bt_yes).setVisibility(View.GONE);
            }
        }
    }

    public void refreshNegativeButtonText(Builder builder, String text) {
        if (builder != null) {
            TextView tv = (TextView) findViewById(R.id.dialog_bt_no);
            tv.setText(text);
        }
    }

    public void refreshNegativeButtonText(Builder builder, String text, boolean only) {
        if (builder != null && builder.isHasDefaultContentMessage()) {
            TextView tv = (TextView) findViewById(R.id.dialog_bt_no);
            tv.setText(text);
            if (only) {
                findViewById(R.id.dialog_bt_yes).setVisibility(View.GONE);
            }
        }
    }

    public void refreshPositiveButtonText(Builder builder, int textId) {
        if (builder != null) {
            TextView tv = (TextView) findViewById(R.id.dialog_bt_yes);
            tv.setVisibility(View.VISIBLE);
            tv.setText(textId);
        }
    }

    public void refreshPositiveButtonText(Builder builder, int textId, boolean only) {
        if (builder != null) {
            TextView tv = (TextView) findViewById(R.id.dialog_bt_yes);
            tv.setVisibility(View.VISIBLE);
            tv.setText(textId);
            if (only) {
                findViewById(R.id.dialog_bt_no).setVisibility(View.GONE);
            }
        }
    }

    public void refreshPositiveButtonText(Builder builder, String text) {
        if (builder != null) {
            TextView tv = (TextView) findViewById(R.id.dialog_bt_yes);
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    public void refreshPositiveButtonText(Builder builder, String text, boolean only) {
        if (builder != null) {
            TextView tv = (TextView) findViewById(R.id.dialog_bt_yes);
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            if (only) {
                findViewById(R.id.dialog_bt_no).setVisibility(View.GONE);
            }
        }
    }

    public void refreshTitle(Builder builder, int textId) {
        if (builder != null && builder.isHasTitle()) {
            TextView tv = (TextView) findViewById(R.id.dialog_title);
            tv.setText(textId);
        }
    }

    public void refreshTitle(Builder builder, String text) {
        if (builder != null && builder.isHasTitle()) {
            TextView tv = (TextView) findViewById(R.id.dialog_title);
            tv.setText(text);
        }
    }

    @Override
    public void show() {
        super.show();

    }
}
