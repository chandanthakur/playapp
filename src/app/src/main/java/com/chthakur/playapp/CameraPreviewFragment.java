package com.chthakur.playapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.List;

public class CameraPreviewFragment extends Fragment {
    private static final String TAG = CallingHubActivity.class.getSimpleName();
    private ViewStub cameraPreviewStub;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_preview_fragment, container, false);
        cameraPreviewStub = (ViewStub)rootView.findViewById(R.id.camera_preview_stub);
        handleCameraPreviewStub(cameraPreviewStub);
        return rootView;
    }

    public void handleCameraPreviewStub(ViewStub cameraPreviewStub) {
        cameraPreviewStub.setOnInflateListener(new ViewStub.OnInflateListener() {
            @Override
            public void onInflate(ViewStub stub, View inflated) {

            }
        });

        cameraPreviewStub.inflate();
    }
}
