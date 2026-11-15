package dbmproject.models;

public class Vaccinations {

    private Integer vMedicalRecordId;
    private String vaccinationType;
    private String spayedNeutered; // maybe change to boolean or enum

    public Vaccinations() {}

    public Vaccinations(Integer vMedicalRecordId, String vaccinationType, String spayedNeutered) {
        this.vMedicalRecordId = vMedicalRecordId;
        this.vaccinationType = vaccinationType;
        this.spayedNeutered = spayedNeutered;
    }

    public Integer getVMedicalRecordId() {
        return vMedicalRecordId;
    }

    public void setVMedicalRecordId(Integer vMedicalRecordId) {
        this.vMedicalRecordId = vMedicalRecordId;
    }

    public String getVaccinationType() {
        return vaccinationType;
    }

    public void setVaccinationType(String vaccinationType) {
        this.vaccinationType = vaccinationType;
    }

    public String getSpayedNeutered() {
        return spayedNeutered;
    }

    public void setSpayedNeutered(String spayedNeutered) {
        this.spayedNeutered = spayedNeutered;
    }
}
