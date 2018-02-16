package com.chthakur.playapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;


public class CallLayoutWrapper extends FrameLayout {
    private static final String TAG = CallLayoutWrapper.class.getSimpleName();

    private CallLayout callLayout;

    public CallLayoutWrapper(Context context) {
        this(context, null, 0);
    }

    public CallLayoutWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CallLayoutWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gvc_grid_view_core_v2, null);
        this.addView(view);
        callLayout = (CallLayout)findViewById(R.id.gvc_custom_layout);
    }

    public void scheduleTest() {
        callLayout.scheduleRun();
    }
}
