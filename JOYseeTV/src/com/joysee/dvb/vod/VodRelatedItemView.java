
package com.joysee.dvb.vod;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.common.utils.JImage;
import com.joysee.dvb.R;

import java.lang.ref.WeakReference;

public class VodRelatedItemView extends RelativeLayout {

    private ImageView mPoster;
    private ImageView mPosterInverted;
    private RelativeLayout mPosterLayout;
    private TextView mNameTv;
    private int mPosterW;
    private int mPosterH;
    private int mInvertedW;
    private int mInvertedH;

    private WeakReference<Bitmap> mPosterBitmap;
    private WeakReference<Bitmap> mFocusInverted;
    private WeakReference<Bitmap> mNormalInverted;

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
        mPoster = (ImageView) findViewById(R.id.poster);
        mPosterInverted = (ImageView) findViewById(R.id.poster_inverted);
        mPosterLayout = (RelativeLayout) findViewById(R.id.poster_layout);
        mNameTv = (TextView) findViewById(R.id.name);

        mPosterW = (int) getResources().getDimension(R.dimen.vod_related_item_poster_w);
        mPosterH = (int) getResources().getDimension(R.dimen.vod_related_item_poster_h);
        mInvertedW = (int) getResources().getDimension(R.dimen.vod_related_item_poster_inverted_w);
        mInvertedH = (int) getResources().getDimension(R.dimen.vod_related_item_poster_inverted_h);

        mPoster.setAlpha(0.5f);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            mNameTv.setVisibility(View.VISIBLE);
            mPoster.setAlpha(1.0f);
            if (mFocusInverted != null && mFocusInverted.get() != null) {
                mPosterInverted.setImageBitmap(mFocusInverted.get());
            }
        } else {
            mPoster.setAlpha(0.5f);
            mNameTv.setVisibility(View.GONE);
            if (mNormalInverted != null && mNormalInverted.get() != null) {
                mPosterInverted.setImageBitmap(mNormalInverted.get());
            }
        }
    }

    public void setData(Bitmap poster, String name) {
        if (poster != null) {
            Bitmap roundPoster = JImage.getRound(poster, 30);
            if (roundPoster != null) {
                mPosterBitmap = new WeakReference<Bitmap>(roundPoster);
                mPoster.setImageBitmap(roundPoster);
                Bitmap inverted = JImage.getReflect(roundPoster, roundPoster.getHeight() * mInvertedH / mPosterH, false);
                if (inverted != null) {
                    mNormalInverted = new WeakReference<Bitmap>(inverted);
                    mPosterInverted.setImageBitmap(inverted);
                }
            }
        }
        mNameTv.setVisibility(View.VISIBLE);
        mNameTv.setText(name);

        // 取poster与name的截图
        mPosterLayout.setDrawingCacheEnabled(true);
        mPosterLayout.measure(
                MeasureSpec.makeMeasureSpec(mPosterW, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mPosterH, MeasureSpec.EXACTLY));
        mPosterLayout.layout(0, 0, mPoster.getMeasuredWidth(), mPoster.getMeasuredHeight());
        mPosterLayout.buildDrawingCache();
        Bitmap newDisplay = mPosterLayout.getDrawingCache();
        if (newDisplay != null) {
            Bitmap focusInverted = JImage.getReflect(newDisplay, mInvertedH, false);
            if (focusInverted != null) {
                mFocusInverted = new WeakReference<Bitmap>(focusInverted);
            }
        }
        mNameTv.setVisibility(View.GONE);
    }

    public void releaseImage() {
        
    }
    
}
