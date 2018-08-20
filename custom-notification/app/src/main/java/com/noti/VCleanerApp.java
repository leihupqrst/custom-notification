package com.noti;

import android.app.Application;
import android.content.Intent;

public class VCleanerApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();


        Intent intent = new Intent();
        intent.setClass(this, TimerTaskService.class);
        startService(intent);
    }
}
