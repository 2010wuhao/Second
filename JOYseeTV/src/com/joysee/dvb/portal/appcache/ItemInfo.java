/**
 * =====================================================================
 *
 * @file  ItemInfo.java
 * @Module Name   com.joysee.launcher.bean
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013-3-23
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
 * YueLiang          2013-3-23           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//


package com.joysee.dvb.portal.appcache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.util.Log;

import com.joysee.common.utils.JLog;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo {
	
	private static final String TAG = JLog.makeTag(ItemInfo.class);

	public static final int NO_ID = -1;

	/**
	 * The id in the settings database for this item
	 */
	public long id = NO_ID;

	/**
	 * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_FOLDER}, or
	 * {@link LauncherSettings.Favorites#ITEM_TYPE_APPWIDGET}.
	 */
	public int itemType;

	/**
	 * The id of the container that holds this item. For the desktop, this will
	 * be {@link LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all
	 * applications folder it will be {@link #NO_ID} (since it is not stored in
	 * the settings DB). For user folders it will be the id of the folder.
	 */
	public long container = NO_ID;

	/**
	 * Iindicates the screen in which the shortcut appears.
	 */
	public int screen = -1;

	/**
	 * Indicates the X position of the associated cell.
	 */
	public int cellX = -1;

	/**
	 * Indicates the Y position of the associated cell.
	 */
	public int cellY = -1;

	/**
	 * Indicates the X cell span.
	 */
	public int spanX = 1;

	/**
	 * Indicates the Y cell span.
	 */
	public int spanY = 1;

	/**
	 * Indicates whether the item is a gesture.
	 */
	public boolean isGesture = false;
	
	/**
	 * The position of the item in a drag-and-drop operation.
	 */
	public int[] dropPos = null;
	public boolean locked = false;
	public String password;
	public boolean isCheckCaStatus = false;

	public ItemInfo() {
	}

	public ItemInfo(ItemInfo info) {
		id = info.id;
		itemType = info.itemType;
		container = info.container;
	}


	public static byte[] flattenBitmap(Bitmap bitmap) {
		// Try go guesstimate how much space the icon will take when serialized
		// to avoid unnecessary allocations/copies during the write.
		int size = bitmap.getWidth() * bitmap.getHeight() * 4;
		ByteArrayOutputStream out = new ByteArrayOutputStream(size);
		try {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
			Log.w(TAG, "Could not write icon");
			return null;
		}
	}

	/**
	 * It is very important that sub-classes implement this if they contain any
	 * references to the activity (anything in the view hierarchy etc.). If not,
	 * leaks can result since ItemInfo objects persist across rotation and can
	 * hence leak by holding stale references to the old view hierarchy /
	 * activity.
	 */
	public void unbind() {
	}

	@Override
	public String toString() {
		return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + screen
				+ " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX + " spanY=" + spanY + " isGesture="
				+ isGesture + " dropPos=" + dropPos + ")";
	}
}
