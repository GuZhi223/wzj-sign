package com.wzj.sign;

public class Account {
    private String uin;
    private String openid;
    private String longitude;
    private String latitude;

    public Account(String uin, String openid, String longitude, String latitude) {
        this.uin = uin;
        this.openid = openid;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String toConfigString() {
        String lon = (longitude == null || longitude.isEmpty()) ? "0" : longitude;
        String lat = (latitude == null || latitude.isEmpty()) ? "0" : latitude;
        return openid + "," + lon + "," + lat;
    }

    public static Account fromConfigString(String uin, String config) {
        String[] parts = config.split(",");
        String openid = parts[0];
        String longitude = parts.length >= 2 ? parts[1] : "";
        String latitude = parts.length >= 3 ? parts[2] : "";
        return new Account(uin, openid, longitude, latitude);
    }
}