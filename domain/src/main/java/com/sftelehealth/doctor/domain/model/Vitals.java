package com.sftelehealth.doctor.domain.model;

import com.sftelehealth.doctor.domain.helper.DateTimeComputationHelper;
import com.sftelehealth.doctor.domain.helper.DateTimeHelper;

import java.io.Serializable;

public class Vitals implements Serializable {
    private String patientId;
    private String bloodTemperature;
    private String pulseRate;
    private String respirationRate;
    private String bloodPressure;
    private String bodyWeight;
    private String bloodSuger;
    private String createdAt;
    private Integer id;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getBloodTemperature() {
        return bloodTemperature;
    }

    public void setBloodTemperature(String bloodTemperature) {
        this.bloodTemperature = bloodTemperature;
    }

    public String getPulseRate() {
        return pulseRate;
    }

    public void setPulseRate(String pulseRate) {
        this.pulseRate = pulseRate;
    }

    public String getRespirationRate() {
        return respirationRate;
    }

    public void setRespirationRate(String respirationRate) {
        this.respirationRate = respirationRate;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getBodyWeight() {
        return bodyWeight;
    }

    public void setBodyWeight(String bodyWeight) {
        this.bodyWeight = bodyWeight;
    }

    public String getBloodSuger() {
        return bloodSuger;
    }

    public void setBloodSuger(String bloodSuger) {
        this.bloodSuger = bloodSuger;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getVitalsDateTime() {
        DateTimeComputationHelper dtch = new DateTimeComputationHelper();
        return DateTimeHelper.toLocaleDayTimeYear(dtch.convertGMTToIST(DateTimeHelper.parseDateTime(createdAt)));
    }
}
