package com.wzj.sign.service;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceManager {

    private final Context context;
    private final NotificationHelper notificationHelper;

    public ServiceManager(Context context) {
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
    }

    public void startSignService() {
        notificationHelper.createChannels();
        Intent intent = new Intent(context, SignForegroundService.class);
        intent.setAction(SignForegroundService.ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void stopSignService() {
        Intent intent = new Intent(context, SignForegroundService.class);
        intent.setAction(SignForegroundService.ACTION_STOP);
        context.startService(intent);
    }

    public boolean isServiceRunning() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        for (ActivityManager.RunningServiceInfo service : am.getRunningServices(Integer.MAX_VALUE)) {
            if (SignForegroundService.class.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }
}
