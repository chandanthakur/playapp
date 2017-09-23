package com.chthakur.playapp;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        //}
    }

    @Override
    public void onResume() {
        super.onResume();
        tryShow(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //tryDelete(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // stores and recycles views as they are scrolled off screen
    static public class FloatFrameLayout extends FrameLayout implements View.OnTouchListener{
        float floatDx;

        float floatDy;

        private static Float positionX;

        private static Float positionY;

        private static Rect windowFrame = new Rect();

        public FloatFrameLayout(Context context) {
            this(context, null, 0);
        }

        public FloatFrameLayout(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public FloatFrameLayout(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            LayoutInflater inflater = LayoutInflater.from(context);
            inflater.inflate(R.layout.float_view_test, this, true);
            ViewGroup.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            this.setLayoutParams(layoutParams);
            this.setOnTouchListener(this);
            this.getWindowVisibleDisplayFrame(windowFrame);
            if(positionX != null && positionY != null) {
                this.setX(positionX);
                this.setY(positionY);
                ALog.i("FloatFrameLayout:create", "position:" + positionX + "x" + positionY);
            } else {
                this.setX(getDefaultX());
                this.setY(getDefaultY());
            }
        }

        private float getDefaultX() {
            this.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            float width = this.getMeasuredWidth();
            return windowFrame.right - 3*width/4;
        }

        private float getDefaultY() {
            return  windowFrame.top + (windowFrame.bottom - windowFrame.top)/4;
        }

        // chthakur - out of bounds pending, snapping to points pending
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    floatDx = view.getX() - event.getRawX();
                    floatDy = view.getY() - event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    positionX = event.getRawX() + floatDx;
                    positionY = event.getRawY() + floatDy;
                    view.setX(positionX);
                    view.setY(positionY);
                    ALog.i("FloatFrameLayout:onTouch", "position:" + positionX + "x" + positionY);
                    break;
                case MotionEvent.ACTION_UP:
                default:
                    return true;
            }
            return true;
        }

        @Override
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
        }

        @Override
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
        }
    }


    public static void tryShow(AppCompatActivity activity) {
        FrameLayout eFrameLayout = (FrameLayout)activity.findViewById(R.id.call_float_active_wrap);
        if(eFrameLayout != null) {
            return;
        }

        ViewGroup activityContentRoot = (ViewGroup)activity.findViewById(android.R.id.content);
        FloatFrameLayout frameLayout = new FloatFrameLayout(activity);
        activityContentRoot.addView(frameLayout);
    }

    public static void tryDelete(AppCompatActivity activity) {
        FrameLayout eFrameLayout = (FrameLayout)activity.findViewById(R.id.call_float_active_wrap);
        if(eFrameLayout != null && eFrameLayout.getParent() != null) {
            ViewGroup viewGroup = (ViewGroup)eFrameLayout.getParent();
            viewGroup.removeView(eFrameLayout);
            return;
        }
    }
}
