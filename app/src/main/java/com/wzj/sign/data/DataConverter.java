package com.wzj.sign.data;

import com.wzj.sign.Account;
import com.wzj.sign.data.entity.AccountEntity;

public class DataConverter {

    public static AccountEntity toEntity(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setUin(account.getUin());
        entity.setOpenid(account.getOpenid());
        entity.setLongitude(account.getLongitude() != null ? account.getLongitude() : "");
        entity.setLatitude(account.getLatitude() != null ? account.getLatitude() : "");
        return entity;
    }

    public static Account toModel(AccountEntity entity) {
        return new Account(
                entity.getUin(),
                entity.getOpenid(),
                entity.getLongitude(),
                entity.getLatitude()
        );
    }

    public static void updateEntity(AccountEntity entity, Account account) {
        entity.setUin(account.getUin());
        entity.setOpenid(account.getOpenid());
        entity.setLongitude(account.getLongitude() != null ? account.getLongitude() : "");
        entity.setLatitude(account.getLatitude() != null ? account.getLatitude() : "");
        entity.setUpdateTime(System.currentTimeMillis());
    }
}
