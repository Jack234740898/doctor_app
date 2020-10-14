package com.sftelehealth.doctor.data.model.request;

public class MediaChannelKeyRequest {

    String channelName;
    String uid;//doctorID
    int expiredTs;
    int userId;//patientUserId
    boolean emergencyCall;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getExpiredTs() {
        return expiredTs;
    }

    public void setExpiredTs(int expiredTs) {
        this.expiredTs = expiredTs;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isEmergencyCall() {
        return emergencyCall;
    }

    public void setEmergencyCall(boolean emergencyCall) {
        this.emergencyCall = emergencyCall;
    }
}
