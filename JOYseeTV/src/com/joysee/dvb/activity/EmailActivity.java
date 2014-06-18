/**
 * =====================================================================
 *
 * @file  EmailActivity.java
 * @Module Name   com.joysee.dvb.activity
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-2-22
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
 * benz          2014-2-22           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.EmailContent;
import com.joysee.adtv.logic.bean.EmailHead;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.widget.LoadingDialogUtil;
import com.joysee.dvb.widget.StyleDialog;
import com.joysee.dvb.widget.menu.ExMenu;
import com.joysee.dvb.widget.menu.ExMenuGroup;
import com.joysee.dvb.widget.menu.ExMenuSub;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EmailActivity extends Activity implements JDVBPlayer.OnMonitorListener {

    private class DetailViewHoler {
        public TextView title;
        public TextView author;
        public TextView date;
        public TextView content;
    }

    private class EmailAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public EmailAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public int getCount() {
            return mEmailHeads != null ? mEmailHeads.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mEmailHeads.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PreViewHolder holder;
            if (convertView == null) {
                holder = new PreViewHolder();
                convertView = inflater.inflate(R.layout.email_listview_item, null);
                holder.title = (TextView) convertView.findViewById(R.id.email_title);
                holder.read_status = (TextView) convertView.findViewById(R.id.email_status);
                holder.author = (TextView) convertView.findViewById(R.id.email_author);
                holder.date = (TextView) convertView.findViewById(R.id.email_date);
                convertView.setTag(holder);
            } else {
                holder = (PreViewHolder) convertView.getTag();
            }

            EmailHead emailHead = mEmailHeads.get(position);
            int textColor = Color.WHITE;
            if (emailHead.isNewEmail()) {
                holder.read_status.setVisibility(View.VISIBLE);
                holder.read_status.setText(getResources().getString(R.string.email_tv_unread));
            } else {
                textColor = getResources().getColor(R.color.gray_858585);
                holder.read_status.setVisibility(View.GONE);
            }
            holder.title.setText(emailHead.getEmailTitle());
            holder.author.setText(getResources().getString(R.string.email_tv_author, "广电"));
            holder.date.setText(mDateFormat.format(new Date(emailHead.getEmailSendTime())));

            holder.read_status.setTextColor(textColor);
            holder.title.setTextColor(textColor);
            holder.author.setTextColor(textColor);
            holder.date.setTextColor(textColor);
            return convertView;
        }

        public void notifyItemChanged(int position) {
            if (mEmailListView != null) {
                int start = mEmailListView.getFirstVisiblePosition();
                int end = mEmailListView.getLastVisiblePosition();
                if (position >= start && position <= end) {
                    int index = position - start;
                    View view = mEmailListView.getChildAt(index);
                    getView(position, view, mEmailListView);
                }
            }
        }

    }

    private class EmailMenuListener implements ExMenu.OnMenuListener {

        @Override
        public void onMenuClose(View lastFocusView) {
        }

        @Override
        public void onMenuOpen() {
        }

        @Override
        public void onItemSubClick(ExMenuSub exMenuSub) {
            mExMenu.hide();
            if (exMenuSub.getSubId() == R.string.email_tv_close_remind) {
                Toast.makeText(EmailActivity.this, "暂未开放的接口", Toast.LENGTH_SHORT).show();
            } else if (exMenuSub.getSubId() == R.string.email_tv_delete_all_email) {
                deleteAllEmail();
            }
        }

        @Override
        public void onGroupExpand(ExMenuGroup whichHideItems) {
            
        }

        @Override
        public void onGroupCollapsed(ExMenuGroup whichShowItems) {
            
        }

    }

    private class PreViewHolder {
        public TextView title;
        public TextView read_status;
        public TextView author;
        public TextView date;
    }

    protected static final String TAG = JLog.makeTag(EmailActivity.class);
    private static final int LOADING_DIALOG = 0;
    private TextView mUnreadCountTv;
    private TextView mTotalCountTv;

    private ListView mEmailListView;

    private ExMenu mExMenu;
    private ViewGroup mEmailContentView;
    private StyleDialog mDetailDialog;
    private int mUnreadCount;
    private int mTotalCount;

    private EmailHead mEmailHead;
    private EmailContent mEmailContent;
    private int mOpenEmailPosition;
    private DateFormat mDateFormat;

    private ArrayList<EmailHead> mEmailHeads;

    private EmailAdapter mEmailAdapter;

    private Handler mHandler = new Handler();

    private void deleteAllEmail() {
        if (mEmailHeads == null) {
            Toast.makeText(this, R.string.email_tv_inbox_is_null, Toast.LENGTH_LONG).show();
            return;
        }
        final int total = mEmailHeads.size();
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "deleteAllEmail--->" + total);
        }
        if (total > 0) {
            StyleDialog.Builder builder = new StyleDialog.Builder(EmailActivity.this);
            builder.setDefaultContentMessage(R.string.email_tv_delete_all_email);
            builder.setPositiveButton(R.string.email_tv_sure_delete);
            builder.setNegativeButton(R.string.email_tv_cancle_delete);
            final StyleDialog styleDialog = builder.show();
            builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    styleDialog.dismissByAnimation();
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        for (int i = 0; i < total; i++) {
                            JDVBPlayer.getInstance().deleteEmailById(mEmailHeads.get(i).getEmailID());
                        }
                        mTotalCount = 0;
                        mUnreadCount = 0;
                        mEmailHeads.clear();
                        mEmailAdapter.notifyDataSetChanged();
                        refreshEmailCountStatus();
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.email_tv_inbox_is_null, Toast.LENGTH_LONG).show();
        }
    }

    private void deleteEmail(int emailId) {
        boolean success = JDVBPlayer.getInstance().deleteEmailById(emailId);
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "deleteEmail--->" + emailId);
        }
        if (success) {
            mEmailHeads.remove(mEmailHead);
            mTotalCount--;
            mEmailAdapter.notifyDataSetChanged();
            refreshEmailCountStatus();
        } else {
            Toast.makeText(this, R.string.email_tv_delete_fail, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (mExMenu.isShowing()) {
                    mExMenu.hide();
                } else {
                    mExMenu.show();
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private EmailContent getEmailDetail() {
        return JDVBPlayer.getInstance().getEmailContentById(mEmailHead.getEmailID());
    }

    private ArrayList<EmailHead> getEmailHeads() {
        ArrayList<EmailHead> heads = JDVBPlayer.getInstance().getAllEmailHead();
        if (heads != null) {
            for (int i = 0; i < heads.size(); i++) {
                EmailHead head = heads.get(i);
                if (head.isNewEmail()) {
                    mUnreadCount++;
                }
            }
        }
        return heads;
    }

    private int getEmailTotalCount() {
        return JDVBPlayer.getInstance().getEmailCount();
    }

    private void loadEmail() {
        long begin = 0;
        if (TvApplication.DEBUG_LOG) {
            begin = JLog.methodBegin(TAG);
        }
        mEmailHeads = getEmailHeads();
        mTotalCount = getEmailTotalCount();
        refreshEmailCountStatus();
        String ret;
        int size = mEmailHeads != null ? mEmailHeads.size() : 0;
        if (size > 0) {
            ret = "count="+size;
            mEmailAdapter.notifyDataSetChanged();
        } else {
            ret = mEmailHeads != null ? "收件箱为空" : "获取邮件失败";
            Toast.makeText(EmailActivity.this, ret, Toast.LENGTH_LONG).show();
        }
        if (TvApplication.DEBUG_LOG) {
            JLog.methodEnd(TAG, begin, "result = "+ret);
        }
    }

    private void loadEmailDetail() {
        long begin = 0;
        if (TvApplication.DEBUG_LOG) {
            begin = JLog.methodBegin(TAG);
        }
        showDialog(LOADING_DIALOG);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEmailContent = getEmailDetail();
                removeDialog(LOADING_DIALOG);
                if (mEmailContent != null) {
                    showDetailDialog(mEmailHead, mEmailContent);
                    if (mEmailHead.isNewEmail()) {
                        setAsRead();
                    }
                } else {
                    Toast.makeText(EmailActivity.this, R.string.email_tv_load_fail, Toast.LENGTH_LONG).show();
                }
            }
        });
        if (TvApplication.DEBUG_LOG) {
            JLog.methodEnd(TAG, begin);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_layout);

        long begin = JLog.methodBegin(TAG);
        int initResult = JDVBPlayer.getInstance().init();
        JDVBPlayer.getInstance().addOnMonitorListener(this);
        if (TvApplication.DEBUG_LOG) {
            JLog.methodEnd(TAG, begin, "to init JDVBPlayer, retCode=" + initResult);
        }
        performView();
        performListener();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == LOADING_DIALOG) {
            return LoadingDialogUtil.getProgressBarDialog(this, -1);
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JDVBPlayer.getInstance().removeOnMonitorListener(this);
    }

    @Override
    public void onMonitor(int monitorType, Object message) {
        if (monitorType == JDVBPlayer.CALLBACK_SERVICE_DIED) {
            // StyleDialog.Builder builder = new StyleDialog.Builder(this);
            // builder.setDefaultContentMessage("dtvservice is died");
            // builder.setPositiveButton("退出");
            // builder.setOnButtonClickListener(new
            // DialogInterface.OnClickListener() {
            // @Override
            // public void onClick(DialogInterface dialog, int which) {
            // dialog.dismiss();
            // EmailActivity.this.finish();
            // }
            // });
            // builder.show();
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "@@@@@@@@@ CALLBACK_SERVICE_DIED @@@@@@@@@");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDetailDialog != null) {
            mDetailDialog.dismiss();
        }
    }

    private void performListener() {
        mExMenu.registerMenuControl(new EmailMenuListener());
        mEmailListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mEmailHead = mEmailHeads.get(position);
                mOpenEmailPosition = position;
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "onItemClick email id--->" + mEmailHead.getEmailID());
                }
                loadEmailDetail();
            }
        });
    }

    private void performMenu() {
        mExMenu = new ExMenu(this);
        ExMenuGroup closeRemind = new ExMenuGroup(this, R.string.email_tv_close_remind, -1, -1);
        ExMenuGroup deleteAll = new ExMenuGroup(this, R.string.email_tv_delete_all_email, -1, -1);
        mExMenu.addSubGroup(closeRemind);
        mExMenu.addSubGroup(deleteAll);
        mExMenu.setDefaultGroupSelect(0);
        mExMenu.setLeftRightControlable(false);
        mExMenu.setResetable(false);
        mExMenu.attach2Window();
    }

    private void performView() {
        mUnreadCountTv = (TextView) findViewById(R.id.title_info_unread_email);
        mTotalCountTv = (TextView) findViewById(R.id.title_info_total_email);
        mEmailListView = (ListView) findViewById(R.id.email_listview);
        mEmailAdapter = new EmailAdapter(getLayoutInflater());
        mEmailHeads = new ArrayList<EmailHead>();
        mEmailContent = new EmailContent();
        mEmailListView.setAdapter(mEmailAdapter);
        performMenu();

        mEmailContentView = (ViewGroup) getLayoutInflater().inflate(R.layout.email_detail_view, null);
        DetailViewHoler viewHolder = new DetailViewHoler();
        viewHolder.title = (TextView) mEmailContentView.findViewById(R.id.email_title);
        viewHolder.author = (TextView) mEmailContentView.findViewById(R.id.email_author);
        viewHolder.date = (TextView) mEmailContentView.findViewById(R.id.email_date);
        viewHolder.content = (TextView) mEmailContentView.findViewById(R.id.email_content);
        mEmailContentView.setTag(viewHolder);

        mDateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);

        loadEmail();
    }

    private void refreshEmailCountStatus() {
        mTotalCount = mTotalCount < 0 ? 0 : mTotalCount;
        mUnreadCount = mUnreadCount < 0 ? 0 : mUnreadCount;
        mTotalCountTv.setText(getResources().getString(R.string.email_tv_title_total, mTotalCount + ""));
        mUnreadCountTv.setText(getResources().getString(R.string.email_tv_title_unread, mUnreadCount + ""));
    }

    private void setAsRead() {
        mUnreadCount--;
        mEmailHead.setNewEmail(false);
        mEmailAdapter.notifyItemChanged(mOpenEmailPosition);
        refreshEmailCountStatus();
    }

    private void showDetailDialog(final EmailHead head, EmailContent content) {
        long begin = 0;
        if (TvApplication.DEBUG_LOG) {
            begin = JLog.methodBegin(TAG);
        }
        DetailViewHoler holder = (DetailViewHoler) mEmailContentView.getTag();
        holder.title.setText(head.getEmailTitle());
        holder.author.setText(getResources().getString(R.string.email_tv_author, "广电"));
        holder.date.setText(mDateFormat.format(new Date(head.getEmailSendTime())));
        holder.content.setText(content != null ? content.getEmailContent() : "");

        ViewGroup group = (ViewGroup) mEmailContentView.getParent();
        if (group != null) {
            group.removeView(mEmailContentView);
        }

        StyleDialog.Builder builder = new StyleDialog.Builder(this);
        builder.setTitle(R.string.email_tv_detail_title);
        builder.setContentView(mEmailContentView);
        builder.setPositiveButton("返回");
        builder.setNegativeButton("删除");
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                boolean ret = false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        View scroll = mEmailContentView.getChildAt(0);
                        if (scroll != null) {
                            ((ScrollView) scroll).smoothScrollBy(0, -200);
                        }
                        ret = true;
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        View scroll = mEmailContentView.getChildAt(0);
                        if (scroll != null) {
                            ((ScrollView) scroll).smoothScrollBy(0, 200);
                        }
                        ret = true;
                    }
                }
                return ret;
            }
        });
        mDetailDialog = builder.show();
        builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDetailDialog.dismissByAnimation();
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    deleteEmail(head.getEmailID());
                }
            }
        });
        if (TvApplication.DEBUG_LOG) {
            JLog.methodEnd(TAG, begin, "showDetailDialog");
        }
    }
}
