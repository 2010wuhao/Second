/**
 * =====================================================================
 *
 * @file  LoadingDialogUtil.java
 * @Module Name   com.joysee.dvb.widget
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-17
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
 * benz          2014-1-17           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget;

import android.app.Activity;
import android.view.View;

import com.joysee.common.widget.JStyleDialog;
import com.joysee.dvb.R;

public class LoadingDialogUtil {

    public static JStyleDialog getProgressBarDialog(Activity activity, int type) {
        JStyleDialog jDialog = new JStyleDialog(activity, R.style.search_dialog);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_loading, null);
        jDialog.setContentView(view);
        return jDialog;
    }

}
