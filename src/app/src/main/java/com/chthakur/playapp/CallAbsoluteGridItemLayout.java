package com.chthakur.playapp;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.List;

public class CallAbsoluteGridItemLayout extends FrameLayout {

    private OnClickListener clickListener;

    private OnDoubleTapListener doubleTapListener;

    private final List<OnTouchListener> onTouchListenerList = new ArrayList<>();

    private Rect rect;

    private Long rectModifiedTS;

    private GestureDetector gestureDetector;

    private boolean isLayoutRTL = false;

    public CallAbsoluteGridItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CallAbsoluteGridItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CallAbsoluteGridItemLayout(Context context, Rect rect) {
        this(context, null, 0);
        ViewGroup.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setLayoutParams(new ViewGroup.LayoutParams(rect.width(), rect.height()));
        this.setX(rect.left);
        this.setY(rect.top);
        this.rect = rect;
        this.isLayoutRTL = UtilsDevice.isLayoutRTL(context);
        rectModifiedTS = System.currentTimeMillis();
        gestureDetector = new GestureDetector(getContext(), new SimpleGestureListener());
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if(clickListener != null) {
                clickListener.onClick(CallAbsoluteGridItemLayout.this);
            }

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(doubleTapListener != null) {
                doubleTapListener.onDoubleTap(CallAbsoluteGridItemLayout.this, e);
            }

            return true;
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDoubleTapListener(OnDoubleTapListener listener) {
        this.doubleTapListener = listener;
    }

    public void setOnTouchListener(OnTouchListener listener) {
        this.onTouchListenerList.add(listener);
    }

    Rect getRect() {
        return this.rect;
    }

    void applyRect(Rect newValue) {
        if (this.isLayoutRTL) {
            applyRTLRect(newValue);
        } else {
            applyLTRRect(newValue);
        }

        this.rect.set(newValue);
        rectModifiedTS = System.currentTimeMillis();
    }

    /**
     * This method will help in applying the rect when layout direction is LTR. By default fallback option is also LTR.
     * @param newValue rect
     */
    private void applyLTRRect(Rect newValue) {
        if (newValue.left != this.rect.left || newValue.top != this.rect.top) {
            this.setX(newValue.left);
            this.setY(newValue.top);
        }

        if (newValue.width() != this.rect.width() || newValue.height() != this.rect.height()) {
            ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
            layoutParams.width = newValue.width();
            layoutParams.height = newValue.height();
            this.setLayoutParams(layoutParams);
        }
    }

    /**
     * This method will help in applying the rect in RTL mode, In RTL mode things draw from Right to left and origin also shift to
     * right directions therefore setX is not behaving properly and we need to fall back to margin way to position the view.
     * @param newValue rect
     */
    private void applyRTLRect(Rect newValue) {
        boolean isParamsUpdateRequired = false;
        FrameLayout.LayoutParams layoutParams = (LayoutParams) this.getLayoutParams();
        if (newValue.left != this.rect.left || newValue.top != this.rect.top) {
            layoutParams.rightMargin = newValue.left;
            layoutParams.topMargin = newValue.top;
            isParamsUpdateRequired = true;
        }

        if (newValue.width() != this.rect.width() || newValue.height() != this.rect.height()) {
            layoutParams.width = newValue.width();
            layoutParams.height = newValue.height();
            isParamsUpdateRequired = true;
        }

        if (isParamsUpdateRequired) {
            this.setLayoutParams(layoutParams);
        }
    }

    public long getRectModifiedTS() {
        return rectModifiedTS;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        for(OnTouchListener touchListener: onTouchListenerList) {
            touchListener.onTouch(this, motionEvent);
        }

        return true;
    }

    interface OnDoubleTapListener{
        void onDoubleTap(View view, MotionEvent motionEvent);
    }
}
