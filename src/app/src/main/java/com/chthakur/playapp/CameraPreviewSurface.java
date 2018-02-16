package com.chthakur.playapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class CameraPreviewSurface extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreviewSurfaceDeprecated.class.getSimpleName();
    private SurfaceHolder mCameraSurfaceHolder;
    private List<Subscription> subscriptions = new ArrayList<>();
    private CameraAsync mCamera;
    public CameraPreviewSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCameraSurfaceHolder = getHolder();
        mCameraSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        ALog.d(TAG, "surfaceCreated:");
        mCamera = CameraAsync.getInstance();
        // The Surface has been created, now tell the camera where to draw the preview.
        Subscription subscription = mCamera.setPreviewDisplay(holder)
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean aBoolean) {
                        return mCamera.startPreview();
                    }
                }).subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        ALog.d(TAG, "camera acquired and started:");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ALog.d(TAG, "Error setting camera preview: ", throwable);
                    }
                });

        subscriptions.add(subscription);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        ALog.d(TAG, "surfaceDestroyed:");
        for(Subscription subscription: subscriptions) {
            subscription.unsubscribe();
        }

        if( mCamera != null) {
            mCamera.release().subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    ALog.i(TAG, "camera released: success");
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    ALog.e(TAG, "camera released:failed");
                }
            });
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mCameraSurfaceHolder.removeCallback(this);
        super.onDetachedFromWindow();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if(mCamera == null || mCameraSurfaceHolder.getSurface() == null) {
            return;
        }

        int layoutWidth = getLayoutParams().width;
        int layoutHeight = getLayoutParams().height;
        Subscription subscription = mCamera.handleSurfaceChange(mCameraSurfaceHolder, layoutWidth, layoutHeight)
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ALog.d(TAG, "Error starting camera preview: " + e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }
                });

        subscriptions.add(subscription);
    }
}
