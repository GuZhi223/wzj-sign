package com.wzj.sign;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.wzj.sign.data.AccountRepository;
import com.wzj.sign.data.DataConverter;
import com.wzj.sign.data.PreferenceManager;
import com.wzj.sign.data.entity.AccountEntity;
import com.wzj.sign.databinding.FragmentHomeBinding;
import com.wzj.sign.log.SignLogger;
import com.wzj.sign.network.NetworkUtils;
import com.wzj.sign.network.SignRepository;
import com.wzj.sign.network.model.ActiveSignResponse;
import com.wzj.sign.network.model.SignResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int MAX_ACCOUNTS = 3;

    private FragmentHomeBinding binding;
    private HomeAccountAdapter accountAdapter;
    private AccountRepository accountRepository;
    private SignLogger logger;
    private HomeFragmentListener listener;

    public interface HomeFragmentListener {
        void onStartSign(List<Account> accounts);
        void onStopSign();
        void onNavigateToSettings();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeFragmentListener) {
            listener = (HomeFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement HomeFragmentListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountRepository = new AccountRepository(requireContext());
        logger = SignLogger.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAccounts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void setupRecyclerView() {
        accountAdapter = new HomeAccountAdapter();
        binding.rvAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAccounts.setAdapter(accountAdapter);

        accountAdapter.setOnItemClickListener((position, account) -> {
            showAccountBottomSheet(account, position);
        });

        accountAdapter.setOnDeleteListener(position -> {
            List<Account> accounts = accountAdapter.getAccounts();
            if (position >= 0 && position < accounts.size()) {
                Account account = accounts.get(position);
                accounts.remove(position);
                accountAdapter.notifyItemRemoved(position);
                accountAdapter.notifyItemRangeChanged(position, accounts.size());
                saveAccounts();
                updateEmptyState();
                logger.info(TAG, "移除了账号: " + account.getUin());
            }
        });
    }

    private void setupButtons() {
        binding.btnAddAccount.setOnClickListener(v -> {
            if (accountAdapter.getAccountCount() >= MAX_ACCOUNTS) {
                android.widget.Toast.makeText(requireContext(),
                        "系统安全熔断：最高允许维持" + MAX_ACCOUNTS + "个并发账号",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            showAccountBottomSheet();
        });

        binding.btnGoSettings.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNavigateToSettings();
            }
        });
    }

    private void showAccountBottomSheet() {
        showAccountBottomSheet(null, -1);
    }

    private void showAccountBottomSheet(Account account, int editPosition) {
        AccountBottomSheet bottomSheet;
        if (account != null) {
            bottomSheet = AccountBottomSheet.newInstance(account);
        } else {
            bottomSheet = AccountBottomSheet.newInstance();
        }
        bottomSheet.setOnAccountSaveListener(savedAccount -> {
            if (editPosition >= 0) {
                accountAdapter.updateAccount(editPosition, savedAccount);
                logger.info(TAG, "更新了账号: " + savedAccount.getUin());
            } else {
                accountAdapter.addAccount(savedAccount);
                logger.info(TAG, "添加了新账号: " + savedAccount.getUin());
            }
            updateEmptyState();
            saveAccounts();
        });
        bottomSheet.show(getChildFragmentManager(), "AccountBottomSheet");
    }

    private void loadAccounts() {
        accountRepository.getAll(entities -> {
            List<Account> accounts = new ArrayList<>();
            for (AccountEntity entity : entities) {
                accounts.add(DataConverter.toModel(entity));
            }
            if (binding != null) {
                accountAdapter.setAccounts(accounts);
                updateEmptyState();
            }
            logger.info(TAG, "加载了 " + accounts.size() + " 个账号");
        });
    }

    private void saveAccounts() {
        List<Account> accounts = accountAdapter.getAccounts();
        List<AccountEntity> entitiesToSave = new ArrayList<>();
        for (Account account : accounts) {
            if (account.getUin() != null && !account.getUin().isEmpty()
                    && account.getOpenid() != null && !account.getOpenid().isEmpty()) {
                entitiesToSave.add(DataConverter.toEntity(account));
            }
        }
        if (entitiesToSave.isEmpty()) {
            logger.warn(TAG, "没有有效的账号数据可保存");
            return;
        }
        accountRepository.deleteAll(v -> {
            for (AccountEntity entity : entitiesToSave) {
                accountRepository.insert(entity, id ->
                        logger.info(TAG, "账号 " + entity.getUin() + " 已保存"));
            }
        });
        logger.info(TAG, "正在保存 " + entitiesToSave.size() + " 个账号到数据库...");
    }

    private void updateEmptyState() {
        if (binding == null) return;
        boolean isEmpty = accountAdapter.getAccountCount() == 0;
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvAccounts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    public void updateSignStatus(String status) {
        if (binding != null) {
            binding.tvSignStatus.setText(status);
        }
    }

    public void setSignRunning(boolean running) {
        if (binding != null) {
            binding.tvSignStatus.setText(running ? "签到中..." : "就绪");
        }
    }

    public List<Account> getAccounts() {
        if (accountAdapter != null) {
            return accountAdapter.getAccounts();
        }
        return new ArrayList<>();
    }
}
