/**
 * =====================================================================
 *
 * @file  ProvinceAdapter.java
 * @Module Name   com.joysee.dvb.search
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-8
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
 * benz          2014-1-8           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.search.wheelview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.joysee.dvb.R;

public class ProvinceAdapter extends AbstractWheelTextAdapter {

    private String[] provinces;

    public ProvinceAdapter(Context context, String[] provinces) {
        super(context, R.layout.search_wheelview_item, R.id.country_name);
        this.provinces = provinces;
    }

    @Override
    public String getCurrentItemValue(int currentIndex) {
        return provinces[currentIndex];
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        View view = super.getItem(index, cachedView, parent);
        return view;
    }

    @Override
    public int getItemsCount() {
        return provinces.length;
    }

    @Override
    protected CharSequence getItemText(int index) {
        return provinces[index];
    }

    @Override
    public boolean isAvailableData() {
        return false;
    }

    @Override
    public void setDataAvailable(boolean availa) {

    }
}
