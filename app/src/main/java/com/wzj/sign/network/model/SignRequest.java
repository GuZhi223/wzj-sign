package com.wzj.sign.network.model;

import com.google.gson.annotations.SerializedName;

public class SignRequest {

    @SerializedName("courseId")
    private long courseId;

    @SerializedName("signId")
    private long signId;

    @SerializedName("lat")
    private String lat;

    @SerializedName("lon")
    private String lon;

    public SignRequest(long courseId, long signId) {
        this.courseId = courseId;
        this.signId = signId;
        this.lat = "0";
        this.lon = "0";
    }

    public SignRequest(long courseId, long signId, String lat, String lon) {
        this.courseId = courseId;
        this.signId = signId;
        this.lat = lat != null ? lat : "0";
        this.lon = lon != null ? lon : "0";
    }

    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }

    public long getSignId() { return signId; }
    public void setSignId(long signId) { this.signId = signId; }

    public String getLat() { return lat; }
    public void setLat(String lat) { this.lat = lat; }

    public String getLon() { return lon; }
    public void setLon(String lon) { this.lon = lon; }
}
