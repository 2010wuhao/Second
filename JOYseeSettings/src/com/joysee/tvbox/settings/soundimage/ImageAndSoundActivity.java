/**
 * =====================================================================
 *
 * @file   ImageAndSoundActivity.java
 * @Module Name   com.joysee.tvbox.settings.soundimage
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 15, 2014
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
 * wumingjun         @Apr 15, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.soundimage;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.os.SystemProperties;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.base.StyleDialog;
import com.joysee.tvbox.settings.entity.ListItem;

public class ImageAndSoundActivity extends BaseActivity implements OnItemClickListener, OnKeyListener {

    private Context mContext;
    private ListViewEx mList;
    private ListView mInnerList;
    private ListAnimationAdapter mAdapter;
    private ArrayList<ListItem> mArray;
    private StyleDialog mClickSoundDialog;
    private RadioGroup mRadioGroup;

    private AudioManager mAudioManager;

    private boolean mClickAudioOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {
        mContext = this;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setContentView(R.layout.activity_image_sound);
        mList = (ListViewEx) findViewById(R.id.list_image_and_sound);
        mArray = new ArrayList<ListItem>();
        mAdapter = ListFactory.initList(mArray, mList, Settings.Type.IMAGE_AND_SOUND);
        mInnerList = mList.getInnerList();
        if (mInnerList != null) {
            mInnerList.setOnItemClickListener(this);
            mInnerList.setOnKeyListener(this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mClickAudioOn = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
        updateClickAudio();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        switch (mList.getSelection()) {
        case 0:
            intent.setClass(view.getContext(), ImageAndSoundModelActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 1:
            intent.setClass(view.getContext(), ImageAndSoundUserDefineActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 2:
            showClickSoundDialog();
            break;
        case 3:
            intent.setClass(view.getContext(), ImageAndSoundResolutionActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 4:
            intent.setClass(view.getContext(), ImageAndSoundDisplayEreaActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 5:
            intent.setClass(view.getContext(), ImageAndSoundAudioActivity.class);
            view.getContext().startActivity(intent);
            break;
        default:
            break;
        }
    }

    private void showClickSoundDialog() {

        if (mClickSoundDialog == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.view_dialog_radio_click_sound, null);
            StyleDialog.Builder builder = new StyleDialog.Builder(this);
            builder.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            builder.setTitle(R.string.title_dialog_click_sound);
            builder.setPositiveButton(R.string.jstyle_dialog_postive_bt);
            builder.setNegativeButton(R.string.jstyle_dialog_negatvie_bt);
            mClickSoundDialog = builder.show();
            mRadioGroup = (RadioGroup) mClickSoundDialog.findViewById(R.id.radio_group);
            mRadioGroup.check(mClickAudioOn ? R.id.radio_on : R.id.radio_off);
            builder.setOnButtonClickListener(new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == DialogInterface.BUTTON_POSITIVE) {

                        int id = mRadioGroup.getCheckedRadioButtonId();

                        if (id == R.id.radio_on) {
                            mAudioManager.loadSoundEffects();
                            mClickAudioOn = true;

                        } else if (id == R.id.radio_off) {
                            mAudioManager.unloadSoundEffects();
                            mClickAudioOn = false;
                        }

                        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SOUND_EFFECTS_ENABLED, mClickAudioOn ? 1 : 0);
                    }
                    updateClickAudio();
                    mClickSoundDialog.dismissByAnimation();
                }
            });
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {

                        mClickSoundDialog.dismissByAnimation();
                    }
                    return false;
                }
            });
            builder.setContentCanFocus(true);
        } else {
            mRadioGroup.check(mClickAudioOn ? R.id.radio_on : R.id.radio_off);
            mClickSoundDialog.show();
        }
        mRadioGroup.requestFocus();
    }

    private void updateClickAudio() {
        mArray.get(2).setSelectDataIndex(mClickAudioOn ? 1 : 2);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKey(View v, int key, KeyEvent event) {
        int selectIndex = mList.getSelection();
        if (selectIndex == 2) {
            if ((key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) && event.getAction() == KeyEvent.ACTION_DOWN) {

                if (mClickAudioOn) {
                    mAudioManager.unloadSoundEffects();
                    mClickAudioOn = false;
                } else {
                    mAudioManager.loadSoundEffects();
                    mClickAudioOn = true;

                }

                android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SOUND_EFFECTS_ENABLED, mClickAudioOn ? 1 : 0);
                updateClickAudio();
            }
        }
        return false;
    }
}
