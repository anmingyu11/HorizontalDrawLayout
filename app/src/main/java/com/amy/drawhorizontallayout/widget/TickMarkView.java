package com.amy.drawhorizontallayout.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.amy.drawhorizontallayout.R;

import java.util.ArrayList;


public class TickMarkView extends View {

    private static final String TAG = TickMarkView.class.getSimpleName();
    static final boolean DBG = false;

    protected Context mContext;
    protected Resources mResources;

    private static final int DEF_MARKER_SIZE = 5;
    //Marker
    protected int mMarkerSize;
    protected ArrayList<Marker> mMarkers;
    protected String[] mMarkerLabels;
    protected Drawable[] mMarkerDrawables;

    private Drawable mThumb;
    private Drawable mStartMarkerDrawable;
    private Drawable mMidMarkerDrawable;
    private Drawable mEndMarkerDrawable;

    Drawable mBridgeDrawable;

    private static final int DEFAULT_WIDTH = 1080;
    private static int DEFAULT_HEIGHT; // 66dp default

    static final int LABEL_COLOR_SELECTED = Color.parseColor("#2bc72b");
    static final int LABEL_COLOR_UNSELECTED = Color.parseColor("#66000000");

    private static int LABEL_TOP_MARGIN; // 6dp
    private static int LABEL_BOTTOM_MARGIN; //8dp
    //label 在Y轴上可移动的距离
    private static int LABEL_INNER_TRANSABLE_Y; // 8dp
    private static int LABEL_TRANS_Y_MIN;
    private static int LABEL_TRANS_Y_MAX;

    private int mPaddingLeft, mPaddingTop, mPaddingRight;

    private int mWidth, mHeight;
    /**
     * the distance of two nearest markers
     */
    private int mMarkerDistance;
    //Start, Mid and End marker drawable should have the same width, height
    private int mMarkerDrawableWidth;
    private int mMarkerDrawableHeight;
    private int mThumbDrawableWidth;
    private int mThumbDrawableHeight;

    private Paint mLabelPaint;
    private Paint.FontMetrics mLabelFontMetrics;

    private int mScaledTouchSlop;
    private boolean mIsDragging;
    private int mCurrentMarker;
    private float mTouchDownX;
    private int mFocusedMarker;

    private OnMarkerChangeListener mMarkerChangeListener;

    public interface OnMarkerChangeListener {
        void onMarkerChanged(int newMarkerIndex);
    }

    public TickMarkView(Context context) {
        this(context, null);
    }

    public TickMarkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TickMarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        mResources = mContext.getResources();

