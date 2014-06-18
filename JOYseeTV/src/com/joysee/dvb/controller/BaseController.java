
package com.joysee.dvb.controller;

import java.util.ArrayList;

public abstract class BaseController {
    ArrayList<IDvbBaseView> mViews = new ArrayList<IDvbBaseView>();

    protected abstract void dispatchMessage(DvbMessage msg);

    public void registerView(IDvbBaseView view) {
        if (!mViews.contains(view)) {
            mViews.add(view);
        }
    }

    public void unRegisterView(IDvbBaseView view) {
        if (mViews != null && mViews.contains(view)) {
            mViews.remove(view);
        }
    }
}
