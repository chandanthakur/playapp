package com.chthakur.playapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.chthakur.playapp.Logger.ALog;

/**
 * Created by chthakur on 10/1/2017.
 */

public class ExampleService extends Service {
    private static final String TAG = ExampleService.class.getSimpleName();

    int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used

    @Override
    public void onCreate() {
        // The service is being created
        ALog.i(TAG, "onCreate");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        ALog.i(TAG, "onStartCommand");
        return mStartMode;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        ALog.i(TAG, "onBind");
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        ALog.i(TAG, "onUnbind");
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        ALog.i(TAG, "onRebind");
    }
    @Override
    public void onDestroy() {
        ALog.i(TAG, "onDestroy");
        // The service is no longer used and is being destroyed
    }
}
