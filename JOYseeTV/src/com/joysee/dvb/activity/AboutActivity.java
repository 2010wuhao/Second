/**
 * =====================================================================
 *
 * @file  AboutActivity.java
 * @Module Name   com.joysee.dvb.activity
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-3-5
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
 * benz          2014-3-5           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
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

import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;
import com.joysee.dvb.widget.StyleDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class AboutActivity extends Activity {

	private class OnDetailKeyListener implements OnKeyListener {

		View scrollView;

		public OnDetailKeyListener(View scrollView) {
			this.scrollView = scrollView;
		}

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			boolean handler = false;
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
					if (scrollView != null && scrollView instanceof ScrollView) {
						((ScrollView) scrollView).smoothScrollBy(0, -200);
					}
					handler = true;
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (scrollView != null && scrollView instanceof ScrollView) {
						((ScrollView) scrollView).smoothScrollBy(0, 200);
					}
					handler = true;
				}
			}
			return handler;
		}

	}

	private class Item {
		public int itemTitleId;
		public String itemTitle;
		public String itemPreview;

		public Item(int titleId, String preview) {
			this.itemTitleId = titleId;
			this.itemTitle = getString(itemTitleId);
			this.itemPreview = preview;
		}
	}

	private class MenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.about_listview_item, null);
			}
			Item item = mItems.get(position);
			((TextView) convertView.findViewById(R.id.item_title)).setText(item.itemTitle);
			((TextView) convertView.findViewById(R.id.item_preview_info)).setText(item.itemPreview);
			return convertView;
		}

	}

	private ListView mListView;
	private ArrayList<Item> mItems;
	private LayoutInflater mInflater;
	private StyleDialog mDetailDialog;
	private String mAgreementSrt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		initView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mDetailDialog != null && mDetailDialog.isShowing()) {
			mDetailDialog.dismiss();
		}
	}

	private String getAgreement() {
		String str = mAgreementSrt;
		if (str == null) {
			InputStream in = null;
			try {
				in = getAssets().open("agreement.txt");
				int size = in.available();
				byte[] buffer = new byte[size];
				in.read(buffer);
				str = new String(buffer, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
				str = "null";
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			mAgreementSrt = str;
		}
		return str;
	}

	private String getPhoneNumber() {
//		return "+86 10 62971199 ext.6617";
		return "暂无";
	}

	private String getVersionName() {
		StringBuilder ret = new StringBuilder();
		PackageInfo localApkInfo = null;
		try {
			localApkInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (localApkInfo != null) {
				ret.append("当前版本:");
				ret.append(localApkInfo.versionName);
			}
		}
		return ret.toString();
	}

	private void initView() {
		mItems = new ArrayList<AboutActivity.Item>();
		// 联系我们
		mItems.add(new Item(R.string.about_menu_tv_call_service, ""));
		// 用户协议
		mItems.add(new Item(R.string.about_menu_tv_user_agreement, ""));
		// 版本介绍
		mItems.add(new Item(R.string.about_menu_tv_app_version, ""));
		// MiTV is not allow custom update
		if (TvApplication.sDestPlatform != DestPlatform.MITV_QCOM && 
		        TvApplication.sDestPlatform != DestPlatform.MITV_2) {
			// 检测新版本
			mItems.add(new Item(R.string.about_menu_tv_check_update, getVersionName()));
		}

		mInflater = getLayoutInflater();
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(new MenuAdapter());
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				handlerItemClick(view, position);
			}
		});

	}

	private void handlerItemClick(View view, int position) {
		Item item = mItems.get(position);

		switch (item.itemTitleId) {
		case R.string.about_menu_tv_call_service:
		    ViewGroup contact = (ViewGroup) mInflater.inflate(R.layout.about_item_contact_view, null);
		    ((TextView) contact.findViewById(R.id.number)).setText(getPhoneNumber());
		    showItemDetail(item.itemTitle, null, null, contact);
			break;
		case R.string.about_menu_tv_user_agreement:
			ViewGroup agreeMentView = (ViewGroup) mInflater.inflate(R.layout.about_item_agreement_view, null);
			((TextView) agreeMentView.findViewById(R.id.part_1)).setText(R.string.about_menu_tv_user_agreement);
			((TextView) agreeMentView.findViewById(R.id.part_1_content)).setText(getAgreement());
			((TextView) agreeMentView.findViewById(R.id.part_2)).setVisibility(View.GONE);
			((TextView) agreeMentView.findViewById(R.id.part_2_content)).setVisibility(View.GONE);
			showItemDetail(item.itemTitle, null, null, agreeMentView);
			break;
		case R.string.about_menu_tv_app_version:
			if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM ||
			        TvApplication.sDestPlatform == DestPlatform.MITV_2) {
				ViewGroup versionView = (ViewGroup) mInflater.inflate(R.layout.about_item_agreement_view, null);
				((TextView) versionView.findViewById(R.id.part_1)).setText(R.string.update_current_version);
				((TextView) versionView.findViewById(R.id.part_1_content)).setText(getVersionName());
				((TextView) versionView.findViewById(R.id.part_2)).setText(R.string.update_repair_ins);
				((TextView) versionView.findViewById(R.id.part_2_content)).setText(TvApplication.getRepairInstructions());
				showItemDetail(item.itemTitle, null, null, versionView);
			} else {
				Intent intent = new Intent(AboutActivity.this, UpdateActivity.class);
				intent.putExtra("check_now", false);
				startActivity(intent);
			}
			break;
		case R.string.about_menu_tv_check_update:
			Intent intent = new Intent(AboutActivity.this, UpdateActivity.class);
			intent.putExtra("check_now", true);
			startActivity(intent);
			break;
		}
	}

	private void showItemDetail(String title, String msg, String point, ViewGroup customContentView) {
		StyleDialog.Builder builder = new StyleDialog.Builder(this);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.jstyle_dialog_postive_bt);
		if (customContentView != null) {
			builder.setContentView(customContentView);
			builder.setOnKeyListener(new OnDetailKeyListener(customContentView.getChildAt(0)));
		} else {
			builder.setDefaultContentMessage(msg);
			builder.setDefaultContentPoint(point);
		}
		builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDetailDialog.dismissByAnimation();
			}
		});
		mDetailDialog = builder.show();
	}

}
