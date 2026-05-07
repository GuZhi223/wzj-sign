package com.wzj.sign.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wzj.sign.network.model.ActiveSignResponse;
import com.wzj.sign.network.model.SignRequest;
import com.wzj.sign.network.model.SignResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignRepository {

    private static final String TAG = "SignRepository";
    private final TeachermateApi api;
    private final Random random;
    private static final int GPS_OFFSET_RANGE = 20;
    private static final double GPS_OFFSET_UNIT = 0.000001;
    private static final int GPS_DECIMAL_PLACES = 5;

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public SignRepository() {
        this.api = RetrofitClient.getApi();
        this.random = new Random();
    }

    public void getActiveSigns(String openid, ResultCallback<List<ActiveSignResponse>> callback) {
        Log.d(TAG, ">>> GET active_signs | Openid: " + (openid != null ? openid.substring(0, Math.min(8, openid.length())) + "..." : "null"));
        Call<ResponseBody> call = api.getActiveSigns(openid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        Log.d(TAG, "<<< active_signs OK | body: " + body);
                        if (body.isEmpty() || body.equals("[]")) {
                            callback.onSuccess(null);
                            return;
                        }
                        if (body.contains("登录信息失效")) {
                            callback.onError("OpenID已过期，请重新抓包绑定");
                            return;
                        }
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<ActiveSignResponse>>() {}.getType();
                        List<ActiveSignResponse> list = gson.fromJson(body, listType);
                        callback.onSuccess(list);
                    } else {
                        String errBody = "";
                        if (response.errorBody() != null) {
                            errBody = response.errorBody().string();
                        }
                        Log.e(TAG, "<<< active_signs FAIL | code=" + response.code() + " body=" + errBody);
                        callback.onError("获取签到任务失败: HTTP " + response.code());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "<<< active_signs ERROR", e);
                    callback.onError("解析签到数据失败: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "<<< active_signs NETWORK ERROR", t);
                callback.onError("网络请求失败: " + t.getMessage());
            }
        });
    }

    public void submitSign(String openid, long courseId, long signId, boolean enableGps,
                           String longitude, String latitude, ResultCallback<SignResponse> callback) {
        String postLat = "0";
        String postLon = "0";

        if (enableGps && longitude != null && !longitude.isEmpty() && latitude != null && !latitude.isEmpty()) {
            try {
                double lon = Double.parseDouble(longitude);
                double lat = Double.parseDouble(latitude);
                int lonOffset = random.nextInt(GPS_OFFSET_RANGE * 2 + 1) - GPS_OFFSET_RANGE;
                int latOffset = random.nextInt(GPS_OFFSET_RANGE * 2 + 1) - GPS_OFFSET_RANGE;
                postLon = formatCoordinate(lon + lonOffset * GPS_OFFSET_UNIT);
                postLat = formatCoordinate(lat + latOffset * GPS_OFFSET_UNIT);
            } catch (NumberFormatException e) {
                Log.w(TAG, "GPS坐标解析失败，使用默认值");
            }
        }

        String finalPostLat = postLat;
        String finalPostLon = postLon;

        String jsonBody;
        if (enableGps) {
            jsonBody = "{\"courseId\":" + courseId + ",\"signId\":" + signId
                    + ",\"lat\":\"" + finalPostLat + "\",\"lon\":\"" + finalPostLon + "\"}";
        } else {
            jsonBody = "{\"courseId\":" + courseId + ",\"signId\":" + signId + "}";
        }

        Log.d(TAG, ">>> POST student-sign-in | courseId=" + courseId + " signId=" + signId
                + " gps=" + enableGps + " lat=" + finalPostLat + " lon=" + finalPostLon);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), jsonBody);

        Call<ResponseBody> call;
        if (enableGps) {
            call = api.submitSign(openid, finalPostLat, finalPostLon, body);
        } else {
            call = api.submitSignNoGps(openid, body);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        Log.d(TAG, "<<< sign-in OK | body: " + body);
                        Gson gson = new Gson();
                        SignResponse signResponse = gson.fromJson(body, SignResponse.class);
                        callback.onSuccess(signResponse);
                    } else {
                        String errBody = "";
                        if (response.errorBody() != null) {
                            errBody = response.errorBody().string();
                        }
                        Log.e(TAG, "<<< sign-in FAIL | code=" + response.code() + " body=" + errBody);
                        callback.onError("签到请求失败: HTTP " + response.code());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "<<< sign-in ERROR", e);
                    callback.onError("解析签到结果失败: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "<<< sign-in NETWORK ERROR", t);
                callback.onError("网络请求失败: " + t.getMessage());
            }
        });
    }

    private String formatCoordinate(double value) {
        String str = String.format(Locale.US, "%." + GPS_DECIMAL_PLACES + "f", value);
        int dotIndex = str.indexOf('.');
        if (dotIndex >= 0 && str.length() > dotIndex + GPS_DECIMAL_PLACES + 1) {
            str = str.substring(0, dotIndex + GPS_DECIMAL_PLACES + 1);
        }
        return str;
    }
}
