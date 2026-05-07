package com.wzj.sign.network.model;

import com.google.gson.annotations.SerializedName;

public class ActiveSignResponse {

    public static final int SIGN_TYPE_NORMAL = 0;
    public static final int SIGN_TYPE_QR = 1;
    public static final int SIGN_TYPE_GPS = 2;

    @SerializedName("courseId")
    private long courseId;

    @SerializedName("signId")
    private long signId;

    @SerializedName("isGPS")
    private int isGps;

    @SerializedName("courseName")
    private String courseName;

    @SerializedName("signType")
    private int signType;

    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }

    public long getSignId() { return signId; }
    public void setSignId(long signId) { this.signId = signId; }

    public int getIsGps() { return isGps; }
    public void setIsGps(int isGps) { this.isGps = isGps; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getSignType() { return signType; }
    public void setSignType(int signType) { this.signType = signType; }

    public boolean requiresGps() {
        return isGps == 1 || signType == SIGN_TYPE_GPS;
    }

    public boolean isQrCodeSign() {
        return signType == SIGN_TYPE_QR;
    }

    public boolean isNormalSign() {
        return signType == SIGN_TYPE_NORMAL && isGps == 0;
    }

    public String getSignTypeName() {
        if (isQrCodeSign()) return "二维码签到";
        if (requiresGps()) return "GPS签到";
        return "普通签到";
    }
}
