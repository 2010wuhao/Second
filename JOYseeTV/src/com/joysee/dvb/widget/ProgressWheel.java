
package com.joysee.dvb.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;

public class ProgressWheel extends View {

    public enum ProgressMode {
        /** 加载圈 */
        LOADING,
        /** 百分比圈 */
        PERCENTAGE
    }

    String TAG = JLog.makeTag(ProgressWheel.class);
    // Sizes (with defaults)
    private int layout_height = 0;
    private int layout_width = 0;
    private int fullRadius = 100;
    private int circleRadius = 80;
    private int barLength = 60;
    private int barWidth = 20;
    private int rimWidth = 20;
    private int textSize = 36;

    private float contourSize = 0;
    // Padding (with defaults)
    private int paddingTop = 5;
    private int paddingBottom = 5;
    private int paddingLeft = 5;

    private int paddingRight = 5;
    // Colors (with defaults)
    private int barColor = 0xAA000000;
    private int contourColor = 0xAA000000;
    private int circleColor = 0x00000000;
    private int rimColor = 0xAADDDDDD;

    private int textColor = 0xFF000000;
    // Paints
    private Paint barPaint = new Paint();
    private Paint circlePaint = new Paint();
    private Paint rimPaint = new Paint();
    private Paint textPaint = new Paint();

    private Paint contourPaint = new Paint();
    // Rectangles
    @SuppressWarnings("unused")
    private RectF rectBounds = new RectF();
    private RectF circleBounds = new RectF();
    private RectF circleOuterContour = new RectF();

    // -----------------------

    private RectF circleInnerContour = new RectF();
    private boolean isStop;
    private long progress = 0;
    private long delayMillis = 20l;
    private int progressSpeed = 3;
    private String mCenterText = "";

    private String[] mCenterTextCharArray = {};
    private Runnable mLoopDrawRunnble;

    private ProgressMode mMode = ProgressMode.LOADING;

