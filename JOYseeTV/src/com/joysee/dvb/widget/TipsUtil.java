
package com.joysee.dvb.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.dvb.R;

public class TipsUtil {

    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.public_tips_view, null);
        TextView tip = (TextView) view.findViewById(R.id.tv);
        tip.setText(text);
        toast.setView(view);
        toast.setDuration(duration);
        return toast;
    }

    public static Toast makeText(Context context, int resId, int duration) {
        Toast toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.public_tips_view, null);
        TextView tip = (TextView) view.findViewById(R.id.tv);
        tip.setText(resId);
        toast.setView(view);
        toast.setDuration(duration);
        return toast;
    }
}
