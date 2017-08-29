package com.amy.drawhorizontallayout.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amy.drawhorizontallayout.R;

public class DoubleTextView extends RelativeLayout {
    private Context mContext;

    private View mContainer;

    private TextView mTopText;
    private TextView mBottomText;

    public DoubleTextView(Context context) {
        this(context, null, 0);
    }

    public DoubleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleTextView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        mContext = context;
        Resources resources = mContext.getResources();

        mContainer = LayoutInflater.from(mContext).inflate(R.layout.double_textview, this, true);

        mTopText = (TextView) myFindViewById(R.id.top_tv);
        mBottomText = (TextView) myFindViewById(R.id.bottom_tv);

        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.DoubleTextView, defStyleAttr, 0);

        //Top
        String topText = a.getString(R.styleable.DoubleTextView_doubleTextView_topText);
        mTopText.setText(topText);

        ColorStateList topTextColor = a.getColorStateList(R.styleable.DoubleTextView_doubleTextView_topTextColor);
        if (topTextColor != null) {
            mTopText.setTextColor(topTextColor);
        }

        /*
        int textSize = a.getDimensionPixelSize(R.styleable.DoubleTextView_doubleTextView_topTextSize, 0);
        if (textSize > 0) {
            mTopText.setTextSize(textSize);
        } else {
            mTopText.setTextSize(resources.getDimensionPixelSize(R.dimen.doubleTextView_default_top_textSize));
        }
        */

        //Bottom
        CharSequence bottomText = a.getText(R.styleable.DoubleTextView_doubleTextView_bottomText);
        mBottomText.setText(bottomText);

        ColorStateList bottomTextColor = a.getColorStateList(R.styleable.DoubleTextView_doubleTextView_bottomTextColor);
        if (bottomTextColor != null) {
            mBottomText.setTextColor(bottomTextColor);
        }

        /*
        int bottomTextSize = a.getDimensionPixelSize(R.styleable.DoubleTextView_doubleTextView_bottomTextSize, 0);
        if (bottomTextSize > 0) {
            mBottomText.setTextSize(bottomTextSize);
        } else {
            mBottomText.setTextSize(resources.getDimensionPixelSize(R.dimen.doubleTextView_default_bottom_textSize));
        }
        */

        a.recycle();
    }

    private View myFindViewById(@IdRes int id) {
        return mContainer.findViewById(id);
    }

    public void setTopText(String str) {
        mTopText.setText(str);
    }

    public void setBottomText(String str) {
        mBottomText.setText(str);
    }

    public void setTopTextSize(float size) {
        mTopText.setTextSize(size);
    }

    public void setBottomTextSize(float size) {
        mBottomText.setTextSize(size);
    }

    public void setTopTextColor(ColorStateList color) {
        mTopText.setTextColor(color);
    }

    public void setBottomTextColor(ColorStateList color) {
        mBottomText.setTextColor(color);
    }
}
