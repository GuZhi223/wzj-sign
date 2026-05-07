package com.wzj.sign.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface TeachermateApi {

    @GET("v1/class-attendance/student/active_signs")
    Call<ResponseBody> getActiveSigns(@Header("Openid") String openid);

    @POST("v1/class-attendance/student-sign-in")
    Call<ResponseBody> submitSign(
            @Header("Openid") String openid,
            @Header("lat") String lat,
            @Header("lon") String lon,
            @Body RequestBody body
    );

    @POST("v1/class-attendance/student-sign-in")
    Call<ResponseBody> submitSignNoGps(
            @Header("Openid") String openid,
            @Body RequestBody body
    );
}
