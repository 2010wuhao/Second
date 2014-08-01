/**
 * =====================================================================
 *
 * @file   ListAnimationProgressAdapter.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 30, 2014
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
 * wumingjun         @Apr 30, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.apps.AppManagerActivity;
import com.joysee.tvbox.settings.apps.ApplicationsState;
import com.joysee.tvbox.settings.apps.ApplicationsState.AppEntry;

public class ListAnimationAppAdapter extends BaseAdapter implements ApplicationsState.Callbacks {

    private int mLastFilterMode = -1;
    private int mLastSortMode = -1;
    private int mWhichSize = AppManagerActivity.SIZE_TOTAL;

    private Holder mHolder;
    private LayoutInflater mInflater;
    private Context mContext;
    private boolean mAnimed;
    private TranslateAnimation mTransAnim;

    private final ApplicationsState mState;
    private ArrayList<ApplicationsState.AppEntry> mBaseEntries;
    private ArrayList<ApplicationsState.AppEntry> mEntries;
    private boolean mResumed = false;
    private CharSequence mCurFilterPrefix;

    public ListAnimationAppAdapter(Context context, ApplicationsState state) {
        super();
        mContext = context;
        mState = state;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mEntries != null ? mEntries.size() : 0;
    }

    public Object getItem(int position) {
        return mEntries.get(position);
    }

    public long getItemId(int position) {
        return mEntries.get(position).id;
    }

    public ApplicationsState.AppEntry getAppEntry(int position) {
        return mEntries.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            mHolder = new Holder();
            convertView = mInflater.inflate(R.layout.view_settings_app_item, null);
            int width = mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_width);
            android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(width, mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_height));
            convertView.setLayoutParams(params);
            mHolder.jtxtName = (JTextViewWithTTF) convertView.findViewById(R.id.txt_settings_model_item_name);
            mHolder.imgRight = (ImageView) convertView.findViewById(R.id.img_list_item_option_right);
            mHolder.imgIcon = (ImageView) convertView.findViewById(R.id.img_list_item_app_icon);

            convertView.setTag(mHolder);
        } else {
            mHolder = (Holder) convertView.getTag();
        }
        final View temp = convertView;

        // Bind data
        ApplicationsState.AppEntry entry = mEntries.get(position);
        synchronized (entry) {
            mHolder.entry = entry;
            if (entry.label != null) {
                mHolder.jtxtName.setText(entry.label);
            }
            mState.ensureIcon(entry);
            if (entry.icon != null) {
                mHolder.imgIcon.setImageDrawable(entry.icon);
            }

        }

        if (!mAnimed && position < 6) {
            temp.setVisibility(View.INVISIBLE);
            temp.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mTransAnim = new TranslateAnimation(950, 0, 0, 0);
                    mTransAnim.setDuration(position * 20 + 200);
                    temp.setVisibility(View.VISIBLE);
                    temp.startAnimation(mTransAnim);
                }
            }, 200);
            if (position == mEntries.size() - 1 || position == ListViewEx.LIST_VISIBLE_ITEMS_COUNT - 1) {

                mAnimed = true;

            }
        }
        return convertView;
    }

    public void resume(int filter, int sort) {

        if (!mResumed) {
            mResumed = true;
            mState.resume(this);
            mLastFilterMode = filter;
            mLastSortMode = sort;
            rebuild(true);
        } else {
            rebuild(filter, sort);
        }
    }

    public void pause() {
        if (mResumed) {
            mResumed = false;
            mState.pause();
        }
    }

    public void rebuild(int filter, int sort) {
        if (filter == mLastFilterMode && sort == mLastSortMode) {
            return;
        }
        mLastFilterMode = filter;
        mLastSortMode = sort;
        rebuild(true);
    }

    public void rebuild(boolean eraseold) {
        ApplicationsState.AppFilter filterObj;
        Comparator<AppEntry> comparatorObj;
        boolean emulated = Environment.isExternalStorageEmulated();
        /*
         * if (emulated) { mWhichSize = SIZE_TOTAL; } else { mWhichSize =
         * SIZE_INTERNAL; }
         */
        mWhichSize = AppManagerActivity.SIZE_TOTAL; // Rony modify 20120321
        switch (mLastFilterMode) {
        case AppManagerActivity.FILTER_APPS_THIRD_PARTY:
            filterObj = ApplicationsState.THIRD_PARTY_FILTER;
            break;
        case AppManagerActivity.FILTER_APPS_SDCARD:
            filterObj = ApplicationsState.ON_SD_CARD_FILTER;
            if (!emulated) {
                mWhichSize = AppManagerActivity.SIZE_EXTERNAL;
            }
            break;
        default:
            filterObj = null;
            break;
        }
        switch (mLastSortMode) {
        case AppManagerActivity.SORT_ORDER_SIZE:
            switch (mWhichSize) {
            case AppManagerActivity.SIZE_INTERNAL:
                comparatorObj = ApplicationsState.INTERNAL_SIZE_COMPARATOR;
                break;
            case AppManagerActivity.SIZE_EXTERNAL:
                comparatorObj = ApplicationsState.EXTERNAL_SIZE_COMPARATOR;
                break;
            default:
                comparatorObj = ApplicationsState.SIZE_COMPARATOR;
                break;
            }
            break;
        default:
            comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
            break;
        }
        ArrayList<ApplicationsState.AppEntry> entries = mState.rebuild(filterObj, comparatorObj);
        if (entries == null && !eraseold) {
            // Don't have new list yet, but can continue using the old one.
            return;
        }
        mBaseEntries = entries;
        if (mBaseEntries != null) {
            mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
        } else {
            mEntries = null;
        }
        notifyDataSetChanged();

    }

    ArrayList<ApplicationsState.AppEntry> applyPrefixFilter(CharSequence prefix, ArrayList<ApplicationsState.AppEntry> origEntries) {
        if (prefix == null || prefix.length() == 0) {
            return origEntries;
        } else {
            String prefixStr = ApplicationsState.normalize(prefix.toString());
            final String spacePrefixStr = " " + prefixStr;
            ArrayList<ApplicationsState.AppEntry> newEntries = new ArrayList<ApplicationsState.AppEntry>();
            for (int i = 0; i < origEntries.size(); i++) {
                ApplicationsState.AppEntry entry = origEntries.get(i);
                String nlabel = entry.getNormalizedLabel();
                if (nlabel.startsWith(prefixStr) || nlabel.indexOf(spacePrefixStr) != -1) {
                    newEntries.add(entry);
                }
            }
            return newEntries;
        }
    }

    @Override
    public void onRunningStateChanged(boolean running) {

    }

    @Override
    public void onPackageListChanged() {
        rebuild(false);
    }

    @Override
    public void onRebuildComplete(ArrayList<AppEntry> apps) {

    }

    @Override
    public void onPackageIconChanged() {

    }

    @Override
    public void onPackageSizeChanged(String packageName) {

    }

    @Override
    public void onAllSizesComputed() {

    }

    public ApplicationsState.AppEntry getEntryAtPostion(int postion) {
        return mEntries.get(postion);
    }

    public class Holder {
        ApplicationsState.AppEntry entry;
        JTextViewWithTTF jtxtName;
        ImageView imgRight;
        ImageView imgIcon;
    }

}
