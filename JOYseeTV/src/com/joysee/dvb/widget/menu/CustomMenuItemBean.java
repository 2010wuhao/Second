/**
 * =====================================================================
 *
 * @file  CustomMenuItemBean.java
 * @Module Name   com.joysee.dvb.widget
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-2-21
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
 * benz          2014-2-21           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget.menu;

import android.graphics.Bitmap;

public class CustomMenuItemBean {

    public int id;
    public String title;
    public Bitmap bitmap;

    public Bitmap getIcon() {
        return bitmap;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setIcon(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
