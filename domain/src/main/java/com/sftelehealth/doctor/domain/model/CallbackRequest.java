package com.sftelehealth.doctor.domain.model;

import com.sftelehealth.doctor.domain.helper.DateTimeComputationHelper;
import com.sftelehealth.doctor.domain.helper.DateTimeHelper;

import java.util.ArrayList;

/**
 * Created by Rahul on 19/06/17.
 */

public class CallbackRequest {

    private Integer callbackId;
    private String status;
    private Boolean isFollowUp;
    private Integer caseId;
    private String consultedAt;
    private String createdAt;
    private String autoRejectAt;
    private String autoExpireAt;
    private String patientName;
    private String patientImage;
    private String gender;
    private String dob;
    private Patient patient;
    private boolean isVideo;
    private boolean showDialer;
    private String videoStartTime;
    private String videoEndTime;
    private ArrayList<Document> docs;
    private boolean allowVideoFollowUp;

    private DateTimeComputationHelper dtch;

    public CallbackRequest() {
        dtch = new DateTimeComputationHelper();
    }

    public Integer getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(Integer callbackId) {
        this.callbackId = callbackId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getFollowUp() {
        return isFollowUp;
    }

    public void setFollowUp(Boolean followUp) {
        isFollowUp = followUp;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public String getConsultedAt() {
        return consultedAt;
    }

    public void setConsultedAt(String consultedAt) {
        this.consultedAt = consultedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getAutoRejectAt() {
        return autoRejectAt;
    }

    public void setAutoRejectAt(String autoRejectAt) {
        this.autoRejectAt = autoRejectAt;
    }

    public String getAutoExpireAt() {
        return autoExpireAt;
    }

    public void setAutoExpireAt(String autoExpireAt) {
        this.autoExpireAt = autoExpireAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientImage() {
        return patientImage;
    }

    public void setPatientImage(String patientImage) {
        this.patientImage = patientImage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public boolean isShowDialer() {
        return showDialer;
    }

    public String getVideoStartTime() {
        return videoStartTime;
    }

    public void setVideoStartTime(String videoStartTime) {
        this.videoStartTime = videoStartTime;
    }

    public String getVideoEndTime() {
        return videoEndTime;
    }

    public void setVideoEndTime(String videoEndTime) {
        this.videoEndTime = videoEndTime;
    }

    public String getCreatedAtFormatted() {
        DateTimeComputationHelper dtch = new DateTimeComputationHelper();
        return DateTimeHelper.toLocaleDayTimeYear(dtch.convertGMTToIST(DateTimeHelper.parseDateTime(createdAt)));
    }

    public String getStatusFormatted() {
        if(status.equalsIgnoreCase("Pending")) {
            return "Pending Request";
        } else if (status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Patient Disconnected") || status.equalsIgnoreCase("Doctor Disconnected")) {
           if(dtch.getElapsedTimeInHours(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(createdAt)) > 3) {  // if time difference is more than 4 hrs then
               //dtch.addTime(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(createdAt), 60 * 60 * 4)
               return "Expired";
           } else {
               return dtch.getRemainingTimeString(dtch.addTime(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(createdAt), 60 * 60 * 4));
           }

        } else if(status.equalsIgnoreCase("In Progress")) {
            return "Call in progress";
        } else if(status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Auto Rejected")) {
            if (status.equalsIgnoreCase("Rejected"))
                return "Rejected by You";
            else
                return "Rejected by Doctor 24x7";
        } else if (status.equalsIgnoreCase("Consulted")) {
            return "Consultation Complete";
        } else if (status.equalsIgnoreCase("Expired")) {
            return "Rejected by Doctor 24x7";
        } else if (status.equalsIgnoreCase("Follow Up")) {
            return "Free Follow Up Pending";
        } else if (status.equalsIgnoreCase("Patient Cancelled")) {
            return "Patient Cancelled";
        } else {
            return "";
        }
    }

    public String getTimeRemaining() {
        if(!isVideo) {
            if (status.equalsIgnoreCase("Pending")) {
                // return dtch.getRemainingTimeFormattedString(dtch.addTime(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(createdAt), 60 * 30));
                return dtch.getRemainingTimeFormattedString(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(autoRejectAt));
            } else if (status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Patient Disconnected") || status.equalsIgnoreCase("Doctor Disconnected")) {
                //return dtch.getRemainingTimeFormattedString(dtch.addTime(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(createdAt), 60 * 60 * 4)) + " left";
                return dtch.getRemainingTimeFormattedString(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(autoExpireAt)) + " left";
            } else {
                return "time elapsed";
            }
        } else {
            if (status.equalsIgnoreCase("Pending")) {
                return dtch.getRemainingTimeFormattedString(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(autoRejectAt));
            } else if (status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Patient Disconnected") || status.equalsIgnoreCase("Doctor Disconnected")) {
                return "";
                //                return DateTimeHelper.toLocaleDayTimeYear(dtch.convertGMTToIST(DateTimeHelper.parseAlmostISO8601DateTimeWithTSeparator(videoStartTime)));
            } else {
                return "";
            }
        }
    }

    public boolean isTimeRemainingVisible() {
        if(status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Patient Disconnected") || status.equalsIgnoreCase("Doctor Disconnected"))
            return true;
        else
            return false;
    }

    public boolean isStatusVisible() {
        if(status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Patient Disconnected") || status.equalsIgnoreCase("Doctor Disconnected"))
            return false;
        else
            return true;
    }

    public boolean isCardDeactivated() {
        if(status.equalsIgnoreCase("Expired") || status.equalsIgnoreCase("Patient Cancelled") || status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Auto Rejected"))
            return true;
        else
            return false;
    }

    public String getPatientAge() {
        DateTimeComputationHelper dtch = new DateTimeComputationHelper();
        return dtch.getAge(DateTimeHelper.parseDate(dob));
    }

    public ArrayList<Document> getDocs() {
        return docs;
    }

    public boolean isAllowVideoFollowUp() {
        return allowVideoFollowUp;
    }

    public void setAllowVideoFollowUp(boolean allowVideoFollowUp) {
        this.allowVideoFollowUp = allowVideoFollowUp;
    }
}