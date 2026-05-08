package com.wzj.sign.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import com.wzj.sign.data.AppDatabase;
import com.wzj.sign.data.PreferenceManager;
import com.wzj.sign.data.dao.AccountDao;
import com.wzj.sign.data.entity.AccountEntity;
import com.wzj.sign.log.SignLogger;
import com.wzj.sign.network.NetworkUtils;
import com.wzj.sign.network.SignRepository;
import com.wzj.sign.network.model.ActiveSignResponse;
import com.wzj.sign.network.model.SignResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SignForegroundService extends Service {

    public static final String ACTION_START = "com.wzj.sign.ACTION_START";
    public static final String ACTION_STOP = "com.wzj.sign.ACTION_STOP";
    private static final String TAG = "SignService";
    private static final long POLL_INTERVAL_MS = 5000;
    private static final long WAKELOCK_TIMEOUT_MS = 60 * 60 * 1000L;

    private NotificationHelper notificationHelper;
    private SignRepository signRepository;
    private SignLogger logger;
    private AccountDao accountDao;
    private PreferenceManager preferenceManager;
    private PowerManager.WakeLock wakeLock;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread pollThread;
    private int lastAccountCount = -1;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SignForegroundService getService() {
            return SignForegroundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new NotificationHelper(this);
        notificationHelper.createChannels();
        signRepository = new SignRepository();
        logger = SignLogger.getInstance(this);
        accountDao = AppDatabase.getInstance(this).accountDao();
        preferenceManager = new PreferenceManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopPolling();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!isRunning.get()) {
            startForeground(1001, notificationHelper.buildServiceNotification("后台签到服务运行中..."));
            acquireWakeLock();
            startPolling();
        }
        return START_STICKY;
    }

    private void startPolling() {
        isRunning.set(true);
        lastAccountCount = -1;
        logger.info(TAG, "后台轮询守护进程已启动 (周期: 5s)");

        pollThread = new Thread(() -> {
            while (isRunning.get()) {
                try {
                    if (!NetworkUtils.isNetworkAvailable(SignForegroundService.this)) {
                        logger.warn(TAG, "网络不可用，等待中...");
                        Thread.sleep(POLL_INTERVAL_MS);
                        continue;
                    }

                    List<AccountEntity> accounts = accountDao.getAll();
                    if (accounts.isEmpty()) {
                        if (lastAccountCount != 0) {
                            logger.warn(TAG, "未配置任何账号");
                            notificationHelper.showServiceNotification("未配置账号，请先添加");
                            lastAccountCount = 0;
                        }
                    } else {
                        if (accounts.size() != lastAccountCount) {
                            notificationHelper.showServiceNotification(
                                    "正在监控 " + accounts.size() + " 个账号...");
                            lastAccountCount = accounts.size();
                        }
                        for (AccountEntity account : accounts) {
                            if (!isRunning.get()) break;
                            checkAndSign(account);
                        }
                    }

                    renewWakeLock();
                    Thread.sleep(POLL_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error(TAG, "轮询异常: " + e.getMessage(), e);
                }
            }
            logger.info(TAG, "后台轮询已安全终止");
        }, "SignPollThread");
        pollThread.start();
    }

    private void checkAndSign(AccountEntity account) {
        String openid = account.getOpenid();
        if (openid == null || openid.isEmpty()) return;

        boolean gpsEnabled = preferenceManager.isGpsEnabled();
        String defaultLon = preferenceManager.getDefaultLongitude();
        String defaultLat = preferenceManager.getDefaultLatitude();

        signRepository.getActiveSigns(openid, new SignRepository.ResultCallback<List<ActiveSignResponse>>() {
            @Override
            public void onSuccess(List<ActiveSignResponse> data) {
                if (data == null || data.isEmpty()) return;
                ActiveSignResponse sign = data.get(0);
                String signType = sign.getSignTypeName();
                logger.info(TAG, "[" + account.getUin() + "] 发现签到任务: " + signType + " Course=" + sign.getCourseId());

                boolean useGps = gpsEnabled && sign.requiresGps();
                String lon = useGps ? (defaultLon.isEmpty() ? account.getLongitude() : defaultLon) : "";
                String lat = useGps ? (defaultLat.isEmpty() ? account.getLatitude() : defaultLat) : "";

                signRepository.submitSign(openid, sign.getCourseId(), sign.getSignId(),
                        useGps, lon, lat,
                        new SignRepository.ResultCallback<SignResponse>() {
                            @Override
                            public void onSuccess(SignResponse response) {
                                if (response.isSignSuccess()) {
                                    logger.info(TAG, "[" + account.getUin() + "] 签到成功! " + response.getDisplayMessage());
                                    notificationHelper.showResultNotification(
                                            "签到成功", "账号 " + account.getUin() + " 签到成功");
                                } else {
                                    logger.warn(TAG, "[" + account.getUin() + "] 签到失败: " + response.getDisplayMessage());
                                }
                            }

                            @Override
                            public void onError(String error) {
                                logger.error(TAG, "[" + account.getUin() + "] 签到错误: " + error);
                            }
                        });
            }

            @Override
            public void onError(String error) {
                logger.error(TAG, "[" + account.getUin() + "] 获取签到任务失败: " + error);
            }
        });
    }

    private void stopPolling() {
        isRunning.set(false);
        if (pollThread != null) {
            pollThread.interrupt();
        }
        releaseWakeLock();
        logger.info(TAG, "正在停止后台服务...");
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wzj:sign_wakelock");
            wakeLock.acquire(WAKELOCK_TIMEOUT_MS);
        }
    }

    private void renewWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire(WAKELOCK_TIMEOUT_MS);
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        stopPolling();
        notificationHelper.cancelServiceNotification();
        super.onDestroy();
    }
}