        initStaticConstants();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalTickMarkView, defStyleAttr, 0);
        try {
            mMarkerSize = a.getInt(R.styleable.HorizontalTickMarkView_marker_size, DEF_MARKER_SIZE);
            verifyMarkerSize(mMarkerSize);
            initMarkers();
            mCurrentMarker = a.getInt(R.styleable.HorizontalTickMarkView_marker, 0);
            int labelsArrayId = a.getResourceId(R.styleable.HorizontalTickMarkView_labels, 0);
            if (labelsArrayId > 0) {
                String[] labels = getResources().getStringArray(labelsArrayId);
                if (labels.length != mMarkerSize) {
                    throw new IllegalArgumentException("illegal argument, the labels array length should be equals with markerSize.");
                }
                setMarkerLabels(labels);
            }
            mStartMarkerDrawable = getResources().getDrawable(
                    a.getResourceId(R.styleable.HorizontalTickMarkView_start_marker_src, R.drawable.tick_mark_track_start));
            mEndMarkerDrawable = getResources().getDrawable(
                    a.getResourceId(R.styleable.HorizontalTickMarkView_end_marker_src, R.drawable.tick_mark_track_end));
            mMidMarkerDrawable = getResources().getDrawable(
                    a.getResourceId(R.styleable.HorizontalTickMarkView_mid_marker_src, R.drawable.tick_mark_track_mid));
            mBridgeDrawable = getResources().getDrawable(
                    a.getResourceId(R.styleable.HorizontalTickMarkView_bridge_src, R.drawable.thin_tick_mark_track_bridge));
            mThumb = getResources().getDrawable(
                    a.getResourceId(R.styleable.HorizontalTickMarkView_thumb, R.drawable.tick_mark_progress_control));

            initLabelPaintParams();

        } finally {
            a.recycle();
        }
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mMarkerDrawableWidth = mStartMarkerDrawable.getIntrinsicWidth();
        mMarkerDrawableHeight = mStartMarkerDrawable.getIntrinsicHeight();
        getThumbSize();
    }

    private void initStaticConstants() {
        DEFAULT_HEIGHT = dp2px(getContext(), 66);
        LABEL_TOP_MARGIN = dp2px(getContext(), 6);
        LABEL_BOTTOM_MARGIN = dp2px(getContext(), 8);
        LABEL_INNER_TRANSABLE_Y = dp2px(getContext(), 8);
        LABEL_TRANS_Y_MIN = 0;
        LABEL_TRANS_Y_MAX = LABEL_TRANS_Y_MIN + LABEL_INNER_TRANSABLE_Y;
    }

    public void setMarkerDrawableEdgeLength(int width, int height) {
        mMarkerDrawableWidth = width;
        mMarkerDrawableHeight = height;
    }

    private void getThumbSize() {
        mThumbDrawableWidth = mThumb.getIntrinsicWidth();
        mThumbDrawableHeight = mThumb.getIntrinsicHeight();
    }

    public void setOnMarkerChangeListener(OnMarkerChangeListener listener) {
        mMarkerChangeListener = listener;
    }

    public void setLabelTextTextSize(int size) {
        mLabelPaint.setTextSize(size);
    }

    private void initLabelPaintParams() {
        if (mLabelPaint == null) {
            mLabelPaint = new Paint();
        }
        mLabelPaint.reset();
        mLabelPaint.setTextSize(dp2px(mContext,10));
        mLabelPaint.setStyle(Paint.Style.FILL);
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setTextAlign(Paint.Align.CENTER);
        mLabelFontMetrics = mLabelPaint.getFontMetrics();
    }

    private void initMarkers() {
        if (mMarkers == null) {
            mMarkers = new ArrayList<>(mMarkerSize);
        }
        mMarkers.clear();
        for (int i = 0; i < mMarkerSize; i++) {
            Marker marker = new Marker();
            if (i == 0) {
                marker.type = Marker.TYPE_START;
            } else if (i == mMarkerSize - 1) {
                marker.type = Marker.TYPE_END;
            } else {
                marker.type = Marker.TYPE_MID;
            }
            marker.index = i;
            mMarkers.add(marker);
        }
        mMarkers.trimToSize();
    }

    private void initMarkerLocation() {
        initMarkerDistance();

        for (int i = 0; i < mMarkerSize; i++) {
            Marker marker = mMarkers.get(i);
            marker.centerX = mThumbCenterXMin + mMarkerDistance * i;
            marker.centerY = mThumbCenterY;
        }
    }

    public void setMarkerLabels(String[] labels) {
        if (labels == null) {
            throw new NullPointerException();
        }
        if (labels.length != mMarkerSize) {
            throw new IllegalArgumentException();
        }
        mMarkerLabels = labels;
    }

    public void setMarkerDrawables(Drawable[] markerDrawables) {
        if (markerDrawables == null) {
            throw new NullPointerException();
        }
        if (markerDrawables.length != mMarkerSize) {
            throw new IllegalArgumentException();
        }
        mMarkerDrawables = markerDrawables;
    }

    public void setMarkerSize(int size) {
        verifyMarkerSize(size);

        if (size == mMarkerSize) return;
        mMarkerSize = size;
        initMarkers();
        initMarkerLocation();
    }

    public int getMarkerSize() {
        return mMarkerSize;
    }

    private boolean verifyMarkerSize(int size) {
        if (size < 2) {
            throw new IllegalArgumentException("illegal marker size, the min marker size should be 2");
        }
        return true;
    }

    //hideeeee
    public boolean isInScrollingContainerr() {
        ViewParent p = getParent();
        while (p != null && p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    /**
     * @see {@link android.widget.AbsSeekBar#onTouchEvent }
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if ((isInScrollingContainerr())) {
                    mTouchDownX = event.getX();
                } else {
                    setPressed(true);
                    if (mThumb != null) {
                        invalidate(mThumb.getBounds()); // This may be within the padding region
                    }
                    onStartTrackingTouch(event);
                    trackTouchEvent(event);
                    attemptClaimDrag();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsDragging) {
                    trackTouchEvent(event);
                } else {
                    final float x = event.getX();
                    if (Math.abs(x - mTouchDownX) > mScaledTouchSlop) {
                        setPressed(true);
                        if (mThumb != null) {
                            invalidate(mThumb.getBounds()); // This may be within the padding region
                        }
                        onStartTrackingTouch(event);
                        trackTouchEvent(event);
                        attemptClaimDrag();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch(event);
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold should
                    // be interpreted as a tap-seek to that location.
                    onStartTrackingTouch(event);
                    trackTouchEvent(event);
                    onStopTrackingTouch(event);
                }
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch(event);
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private int mThumbCenterX;
    private int mThumbCenterY;
    private int mThumbCenterXMin;
    private int mThumbCenterXMax;

    private void trackTouchEvent(MotionEvent event) {
        mThumbCenterX = getThumbCenterXByTouchEvent(event);

        int marker = thumbCenterXToMarkerIndex(mThumbCenterX);
        if (marker != mFocusedMarker) {
            markerFocusChanged(marker);
        }
        mFocusedMarker = marker;
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setMarker(marker);
                return;
        }

        updateThumbBounds();
    }

    int getThumbCenterXByTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        int result;
        if (x < mThumbCenterXMin) {
            result = mThumbCenterXMin;
        } else if (x > mThumbCenterXMax) {
            result = mThumbCenterXMax;
        } else {
            result = x;
        }
        return result;
    }

    private void updateThumbBounds() {
        mThumb.setBounds(mThumbCenterX - mThumbDrawableWidth / 2,
                mThumbCenterY - mThumbDrawableHeight / 2,
                mThumbCenterX + mThumbDrawableWidth / 2,
                mThumbCenterY + mThumbDrawableHeight / 2);
        invalidate();
    }

    /**
     * @param index the marker index to be selected
     */
    public void setMarker(int index) {
        //    if (index == mCurrentMarker) return;

        if (index < 0 || index > mMarkerSize - 1) {
            throw new IllegalArgumentException("invalid marker index=" + index +
                    ", the index should between 0 and " + (mMarkerSize - 1));
        }

        mThumbCenterX = markerIndexToThumbCenterX(index);
        updateThumbBounds();
        if (mCurrentMarker != index && mMarkerChangeListener != null) {
            mMarkerChangeListener.onMarkerChanged(index);
        }
        mCurrentMarker = index;
        markerFocusChanged(index);
    }

    private int markerIndexToThumbCenterX(int index) {
        return mMarkers.get(index).centerX; //mMarkerDistance * index + mThumbCenterXMin;
    }

    int thumbCenterXToMarkerIndex(int thumbCenterX) {
        return Math.round((thumbCenterX - mThumbCenterXMin) * 1f / mMarkerDistance);
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch(MotionEvent event) {
        mIsDragging = true;
        if (mTrackListener != null) {
            mTrackListener.onStartTrack(event);
        }
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    void onStopTrackingTouch(MotionEvent event) {
        mIsDragging = false;
        if (mTrackListener != null) {
            mTrackListener.onStopTrack(event);
        }
    }

    interface TrackTouchListener {
        void onStartTrack(MotionEvent event);

        void onStopTrack(MotionEvent event);
    }

    private TrackTouchListener mTrackListener;

    void setTrackTouchListener(TrackTouchListener listener) {
        mTrackListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        initThumbCenterRange();
        initMarkerLocation();
        updateThumbPosInnerIfSizeChanged();
    }

    void updateThumbPosInnerIfSizeChanged() {
        updateThumbCenter(markerIndexToThumbCenterX(mCurrentMarker), mThumbCenterY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            mWidth = DEFAULT_WIDTH;
        } else {
            mWidth = widthSize;
        }
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            mHeight = DEFAULT_HEIGHT;
        } else {
            mHeight = heightSize;
        }
        if (DBG) {
            Log.d(TAG, "mWidth:" + mWidth + "  widthSize:" + widthSize + "  widthMode:" + widthMode +
                    "  mHeight:" + mHeight + "  heightSize:" + heightSize + "  heightMode:" + heightMode);
        }
        setMeasuredDimension(mWidth, mHeight);

        initThumbCenterRange();
        initMarkerLocation();
        setMarker(mCurrentMarker);
    }

    private void initThumbCenterRange() {
        final int avalilableWidth = mWidth - mPaddingLeft - mPaddingRight;
        mThumbCenterXMin = mPaddingLeft + mThumbDrawableWidth / 2;
        mThumbCenterXMax = mPaddingLeft + avalilableWidth - mThumbDrawableWidth / 2;
        int fontHeight = (int) (mLabelFontMetrics.descent - mLabelFontMetrics.ascent);
        mThumbCenterY = mPaddingTop + LABEL_TOP_MARGIN + fontHeight + LABEL_INNER_TRANSABLE_Y + mMarkerDrawableHeight / 2 +
                LABEL_BOTTOM_MARGIN;
    }

    int getThumbMinX() {
        return mThumbCenterXMin;
    }

    int getThumbMaxX() {
        return mThumbCenterXMax;
    }

    int getThumbY() {
        return mThumbCenterY;
    }

    int getThumbX() {
        return mThumbCenterX;
    }

    void updateThumbCenter(int x, int y) {
        mThumbCenterX = x;
        mThumbCenterY = y;
        updateThumbBounds();
    }

    private void initMarkerDistance() {
        mMarkerDistance = (mThumbCenterXMax - mThumbCenterXMin) / (mMarkerSize - 1);
    }

    private Paint mTestPaint;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawDebugLineIfDebugMode(canvas);

        drawLabels(canvas);

        drawMarkers(canvas);

        drawThumb(canvas);
    }

    void drawDebugLineIfDebugMode(Canvas canvas) {
        if (DBG) {
            if (mTestPaint == null) {
                mTestPaint = new Paint();
            }
            mTestPaint.setStyle(Paint.Style.STROKE);
            mTestPaint.setAntiAlias(true);
            mTestPaint.setStrokeWidth(6);
            mTestPaint.setColor(Color.WHITE);
            canvas.drawRect(0, 0, mWidth, mHeight, mTestPaint);

            mTestPaint.setStyle(Paint.Style.FILL);
            mTestPaint.setStrokeWidth(1);
            mTestPaint.setColor(Color.BLACK);
            canvas.drawLine(mPaddingLeft, 0, mPaddingLeft, mHeight, mTestPaint);
            canvas.drawLine(mWidth - mPaddingRight, 0,
                    mWidth - mPaddingRight, mHeight, mTestPaint);
            mTestPaint.setColor(Color.RED);
            canvas.drawLine(mThumbCenterXMin, 0, mThumbCenterXMin, mHeight, mTestPaint);
            canvas.drawLine(mThumbCenterXMax, 0, mThumbCenterXMax, mHeight, mTestPaint);
        }
    }

    private void drawMarkers(Canvas canvas) {
        int deltaWidth = Math.max(0, mThumbDrawableWidth - mMarkerDrawableWidth);
        int bridgeWidth = (mWidth - mPaddingLeft - mPaddingRight - deltaWidth - mMarkerDrawableWidth * mMarkerSize) / (mMarkerSize - 1);
        canvas.save();
        for (int i = 0; i < mMarkerSize; i++) {
            Marker marker = mMarkers.get(i);
            int topStart = marker.centerY - mMarkerDrawableHeight / 2;
            Drawable targetDrawable;
            boolean drawBridge = true;
            if (mMarkerDrawables != null
                    && i <= mMarkerDrawables.length
                    && mMarkerDrawables[i] != null) {
                //Draw custom drawables.
                targetDrawable = mMarkerDrawables[i];
                if (marker.type == Marker.TYPE_END) {
                    drawBridge = false;
                }
            } else if (marker.type == Marker.TYPE_START) {
                targetDrawable = mStartMarkerDrawable;
            } else if (marker.type == Marker.TYPE_END) {
                targetDrawable = mEndMarkerDrawable;
                drawBridge = false;
            } else {
                targetDrawable = mMidMarkerDrawable;
            }

            targetDrawable.setBounds(
                    marker.centerX - mMarkerDrawableWidth / 2,
                    topStart,
                    marker.centerX + mMarkerDrawableWidth / 2,
                    mMarkerDrawableHeight + topStart);
            targetDrawable.draw(canvas);
            if (drawBridge) {
                int bridgeHeight = mBridgeDrawable.getIntrinsicHeight();
                int bridgeTop = marker.centerY - bridgeHeight / 2;
                int bridgeBottom = bridgeTop + bridgeHeight;
                mBridgeDrawable.setBounds(
                        marker.centerX + mMarkerDrawableWidth / 2,
                        bridgeTop,
                        marker.centerX + mMarkerDrawableWidth / 2 + bridgeWidth,
                        bridgeBottom);
                mBridgeDrawable.draw(canvas);
            }
        }
        canvas.restore();
    }

    void drawThumb(Canvas canvas) {
        canvas.save();
        mThumb.draw(canvas);
        canvas.restore();
    }

    Drawable getThumbDrawable() {
        return mThumb;
    }

    void setThumbDrawable(Drawable thumb) {
        mThumb = thumb;
        getThumbSize();
        initThumbCenterRange();
        updateThumbBounds();
    }

    private void animateThumbToMarker(int destIndex) {
        // TODO:
    }

    private void drawLabels(Canvas canvas) {
        canvas.save();
        float y = Math.abs(mLabelFontMetrics.ascent) + mPaddingTop + LABEL_TOP_MARGIN;
        for (int i = 0; i < mMarkerSize; i++) {
            Marker marker = mMarkers.get(i);
            mLabelPaint.setColor(marker.labelColor);
            mLabelPaint.setTypeface(Typeface.defaultFromStyle(marker.focused ? Typeface.BOLD : Typeface.NORMAL));
            if (mMarkerLabels != null) {
                marker.label = mMarkerLabels[i];
            }
            String text = marker.label == null ? String.valueOf(marker.index) : marker.label;
            canvas.drawText(text, marker.centerX, y + marker.labelTransY, mLabelPaint);
        }
        canvas.restore();
    }

    public float getUnfocusedNormalLabelY() {
        float y = Math.abs(mLabelFontMetrics.ascent) + mPaddingTop + LABEL_TOP_MARGIN;
        return LABEL_TRANS_Y_MAX + y;
    }

    private void markerFocusChanged(final int focusedIndex) {
        //do animation, change label's position
        synchronized (mMarkers) {
            Marker marker = mMarkers.get(focusedIndex);
            if (!marker.focused) {
                marker.focused = true;
                for (int i = 0; i < mMarkerSize; i++) {
                    if (i != focusedIndex) {
                        mMarkers.get(i).focused = false;
                    }
                    mMarkers.get(i).animateLabel();
                }
            }
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState ss = new SavedState(parcelable);
        ss.currentMarker = mCurrentMarker;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setMarker(ss.currentMarker);
    }


    public class Marker {
        public static final int TYPE_START = 0;
        public static final int TYPE_MID = 1;
        public static final int TYPE_END = 2;

        int type;
        int index;
        int centerX;
        int centerY;
        float labelTransY = LABEL_TRANS_Y_MAX;
        int labelColor = LABEL_COLOR_UNSELECTED;
        String label;

        boolean focused;

        public void animateLabel() {
            boolean animating = true;
            float moveSpeed = 4f;
            if (focused) {//up animation
                labelColor = LABEL_COLOR_SELECTED;
                labelTransY -= moveSpeed;
                if (labelTransY < LABEL_TRANS_Y_MIN) {
                    labelTransY = LABEL_TRANS_Y_MIN;
                    animating = false;
                }
            } else {//down animation
                labelColor = LABEL_COLOR_UNSELECTED;
                labelTransY += moveSpeed;
                if (labelTransY > LABEL_TRANS_Y_MAX) {
                    labelTransY = LABEL_TRANS_Y_MAX;
                    animating = false;
                }
            }

            if (animating) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateLabel();
                    }
                }, 10);
            }

            invalidate();
        }
    }

    static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    static class SavedState extends BaseSavedState {

        int currentMarker;

        public SavedState(Parcel source) {
            super(source);
            currentMarker = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentMarker);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
