package com.chthakur.playapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;

import java.util.HashMap;

// stores and recycles views as they are scrolled off screen
public class VideoLoadManager {

    private static final String TAG = "M2CALL";

    private static final String LOGGER_PREFIX = VideoLoadManager.class.getSimpleName() + ":";

    static HashMap<Integer, CallLocalMemberView> localCameraViewMap = new HashMap<>();

    static HashMap<Integer, CallRemoteMemberView> remoteCameraViewMap = new HashMap<>();

    static LocalCameraView loadLocalCameraView(Context context, CallLocalMemberView newParent, int videoId) {
        if(localCameraViewMap.containsKey(videoId)) {
            CallLocalMemberView existingParent =  localCameraViewMap.get(videoId);
            if(newParent == existingParent) {
                ALog.i(TAG, LOGGER_PREFIX + "loadRemoteCameraView: same parent:" + videoId);
                FrameLayout cameraViewWrap = (FrameLayout)existingParent.findViewById(R.id.local_camera_wrap);
                if(cameraViewWrap != null && cameraViewWrap.getChildCount() > 0) {
                    View cameraView = cameraViewWrap.getChildAt(0);
                    return (LocalCameraView) cameraView;
                }

                return null;
            }

            FrameLayout cameraViewWrap = (FrameLayout)existingParent.findViewById(R.id.local_camera_wrap);
            if(cameraViewWrap != null && cameraViewWrap.getChildCount() > 0) {
                View cameraView = cameraViewWrap.getChildAt(0);
                cameraViewWrap.removeAllViews(); // we clean if any views within
                reparent(existingParent, cameraView, newParent);
                localCameraViewMap.put(videoId, newParent);
                ALog.i(TAG, LOGGER_PREFIX + "loadLocalCameraView:reparent success:" + videoId + ", cameraView:" + cameraView.hashCode());
                return (LocalCameraView)cameraView;
            }
        }

        FrameLayout cameraViewWrap = (FrameLayout)newParent.findViewById(R.id.local_camera_wrap);
        LocalCameraView cameraView = new LocalCameraView(context);
        cameraViewWrap.addView(cameraView);
        localCameraViewMap.put(videoId, newParent);
        ALog.i(TAG, LOGGER_PREFIX + "loadLocalCameraView:fresh view loaded:" + videoId + ", cameraView:" + cameraView.hashCode());
        return cameraView;
    }

    static RemoteCameraView loadRemoteCameraView(Context context, CallRemoteMemberView newParent, int videoId) {
        if(remoteCameraViewMap.containsKey(videoId)) {
            CallRemoteMemberView existingParent =  remoteCameraViewMap.get(videoId);
            if(newParent == existingParent) {
                FrameLayout cameraViewWrap = (FrameLayout)existingParent.findViewById(R.id.remote_camera_wrap);
                if(cameraViewWrap != null && cameraViewWrap.getChildCount() > 0) {
                    View cameraView = cameraViewWrap.getChildAt(0);
                    return (RemoteCameraView) cameraView;
                }
            }

            FrameLayout cameraViewWrap = (FrameLayout)existingParent.findViewById(R.id.remote_camera_wrap);
            if(cameraViewWrap != null && cameraViewWrap.getChildCount() > 0) {
                View cameraView = cameraViewWrap.getChildAt(0);
                cameraViewWrap.removeAllViews(); // we clean if any views within
                reparent(existingParent, cameraView, newParent);
                remoteCameraViewMap.put(videoId, newParent);
                ALog.i(TAG, LOGGER_PREFIX + "loadRemoteCameraView:reparent success:" + videoId + ", cameraView:" + cameraView.hashCode());
                return (RemoteCameraView) cameraView;
            }
        }

        FrameLayout cameraViewWrap = (FrameLayout)newParent.findViewById(R.id.remote_camera_wrap);
        RemoteCameraView cameraView = new RemoteCameraView(context);
        cameraView.setId(R.id.remote_camera_wrap);

        cameraViewWrap.addView(cameraView);
        remoteCameraViewMap.put(videoId, newParent);
        ALog.i(TAG, LOGGER_PREFIX + "loadRemoteCameraView:fresh view loaded:" + videoId + ", cameraView:" + cameraView.hashCode());
        return (RemoteCameraView) cameraView;
    }

    static void reparent(ViewGroup parent, View child, ViewGroup newParent) {
        parent.removeView(child);
        newParent.addView(child);
    }

}
