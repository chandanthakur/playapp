package com.chthakur.playapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.chthakur.playapp.Logger.ALog;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Func1;

public class CameraAsync {
    private static final String TAG = CameraAsync.class.getSimpleName();
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
}
