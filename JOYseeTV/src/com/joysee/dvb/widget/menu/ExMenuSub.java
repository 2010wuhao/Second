
package com.joysee.dvb.widget.menu;

import android.content.Context;
import android.widget.RelativeLayout;

public abstract class ExMenuSub extends RelativeLayout {

    protected int mId;
    protected String mName;
    protected Context mContext;

    public ExMenuSub(Context context, int id, String name) {
        super(context);
        this.mId = id;
        this.mName = name;
        this.mContext = context;
    }

    public int getSubId() {
        return mId;
    }

    public String getSubName() {
        return mName;
    }

    public abstract void onFocusChanged(boolean gainFocus);
}
