package com.wzj.sign.data;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wzj.sign.data.entity.AccountEntity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupManager {

    private static final String BACKUP_DIR = "WzjSignBackup";
    private static final String BACKUP_PREFIX = "wzj_accounts_";
    private static final String BACKUP_SUFFIX = ".json";

    private final Context context;
    private final Gson gson;

    public BackupManager(Context context) {
        this.context = context;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public String exportAccounts(List<AccountEntity> accounts) {
        try {
            File backupDir = getBackupDirectory();
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = BACKUP_PREFIX + sdf.format(new Date()) + BACKUP_SUFFIX;
            File backupFile = new File(backupDir, fileName);

            try (FileWriter writer = new FileWriter(backupFile)) {
                gson.toJson(accounts, writer);
            }

            return backupFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<AccountEntity> importAccounts(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<AccountEntity>>() {}.getType();
                List<AccountEntity> accounts = gson.fromJson(reader, listType);
                return accounts != null ? accounts : new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getBackupFiles() {
        List<String> backupFiles = new ArrayList<>();
        File backupDir = getBackupDirectory();
        if (backupDir.exists()) {
            File[] files = backupDir.listFiles((dir, name) -> 
                name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_SUFFIX));
            if (files != null) {
                for (File file : files) {
                    backupFiles.add(file.getAbsolutePath());
                }
            }
        }
        return backupFiles;
    }

    public boolean deleteBackup(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    private File getBackupDirectory() {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return new File(externalDir, BACKUP_DIR);
    }

    public String getBackupDirectoryPath() {
        return getBackupDirectory().getAbsolutePath();
    }
}