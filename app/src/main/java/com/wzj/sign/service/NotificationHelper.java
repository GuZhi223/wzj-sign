package com.wzj.sign.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.wzj.sign.MainActivity;
import com.wzj.sign.R;

public class NotificationHelper {

    public static final String CHANNEL_SERVICE = "sign_service_channel";
    public static final String CHANNEL_RESULT = "sign_result_channel";
    private static final int NOTIFICATION_ID_SERVICE = 1001;
    private static final int NOTIFICATION_ID_RESULT = 1002;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createChannels() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_SERVICE,
                "签到服务",
                NotificationManager.IMPORTANCE_LOW
        );
        serviceChannel.setDescription("后台签到服务通知");
        serviceChannel.setShowBadge(false);

        NotificationChannel resultChannel = new NotificationChannel(
                CHANNEL_RESULT,
                "签到结果",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        resultChannel.setDescription("签到结果通知");

        notificationManager.createNotificationChannel(serviceChannel);
        notificationManager.createNotificationChannel(resultChannel);
    }

    public Notification buildServiceNotification(String contentText) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, CHANNEL_SERVICE)
                .setContentTitle("微助教自动签到")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public void showServiceNotification(String contentText) {
        notificationManager.notify(NOTIFICATION_ID_SERVICE, buildServiceNotification(contentText));
    }

    public void showResultNotification(String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_RESULT)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID_RESULT, notification);
    }

    public void cancelServiceNotification() {
        notificationManager.cancel(NOTIFICATION_ID_SERVICE);
    }
}
