/*
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.joysee.dvb.search.wheelview;

import android.content.Context;

/**
 * The simple Array wheel adapter
 * 
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> extends AbstractWheelTextAdapter {

    // items
    private T items[];

    private boolean isAvailable = true;

    /**
     * Constructor
     * 
     * @param context the current context
     * @param items the items
     */
    public ArrayWheelAdapter(Context context, T items[]) {
        // super(context, R.layout.search_wheelview_item, R.id.country_name);
        super(context);
        setEmptyItemResource(TEXT_VIEW_ITEM_RESOURCE);
        this.items = items;
    }

    @Override
    public String getCurrentItemValue(int currentIndex) {
        if (currentIndex < 0) {
            return (String) items[0];
        } else if (currentIndex >= items.length) {
            return (String) items[items.length - 1];
        }
        else {
            return (String) items[currentIndex];
        }
    }

    @Override
    public int getItemsCount() {
        return items.length;
    }

    @Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < items.length) {
            T item = items[index];
            if (item instanceof CharSequence) {
                return (CharSequence) item;
            }
            return item.toString();
        }
        return null;
    }

    /**
     * @return 反回此wheelview中数据是否是可用数据
     */
    public boolean isAvailableData() {
        return isAvailable;
    }

    public void refreshData(T items[]) {
        this.items = items;
    }

    public void setDataAvailable(boolean a) {
        isAvailable = a;
    }
}
