package com.amy.drawhorizontallayout;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.amy.drawhorizontallayout.widget.TickMarkView;

public class MainActivity extends AppCompatActivity {

    private TickMarkView mTickMarkView;

    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mResources = getResources();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }
}
