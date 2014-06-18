
package com.joysee.dvb.portal.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.portal.PortalModle;
import com.joysee.dvb.portal.appcache.ApplicationInfo;

import java.util.ArrayList;

public class PortalAppListView extends RelativeLayout {

    public interface Callback {
        public void onHide(PortalState portalState);

        public void onShow();
    }

    private class AppListObserver implements PortalModle.AppListListener {

        @Override
        public void onAllAppsGot(ArrayList<ApplicationInfo> apps) {
            mAppList.clear();
            mAppList.addAll(apps);
            handlerDataChange();
        }

        @Override
        public void onAppsAdded(ArrayList<ApplicationInfo> apps) {
            mAppList.addAll(apps);
            handlerDataChange();
        }

        @Override
        public void onAppsRemoved(ArrayList<ApplicationInfo> apps) {
            mAppList.removeAll(apps);
            handlerDataChange();
        }

        @Override
        public boolean isLoadOnResume() {
            return PortalAppListView.this.getVisibility() == View.VISIBLE;
        }

    }

    public static class PortalState {
        public View lastFocusView;
        public int lastPageId;
    }

    private static final String TAG = JLog.makeTag(PortalAppListView.class);

    private Callback mCallback;
    private GridView mGridView;
    private ImageView mMoreTag;
    private PortalState mPortalState;
    private PortalModle mPortalModle;

    private AppListAdapter mAppListAdapter;
    private ArrayList<ApplicationInfo> mAppList;

    public PortalAppListView(Context context) {
        super(context);
    }

    public PortalAppListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortalAppListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGridView = (GridView) findViewById(R.id.applist_gridview);
        mMoreTag = (ImageView) findViewById(R.id.applist_more_tag);

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppListViewHoder holder = (AppListViewHoder) view.getTag();
                getContext().startActivity(holder.intent);
            }
        });

        mGridView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount + firstVisibleItem == totalItemCount) {
                    mMoreTag.setVisibility(View.INVISIBLE);
                } else {
                    mMoreTag.setVisibility(View.VISIBLE);
                }
            }
        });

        mGridView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mAppList = new ArrayList<ApplicationInfo>();
        mAppListAdapter = new AppListAdapter();
        mGridView.setAdapter(mAppListAdapter);
    }

    public void register(PortalModle model, Callback cb) {
        JLog.d(TAG, "APPLIST visible is " + TvApplication.AS_LAUNCHER);
        this.mCallback = cb;
        this.mPortalModle = model;
        if (TvApplication.AS_LAUNCHER) {
            mPortalModle.registerAppListListener(new AppListObserver());
            mPortalModle.startLoader(getContext(), false);
        }
    }

    public void showAppList(PortalState state) {
        if (mCallback != null) {
            mCallback.onShow();
        }
        mPortalState = state;
        setVisibility(View.VISIBLE);
        mAppListAdapter.notifyDataSetChanged();
        mGridView.requestFocus();
    }

    public void hideAppList() {
        if (mCallback != null) {
            mCallback.onHide(mPortalState);
        }
        setVisibility(View.INVISIBLE);
    }

    private void handlerDataChange() {
        if (getVisibility() == View.VISIBLE) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    mAppListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handler = super.dispatchKeyEvent(event);
        if (!handler && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                event.startTracking();
            }
        }
        if (!handler && event.getAction() == KeyEvent.ACTION_UP) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                if (event.isTracking()) {
                    hideAppList();
                }
                handler = true;
            }
        }
        return handler;
    }

    private class AppListViewHoder {
        public ImageView icon;
        public TextView title;
        public Intent intent;
    }

    class AppListAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public AppListAdapter() {
            this.inflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return mAppList.size();
        }

        @Override
        public Object getItem(int position) {
            return mAppList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void notifySelectChanged(int selectId) {
            // View selectView = mAllAppGridView.getChildAt(selectId -
            // mAllAppGridView.getFirstVisiblePosition());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            AppListViewHoder viewHoder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.portal_applist_item, null);
                viewHoder = new AppListViewHoder();
                viewHoder.icon = (ImageView) convertView.findViewById(R.id.icon);
                viewHoder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(viewHoder);
            } else {
                viewHoder = (AppListViewHoder) convertView.getTag();
            }
            viewHoder.icon.setImageBitmap(mAppList.get(position).iconBitmap);
            viewHoder.title.setText(mAppList.get(position).title);
            viewHoder.intent = mAppList.get(position).intent;
            return convertView;
        }

    }
}
