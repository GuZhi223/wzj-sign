package com.wzj.sign;

import android.app.Application;

import com.wzj.sign.log.SignLogger;
import com.wzj.sign.service.NotificationHelper;

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SignLogger.getInstance(this).info("MyApplication", "应用启动");
        new NotificationHelper(this).createChannels();
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
