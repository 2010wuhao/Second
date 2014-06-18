
package com.joysee.dvb.search;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class WeakHandler<T> extends Handler {

    private final WeakReference<T> weakReference;

    public WeakHandler(T t) {
        weakReference = new WeakReference<T>(t);
    }

    @Override
    public final void handleMessage(Message msg) {
        if (weakReference.get() != null) {
            weakReferenceMessage(msg);
        }
    }

    protected void weakReferenceMessage(Message msg) {

    }

}
