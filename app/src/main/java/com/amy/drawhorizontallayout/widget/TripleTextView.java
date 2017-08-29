package com.amy.drawhorizontallayout.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amy.drawhorizontallayout.R;

public class TripleTextView extends RelativeLayout {
    private Context mContext;
    private Resources mResources;

    private View mRoot;
    private TextView mTop;
    private TextView mCenter;
    private TextView mBottom;

    public TripleTextView(Context context) {
        this(context, null, 0);
    }

    public TripleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TripleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        mResources = mContext.getResources();

        mRoot = LayoutInflater.from(mContext).inflate(R.layout.triple_text_view, this, true);
        mTop = (TextView) myFindViewById(R.id.triple_textView_top);
        mCenter = (TextView) myFindViewById(R.id.triple_textView_center);
        mBottom = (TextView) myFindViewById(R.id.triple_textView_bottom);

        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.TripleTextView, defStyleAttr, 0);

        String topText = a.getString(R.styleable.TripleTextView_tripleTextView_topText);
        setTopText(topText);

        String centerText = a.getString(R.styleable.TripleTextView_tripleTextView_centerText);
        setCenterText(centerText);

        String bottomText = a.getString(R.styleable.TripleTextView_tripleTextView_bottomText);
        setBottomText(bottomText);

        a.recycle();
    }

    private View myFindViewById(@IdRes int id) {
        return mRoot.findViewById(id);
    }

    public void setTopText(String str) {
        if (!TextUtils.isEmpty(str)) {
            mTop.setText(str);
        }
    }

    public void setCenterText(String str) {
        if (!TextUtils.isEmpty(str)) {
            mCenter.setText(str);
        }
    }

    public void setBottomText(String str) {
        if (!TextUtils.isEmpty(str)) {
            mBottom.setText(str);
        }
    }

}
