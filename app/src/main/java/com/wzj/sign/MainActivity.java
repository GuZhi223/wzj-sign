package com.wzj.sign;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wzj.sign.data.AccountRepository;
import com.wzj.sign.data.BackupManager;
import com.wzj.sign.data.DataConverter;
import com.wzj.sign.data.PreferenceManager;
import com.wzj.sign.data.entity.AccountEntity;
import com.wzj.sign.databinding.ActivityMainBinding;
import com.wzj.sign.log.SignLogger;
import com.wzj.sign.network.NetworkUtils;
import com.wzj.sign.network.SignRepository;
import com.wzj.sign.network.model.ActiveSignResponse;
import com.wzj.sign.network.model.SignResponse;
import com.wzj.sign.service.ServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.HomeFragmentListener,
                   SettingsFragment.SettingsFragmentListener {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private HomeFragment homeFragment;
    private LogFragment logFragment;
    private SettingsFragment settingsFragment;
    private AboutFragment aboutFragment;
    private Fragment activeFragment;

    private AccountRepository accountRepository;
    private PreferenceManager preferenceManager;
    private SignRepository signRepository;
    private SignLogger logger;
    private ServiceManager serviceManager;
    private BackupManager backupManager;
    private ExecutorService executorService;
    private final AtomicBoolean isSignRunning = new AtomicBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        preferenceManager = new PreferenceManager(this);
        accountRepository = new AccountRepository(this);
        signRepository = new SignRepository();
        logger = SignLogger.getInstance(this);
        serviceManager = new ServiceManager(this);
        backupManager = new BackupManager(this);
        executorService = Executors.newFixedThreadPool(3);

        setupFragments();
        setupBottomNavigation();
        setupFab();
    }

    private void setupFragments() {
        homeFragment = new HomeFragment();
        logFragment = new LogFragment();
        settingsFragment = new SettingsFragment();
        aboutFragment = new AboutFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, aboutFragment, "about").hide(aboutFragment)
                .add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragmentContainer, logFragment, "log").hide(logFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit();

        activeFragment = homeFragment;
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment, getString(R.string.nav_home));
            } else if (id == R.id.nav_log) {
                switchFragment(logFragment, getString(R.string.nav_log));
            } else if (id == R.id.nav_settings) {
                switchFragment(settingsFragment, getString(R.string.nav_settings));
            } else if (id == R.id.nav_about) {
                switchFragment(aboutFragment, getString(R.string.nav_about));
            }
            updateFabVisibility();
            return true;
        });
    }

    private void setupFab() {
        binding.fabSign.setOnClickListener(v -> {
            if (isSignRunning.get()) {
                stopSignProcess();
            } else {
                List<Account> accounts = homeFragment != null ? homeFragment.getAccounts() : new ArrayList<>();
                startSignProcess(accounts);
            }
        });
        updateFabState(false);
    }

    private void switchFragment(Fragment target, String title) {
        if (target == activeFragment) return;
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
        binding.topAppBar.setTitle(title);
    }

    private void updateFabVisibility() {
        if (activeFragment == homeFragment) {
            binding.fabSign.show();
        } else {
            binding.fabSign.hide();
        }
    }

    private void updateFabState(boolean running) {
        if (running) {
            binding.fabSign.setImageResource(R.drawable.ic_stop);
            binding.fabSign.setContentDescription("停止签到");
        } else {
            binding.fabSign.setImageResource(R.drawable.ic_play);
            binding.fabSign.setContentDescription("开始签到");
        }
    }

    // ---- HomeFragmentListener ----

    @Override
    public void onStartSign(List<Account> accounts) {
        startSignProcess(accounts);
    }

    @Override
    public void onStopSign() {
        stopSignProcess();
    }

    @Override
    public void onNavigateToSettings() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_settings);
    }

    // ---- SettingsFragmentListener ----

    @Override
    public void onSignConfigChanged() {
        logger.info(TAG, "签到配置已更新");
    }

    @Override
    public void onDaemonStatusChanged(boolean enabled) {
        if (enabled) {
            serviceManager.startSignService();
            logger.info(TAG, "后台轮询守护进程已启动");
        } else {
            serviceManager.stopSignService();
            logger.info(TAG, "后台轮询已安全终止");
        }
    }

    // ---- 签到核心逻辑 ----

    public void startSignProcess(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            Toast.makeText(this, "请先添加账号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSignRunning.get()) {
            Toast.makeText(this, "签到正在进行中", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用，请检查网络连接", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = preferenceManager.getSignCount();
        int interval = preferenceManager.getSignInterval();

        if (interval < 100) {
            Toast.makeText(this, "⚠️ 强制调整为300ms防拦截", Toast.LENGTH_SHORT).show();
            interval = 300;
            preferenceManager.setSignInterval(300);
        }

        boolean enableGps = preferenceManager.isGpsEnabled();
        String longitude = preferenceManager.getDefaultLongitude();
        String latitude = preferenceManager.getDefaultLatitude();

        isSignRunning.set(true);
        updateFabState(true);
        if (homeFragment != null) {
            homeFragment.setSignRunning(true);
            homeFragment.updateSignStatus("签到中...");
        }

        logger.info(TAG, "🚀 开始签到流程，共 " + accounts.size() + " 个账号");
        logger.info(TAG, "📊 参数: 次数=" + count + ", 间隔=" + interval + "ms, GPS=" + (enableGps ? "启用" : "禁用"));

        final int finalCount = count;
        final int finalInterval = interval;

        for (Account account : accounts) {
            if (account.getUin().isEmpty() || account.getOpenid().isEmpty()) {
                logger.warn(TAG, "账号信息不完整，跳过");
                continue;
            }

            final String finalLon = enableGps ? (longitude.isEmpty() ? account.getLongitude() : longitude) : "";
            final String finalLat = enableGps ? (latitude.isEmpty() ? account.getLatitude() : latitude) : "";

            executorService.execute(() -> runSignLoop(account, finalCount, finalInterval, enableGps, finalLon, finalLat));
        }
    }

    private void runSignLoop(Account account, int count, int interval, boolean enableGps, String lon, String lat) {
        logger.info(TAG, "🎯 [" + account.getUin() + "] 开始签到任务");
        for (int i = 1; i <= count; i++) {
            if (!isSignRunning.get()) break;
            if (i == 1 || i == count || interval >= 1000 || i % 5 == 0) {
                logger.info(TAG, "📡 [" + account.getUin() + "] 扫描波次: " + i + "/" + count);
            }

            boolean success = doSign(account, enableGps, lon, lat);
            if (success) {
                logger.info(TAG, "🏆 [" + account.getUin() + "] 签到成功！");
                break;
            }

            if (i < count && isSignRunning.get()) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        logger.info(TAG, "🏁 [" + account.getUin() + "] 签到任务结束");
    }

    private boolean doSign(Account account, boolean enableGps, String lon, String lat) {
        final boolean[] result = {false};
        final Object lock = new Object();
        final AtomicBoolean completed = new AtomicBoolean(false);

        signRepository.getActiveSigns(account.getOpenid(), new SignRepository.ResultCallback<List<ActiveSignResponse>>() {
            @Override
            public void onSuccess(List<ActiveSignResponse> data) {
                if (data == null || data.isEmpty()) {
                    synchronized (lock) { completed.set(true); lock.notifyAll(); }
                    return;
                }
                ActiveSignResponse sign = data.get(0);
                String signType = sign.getSignTypeName();
                logger.info(TAG, "🎯 发现签到任务: " + signType + " Course=" + sign.getCourseId() + ", Sign=" + sign.getSignId());

                signRepository.submitSign(account.getOpenid(), sign.getCourseId(), sign.getSignId(),
                        enableGps && sign.requiresGps(), lon, lat,
                        new SignRepository.ResultCallback<SignResponse>() {
                            @Override
                            public void onSuccess(SignResponse response) {
                                result[0] = response.isSignSuccess();
                                logger.info(TAG, "[" + account.getUin() + "] 服务器回包: " + response.getDisplayMessage());
                                synchronized (lock) { completed.set(true); lock.notifyAll(); }
                            }

                            @Override
                            public void onError(String error) {
                                logger.error(TAG, "[" + account.getUin() + "] 签到错误: " + error);
                                synchronized (lock) { completed.set(true); lock.notifyAll(); }
                            }
                        });
            }

            @Override
            public void onError(String error) {
                logger.error(TAG, "[" + account.getUin() + "] 获取签到任务失败: " + error);
                synchronized (lock) { completed.set(true); lock.notifyAll(); }
            }
        });

        synchronized (lock) {
            try {
                if (!completed.get()) lock.wait(15000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return result[0];
    }

    private void stopSignProcess() {
        isSignRunning.set(false);
        executorService.shutdownNow();
        executorService = Executors.newFixedThreadPool(3);
        logger.info(TAG, "🛑 签到流程已停止");

        runOnUiThread(() -> {
            updateFabState(false);
            if (homeFragment != null) {
                homeFragment.setSignRunning(false);
                homeFragment.updateSignStatus("已停止");
            }
        });
    }

    // ---- 生命周期 ----

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow();
        }
        binding = null;
    }
}
