package com.chthakur.playapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chthakur.playapp.Logger.ALog;

import org.w3c.dom.Text;


public class RemoteCameraView extends FrameLayout{

    private static final String TAG = "M2CALL";

    private static final String LOGGER_PREFIX = "RemoteCameraView";

    private TextView textView;

    public RemoteCameraView(Context context) {
        this(context, null, 0);
    }

    public RemoteCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoteCameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflateView(context);
    }

    void inflateView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View viewRoot = inflater.inflate(R.layout.remote_camera_view, this, true);
        textView = (TextView)viewRoot.findViewById(R.id.call_camera_id);
    }

    void setVideoId(int videoId) {
        textView.setText(String.valueOf(videoId));
    }

    @Override
    public void onDetachedFromWindow() {
        ALog.i(TAG, LOGGER_PREFIX + ":onDetachedFromWindow: " + this.hashCode());
        super.onDetachedFromWindow();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ALog.i(TAG, LOGGER_PREFIX + ":onAttachedToWindow: " + this.hashCode());
    }
}
