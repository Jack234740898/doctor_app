package com.sftelehealth.doctor.domain.model;

import com.sftelehealth.doctor.domain.helper.DateTimeComputationHelper;
import com.sftelehealth.doctor.domain.helper.DateTimeHelper;

/**
 * Created by Rahul on 19/06/17.
 */

public class Case {

    private int caseId;
    private String patientName;
    private String patientImage;
    private String lastCall;
    private int isCallback;
    private int callbackId;
    private int hasPrescription;
    private int lastConusltId;
    private Patient patient;
    private boolean isFollowUp;
    private boolean freeFollowUp;
    private boolean isConsultPending;
    private CasePrescriptionsAndDocuments prescriptionAndDocuments;
    private boolean isVideo;
    private boolean allowVideoFollowUp;

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
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

    public String getLastCall() {
        return lastCall;
    }

    public void setLastCall(String lastCall) {
        this.lastCall = lastCall;
    }

    public int getIsCallback() {
        return isCallback;
    }

    public void setIsCallback(int isCallback) {
        this.isCallback = isCallback;
    }

    public int getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(int callbackId) {
        this.callbackId = callbackId;
    }

    public int getHasPrescription() {
        return hasPrescription;
    }

    public void setHasPrescription(int hasPrescription) {
        this.hasPrescription = hasPrescription;
    }

    public int getLastConusltId() {
        return lastConusltId;
    }

    public void setLastConusltId(int lastConusltId) {
        this.lastConusltId = lastConusltId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public boolean isFollowUp() {
        return isFollowUp;
    }

    public void setFollowUp(boolean followUp) {
        isFollowUp = followUp;
    }

    public boolean isFreeFollowUp() {
        return freeFollowUp;
    }

    public void setFreeFollowUp(boolean freeFollowUp) {
        this.freeFollowUp = freeFollowUp;
    }

    public boolean isConsultPending() {
        return isConsultPending;
    }

    public void setConsultPending(boolean consultPending) {
        isConsultPending = consultPending;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public CasePrescriptionsAndDocuments getPrescriptionAndDocuments() {
        return prescriptionAndDocuments;
    }

    public void setPrescriptionAndDocuments(CasePrescriptionsAndDocuments prescriptionAndDocuments) {
        this.prescriptionAndDocuments = prescriptionAndDocuments;
    }

    public String getLastCallFormatted() {
        DateTimeComputationHelper dtch = new DateTimeComputationHelper();
        return DateTimeHelper.toLocaleDayTimeYear(dtch.convertGMTToIST(DateTimeHelper.parseDateTime(lastCall)));
    }

    public Boolean isCreatePrescriptionVisible() {
        if(prescriptionAndDocuments.getPrescriptions() != null && prescriptionAndDocuments.getPrescriptions().size() > 0 && !isFollowUp && !freeFollowUp)
            return (lastConusltId == prescriptionAndDocuments.getPrescriptions().get(0).getConsultId()) ? false : true;
        else if(prescriptionAndDocuments.getPrescriptions() != null && prescriptionAndDocuments.getPrescriptions().size() > 0 && isFollowUp && !freeFollowUp)
            return (lastConusltId == prescriptionAndDocuments.getPrescriptions().get(0).getConsultId()) ? false : true;
        else
            return true;
    }

    public boolean isAllowVideoFollowUp() {
        return allowVideoFollowUp;
    }

    public void setAllowVideoFollowUp(boolean allowVideoFollowUp) {
        this.allowVideoFollowUp = allowVideoFollowUp;
    }
}
