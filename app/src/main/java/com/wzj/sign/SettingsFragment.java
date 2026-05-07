package com.wzj.sign;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wzj.sign.data.AccountRepository;
import com.wzj.sign.data.BackupManager;
import com.wzj.sign.data.PreferenceManager;
import com.wzj.sign.data.entity.AccountEntity;
import com.wzj.sign.databinding.FragmentSettingsBinding;
import com.wzj.sign.log.SignLogger;
import com.wzj.sign.service.ServiceManager;

import java.util.List;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private FragmentSettingsBinding binding;
    private PreferenceManager preferenceManager;
    private ServiceManager serviceManager;
    private BackupManager backupManager;
    private AccountRepository accountRepository;
    private SignLogger logger;
    private boolean isUpdatingSwitch = false;

    public interface SettingsFragmentListener {
        default void onSignConfigChanged() {}
        default void onDaemonStatusChanged(boolean enabled) {}
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());
        serviceManager = new ServiceManager(requireContext());
        backupManager = new BackupManager(requireContext());
        accountRepository = new AccountRepository(requireContext());
        logger = SignLogger.getInstance(requireContext());

        loadPreferences();
        setupListeners();
        updateServiceStatus();

        logger.info(TAG, "设置页面已加载");
    }

    @Override
    public void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    private void loadPreferences() {
        isUpdatingSwitch = true;

        binding.etSignCount.setText(String.valueOf(preferenceManager.getSignCount()));
        binding.etInterval.setText(String.valueOf(preferenceManager.getSignInterval()));

        boolean gpsEnabled = preferenceManager.isGpsEnabled();
        binding.switchGps.setChecked(gpsEnabled);
        binding.layoutGps.setVisibility(gpsEnabled ? View.VISIBLE : View.GONE);

        binding.etLongitude.setText(preferenceManager.getDefaultLongitude());
        binding.etLatitude.setText(preferenceManager.getDefaultLatitude());

        binding.switchService.setChecked(preferenceManager.isDaemonEnabled());

        isUpdatingSwitch = false;
    }

    private void setupListeners() {
        binding.etSignCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingSwitch) {
                    saveSignCount();
                }
            }
        });

        binding.etInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingSwitch) {
                    saveInterval();
                }
            }
        });

        binding.switchGps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingSwitch) {
                binding.layoutGps.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                preferenceManager.setGpsEnabled(isChecked);
                logger.info(TAG, "模拟定位: " + (isChecked ? "已启用" : "已禁用"));
                notifySettingsChanged();
            }
        });

        binding.etLongitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingSwitch) {
                    preferenceManager.setDefaultLongitude(s.toString());
                    notifySettingsChanged();
                }
            }
        });

        binding.etLatitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingSwitch) {
                    preferenceManager.setDefaultLatitude(s.toString());
                    notifySettingsChanged();
                }
            }
        });

        binding.switchService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingSwitch) {
                preferenceManager.setDaemonEnabled(isChecked);
                notifyDaemonStatusChanged(isChecked);
                updateServiceStatus();
            }
        });

        binding.btnExportData.setOnClickListener(v -> exportData());
        binding.btnImportData.setOnClickListener(v -> importData());
    }

    private void saveSignCount() {
        try {
            String text = binding.etSignCount.getText().toString().trim();
            if (!text.isEmpty()) {
                int count = Integer.parseInt(text);
                if (count > 0) {
                    preferenceManager.setSignCount(count);
                    logger.info(TAG, "签到次数已保存: " + count);
                    notifySettingsChanged();
                }
            }
        } catch (NumberFormatException e) {
            logger.error(TAG, "签到次数格式错误", e);
        }
    }

    private void saveInterval() {
        try {
            String text = binding.etInterval.getText().toString().trim();
            if (!text.isEmpty()) {
                int interval = Integer.parseInt(text);
                if (interval > 0) {
                    preferenceManager.setSignInterval(interval);
                    logger.info(TAG, "签到间隔已保存: " + interval + "ms");
                    notifySettingsChanged();
                }
            }
        } catch (NumberFormatException e) {
            logger.error(TAG, "签到间隔格式错误", e);
        }
    }

    private void updateServiceStatus() {
        boolean isRunning = serviceManager.isServiceRunning();
        binding.tvServiceStatus.setText(isRunning ? "运行中" : "未运行");

        isUpdatingSwitch = true;
        binding.switchService.setChecked(isRunning);
        isUpdatingSwitch = false;
    }

    private void exportData() {
        accountRepository.getAll(accounts -> {
            if (accounts == null || accounts.isEmpty()) {
                Toast.makeText(requireContext(), "没有数据可导出", Toast.LENGTH_SHORT).show();
                logger.warn(TAG, "导出失败: 没有数据");
                return;
            }

            String filePath = backupManager.exportAccounts(accounts);
            if (filePath != null) {
                Toast.makeText(requireContext(), "导出成功: " + filePath, Toast.LENGTH_LONG).show();
                logger.info(TAG, "数据导出成功: " + filePath);
            } else {
                Toast.makeText(requireContext(), "导出失败", Toast.LENGTH_SHORT).show();
                logger.error(TAG, "数据导出失败");
            }
        });
    }

    private void importData() {
        List<String> backupFiles = backupManager.getBackupFiles();
        if (backupFiles == null || backupFiles.isEmpty()) {
            Toast.makeText(requireContext(), "没有找到备份文件", Toast.LENGTH_SHORT).show();
            logger.warn(TAG, "导入失败: 没有备份文件");
            return;
        }

        String latestBackup = backupFiles.get(backupFiles.size() - 1);
        List<AccountEntity> accounts = backupManager.importAccounts(latestBackup);

        if (accounts == null) {
            Toast.makeText(requireContext(), "导入失败: 无法读取备份文件", Toast.LENGTH_SHORT).show();
            logger.error(TAG, "导入失败: 无法读取备份文件");
            return;
        }

        accountRepository.deleteAll(deleteResult -> {
            final int[] importedCount = {0};
            if (accounts.isEmpty()) {
                Toast.makeText(requireContext(), "导入成功: 0 条数据", Toast.LENGTH_SHORT).show();
                logger.info(TAG, "数据导入完成: 0 条");
                return;
            }

            for (AccountEntity account : accounts) {
                accountRepository.insert(account, insertResult -> {
                    importedCount[0]++;
                    if (importedCount[0] == accounts.size()) {
                        Toast.makeText(requireContext(),
                                "导入成功: " + importedCount[0] + " 条数据",
                                Toast.LENGTH_SHORT).show();
                        logger.info(TAG, "数据导入完成: " + importedCount[0] + " 条");
                    }
                });
            }
        });
    }

    private void notifySettingsChanged() {
        if (getActivity() instanceof SettingsFragmentListener) {
            ((SettingsFragmentListener) getActivity()).onSignConfigChanged();
        }
    }

    private void notifyDaemonStatusChanged(boolean enabled) {
        if (getActivity() instanceof SettingsFragmentListener) {
            ((SettingsFragmentListener) getActivity()).onDaemonStatusChanged(enabled);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
