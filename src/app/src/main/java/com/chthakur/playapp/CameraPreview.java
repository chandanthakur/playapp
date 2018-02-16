package com.chthakur.playapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.List;

/** A basic Camera preview class */
public class CameraPreview extends FrameLayout {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private CameraPreview mCameraSurface;
    private Camera.Size previewSize;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
