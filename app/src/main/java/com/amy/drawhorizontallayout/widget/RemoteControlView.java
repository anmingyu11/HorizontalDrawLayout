package com.amy.drawhorizontallayout.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;

import com.amy.drawhorizontallayout.R;

public class RemoteControlView extends TickMarkView {

    private final String[] mLabels = new String[]{
            getString(R.string.close),
            getString(R.string.fast),
            getString(R.string.sleep),
            getString(R.string.auto),
            getString(R.string.manual)
    };

    private final Drawable[] mDrawables = new Drawable[]{
            getDrawable(R.drawable.tick_mark_1_close),
            getDrawable(R.drawable.tick_mark_2_fast),
            getDrawable(R.drawable.tick_mark_3_sleep),
            getDrawable(R.drawable.tick_mark_4_auto),
            getDrawable(R.drawable.tick_mark_5_manual),
    };

    public RemoteControlView(Context context) {
        this(context, null, 0);
    }

    public RemoteControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoteControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setMarkerLabels(mLabels);
        setMarkerDrawables(mDrawables);
        final int markerDrawableWidth = mMarkerDrawables[0].getIntrinsicWidth();
        final int markerDrawableHeight = mMarkerDrawables[0].getIntrinsicHeight();
        setMarkerDrawableEdgeLength(markerDrawableWidth, markerDrawableHeight);

        invalidate();
    }

    private int getColor(@ColorRes int colorRes){
        return mResources.getColor(colorRes);
    }

    private String getString(@StringRes int id) {
        return mResources.getString(id);
    }

    private Drawable getDrawable(@DrawableRes int id) {
        return mResources.getDrawable(id);
    }
}
