package com.amy.drawhorizontallayout.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.amy.drawhorizontallayout.R;
import com.amy.drawhorizontallayout.util.LogUtil;
import com.amy.drawhorizontallayout.util.UIUtil;

import java.util.ArrayList;


public class VerticalTickMarkView extends View {

    protected Context mContext;
    protected Resources mResources;
    private int mScaledTouchSlop;

    private final int DEFAULT_MARKER_SIZE = 6;
    private final int DEFAULT_MARKER_DISTANCE_DP = 24;
    private final int DEFAULT_WIDTH;
    private final int DEFAULT_HEIGHT;

    private int mWidth;
    private int mHeight;
    private int mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom;

    private int mCurrentMarker = 0;

    private boolean isDragging;
    //Marker
    protected int mMarkerSize;
    protected ArrayList<Marker> mMarkers;
    //protected String[] mMarkerLabels;
    protected Drawable[] mMarkerDrawables;
    protected int[][] mMarkerMinMaxXY;
    protected final static int MarkerMinXIndex = 0;
    protected final static int MarkerMaxXIndex = 1;
    protected final static int MarkerMinYIndex = 2;
    protected final static int MarkerMaxYIndex = 3;

    /**
     * the distance of two nearest markers
     */
    private int mMarkerDistance;
    //Start, Mid and End marker drawable should have the same width, height
    private int mMarkerDrawableWidth;
    private int mMarkerDrawableHeight;


    private Drawable mStartMarkerDrawable;
    private Drawable mMidMarkerDrawable;
    private Drawable mEndMarkerDrawable;
    private Drawable mBridgeDrawable;
    private int mBridgeWidth;
    private int mBridgeHeight;

    //Thumb
    private Drawable mThumb;
    private int mThumbDrawableWidth;
    private int mThumbDrawableHeight;
    private int mThumbCenterX;
    private int mThumbCenterY;
    private int mThumbCenterYMin;
    private int mThumbCenterYMax;

    //Progress
    private Drawable mProgress;

    //Touch
    private float mTouchDownY;

    private TrackTouchListener mTrackListener;
    private OnMarkerChangeListener mMarkerChangeListener;

    public interface OnMarkerChangeListener {
        void onMarkerChanged(int newMarkerIndex);
    }

    interface TrackTouchListener {
        void onStartTrack(MotionEvent event);

        void onStopTrack(MotionEvent event);
    }

    public VerticalTickMarkView(Context context) {
        this(context, null, 0);
    }

    public VerticalTickMarkView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalTickMarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        mResources = context.getResources();

