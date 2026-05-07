package com.wzj.sign.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.wzj.sign.data.entity.AccountEntity;

import java.util.List;

@Dao
public interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY create_time ASC")
    List<AccountEntity> getAll();

    @Query("SELECT * FROM accounts WHERE uin = :uin LIMIT 1")
    AccountEntity getByUin(String uin);

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    AccountEntity getById(long id);

    @Query("SELECT COUNT(*) FROM accounts")
    int getCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AccountEntity account);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<AccountEntity> accounts);

    @Update
    void update(AccountEntity account);

    @Delete
    void delete(AccountEntity account);

    @Query("DELETE FROM accounts WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM accounts")
    void deleteAll();
}
