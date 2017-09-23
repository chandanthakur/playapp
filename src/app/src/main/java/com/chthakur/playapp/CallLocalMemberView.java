package com.chthakur.playapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;


public class CallLocalMemberView extends FrameLayout {

    private static final String TAG = "M2CALL";

    private static final String LOGGER_PREFIX = CallLocalMemberView.class.getSimpleName() + ":";

    private boolean isViewInflated;

    public CallLocalMemberView(Context context) {
        this(context, null, 0);
    }

    public CallLocalMemberView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CallLocalMemberView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflateView(context);
        ALog.i(TAG, LOGGER_PREFIX + "constructor");
    }

    void inflateView(Context context) {
        if(isViewInflated) {
            return;
        }

        isViewInflated = true;
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.call_local_member_view, this, true);
    }

    public void setVideoId(int videoId) {
        LocalCameraView localCameraView = VideoLoadManager.loadLocalCameraView(getContext(), this, videoId);
        localCameraView.setVideoId(videoId);
    }

    @Override
    public void onDetachedFromWindow() {
        ALog.i(TAG, LOGGER_PREFIX + "onDetachedFromWindow");
        super.onDetachedFromWindow();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ALog.i(TAG, LOGGER_PREFIX + "onAttachedToWindow");
    }

    public void destroyView() {
        ALog.i(TAG, LOGGER_PREFIX + "destroyView");
    }
}
