package com.wzj.sign.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.wzj.sign.data.dao.AccountDao;
import com.wzj.sign.data.entity.AccountEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountRepository {

    private final AccountDao accountDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public AccountRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        accountDao = db.accountDao();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface Callback<T> {
        void onResult(T result);
    }

    public void getAll(Callback<List<AccountEntity>> callback) {
        executor.execute(() -> {
            List<AccountEntity> result = accountDao.getAll();
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    public void getByUin(String uin, Callback<AccountEntity> callback) {
        executor.execute(() -> {
            AccountEntity result = accountDao.getByUin(uin);
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    public void getCount(Callback<Integer> callback) {
        executor.execute(() -> {
            int count = accountDao.getCount();
            mainHandler.post(() -> callback.onResult(count));
        });
    }

    public void insert(AccountEntity account, Callback<Long> callback) {
        executor.execute(() -> {
            account.setCreateTime(System.currentTimeMillis());
            account.setUpdateTime(System.currentTimeMillis());
            long id = accountDao.insert(account);
            mainHandler.post(() -> callback.onResult(id));
        });
    }

    public void update(AccountEntity account, Callback<Void> callback) {
        executor.execute(() -> {
            account.setUpdateTime(System.currentTimeMillis());
            accountDao.update(account);
            mainHandler.post(() -> callback.onResult(null));
        });
    }

    public void delete(AccountEntity account, Callback<Void> callback) {
        executor.execute(() -> {
            accountDao.delete(account);
            mainHandler.post(() -> callback.onResult(null));
        });
    }

    public void deleteById(long id, Callback<Void> callback) {
        executor.execute(() -> {
            accountDao.deleteById(id);
            mainHandler.post(() -> callback.onResult(null));
        });
    }

    public void deleteAll(Callback<Void> callback) {
        executor.execute(() -> {
            accountDao.deleteAll();
            mainHandler.post(() -> callback.onResult(null));
        });
    }

    public void replaceAll(List<AccountEntity> accounts, Callback<Void> callback) {
        executor.execute(() -> {
            accountDao.deleteAll();
            if (!accounts.isEmpty()) {
                accountDao.insertAll(accounts);
            }
            mainHandler.post(() -> callback.onResult(null));
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
