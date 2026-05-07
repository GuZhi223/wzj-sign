package com.wzj.sign.network.model;

import com.google.gson.annotations.SerializedName;

public class SignResponse {

    @SerializedName("signRank")
    private Integer signRank;

    @SerializedName("studentRank")
    private Integer studentRank;

    @SerializedName("errorCode")
    private Integer errorCode;

    @SerializedName("msg")
    private String msg;

    @SerializedName("msgClient")
    private String msgClient;

    public Integer getSignRank() { return signRank; }
    public void setSignRank(Integer signRank) { this.signRank = signRank; }

    public Integer getStudentRank() { return studentRank; }
    public void setStudentRank(Integer studentRank) { this.studentRank = studentRank; }

    public Integer getErrorCode() { return errorCode; }
    public void setErrorCode(Integer errorCode) { this.errorCode = errorCode; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public String getMsgClient() { return msgClient; }
    public void setMsgClient(String msgClient) { this.msgClient = msgClient; }

    public boolean isSignSuccess() {
        if (studentRank != null && studentRank > 0) return true;
        if (signRank != null && signRank > 0) return true;
        if (errorCode != null && errorCode == 305) return true;
        if (msgClient != null && msgClient.contains("签到成功")) return true;
        if (msg != null && msg.contains("repeat sign in")) return true;
        return false;
    }

    public String getDisplayMessage() {
        if (msgClient != null && !msgClient.isEmpty()) return msgClient;
        if (msg != null && !msg.isEmpty()) return msg;
        if (signRank != null) return "签到成功，排名: " + signRank;
        if (studentRank != null) return "签到成功，排名: " + studentRank;
        return "未知结果";
    }
}
