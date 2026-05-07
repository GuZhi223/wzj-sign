package com.wzj.sign.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.wzj.sign.data.dao.AccountDao;
import com.wzj.sign.data.entity.AccountEntity;

@Database(entities = {AccountEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract AccountDao accountDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "wzj_sign.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