    public ProgressWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.ProgressWheel));
        runLoop();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the rim
        canvas.drawArc(circleBounds, 360, 360, false, rimPaint);
        canvas.drawArc(circleOuterContour, 360, 360, false, contourPaint);
        canvas.drawArc(circleInnerContour, 360, 360, false, contourPaint);

        /** Draw the animation bar */
        if (!isStop) {
            if (mMode == ProgressMode.LOADING) {
                canvas.drawArc(circleBounds, progress - 90, barLength, false, barPaint);
            } else if (mMode == ProgressMode.PERCENTAGE) {
                canvas.drawArc(circleBounds, -90, progress, false, barPaint);
            }
        }

        // Draw the inner circle
        canvas.drawCircle((circleBounds.width() / 2) + rimWidth + paddingLeft,
                (circleBounds.height() / 2) + rimWidth + paddingTop,
                circleRadius,
                circlePaint);
        // Draw the mCenterText (attempts to center it horizontally and
        // vertically)
        float textHeight = textPaint.descent() - textPaint.ascent();
        float verticalTextOffset = (textHeight / 2) - textPaint.descent();

        for (String s : mCenterTextCharArray) {
            float horizontalTextOffset = textPaint.measureText(s) / 2;
            canvas.drawText(s, this.getWidth() / 2 - horizontalTextOffset,
                    this.getHeight() / 2 + verticalTextOffset, textPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layout_width = w;
        layout_height = h;
        setupBounds();
        setupPaints();
        invalidate();
    }

    private void parseAttributes(TypedArray a) {
        barWidth = (int) a.getDimension(R.styleable.ProgressWheel_barWidth,
                barWidth);

        rimWidth = (int) a.getDimension(R.styleable.ProgressWheel_rimWidth,
                rimWidth);

        progressSpeed = (int) a.getDimension(R.styleable.ProgressWheel_spinSpeed,
                progressSpeed);

        if (delayMillis < 0) {
            delayMillis = 0;
        }

        barColor = a.getColor(R.styleable.ProgressWheel_barColor, barColor);

        barLength = (int) a.getDimension(R.styleable.ProgressWheel_barLength,
                barLength);

        textSize = (int) a.getDimension(R.styleable.ProgressWheel_textSize,
                textSize);

        textColor = a.getColor(R.styleable.ProgressWheel_textColor,
                textColor);

        // if the mCenterText is empty , so ignore it
        if (a.hasValue(R.styleable.ProgressWheel_text)) {
            setText(a.getString(R.styleable.ProgressWheel_text));
        }

        rimColor = a.getColor(R.styleable.ProgressWheel_rimColor,
                rimColor);

        circleColor = a.getColor(R.styleable.ProgressWheel_circleColor,
                circleColor);

        contourColor = a.getColor(R.styleable.ProgressWheel_contourColor, contourColor);
        contourSize = a.getDimension(R.styleable.ProgressWheel_contourSize, contourSize);

        a.recycle();
    }

    private void reset() {
        progress = 0;
        setText("");
    }

    private void runLoop() {
        mLoopDrawRunnble = new Runnable() {
            @Override
            public void run() {
                ProgressWheel.this.post(new Runnable() {
                    @Override
                    public void run() {
                        if (getVisibility() == View.VISIBLE) {
                            ProgressWheel.this.invalidate();
                            if (mMode == ProgressMode.LOADING) {
                                progress = progress + progressSpeed;
                            }
                            if (!isStop) {
                                postDelayed(this, 80L);
                            }
                        }
                    }
                });
            }
        };
    }

    public void setMode(ProgressMode mode) {
        this.mMode = mode;
    }

    public void setProgress(int i) {
        int temp = i > 100 ? 100 : (i < 0 ? 0 : i);
        progress = temp * 360 / 100;
        if (mMode == ProgressMode.PERCENTAGE) {
            setText(i + "%");
        }
    }

    public void setText(String text) {
        this.mCenterText = text;
        mCenterTextCharArray = this.mCenterText.split("\n");
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    /**
     * Set the bounds of the component
     */
    private void setupBounds() {
        int minValue = Math.min(layout_width, layout_height);

        int xOffset = layout_width - minValue;
        int yOffset = layout_height - minValue;

        paddingTop = this.getPaddingTop() + (yOffset / 2);
        paddingBottom = this.getPaddingBottom() + (yOffset / 2);
        paddingLeft = this.getPaddingLeft() + (xOffset / 2);
        paddingRight = this.getPaddingRight() + (xOffset / 2);

        rectBounds = new RectF(paddingLeft,
                paddingTop,
                this.getLayoutParams().width - paddingRight,
                this.getLayoutParams().height - paddingBottom);

        circleBounds = new RectF(paddingLeft + barWidth,
                paddingTop + barWidth,
                this.getLayoutParams().width - paddingRight - barWidth,
                this.getLayoutParams().height - paddingBottom - barWidth);
        circleInnerContour = new RectF(circleBounds.left + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.top + (rimWidth / 2.0f)
                + (contourSize / 2.0f), circleBounds.right - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.bottom
                - (rimWidth / 2.0f) - (contourSize / 2.0f));
        circleOuterContour = new RectF(circleBounds.left - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.top - (rimWidth / 2.0f)
                - (contourSize / 2.0f), circleBounds.right + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.bottom
                + (rimWidth / 2.0f) + (contourSize / 2.0f));

        fullRadius = (this.getLayoutParams().width - paddingRight - barWidth) / 2;
        circleRadius = (fullRadius - barWidth) + 1;
    }

    /**
     * Set the properties of the paints we're using to draw the progress wheel
     */
    private void setupPaints() {
        barPaint.setColor(barColor);
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Style.STROKE);
        barPaint.setStrokeWidth(barWidth);

        rimPaint.setColor(rimColor);
        rimPaint.setAntiAlias(true);
        rimPaint.setStyle(Style.STROKE);
        rimPaint.setStrokeWidth(rimWidth);

        circlePaint.setColor(circleColor);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Style.FILL);

        textPaint.setColor(textColor);
        textPaint.setStyle(Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);

        contourPaint.setColor(contourColor);
        contourPaint.setAntiAlias(true);
        contourPaint.setStyle(Style.STROKE);
        contourPaint.setStrokeWidth(contourSize);
    }

    public void start() {
        isStop = false;
        this.post(mLoopDrawRunnble);
    }

    public void stop() {
        isStop = true;
        reset();
    }
}
