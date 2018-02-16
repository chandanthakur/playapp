package com.chthakur.playapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CameraPreviewSurfaceDeprecated extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreviewSurfaceDeprecated.class.getSimpleName();
    private SurfaceHolder mCameraSurfaceHolder;
    private List<Subscription> subscriptions = new ArrayList<>();
    private CameraAsync mCamera;
    public CameraPreviewSurfaceDeprecated(Context context, AttributeSet attrs) {
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
                .observeOn(Schedulers.io())
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
                .subscribeOn(Schedulers.io()).subscribe(new Subscriber<Boolean>() {
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

    static private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    static class CameraAsync {
        static private CameraAsync instance;
        static private Camera mCamera;

        private CameraAsync() {
        }

        static public CameraAsync getInstance() {
            if(instance != null) {
                return null;
            } else {
                instance = new CameraAsync();
            }

            return instance;
        }

        private Observable<Camera> getCamera(){
            return Observable.fromCallable(new Callable<Camera>() {
                @Override
                public Camera call() throws Exception {
                    if(mCamera != null) {
                        return mCamera;
                    }

                    ALog.i(TAG, "getCameraInstance:start");
                    Camera c = null;
                    if(!checkCameraHardware()) {
                        return c;
                    }

                    c = getFrontFacingCamera(); // attempt to get a Camera instance
                    ALog.i(TAG, "getCameraInstance:end");
                    mCamera = c;
                    return c; // returns null if camera is unavailable
                }
            });
        }

        public Observable<Boolean> setPreviewDisplay(final SurfaceHolder surfaceHolder) {
            return getCamera().map(new Func1<Camera, Boolean>() {
                @Override
                public Boolean call(Camera camera) {
                    if(camera == null) {
                        return false;
                    }

                    boolean isSuccess = false;
                    try {
                        mCamera.setPreviewDisplay(surfaceHolder);
                        isSuccess = true;
                    } catch (Exception ex) {
                        isSuccess = false;
                    }

                    return isSuccess;
                }
            });
        }

        public Observable<Boolean> startPreview() {
            return getCamera().map(new Func1<Camera, Boolean>() {
                @Override
                public Boolean call(Camera camera) {
                    if(camera == null) {
                        return false;
                    }

                    camera.startPreview();
                    return true;
                }
            });
        }

        public Observable<Boolean> stopPreview() {
            return getCamera().map(new Func1<Camera, Boolean>() {
                @Override
                public Boolean call(Camera camera) {
                    if(camera == null) {
                        return false;
                    }

                    camera.stopPreview();
                    return true;
                }
            });
        }

        public Observable<Boolean> handleSurfaceChange(final SurfaceHolder surfaceHolder, final int layoutWidth, final int layoutHeight) {
            return getCamera().map(new Func1<Camera, Boolean>() {
                @Override
                public Boolean call(Camera camera) {
                    if(camera == null) {
                        return false;
                    }

                    try {
                        mCamera.stopPreview();
                    } catch (Exception ex) {
                        // don't care
                    }

                    try {
                        Display display = ((WindowManager) App.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                        if (display.getRotation() == Surface.ROTATION_0) {
                            mCamera.setDisplayOrientation(90);
                        } else if (display.getRotation() == Surface.ROTATION_270) {
                            mCamera.setDisplayOrientation(180);
                        }

                        Camera.Parameters parameters = mCamera.getParameters();
                        Camera.Size previewSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), layoutWidth, layoutHeight);
                        parameters.setPreviewSize(previewSize.width, previewSize.height);
                        mCamera.setParameters(parameters);
                        mCamera.setPreviewDisplay(surfaceHolder);
                        mCamera.startPreview();
                    } catch (Exception ex) {
                        return false;
                    }

                    return true;
                }
            });
        }

        public Observable<Boolean> release() {
            return Observable.fromCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    if(mCamera != null) {
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                        instance = null;
                    }

                    return true;
                }
            });
        }

        private boolean checkCameraHardware() {
            if (App.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                return true;
            } else {
                return false;
            }
        }

        private static Camera getFrontFacingCamera() {
            Camera c = null;  // object that use
            Camera.CameraInfo info = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int i = 0; i< cameraCount; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        c = Camera.open(i);
                    } catch (RuntimeException e) {
                        ALog.e(TAG, "Failed to open front camera", e);
                    }

                    break;
                }
            }

            return c;
        }
    }
}
