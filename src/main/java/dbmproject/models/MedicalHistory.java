package dbmproject.models;

import java.time.LocalDate;

public class MedicalHistory {

    private int medicalRecordId;
    private Integer mPetId;
    private String visitType;
    private LocalDate visitDate; // maybe change to a regular date that must be input?

    public MedicalHistory() {}

    public MedicalHistory(Integer mPetId, String visitType, LocalDate visitDate) {
        this.mPetId = mPetId;
        this.visitType = visitType;
        this.visitDate = visitDate;
    }

    public int getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(int medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public Integer getMPetId() {
        return mPetId;
    }

    public void setMPetId(Integer mPetId) {
        this.mPetId = mPetId;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }
}