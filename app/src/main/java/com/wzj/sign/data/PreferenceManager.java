package com.wzj.sign.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "wzj_sign_prefs";
    private static final String KEY_SIGN_COUNT = "sign_count";
    private static final String KEY_SIGN_INTERVAL = "sign_interval";
    private static final String KEY_GPS_ENABLED = "gps_enabled";
    private static final String KEY_DEFAULT_LONGITUDE = "default_longitude";
    private static final String KEY_DEFAULT_LATITUDE = "default_latitude";
    private static final String KEY_DAEMON_ENABLED = "daemon_enabled";
    private static final String KEY_LOG_MAX_SIZE = "log_max_size";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getSignCount() {
        return prefs.getInt(KEY_SIGN_COUNT, 20);
    }

    public void setSignCount(int count) {
        prefs.edit().putInt(KEY_SIGN_COUNT, count).apply();
    }

    public int getSignInterval() {
        return prefs.getInt(KEY_SIGN_INTERVAL, 300);
    }

    public void setSignInterval(int interval) {
        prefs.edit().putInt(KEY_SIGN_INTERVAL, interval).apply();
    }

    public boolean isGpsEnabled() {
        return prefs.getBoolean(KEY_GPS_ENABLED, true);
    }

    public void setGpsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_GPS_ENABLED, enabled).apply();
    }

    public String getDefaultLongitude() {
        return prefs.getString(KEY_DEFAULT_LONGITUDE, "");
    }

    public void setDefaultLongitude(String longitude) {
        prefs.edit().putString(KEY_DEFAULT_LONGITUDE, longitude).apply();
    }

    public String getDefaultLatitude() {
        return prefs.getString(KEY_DEFAULT_LATITUDE, "");
    }

    public void setDefaultLatitude(String latitude) {
        prefs.edit().putString(KEY_DEFAULT_LATITUDE, latitude).apply();
    }

    public boolean isDaemonEnabled() {
        return prefs.getBoolean(KEY_DAEMON_ENABLED, false);
    }

    public void setDaemonEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DAEMON_ENABLED, enabled).apply();
    }

    public int getLogMaxSize() {
        return prefs.getInt(KEY_LOG_MAX_SIZE, 5000);
    }

    public void setLogMaxSize(int size) {
        prefs.edit().putInt(KEY_LOG_MAX_SIZE, size).apply();
    }
}
