package com.chthakur.playapp;
import android.app.Application;
import android.content.Context;

import com.chthakur.playapp.Logger.ALog;

public class App extends Application {
    public static Context context;

    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        ALog.LoggerConfiguration.initializeLogger();
    }

    static public Context getContext() {
        return context;
    }
}