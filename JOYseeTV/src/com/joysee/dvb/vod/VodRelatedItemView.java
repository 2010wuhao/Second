
package com.joysee.dvb.vod;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.joysee.common.data.JFetchBackListener;
import com.joysee.common.data.JHttpHelper;
import com.joysee.common.utils.JImage;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;

public class VodRelatedItemView extends RelativeLayout {

    private RoundImageView mPoster;
    private ImageView mPosterInverted;
    private Bitmap mInvertedBitmap;
    private JTextViewWithTTF mNameTv;
    private int mInvertedH;
    private int[] mSize;

    public VodRelatedItemView(Context context) {
        super(context);
    }

    public VodRelatedItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VodRelatedItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPoster = (RoundImageView) findViewById(R.id.poster);
        mPosterInverted = (ImageView) findViewById(R.id.poster_inverted);
        mNameTv = (JTextViewWithTTF) findViewById(R.id.name);
        mNameTv.setManualScrollable(true);

        mSize = new int[2];
        mSize[0] = (int) getResources().getDimension(R.dimen.vod_related_item_poster_w);
        mSize[1] = (int) getResources().getDimension(R.dimen.vod_related_item_poster_h);
        mInvertedH = (int) getResources().getDimension(R.dimen.vod_related_item_poster_inverted_h);

        mPoster.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mNameTv.setVisibility(View.VISIBLE);
                    postDelayed(mDelayScrollText, 500);
                } else {
                    removeCallbacks(mDelayScrollText);
                    mNameTv.setVisibility(View.GONE);
                    mNameTv.setTextScrollable(false);
                    mNameTv.setSelected(false);
                }
            }
        });
    }

    private Runnable mDelayScrollText = new Runnable() {
        @Override
        public void run() {
            mNameTv.setVisibility(View.GONE);
            mNameTv.setSelected(true);
            mNameTv.setTextScrollable(true);
            mNameTv.setVisibility(View.VISIBLE);
        }
    };

    public void setOnItemClickListener(OnClickListener l) {
        mPoster.setOnClickListener(l);
    }

    public void setData(String url, String name) {
        if (name != null) {
            mNameTv.setText(name);
        }
        JHttpHelper.getImage(getContext(), url, mSize, new JFetchBackListener() {
            @Override
            public void fetchSuccess(String arg0, BitmapDrawable arg1) {
                if (arg1 != null && arg1.getBitmap() != null) {
                    mPoster.setImageSource(arg1.getBitmap(), mSize, 6, true);
                    mPoster.setBackgroundColor(Color.TRANSPARENT);

                    mPoster.setDrawingCacheEnabled(true);
                    mPoster.measure(
                            MeasureSpec.makeMeasureSpec(mSize[0], MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(mSize[1], MeasureSpec.EXACTLY));
                    mPoster.layout(0, 0, mPoster.getMeasuredWidth(), mPoster.getMeasuredHeight());
                    mPoster.buildDrawingCache();
                    Bitmap newDisplay = mPoster.getDrawingCache();
                    if (newDisplay != null) {
                        mInvertedBitmap = JImage.getReflect(newDisplay, mInvertedH, false);
                        if (mInvertedBitmap != null) {
                            mPosterInverted.setImageBitmap(mInvertedBitmap);
                        }
                        mPoster.destroyDrawingCache();
                        newDisplay.recycle();
                    }
                }
            }
        });
    }

    public void releaseImage() {
        if (mInvertedBitmap != null) {
            mInvertedBitmap.recycle();
        }
    }

}
