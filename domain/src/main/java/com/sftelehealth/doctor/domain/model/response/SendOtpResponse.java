package com.sftelehealth.doctor.domain.model.response;

/**
 * Created by rahul on 02/10/16.
 */
public class SendOtpResponse {

    boolean success, isRegistered;
    String otp;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
