package com.chthakur.playapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;

public class CallRemoteMemberView extends FrameLayout {

    private static final String TAG = "M2CALL";

    private static final String LOGGER_PREFIX = CallRemoteMemberView.class.getSimpleName() + ":";

    private Context context;

    private int height;

    private int width;

    private int currentVideoId = -1;

    private boolean isViewInflated;

    public CallRemoteMemberView(Context context) {
        this(context, null, 0);
    }

    public CallRemoteMemberView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CallRemoteMemberView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        inflateView(context);
        ALog.i(TAG, LOGGER_PREFIX + "constructor");
    }

    void inflateView(Context context) {
        if(isViewInflated) {
            return;
        }

        isViewInflated = true;
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.call_remote_member_view, this, true);
    }

    public void setVideoId(int videoId) {
        RemoteCameraView remoteCameraView = VideoLoadManager.loadRemoteCameraView(getContext(), this, videoId);
        remoteCameraView.setVideoId(videoId);
    }


    @Override
    public void onDetachedFromWindow() {
        ALog.i(TAG, LOGGER_PREFIX + ":onDetachedFromWindow");
        super.onDetachedFromWindow();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ALog.i(TAG, LOGGER_PREFIX + ":onAttachedToWindow");
    }
}
