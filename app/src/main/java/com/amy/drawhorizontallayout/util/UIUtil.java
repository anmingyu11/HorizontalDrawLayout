package com.amy.drawhorizontallayout.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

public class UIUtil {

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int sp2dp(Context context, float spValue) {
        return px2dip(context, sp2px(context, spValue));
    }

    public static Drawable view2Drawable(View view){
        Bitmap snapshot = view2Bitmap(view);
        Drawable drawable = new BitmapDrawable(snapshot);
        return drawable;
    }

    public static Drawable layout2Drawable(Context context, int layoutId) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutId, null);

        Bitmap snapshot = view2Bitmap(view);
        Drawable drawable = new BitmapDrawable(snapshot);
        return drawable;
    }

    public static Bitmap view2Bitmap(View view) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        view.layout(0, 0, 300,300);  //根据字符串的长度显示view的宽度
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }


}
