package com.sftelehealth.doctor.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul on 19/06/17.
 */

public class CasePrescriptionsAndDocuments {

    private Integer caseId;
    private Integer latestConsultId;
    private Patient patient;
    private Integer doctorCategoryId;
    private List<Document> docs = new ArrayList<>();
    private List<Prescription> prescriptions = new ArrayList<>();

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public Integer getLatestConsultId() {
        return latestConsultId;
    }

    public void setLatestConsultId(Integer latestConsultId) {
        this.latestConsultId = latestConsultId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<Document> getDocs() {
        return docs;
    }

    public void setDocs(List<Document> docs) {
        this.docs = docs;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public Integer getDoctorCategoryId() {
        return doctorCategoryId;
    }

    public void setDoctorCategoryId(Integer doctorCategoryId) {
        this.doctorCategoryId = doctorCategoryId;
    }
}
