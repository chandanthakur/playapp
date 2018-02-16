package com.chthakur.playapp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.HashMap;

// stores and recycles views as they are scrolled off screen
public class MainFrameLayout extends FrameLayout {
    public MainFrameLayout(Context context) {
        this(context, null, 0);
    }

    public MainFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        //GvcGridCustomLayout.this.onTouchEvent(motionEvent);
        return true;
    }
}
