package com.amy.drawhorizontallayout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;

import com.amy.drawhorizontallayout.util.UIUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private Resources mResources;

    private LineChart mLineChart;

    private String[] mXAxisLabels = new String[]{
            "5.23",
            "5.24",
            "5.25",
            "5.26",
            "今天"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = this;
        mResources = getResources();

        setContentView(R.layout.activity_main);

        mLineChart = (LineChart) findViewById(R.id.lineChart);

        //Description
        mLineChart.getLegend().setEnabled(false);
        mLineChart.getDescription().setEnabled(false);

        //Touch
        mLineChart.setTouchEnabled(true);
        mLineChart.setDragEnabled(false);
        mLineChart.setScaleEnabled(false);
        mLineChart.setPinchZoom(false);


        YAxis yLeft = mLineChart.getAxisLeft();
        yLeft.setEnabled(false);

        YAxis yRight = mLineChart.getAxisRight();
        yRight.setEnabled(true);

        XAxis x = mLineChart.getXAxis();
        x.setEnabled(true);
        //Label
        x.setDrawLabels(true);
        x.setLabelCount(mXAxisLabels.length, true);
        x.setTextSize(UIUtil.sp2dp(mContext, 10));
        x.setTextColor(mResources.getColor(R.color.xAxis_label_color));
        x.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        //x轴的数据标识线
        x.setDrawGridLines(false);
        x.setAxisLineWidth(UIUtil.px2dip(mContext, 2f));
        x.setAxisLineColor(mResources.getColor(R.color.xAxis_label_color));
        //x轴的label
        x.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mXAxisLabels[(int) value];
            }
        });
        //offset

        mLineChart.getAxisRight().setEnabled(false);

        setData(5, 5);

        List<ILineDataSet> sets = mLineChart.getLineData().getDataSets();
        for (ILineDataSet iSet : sets) {
            LineDataSet set = (LineDataSet) iSet;
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        }

        mLineChart.invalidate();
    }

    private ArrayList<Entry> createDataSet(int range, int count) {

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult) + 20;// + (float)
            // ((mult *
            // 0.1) / 10);
            Entry entry;
            entry = new Entry(i, val);
            yVals.add(entry);
        }

        return yVals;
    }

    private LineDataSet createDataSet(int range, int count, @ColorRes int color, Drawable icon) {

        LineDataSet set;

        List<Entry> entries = createDataSet(range, count);
        for (int i = 0; i < entries.size(); i++) {
            if (i == entries.size() - 1) {
                Entry entry = entries.get(i);
                entry.setIcon(icon);
                entry.setData("dada");
            }
        }

        set = new LineDataSet(
                entries,
                "DataSet 1"
        );

        //Cubic
        set.setDrawIcons(true);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);

        //Line params
        set.setLineWidth(Utils.convertPixelsToDp(4f));
        set.setColor(mResources.getColor(color));

        //Values
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setValueTextSize(UIUtil.sp2dp(mContext, 10));
        set.setValueTextColor(Color.parseColor("#33000000"));
/*        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

                LogUtil.d("data is null " + (entry.getData() == null));
                LogUtil.d("transX : " + viewPortHandler.getTransX() + " transY : " + viewPortHandler.getTransY());
                viewPortHandler.translate(new float[]{500, 500});
                if (entry.getData() != null) {
                    return (String) entry.getData();
                } else {
                    return "";
                }
            }
        });*/

        set.setHighlightEnabled(false);

        return set;
    }

    private void setData(int range, int count) {

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        dataSets.add(createDataSet(
                range,
                count,
                R.color.line_color_green,
                mResources.getDrawable(R.drawable.circle_green)
        ));
        dataSets.add(createDataSet(
                range,
                count,
                R.color.line_color_grey,
                mResources.getDrawable(R.drawable.circle_grey)
        ));

        LineData data = new LineData(dataSets);

        mLineChart.setData(data);
    }

}
