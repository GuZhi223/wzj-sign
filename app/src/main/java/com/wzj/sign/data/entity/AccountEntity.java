package com.wzj.sign.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class AccountEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "uin")
    private String uin;

    @ColumnInfo(name = "openid")
    private String openid;

    @ColumnInfo(name = "longitude", defaultValue = "")
    private String longitude;

    @ColumnInfo(name = "latitude", defaultValue = "")
    private String latitude;

    @ColumnInfo(name = "create_time")
    private long createTime;

    @ColumnInfo(name = "update_time")
    private long updateTime;

    public AccountEntity() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUin() { return uin; }
    public void setUin(String uin) { this.uin = uin; }

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }

    public long getUpdateTime() { return updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }
}