        DEFAULT_WIDTH = UIUtil.dp2px(mContext, 60);
        DEFAULT_HEIGHT = UIUtil.dp2px(mContext, 195);

        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.VerticalTickMarkView, defStyleAttr, 0);
        try {
            mMarkerSize = a.getInt(R.styleable.VerticalTickMarkView_vertical_marker_size, DEFAULT_MARKER_SIZE);
            verifyMarkerSize(mMarkerSize);
            initMarkers();
            mCurrentMarker = a.getInt(R.styleable.VerticalTickMarkView_vertical_current_marker, 0);
            /*
            int labelsArrayId = a.getResourceId(R.styleable.HorizontalTickMarkView_labels, 0);
            if (labelsArrayId > 0) {
                String[] labels = getResources().getStringArray(labelsArrayId);
                if (labels.length != mMarkerSize) {
                    throw new IllegalArgumentException("illegal argument, the labels array length should be equals with markerSize.");
                }
                setMarkerLabels(labels);
            }
            */
            mStartMarkerDrawable = getResources().getDrawable(
                    a.getResourceId(R.styleable.VerticalTickMarkView_vertical_start_marker_src, R.drawable.tick_mark_vertical_track_start));
            mEndMarkerDrawable = getResources().getDrawable(
                    a.getResourceId(R.styleable.VerticalTickMarkView_vertical_end_marker_src, R.drawable.tick_mark_vertical_track_end));
            mMidMarkerDrawable = getResources().getDrawable(
                    a.getResourceId(R.styleable.VerticalTickMarkView_vertical_mid_marker_src, R.drawable.tick_mark_vertical_track_mid));
            mBridgeDrawable = getResources().getDrawable(
                    a.getResourceId(R.styleable.VerticalTickMarkView_vertical_bridge_src, R.drawable.tick_mark_vertical_track_bridge));
            mThumb = getResources().getDrawable(
                    a.getResourceId(R.styleable.VerticalTickMarkView_vertical_thumb, R.drawable.tick_mark_progress_control_white));

            mProgress = getResources().getDrawable(R.drawable.tick_mark_vertical_progress);

            //
            mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            mPaddingLeft = getPaddingLeft();
            mPaddingRight = getPaddingRight();
            mPaddingTop = getPaddingTop();
            mPaddingBottom = getPaddingBottom();
            mMarkerDrawableWidth = mStartMarkerDrawable.getIntrinsicWidth();
            mMarkerDrawableHeight = mStartMarkerDrawable.getIntrinsicHeight();
            mThumbDrawableWidth = mThumb.getIntrinsicWidth();
            mThumbDrawableHeight = mThumb.getIntrinsicHeight();

        } finally {
            a.recycle();
        }

    }

    public void setMarkerDrawableEdge(int width, int height) {
        mMarkerDrawableWidth = width;
        mMarkerDrawableHeight = height;
    }

    public void setBridgeDrawableEdge(int width, int height) {
        mBridgeWidth = width;
        mBridgeHeight = height;
    }

    public void setOnMarkerChangeListener(OnMarkerChangeListener listener) {
        mMarkerChangeListener = listener;
    }

    private boolean verifyMarkerSize(int size) {
        if (size < 2) {
            throw new IllegalArgumentException("illegal marker size, the min marker size should be 2");
        }
        return true;
    }

    private void initMarkers() {
        if (mMarkers == null) {
            mMarkers = new ArrayList<Marker>(mMarkerSize);
        }
        mMarkers.clear();
        mMarkerMinMaxXY = new int[mMarkerSize][4];
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
            marker.centerX = mThumbCenterX;
            marker.centerY = mThumbCenterYMin + mMarkerDistance * i;

            marker.minX = marker.centerX - mMarkerDrawableWidth / 2;
            marker.maxX = marker.centerX + mMarkerDrawableWidth / 2;
            marker.minY = marker.centerY - mMarkerDrawableHeight / 2;
            marker.maxY = marker.centerY + mMarkerDrawableHeight / 2;
            mMarkerMinMaxXY[i][MarkerMinXIndex] = marker.minX - mThumbDrawableWidth / 2;
            mMarkerMinMaxXY[i][MarkerMaxXIndex] = marker.maxX + mThumbDrawableWidth / 2;
            mMarkerMinMaxXY[i][MarkerMinYIndex] = marker.minY - mThumbDrawableHeight / 2;
            mMarkerMinMaxXY[i][MarkerMaxYIndex] = marker.maxY + mThumbDrawableHeight / 2;
        }
    }

    private void initMarkerDistance() {
        mMarkerDistance =
                //UIUtil.dp2px(mContext, DEFAULT_MARKER_DISTANCE_DP);
        (mThumbCenterYMax - mThumbCenterYMin) / (mMarkerSize - 1);
    }


    //hide
    public boolean isInScrollingContainer() {
        ViewParent p = getParent();
        while (p != null && p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    private void updateThumbCenterY(MotionEvent event) {
        final int y = (int) event.getY();
        int result;
        if (y < mThumbCenterYMin) {
            result = mThumbCenterYMin;
        } else if (y > mThumbCenterYMax) {
            result = mThumbCenterYMax;
        } else {
            result = y;
        }
        mThumbCenterY = result;
        LogUtil.d("centerY : " + mThumbCenterY);
    }

    private void updateThumbBounds() {
        mThumb.setBounds(
                mThumbCenterX - mThumbDrawableWidth / 2,
                mThumbCenterY - mThumbDrawableHeight / 2,
                mThumbCenterX + mThumbDrawableWidth / 2,
                mThumbCenterY + mThumbDrawableHeight / 2
        );
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        LogUtil.d("mWidth:" + mWidth + "  widthSize:" + widthSize + "  widthMode:" + widthMode +
                "  mHeight:" + mHeight + "  heightSize:" + heightSize + "  heightMode:" + heightMode);
        setMeasuredDimension(mWidth, mHeight);

        initThumbCenterRange();
        initMarkerLocation();
        setMarker(mCurrentMarker);

    }

    private void initThumbCenterRange() {
        final int availableHeight = mHeight - mPaddingTop - mPaddingBottom;
        final int availableWidth = mWidth - mPaddingLeft - mPaddingRight;
        mThumbCenterYMin = mPaddingTop + mThumbDrawableHeight / 2;
        mThumbCenterYMax = availableHeight - mThumbDrawableHeight / 2;
        mThumbCenterY = mPaddingTop + mMarkerDrawableHeight / 2;
        mThumbCenterX = (availableWidth - mPaddingLeft - mPaddingRight) / 2;
        LogUtil.d("thumb min centerY : " + mThumbCenterYMin);
        LogUtil.d("thumb max centerY : " + mThumbCenterYMax);
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

        LogUtil.d("current : " + mCurrentMarker + " marker : " + index);
        mThumbCenterY = markerIndex2ThumbCenterY(index);
        updateThumbBounds();
        if (mCurrentMarker != index && mMarkerChangeListener != null) {
            mMarkerChangeListener.onMarkerChanged(index);
        }
        mCurrentMarker = index;
        markerFocusChange(index);
    }

    private int markerIndex2ThumbCenterY(int index) {
        return mMarkers.get(index).centerY;
    }

    private int thumbCenterY2MarkerIndex(int thumbCenterY) {
        return Math.round((thumbCenterY - mThumbCenterYMin) * 1f / mMarkerDistance);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mWidth = w;
        mHeight = h;
        initThumbCenterRange();
        initMarkerLocation();
        updateThumbPosInnerIfSizeChanged();
    }

    private void updateThumbPosInnerIfSizeChanged() {
        updateThumbCenter(markerIndex2ThumbCenterY(mCurrentMarker), mThumbCenterY);
    }

    private void updateThumbCenter(int x, int y) {
        mThumbCenterX = x;
        mThumbCenterY = y;
        updateThumbBounds();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawMarkers(canvas);

        drawProgress(canvas);

        drawThumb(canvas);

    }

    private void drawMarkers(Canvas canvas) {
        canvas.save();

        for (int i = 0; i < mMarkerSize; i++) {
            Marker marker = mMarkers.get(i);
            //LogUtil.d(
            //        "marker centerX : " + marker.centerX +
            //                " marker centerY : " + marker.centerY
            //);

            Drawable targetDrawable = null;
            boolean drawBridge = true;
            if (mMarkerDrawables != null
                    && i <= mMarkerDrawables.length
                    && mMarkerDrawables[i] != null) {
                //Draw custom drawables.
                targetDrawable = mMarkerDrawables[i];
                if (marker.type == Marker.TYPE_END) {
                    drawBridge = false;
                }
            } else {
                if (marker.type == Marker.TYPE_START) {
                    targetDrawable = mStartMarkerDrawable;
                } else if (marker.type == Marker.TYPE_END) {
                    targetDrawable = mEndMarkerDrawable;
                    drawBridge = false;
                } else {
                    targetDrawable = mMidMarkerDrawable;
                }
            }

            targetDrawable.setBounds(
                    marker.centerX - mMarkerDrawableWidth / 2,
                    marker.centerY - mMarkerDrawableHeight / 2,
                    marker.centerX + mMarkerDrawableWidth / 2,
                    marker.centerY + mMarkerDrawableHeight / 2
            );
            targetDrawable.draw(canvas);
            //LogUtil.d(
            //        "targetMarker : " + " type : " + marker.typeToString() +
            //                " \nleft : " + targetDrawable.getBounds().left +
            //                " \nright : " + targetDrawable.getBounds().right +
            //                " \ntop : " + targetDrawable.getBounds().top +
            //                " \nbottom : " + targetDrawable.getBounds().bottom
            //);

            if (drawBridge) {
                int bridgeWidth = mBridgeDrawable.getIntrinsicWidth();
                int top = marker.centerY + mMarkerDrawableHeight / 2;
                int bottom = top + mMarkerDistance - mMarkerDrawableHeight;
                mBridgeDrawable.setBounds(
                        marker.centerX - bridgeWidth / 2,
                        top,
                        marker.centerX + bridgeWidth / 2,
                        bottom
                );
                mBridgeDrawable.draw(canvas);
            /*
              LogUtil.i(
                        "bridge : " + " type : " + marker.typeToString() +
                                " \nleft : " + mBridgeDrawable.getBounds().left +
                                " \nright : " + mBridgeDrawable.getBounds().right +
                                " \ntop : " + mBridgeDrawable.getBounds().top +
                                " \nbottom : " + mBridgeDrawable.getBounds().bottom
                );
                */
            }
        }
        canvas.restore();
    }

    private void drawThumb(Canvas canvas) {
        canvas.save();
        mThumb.draw(canvas);
        canvas.restore();
    }

    private void drawProgress(Canvas canvas) {
        canvas.save();
        int progressWidth = mProgress.getIntrinsicWidth();
        mProgress.setBounds(
                mThumbCenterX - progressWidth / 2,
                mThumbCenterYMin - mMarkerDrawableHeight / 2,
                mThumbCenterX + progressWidth / 2,
                mThumbCenterY
        );
        mProgress.draw(canvas);
        canvas.restore();
    }

    private void markerFocusChange(final int marker) {
        //do animation, change label's position
        synchronized (mMarkers) {
            for (int i = 0; i < mMarkerSize; i++) {
                if (i != marker) {
                    mMarkers.get(i).focused = false;
                } else {
                    mMarkers.get(i).focused = true;
                }
                mMarkers.get(i).animateLabel();
            }
        }

    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch(MotionEvent event) {
        isDragging = true;
        //LogUtil.d("isDragging : " + isDragging);
        if (mTrackListener != null) {
            mTrackListener.onStartTrack(event);
        }
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    void onStopTrackingTouch(MotionEvent event) {
        isDragging = false;
        //LogUtil.d("isDragging : " + isDragging);
        if (mTrackListener != null) {
            mTrackListener.onStopTrack(event);
        }
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    void setTrackTouchListener(TrackTouchListener listener) {
        mTrackListener = listener;
    }

    private void trackTouchEvent(MotionEvent event) {
        updateThumbCenterY(event);

        int marker = thumbCenterY2MarkerIndex(mThumbCenterY);
        if (marker != mCurrentMarker) {
            markerFocusChange(marker);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setMarker(marker);
                return;
        }

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
                if ((isInScrollingContainer())) {
                    mTouchDownY = event.getY();
                } else {
                    if (isTouchingMarker(event) < 0) {
                        return false;
                    }
                    setPressed(true);
                    onStartTrackingTouch(event);
                    trackTouchEvent(event);
                    attemptClaimDrag();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    trackTouchEvent(event);
                    updateThumbBounds();
                } else {
                    final float y = event.getY();
                    if (Math.abs(y - mTouchDownY) > mScaledTouchSlop) {
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
                if (isDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch(event);
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold should
                    // be interpreted as a tap-seek to that location.
                    onStartTrackingTouch(event);
                    trackTouchEvent(event);
                    updateThumbBounds();
                    onStopTrackingTouch(event);
                }
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    trackTouchEvent(event);
                    updateThumbBounds();
                    onStopTrackingTouch(event);
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;

    }

    public int isTouchingMarker(MotionEvent event) {
        int eX = (int) event.getX();
        int eY = (int) event.getY();
        //LogUtil.d("eX : " + eX + " eY : " + eY);
        for (int i = 0; i < mMarkerMinMaxXY.length; i++) {
            //LogUtil.i("minX : " + mMarkerMinMaxXY[i][MarkerMinXIndex] + " maxX : " + mMarkerMinMaxXY[i][MarkerMaxXIndex] +
            //      "minY : " + mMarkerMinMaxXY[i][MarkerMinYIndex] + " maxY : " + mMarkerMinMaxXY[i][MarkerMaxYIndex]);
            if (eX >= mMarkerMinMaxXY[i][MarkerMinXIndex] && eX <= mMarkerMinMaxXY[i][MarkerMaxXIndex]
                    && eY >= mMarkerMinMaxXY[i][MarkerMinYIndex] && eY <= mMarkerMinMaxXY[i][MarkerMaxYIndex]) {
                LogUtil.d("isTouchingMarker  : " + i);
                return i;
            }
        }
        LogUtil.d("isTouchingMarker  : " + false);
        return -1;
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

    public class Marker {
        public static final int TYPE_START = 0;
        public static final int TYPE_MID = 1;
        public static final int TYPE_END = 2;

        int type;
        int index;
        int centerX;
        int centerY;
        int minX;
        int maxX;
        int minY;
        int maxY;
        //float labelTransY = LABEL_TRANS_Y_MAX;
        //int labelColor = LABEL_COLOR_UNSELECTED;
        //String label;

        boolean focused;

        public String typeToString() {
            if (type == TYPE_START) {
                return "typeStart";
            } else if (type == TYPE_MID) {
                return "typeEnd";
            } else {
                return "typeMid";
            }
        }

        public void animateLabel() {
         /*
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
        */
        }
    }

}
